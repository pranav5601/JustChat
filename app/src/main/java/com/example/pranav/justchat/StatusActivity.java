package com.example.pranav.justchat;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    Toolbar statusToolbar;
    TextInputLayout statusChange;
    Button saveBtn;
    DatabaseReference mStatusRefernce;
    FirebaseUser currentUser;
    String uid;
    ProgressBar statusProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        statusChange = findViewById(R.id.status_change);
        saveBtn = findViewById(R.id.save_btn);
        statusToolbar = findViewById(R.id.status_toolbar);
        setSupportActionBar(statusToolbar);
        getSupportActionBar().setTitle("Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        uid= currentUser.getUid();
        mStatusRefernce = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        //toolbar
        statusProgress = findViewById(R.id.progressBar2);
        String status_value = getIntent().getStringExtra("status_value");
        statusChange.getEditText().setText(status_value);

        //chang status
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                statusProgress.setVisibility(View.VISIBLE);
                String status = statusChange.getEditText().getText().toString();
                mStatusRefernce.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            statusProgress.setVisibility(View.INVISIBLE);
                            finish();
                        }else{
                            String error = task.getException().getMessage();
                            Toast.makeText(StatusActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });

    }
}
