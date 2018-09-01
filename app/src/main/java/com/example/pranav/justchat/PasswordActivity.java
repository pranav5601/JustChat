package com.example.pranav.justchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PasswordActivity extends AppCompatActivity {
    EditText changePass;
    Button done;
    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;
    ProgressDialog passwordDialog;
    Toolbar passToolbar;
    CheckBox showPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        changePass = findViewById(R.id.change_password_et);
        done = findViewById(R.id.done);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        passwordDialog = new ProgressDialog(this);
        passToolbar = findViewById(R.id.pass_toolbar);
        setSupportActionBar(passToolbar);
        getSupportActionBar().setTitle("Change Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        showPass = findViewById(R.id.show_pass_3);

        showPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(showPass.isChecked()){
                    changePass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }else{
                    changePass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });
        
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                String pass = changePass.getText().toString();
                if(!pass.isEmpty()){
                    passwordDialog.setCanceledOnTouchOutside(false);
                    passwordDialog.setTitle("Updating");
                    passwordDialog.setMessage("Please wait...");
                    passwordDialog.show();
                    if(mCurrentUser != null){
                        mCurrentUser.updatePassword(pass).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    mAuth.signOut();
                                    Intent startIntent = new Intent(PasswordActivity.this,StartActivity.class);
                                    startActivity(startIntent);
                                    finish();


                                }else{
                                    String error = task.getException().getMessage();
                                    Toast.makeText(PasswordActivity.this, error, Toast.LENGTH_LONG).show();
                                }
                                passwordDialog.dismiss();
                            }
                        });
                    }

                }else{
                    Toast.makeText(PasswordActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
