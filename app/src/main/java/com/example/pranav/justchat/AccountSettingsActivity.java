package com.example.pranav.justchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class AccountSettingsActivity extends AppCompatActivity {

    DatabaseReference mDatabaseReference;
    FirebaseUser mFirebaseUser;
    CircleImageView displayImage;
    TextView dispName, dispStatus;
    Button changeImageBtn, changeStatusbtn;
    Toolbar settingsToolbar;
    ProgressDialog progressDialog;
    DatabaseReference mUserRef;
    FirebaseAuth mAuth;

    String downloadLink;
    String thumbDownloadLink;
    File thumb_image_file;
    byte[] thumbImage;

    //storage database
    private StorageReference imageStorage;
    private FirebaseDatabase mUserDatabse;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mFirebaseUser.getUid();
        displayImage = findViewById(R.id.settings_image);
        dispName = findViewById(R.id.display_name);
        dispStatus = findViewById(R.id.setting_status);
        changeImageBtn = findViewById(R.id.change_image_btn);
        changeStatusbtn = findViewById(R.id.change_status_btn);
        settingsToolbar = findViewById(R.id.settings_toolbar);
        imageStorage = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(AccountSettingsActivity.this);

        if (mAuth.getCurrentUser() != null) {

            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }


        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        changeStatusbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String status_value = dispStatus.getText().toString();

                Intent statusIntent = new Intent(AccountSettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("status_value", status_value);
                startActivity(statusIntent);

            }
        });

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mDatabaseReference.keepSynced(true);

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                dispName.setText(name);
                dispStatus.setText(status);

                if (!image.equals("default")) {
//                    Picasso.get().load(image).placeholder(R.drawable.pro_pic).into(displayImage);
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.pro_pic).into(displayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.pro_pic).into(displayImage);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        changeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .setMinCropWindowSize(1000,1000)
                        .start(AccountSettingsActivity.this);
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                progressDialog.setTitle("Uploading");
                progressDialog.setMessage("Please wait..");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();
                displayImage.setImageURI(resultUri);
                final File thumb_file = new File(resultUri.getPath());
                try {
                    Bitmap thumb_bitmap = new Compressor(this).setMaxWidth(200).setMaxHeight(200).setQuality(75).compressToBitmap(thumb_file);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    thumbImage = baos.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                String current_user_id = mFirebaseUser.getUid();

                final StorageReference filepath = imageStorage.child("profile_images").child(current_user_id + ".jpg");
                final StorageReference thumb_filepath = imageStorage.child("profile_images").child("thumbs").child(current_user_id+".jpg");


                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            downloadLink = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filepath.putBytes(thumbImage);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if(task.isSuccessful()){

                                    String thumbUrl = task.getResult().getDownloadUrl().toString();

                                    Map updateMap = new HashMap();
                                    updateMap.put("image",downloadLink);
                                    updateMap.put("thumb_image",thumbUrl);

                                    mDatabaseReference.updateChildren(updateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                progressDialog.dismiss();
                                                Toast.makeText(AccountSettingsActivity.this, "Successfully Uploaded", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                                }else{
                                    //TODO remove 1
                                    String error = task.getException().getMessage();
                                    Toast.makeText(AccountSettingsActivity.this, "1 "+error, Toast.LENGTH_LONG).show();
                                    progressDialog.dismiss();
                                }
                                }
                            });



                        } else {
                            //TODO remove 2
                            String error = task.getException().getMessage();
                            Toast.makeText(AccountSettingsActivity.this, "2 "+error, Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.remove_image: {
                Map deleteImage = new HashMap();
                deleteImage.put("image","default");
                deleteImage.put("thumb_image","default");
                mDatabaseReference.updateChildren(deleteImage).addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Picasso.get().load(R.drawable.pro_pic).into(displayImage);
                            Toast.makeText(AccountSettingsActivity.this, "Successfully removed", Toast.LENGTH_SHORT).show();
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(AccountSettingsActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                return true;
            }
            case R.id.change_password:{

                startActivity(new Intent(AccountSettingsActivity.this,PasswordActivity.class));

            }
        }
        return super.onOptionsItemSelected(item);
    }


}
