package com.example.pranav.justchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    TextView profileName,profileStatus,totalFriend;
    Button sendRequestBtn,declineBtn;
    ImageView profileImage;
    ProgressDialog progressDialog;
    String current_state;
    String display_name;

    DatabaseReference mDatabseReference;
    DatabaseReference mFriendsRequestDatabase;
    DatabaseReference mFriendDatabase;
    DatabaseReference mNotificationDatabase;
    DatabaseReference mRootRef;
    DatabaseReference mUserRef;

    FirebaseUser mCurrentUesr;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileName = findViewById(R.id.profile_name);
        profileStatus = findViewById(R.id.profile_status);
        totalFriend = findViewById(R.id.total_friends);
        sendRequestBtn = findViewById(R.id.send_request_btn);
        profileImage = findViewById(R.id.profile_image);
        declineBtn = findViewById(R.id.decline_btn);

        mAuth = FirebaseAuth.getInstance();

        final String user_id = getIntent().getStringExtra("user_id");
        current_state = "not_friends";

        mDatabseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendsRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_request");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootRef = FirebaseDatabase.getInstance().getReference();

        if (mAuth.getCurrentUser() != null) {

            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }

        mCurrentUesr = FirebaseAuth.getInstance().getCurrentUser();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Loading");
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();

        //hide decline btn
        declineBtn.setVisibility(View.INVISIBLE);
        declineBtn.setEnabled(false);


        mDatabseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            display_name = dataSnapshot.child("name").getValue().toString();
            String display_image = dataSnapshot.child("image").getValue().toString();
            String display_status = dataSnapshot.child("status").getValue().toString();

            profileName.setText(display_name);
            profileStatus.setText(display_status);
                Picasso.get().load(display_image).placeholder(R.drawable.pro_pic).into(profileImage);

                //---------------Friendlist-----------------------------------------
                mFriendsRequestDatabase.child(mCurrentUesr.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if(req_type.equals("received")){
                                current_state = "request_received";
                                sendRequestBtn.setText("Accept Friend request");
                                declineBtn.setVisibility(View.VISIBLE);
                                declineBtn.setEnabled(true);
                            }else if(current_state.equals("sent")){
                                current_state = "req_sent";
                                sendRequestBtn.setText("Cancel friend request");
                                declineBtn.setVisibility(View.INVISIBLE);
                                declineBtn.setEnabled(false);
                            }
                        }else {
                            mFriendDatabase.child(mCurrentUesr.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){
                                        current_state = "friends";
                                        sendRequestBtn.setText("Unfriend");
                                        declineBtn.setVisibility(View.INVISIBLE);
                                        declineBtn.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        progressDialog.dismiss();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        sendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendRequestBtn.setEnabled(false);

                //--------------------not friend state-------------------------
                if(current_state.equals("not_friends")){

                    DatabaseReference newNotification = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationID = newNotification.getKey();
                    final HashMap<String,String> hashMap = new HashMap<>();
                    hashMap.put("from",mCurrentUesr.getUid());
                    hashMap.put("type","request");

                   Map requestMap = new HashMap();
                   requestMap.put("Friend_request/"+ mCurrentUesr.getUid() +"/"+ user_id + "/request_type","sent");
                   requestMap.put("Friend_request/"+ user_id +"/"+ mCurrentUesr.getUid() +"/request_type","received");
                   requestMap.put("notifications/"+ user_id+"/"+ newNotificationID,hashMap);

                   mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                       @Override
                       public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                           if(databaseError!= null){
                               String error = databaseError.getMessage();
                               Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                           }
                               current_state = "req_sent";
                           sendRequestBtn.setEnabled(true);
                               sendRequestBtn.setText("Cancel friend request");
                               declineBtn.setVisibility(View.INVISIBLE);
                               declineBtn.setEnabled(false);
                               Toast.makeText(ProfileActivity.this, "Request sent", Toast.LENGTH_SHORT).show();


                       }
                   });
                }

                //---------------------request sent--------------------------
                if(current_state == "req_sent"){
                    mFriendsRequestDatabase.child(mCurrentUesr.getUid()).child(user_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        mFriendsRequestDatabase.child(user_id).child(mCurrentUesr.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                sendRequestBtn.setEnabled(true);
                                current_state = "not_friends";
                                sendRequestBtn.setText("Send friend request");
                                declineBtn.setVisibility(View.INVISIBLE);
                                declineBtn.setEnabled(false);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                sendRequestBtn.setEnabled(true);
                                Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                if(current_state.equals("request_received")){
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendMap = new HashMap();
                    friendMap.put("Friends/"+ mCurrentUesr.getUid()+"/"+user_id+"/date",currentDate);
                    friendMap.put("Friends/"+user_id+"/"+mCurrentUesr.getUid()+"/date",currentDate);

                    friendMap.put("Friend_request/"+mCurrentUesr.getUid()+"/"+user_id,null);
                    friendMap.put("Friend_request/"+user_id+"/"+mCurrentUesr.getUid(),null);

                    mRootRef.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                         if(databaseError == null){
                             sendRequestBtn.setEnabled(true);
                             current_state = "friends";
                             sendRequestBtn.setText("Unfriend");
                             declineBtn.setVisibility(View.INVISIBLE);
                             declineBtn.setEnabled(false);
                         }else{
                                String error = databaseError.getMessage();
                             Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                         }
                        }
                    });
                }
                //--------------remove friend-----------------------
                if(current_state.equals("friends")){
                    mFriendDatabase.child(mCurrentUesr.getUid()).child(user_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendDatabase.child(user_id).child(mCurrentUesr.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            sendRequestBtn.setEnabled(true);
                                            current_state = "not_friends";
                                            sendRequestBtn.setText("Send friend request");
                                            declineBtn.setVisibility(View.INVISIBLE);
                                            declineBtn.setEnabled(false);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            sendRequestBtn.setEnabled(true);
                                            Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
            }
        });

        declineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequestBtn.setEnabled(false);
                declineBtn.setEnabled(false);
                mFriendsRequestDatabase.child(mCurrentUesr.getUid()).child(user_id).removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mFriendsRequestDatabase.child(user_id).child(mCurrentUesr.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        sendRequestBtn.setEnabled(true);
                                        current_state = "not_friends";
                                        sendRequestBtn.setText("Send friend request");
                                        declineBtn.setVisibility(View.INVISIBLE);
                                        declineBtn.setEnabled(false);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        declineBtn.setEnabled(true);
                                        sendRequestBtn.setEnabled(true);
                                        Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        });
    }


}
