package in.connect;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import in.connect.Model.User;
import in.connect.Model.Story;
import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    int counter = 0;
    long pressTime = 0;
    long limit = 500L;
    ImageView image, story_photo;
    TextView story_username;
    LinearLayout r_seen;
    TextView seen_number;
    ImageView story_delete;

    List<String> images;
    List<String> storyids;
    String userid;

    StoriesProgressView storiesProgressView;

    private final View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_story);

        r_seen = findViewById(R.id.r_seen);
        story_delete = findViewById(R.id.story_delete);
        seen_number = findViewById(R.id.seen_numbers);

        storiesProgressView = findViewById(R.id.Stories);
        image = findViewById(R.id.image);
        story_photo = findViewById(R.id.story_photo);
        story_username = findViewById(R.id.story_username);

        r_seen.setVisibility(View.GONE);
        story_delete.setVisibility(View.GONE);

        userid = getIntent().getStringExtra("userid");
        if (userid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            r_seen.setVisibility(View.VISIBLE);
            story_delete.setVisibility(View.VISIBLE);
        }

        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);

        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);

        getStories(userid);
        userInfo(userid);

        r_seen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StoryActivity.this, FollowersActivity.class);
                intent.putExtra("id", userid);
                intent.putExtra("storyid", storyids.get(counter));
                intent.putExtra("title", "Views");
                startActivity(intent);
            }
        });
        story_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userid)
                        .child(storyids.get(counter));
                reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(StoryActivity.this, "deleted", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(StoryActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(StoryActivity.this, "Failed to delete story", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(e -> Toast.makeText(StoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onNext() {
        if (counter < images.size() - 1) {
            Glide.with(getApplicationContext()).load(images.get(++counter)).into(image);
            addview(storyids.get(counter));
            seenNumber(storyids.get(counter));
        }
    }

    @Override
    public void onPrev() {
        if (counter > 0) {
            Glide.with(getApplicationContext()).load(images.get(--counter)).into(image);
            addview(storyids.get(counter));
            seenNumber(storyids.get(counter));
        }
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }

    private void getStories(String userid) {
        images = new ArrayList<>();
        storyids = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                images.clear();
                storyids.clear();
                long timeCurrent = System.currentTimeMillis();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Story story = snapshot.getValue(Story.class);
                    if (story != null && timeCurrent > story.getTimeStart() && timeCurrent < story.getTimeEnd()) {
                        images.add(story.getImageUrl());
                        storyids.add(story.getStoryId());
                    }
                }
                storiesProgressView.setStoriesCount(images.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(StoryActivity.this);
                storiesProgressView.startStories(counter);
                if (!images.isEmpty()) {
                    Glide.with(getApplicationContext()).load(images.get(counter)).into(image);
                    addview(storyids.get(counter));
                    seenNumber(storyids.get(counter));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StoryActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void userInfo(String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    Glide.with(getApplicationContext()).load(user.getImageurl()).into(story_photo);
                    story_username.setText(user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StoryActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToPreviousActivity();
    }

    private void navigateToPreviousActivity() {
        // Navigate to the previous activity in the stack
        Intent intent = new Intent(StoryActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void addview(String storyid) {
        FirebaseDatabase.getInstance().getReference("Story").child(userid)
                .child(storyid).child("views")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true)
                .addOnFailureListener(e -> Toast.makeText(StoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void seenNumber(String storyid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userid)
                .child(storyid).child("views");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                seen_number.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StoryActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
