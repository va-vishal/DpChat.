
package in.connect;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class AddStoryActivity extends AppCompatActivity {

    private Uri mImageUri;
    private ImageView image_added;
    String myUrl = "";
    private Button publish_button;
    private StorageTask<UploadTask.TaskSnapshot> storageTask;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        storageReference = FirebaseStorage.getInstance().getReference("Story");

        publish_button = findViewById(R.id.publish_button);
        image_added=findViewById(R.id.image_added);

        CropImage.activity().setAspectRatio(7, 14).start(AddStoryActivity.this);

        publish_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mImageUri != null) {
                    publishStory();
                    Intent intent = new Intent(AddStoryActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(AddStoryActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void publishStory() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Posting");
        pd.show();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            pd.dismiss();
            Toast.makeText(this, "User not authenticated. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddStoryActivity.this, LoginActivity.class));
            finish();
            return;
        }

        String currentUserId = firebaseUser.getUid();

        if (mImageUri != null) {
            StorageReference imageReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));

            storageTask = imageReference.putFile(mImageUri);
            storageTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(currentUserId);

                        String storyId = reference.push().getKey();
                        long timeEnd = System.currentTimeMillis() + 86400000; // 24 hours in milliseconds

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("imageUrl", myUrl);
                        hashMap.put("timeStart", System.currentTimeMillis());
                        hashMap.put("timeEnd", timeEnd);
                        hashMap.put("storyId", storyId);
                        hashMap.put("userId", currentUserId);

                        reference.child(storyId).setValue(hashMap);

                        pd.dismiss();
                        finish();
                    } else {
                        pd.dismiss();
                        Toast.makeText(AddStoryActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(AddStoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            pd.dismiss();
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK ) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                mImageUri = result.getUri();
                image_added.setImageURI(mImageUri);

            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AddStoryActivity.this, HomeActivity.class));
                finish();
            }
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddStoryActivity.this, HomeActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AddStoryActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
