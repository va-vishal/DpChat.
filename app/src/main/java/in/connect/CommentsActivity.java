package in.connect;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.connect.Adapter.CommentAdapter;
import in.connect.Model.Comment;
import in.connect.Model.User;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private Button post;
    private EditText addcomment;
    private ImageView profile_Image;

    private String postid;
    private String publisherid;

    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList);
        recyclerView.setAdapter(commentAdapter);

        addcomment = findViewById(R.id.comment);
        profile_Image = findViewById(R.id.profile_image);
        post = findViewById(R.id.post);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addcomment.getText().toString().equals("")) {
                    Toast.makeText(CommentsActivity.this, "you can not empty comment", Toast.LENGTH_SHORT).show();
                } else {
                    addComment();
                }
            }
        });

        postid = getIntent().getStringExtra("postid");
        publisherid = getIntent().getStringExtra("publisherid");

        getImage();
        readComments();
    }

    private void addComment() {
        if (postid != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("comment", addcomment.getText().toString());
            hashMap.put("publisher", firebaseUser.getUid());

            reference.push().setValue(hashMap);
            addNotification();
            addcomment.setText("");
        }
    }
    private void addNotification()
    {
        DatabaseReference reference =FirebaseDatabase.getInstance().getReference("Notifications").child(publisherid);

        HashMap<String ,Object>hashMap=new HashMap<>();
        hashMap.put("userid",firebaseUser.getUid());
        hashMap.put("text","Commented:"+addcomment.getText().toString());
        hashMap.put("postid",postid);
        hashMap.put("ispost",true);

        reference.push().setValue(hashMap);
    }

    private void getImage() {
        if (publisherid != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(publisherid);

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        Glide.with(getApplicationContext()).load(user.getImageurl()).into(profile_Image);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle errors
                }
            });
        }
    }

    private void readComments() {
        if (postid != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    commentList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Comment comment = snapshot.getValue(Comment.class);
                        commentList.add(comment);
                    }
                    commentAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle errors
                }
            });
        }
    }
}
