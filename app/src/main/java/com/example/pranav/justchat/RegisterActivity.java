package com.example.pranav.justchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout mDisplayName, mEmail, mPass;
    private Button mRegBtn;
    FirebaseAuth mAuth;
    Toolbar regToolbar;
    private ProgressDialog regProgress;
    DatabaseReference mDatabase;
    String uid;
    CheckBox regShowPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mDisplayName = findViewById(R.id.reg_displayname);
        mEmail = findViewById(R.id.reg_email);
        mPass = findViewById(R.id.reg_pass);
        mRegBtn = findViewById(R.id.regBtn);
        mAuth = FirebaseAuth.getInstance();
        regShowPass = findViewById(R.id.reg_show_pass);
        regToolbar = findViewById(R.id.reg_toolbar);
        setSupportActionBar(regToolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        regProgress = new ProgressDialog(RegisterActivity.this);

        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dispName = mDisplayName.getEditText().getText().toString();
                String regEmail = mEmail.getEditText().getText().toString();
                String regPass = mPass.getEditText().getText().toString();
                if(!(regEmail.isEmpty() || regPass.isEmpty() || dispName.isEmpty())) {

                    regProgress.setTitle("Registering User");
                    regProgress.setMessage("Please Wait..");
                    regProgress.setCanceledOnTouchOutside(false);
                    regProgress.show();
                    register_user(dispName, regEmail, regPass);

                }else{
                    Toast.makeText(RegisterActivity.this, "Enter the details properly..", Toast.LENGTH_SHORT).show();

                }
            }
        });

        regShowPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(regShowPass.isChecked()){
                    mPass.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }else{
                    mPass.getEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });

    }
    private void register_user(final String dispName, String regEmail, String regPass) {
        mAuth.createUserWithEmailAndPassword(regEmail, regPass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            final FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            if(current_user != null){
                                uid = current_user.getUid();
                            }
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", dispName);
                            userMap.put("status", "Hello, I'm using Friends Adda.");
                            userMap.put("image","default");
                            userMap.put("thumb_image","default");
                            userMap.put("device_token",deviceToken);

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        sendEmailVerification();
                                    }else
                                    {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                        } else {
                            // If sign in fails, display a message to the user.
                            regProgress.hide();
                            String error = task.getException().getMessage();

                            Toast.makeText(RegisterActivity.this, error,
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }
    private void sendEmailVerification(){
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if(firebaseUser != null){
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Successfully registered, Verify your email.", Toast.LENGTH_SHORT).show();
                        regProgress.dismiss();
                        mAuth.signOut();
                        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(loginIntent);
                        finish();
                    }else{
                        String error = task.getException().getMessage();
                        Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
