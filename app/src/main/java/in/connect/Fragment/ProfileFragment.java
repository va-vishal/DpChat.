package in.connect.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import in.connect.Adapter.MyFotoAdapter;
import in.connect.EditProfileActivity;
import in.connect.FollowersActivity;
import in.connect.Model.Post;
import in.connect.Model.User;
import in.connect.OptionsActivity;
import in.connect.R;

public class ProfileFragment extends Fragment {

    private TextView username, posts, followers, following, fullname, bio;
    private CircleImageView imageProfile;
    private Button editProfile;
    private ImageView options;
    private ImageButton myPhotos, savedPhotos;
    private RecyclerView recyclerView, recyclerViewSaved;
    private AppBarLayout appBarLayout;
    private String profileid;
    private RecyclerView recyclerView1, recyclerViewSaves;
    private MyFotoAdapter myFotoAdapter, myFotoAdapterSaves;
    private List<Post> postList, postListSaves;
    private List<String> mySaves;
    private FirebaseUser firebaseUser;
    private DatabaseReference usersReference;
    private DatabaseReference followReference;
    private DatabaseReference postReference;
    private DatabaseReference saveReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Database references
        usersReference = FirebaseDatabase.getInstance().getReference("Users");
        followReference = FirebaseDatabase.getInstance().getReference("Follow");
        postReference = FirebaseDatabase.getInstance().getReference("Posts");
        saveReference = FirebaseDatabase.getInstance().getReference("Saves");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = prefs.getString("profileid", "none");

        appBarLayout = view.findViewById(R.id.bar);
        username = view.findViewById(R.id.username);
        posts = view.findViewById(R.id.posts);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        fullname = view.findViewById(R.id.fullname);
        bio = view.findViewById(R.id.bio);
        imageProfile = view.findViewById(R.id.image_profile);
        editProfile = view.findViewById(R.id.edit_profile);
        options = view.findViewById(R.id.options);
        savedPhotos = view.findViewById(R.id.saved_photo);
        myPhotos = view.findViewById(R.id.my_photos);

        recyclerView = view.findViewById(R.id.recycle_view);
        recyclerViewSaved = view.findViewById(R.id.recycle_view_saved);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewSaved.setHasFixedSize(true);
        recyclerViewSaved.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerView1 = view.findViewById(R.id.recycle_view);
        recyclerView1.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView1.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        myFotoAdapter = new MyFotoAdapter(getContext(), postList);
        recyclerView1.setAdapter(myFotoAdapter);

        recyclerViewSaves = view.findViewById(R.id.recycle_view_saved);
        recyclerViewSaves.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManagerSaves = new GridLayoutManager(getContext(), 3);
        recyclerViewSaves.setLayoutManager(linearLayoutManagerSaves);
        postListSaves = new ArrayList<>();
        myFotoAdapterSaves = new MyFotoAdapter(getContext(), postListSaves);
        recyclerViewSaves.setAdapter(myFotoAdapterSaves);
        recyclerView1.setVisibility(View.VISIBLE);
        recyclerViewSaves.setVisibility(View.GONE);

        userInfo();
        getFollowers();
        getNPosts();
        myFotos();
        mysaves();

        if (profileid.equals(firebaseUser.getUid())) {
            editProfile.setText("Edit Profile");
        } else {
            checkFollow();
            savedPhotos.setVisibility(View.GONE);
        }

        followers.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FollowersActivity.class);
            intent.putExtra("id", profileid);
            intent.putExtra("title", "followers");
            startActivity(intent);
        });

        following.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FollowersActivity.class);
            intent.putExtra("id", profileid);
            intent.putExtra("title", "following");
            startActivity(intent);
        });

        editProfile.setOnClickListener(v -> {
            String btn = editProfile.getText().toString();
            if (btn.equals("Edit Profile")) {
                startActivity(new Intent(getContext(), EditProfileActivity.class));
            } else if (btn.equals("follow")) {
                followUser();
                addNotification();
            } else if (btn.equals("following")) {
                unfollowUser();
            }
        });

        options.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), OptionsActivity.class);
            startActivity(intent);
        });

        myPhotos.setOnClickListener(v -> {
            recyclerView1.setVisibility(View.VISIBLE);
            recyclerViewSaves.setVisibility(View.GONE);
        });

        savedPhotos.setOnClickListener(v -> {
            recyclerView1.setVisibility(View.GONE);
            recyclerViewSaves.setVisibility(View.VISIBLE);
        });

        return view;
    }
    private void addNotification()
    {
        DatabaseReference reference =FirebaseDatabase.getInstance().getReference("Notifications").child(profileid);

        HashMap<String ,Object>hashMap=new HashMap<>();
        hashMap.put("userid",firebaseUser.getUid());
        hashMap.put("text","sarted following you");
        hashMap.put("postid","");
        hashMap.put("ispost",false);

        reference.push().setValue(hashMap);
    }

    private void userInfo() {
        usersReference.child(profileid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (getContext() == null) {
                    return;
                }
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    Glide.with(getContext()).load(user.getImageurl()).into(imageProfile);
                    username.setText(user.getUsername());
                    fullname.setText(user.getFullname());
                    bio.setText(user.getBio());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Failed to load user info: " + error.getMessage());
            }
        });
    }

    private void checkFollow() {
        followReference.child(firebaseUser.getUid()).child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(profileid).exists()) {
                    editProfile.setText("following");
                } else {
                    editProfile.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Failed to check follow status: " + error.getMessage());
            }
        });
    }

    public void getFollowers() {
        followReference.child(profileid).child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followers.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Failed to get followers count: " + error.getMessage());
            }
        });

        followReference.child(profileid).child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                following.setText(" " + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Failed to get following count: " + error.getMessage());
            }
        });
    }

    private void getNPosts() {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post != null && post.getPublisher().equals(profileid)) {
                        count++;
                    }
                }
                posts.setText(String.valueOf(""+count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Failed to get post count: " + error.getMessage());
            }
        });
    }

    private void followUser() {
        followReference.child(firebaseUser.getUid()).child("following").child(profileid).setValue(true);
        followReference.child(profileid).child("followers").child(firebaseUser.getUid()).setValue(true);
        addNotification();
    }

    private void unfollowUser() {
        followReference.child(firebaseUser.getUid()).child("following").child(profileid).removeValue();
        followReference.child(profileid).child("followers").child(firebaseUser.getUid()).removeValue();
    }

    private void myFotos() {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post != null && post.getPublisher().equals(profileid)) {
                        postList.add(post);
                    }
                }
                Collections.reverse(postList);
                myFotoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Failed to fetch photos: " + error.getMessage());
            }
        });
    }

    private void mysaves() {
        mySaves = new ArrayList<>();
        saveReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mySaves.add(snapshot.getKey());
                }
                readSaves();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Failed to fetch saved photos: " + error.getMessage());
            }
        });
    }

    private void readSaves() {
        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postListSaves.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    for (String id : mySaves) {
                        if (post != null && post.getPostid().equals(id)) {
                            postListSaves.add(post);
                        }
                    }
                }
                myFotoAdapterSaves.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Failed to fetch saved photos: " + error.getMessage());
            }
        });
    }
}
