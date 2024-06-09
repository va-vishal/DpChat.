package in.connect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText username, fullname, email, phone, password;
    private Button register;
    private TextView txt_Login;

    private FirebaseAuth mAuth;
    private DatabaseReference reference;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        mAuth = FirebaseAuth.getInstance();

        username = findViewById(R.id.username);
        fullname = findViewById(R.id.fullname);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);
        txt_Login = findViewById(R.id.txt_Login);

        txt_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLogin();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String susername = username.getText().toString().trim();
                String sfullName = fullname.getText().toString().trim();
                String semail = email.getText().toString().trim();
                String sphone = phone.getText().toString().trim();
                String spassword = password.getText().toString().trim();

                if (TextUtils.isEmpty(susername) || TextUtils.isEmpty(sfullName) || TextUtils.isEmpty(semail) || TextUtils.isEmpty(sphone) || TextUtils.isEmpty(spassword)) {
                    Toast.makeText(RegisterActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
                } else if (spassword.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password must be greater than 6 characters", Toast.LENGTH_SHORT).show();
                } else {
                    pd = new ProgressDialog(RegisterActivity.this);
                    pd.setMessage("Please wait...");
                    pd.show();

                    registerUser(susername, sfullName, semail, spassword, sphone);
                }
            }
        });
    }

    private void registerUser(String username, String fullname, String email, String password, String phone) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String userid = firebaseUser.getUid();
                                reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("id", userid);
                                hashMap.put("username", username.toLowerCase());
                                hashMap.put("fullname", fullname);
                                hashMap.put("phone", phone);
                                hashMap.put("bio", "");
                                hashMap.put("imageurl", "https://firebasestorage.googleapis.com/v0/b/connect-5091d.appspot.com/o/posts%2F1717822325406.null?alt=media&token=fe40df5c-e706-4b0c-bb94-8c5aa8fc65e2");
                                hashMap.put("satatus", "offline");
                                hashMap.put("search", username.toLowerCase());
                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            pd.dismiss();
                                            Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                            navigateToLogin();
                                        } else {
                                            pd.dismiss();
                                            Toast.makeText(RegisterActivity.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "you cant Register with this emailand Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToMain();
    }

    private void navigateToLogin() {
        Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    private void navigateToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
