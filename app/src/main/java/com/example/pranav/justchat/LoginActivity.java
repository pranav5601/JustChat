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

public class LoginActivity extends AppCompatActivity {

    Button loginBtn;
    TextInputLayout loginEmail, loginPass;
    Toolbar loginToolbar;
    FirebaseAuth mAuth;
    ProgressDialog loginProgress;
    DatabaseReference  mUserDatabase;
    CheckBox showPass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginBtn = findViewById(R.id.login_btn);
        loginEmail = findViewById(R.id.login_email);
        loginPass = findViewById(R.id.login_pass);
        mAuth = FirebaseAuth.getInstance();
        loginToolbar = findViewById(R.id.login_toolbar);
        loginProgress = new ProgressDialog(LoginActivity.this);
        showPass = findViewById(R.id.show_pass);

       showPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(showPass.isChecked()){
                    loginPass.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }else{
                    loginPass.getEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });



            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


        setSupportActionBar(loginToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmail.getEditText().getText().toString();
                String password = loginPass.getEditText().getText().toString();

                if (!email.isEmpty() || !password.isEmpty()){

                    loginProgress.setTitle("Login..");
                    loginProgress.setMessage("Please wait..");
                    loginProgress.setCanceledOnTouchOutside(false);
                    loginProgress.show();
                    login(email, password);
                }else{
                    Toast.makeText(LoginActivity.this, "Enter details first!!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loginProgress.dismiss();

                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            String current_user_id = mAuth.getCurrentUser().getUid();

                            mUserDatabase.child(current_user_id).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    checkEmailVarification();
                                }else{
                                    String error = task.getException().getMessage();
                                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                                }
                                }
                            });




                        } else {
                            // If sign in fails, display a message to the user.
                            loginProgress.hide();
                            String error = task.getException().getMessage();
                            Toast.makeText(LoginActivity.this, error,
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }

    private void checkEmailVarification(){
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        Boolean checkFlag = firebaseUser.isEmailVerified();
        if(checkFlag){
            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
            startActivity(mainIntent);
            finish();
        }else{
            Toast.makeText(this, "Verify your email first.", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
        }
    }

}
