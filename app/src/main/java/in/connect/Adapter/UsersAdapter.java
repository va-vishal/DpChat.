package in.connect.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import in.connect.MainActivity2;
import in.connect.Model.Chat;
import in.connect.Model.User;
import in.connect.R;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private boolean isChat;

    String theLastMessage;

    public UsersAdapter(Context mContext, List<User> mUsers, boolean isChat) {
        this.mUsers = mUsers;
        this.isChat = isChat;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.users_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.username.setText(user.getUsername());
        if (user.getImageurl() != null && user.getImageurl().equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageurl()).into(holder.profile_image);
        }

        if(isChat) {
            lastMessage(user.getId(), holder.last_msg);
        }
        else
        {
            holder.last_msg.setVisibility(View.GONE);
        }
        if (isChat) {
            if (user.getStatus() != null && user.getStatus().equals("online")) {
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, MainActivity2.class);
            intent.putExtra("userid", user.getId());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username,last_msg;
        public ImageView profile_image;
        public ImageView img_on, img_off;

        public ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);

        }
    }
    private void lastMessage(String userid,TextView last_msg){
        theLastMessage="default";
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    Chat chat =snapshot.getValue(chat.class());
                    if(chat.getReceiver().equals(firebaseUser.getUid())&& chat.getSender().equals(userid)||
                            chat.getReceiver().equals(userid)&&chat.getSender().equals(firebaseUser.getUid()){
                        theLastMessage= chat.getMessage();
                }

                }
                switch (theLastMessage){
                    case "default":
                        last_msg.setText("no Message");
                        break;

                    default:
                        last_msg.setText(theLastMessage);
                        break;
                }
                theLastMessage="default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        })
    }
}
