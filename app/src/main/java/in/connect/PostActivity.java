package in.connect;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class PostActivity extends AppCompatActivity {
    private Uri imageUri;
    private String myUri = "";
    private StorageTask uploadTask;
    private StorageReference storageReference;
    private ImageView close, image_added;
    private Button post;
    private TextView description;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close = findViewById(R.id.close);
        image_added = findViewById(R.id.image_added);
        post = findViewById(R.id.post_button);
        description = findViewById(R.id.description);

        storageReference = FirebaseStorage.getInstance().getReference("posts");

        close.setOnClickListener(v -> {
            startActivity(new Intent(PostActivity.this, HomeActivity.class));
            finish();
        });

        post.setOnClickListener(v -> uploadImage());

        CropImage.activity().setAspectRatio(6, 5).start(PostActivity.this);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.show();

        if (imageUri != null) {
            StorageReference filereference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            uploadTask = filereference.putFile(imageUri);
            uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return filereference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                   Object downloadUri=task.getResult();
                    myUri = downloadUri.toString();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
                    String postid = reference.push().getKey();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("postid", postid);
                    hashMap.put("postimage", myUri);
                    hashMap.put("description", description.getText().toString());
                    hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());

                    reference.child(postid).setValue(hashMap);

                    progressDialog.dismiss();
                    startActivity(new Intent(PostActivity.this, HomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(PostActivity.this, "Failed to upload", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            image_added.setImageURI(imageUri);
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PostActivity.this, HomeActivity.class));
            finish();
        }
    }
}
