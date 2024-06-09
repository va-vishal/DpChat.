package in.connect.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import in.connect.AddStoryActivity;
import in.connect.Model.Story;
import in.connect.Model.User;
import in.connect.R;
import in.connect.StoryActivity;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {
    private Context mContext;
    private List<Story> mStory;

    public StoryAdapter(Context mContext, List<Story> mStory) {
        this.mContext = mContext;
        this.mStory = mStory;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(mContext).inflate(R.layout.add_story_item, parent, false);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.story_item, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Story story = mStory.get(position);
        userInfo(holder, story.getUserId(), position);

        if (holder.getAdapterPosition() != 0) {
            seenStory(holder, story.getUserId());
        }

        if (holder.getAdapterPosition() == 0) {
            myStory(holder.add_story_text, holder.story_plus, false);
        }

        holder.itemView.setOnClickListener(v -> {
            if (holder.getAdapterPosition() == 0) {
                myStory(holder.add_story_text, holder.story_plus, true);
            } else {
                Intent intent = new Intent(mContext, StoryActivity.class);
                intent.putExtra("userid", story.getUserId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mStory.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView story_photo, story_photo_seen, story_plus;
        public TextView story_username, add_story_text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            story_photo = itemView.findViewById(R.id.story_photo);
            story_photo_seen = itemView.findViewById(R.id.story_photo_seen);
            story_plus = itemView.findViewById(R.id.story_plus);
            story_username = itemView.findViewById(R.id.story_username);
            add_story_text = itemView.findViewById(R.id.add_story_text);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1;
    }

    private void userInfo(final ViewHolder viewHolder, String userId, final int pos) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        Glide.with(mContext).load(user.getImageurl()).into(viewHolder.story_photo);
                        if (pos != 0) {
                            Glide.with(mContext).load(user.getImageurl()).into(viewHolder.story_photo_seen);
                            viewHolder.story_username.setText(user.getUsername());
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(mContext, "Error loading user info", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, "Failed to load user info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void myStory(TextView textView, ImageView imageView, final Boolean click) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                long timeCurrent = System.currentTimeMillis();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Story story = snapshot.getValue(Story.class);
                        if (story != null && timeCurrent > story.getTimeStart() && timeCurrent < story.getTimeEnd()) {
                            count++;
                        }
                    } catch (Exception e) {
                        Toast.makeText(mContext, "Error processing story data", Toast.LENGTH_SHORT).show();
                    }
                }
                if (click) {
                    if (count > 0) {
                        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View story",
                                (dialog, which) -> {
                                    Intent intent = new Intent(mContext, StoryActivity.class);
                                    intent.putExtra("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    mContext.startActivity(intent);
                                    dialog.dismiss();
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add story",
                                (dialog, which) -> {
                                    Intent intent = new Intent(mContext, AddStoryActivity.class);
                                    mContext.startActivity(intent);
                                    dialog.dismiss();
                                });
                        alertDialog.show();
                    } else {
                        Intent intent = new Intent(mContext, AddStoryActivity.class);
                        mContext.startActivity(intent);
                    }
                } else {
                    if (count > 0) {
                        textView.setText("My Story");
                        imageView.setVisibility(View.GONE);
                    } else {
                        textView.setText("Add story");
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, "Failed to load story data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void seenStory(ViewHolder viewHolder, String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    int count = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Story story = snapshot.getValue(Story.class);
                        if (story != null && !snapshot.child("views")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists()
                                && System.currentTimeMillis() < story.getTimeEnd()) {
                            count++;
                        }
                    }
                    if (count > 0) {
                        viewHolder.story_photo.setVisibility(View.VISIBLE);
                        viewHolder.story_photo_seen.setVisibility(View.GONE);
                    } else {
                        viewHolder.story_photo.setVisibility(View.GONE);
                        viewHolder.story_photo_seen.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    Toast.makeText(mContext, "Error processing story views", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, "Failed to load story views", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
