package com.example.pranav.justchat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    String mChatUser, mChatUserName, mChatUserImage, mCurrentUser;
    private Toolbar chatToolbar;
    private DatabaseReference mRootRef;
    private DatabaseReference mUserRef;
    private ActionBar actionBar;
    private TextView chatDisplayName, chatLastSeen;
    EditText chatSend;
    ImageView chatAddBtn, chatSendBtn;
    RecyclerView mMessageList;
    SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    public static final int total_item_to_load = 10;
    private int mCurrentPage = 1;

    FirebaseAuth mAuth;
    private StorageReference mImageStorage;

    private int itemPos = 0;
    private String mLastKey = "";
    private String prevKey = "";
    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser().getUid();

        mChatUser = getIntent().getStringExtra("user_id");
        mChatUserName = getIntent().getStringExtra("chat_user_name");
        mChatUserImage = getIntent().getStringExtra("chat_user_image");

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbar_view = layoutInflater.inflate(R.layout.chat_custom_layout,null);
        CircleImageView chatImage = actionbar_view.findViewById(R.id.custom_chat_image_view);
        Picasso.get().load(mChatUserImage).placeholder(R.drawable.pro_pic).into(chatImage);
        actionBar.setCustomView(actionbar_view);

        chatSend = findViewById(R.id.chat_send);
        chatAddBtn = findViewById(R.id.chat_plus_btn);
        chatSendBtn = findViewById(R.id.chat_send_btn);


        chatDisplayName = findViewById(R.id.chat_display_name);
        chatLastSeen = findViewById(R.id.chat_last_seen);
        chatDisplayName.setText(mChatUserName);

        mAdapter = new MessageAdapter(messagesList);

        mMessageList = findViewById(R.id.chat_list);
        mRefreshLayout = findViewById(R.id.message_swipe_layout);

        mLinearLayout = new LinearLayoutManager(this);
        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearLayout);
        mMessageList.setAdapter(mAdapter);
        mImageStorage = FirebaseStorage.getInstance().getReference();




        loadMessages();

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();

                if(online.equals("true")){
                    chatLastSeen.setText("Online");
                }else{

                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    long last_time = Long.parseLong(online);

                    String lastSeenTimeAgo = getTimeAgo.getTimeAgo(last_time,getApplicationContext());

                    chatLastSeen.setText(lastSeenTimeAgo);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mChatUser)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestemp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/"+mCurrentUser+"/"+mChatUser,chatAddMap);
                    chatUserMap.put("Chat/"+mChatUser+"/"+mCurrentUser,chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError == null){

                        }else{
                            String error = databaseError.getMessage();
                            Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //------------SEND THE MESSAGE---------------------------------

        chatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos = 0;
                loadMoreMessages();
            }
        });

        chatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            final String current_user_ref = "messages/" + mCurrentUser + "/" + mChatUser;
            final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUser;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUser).child(mChatUser).push();

            final String push_id = user_message_push.getKey();


            StorageReference filepath = mImageStorage.child("message_images").child( push_id + ".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){

                        String download_url = task.getResult().getDownloadUrl().toString();


                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUser);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        chatSend.setText("");


                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if(databaseError != null){

                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                }

                            }
                        });


                    }

                }
            });

        }

    }



    public void loadMoreMessages(){
        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUser).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);

                String messageKey = dataSnapshot.getKey();

                if(!prevKey.equals(messageKey)){
                    messagesList.add(itemPos++,messages);
                }else{
                    prevKey = mLastKey;
                }
                if(itemPos == 1){

                    mLastKey = messageKey;
                }
                mAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10,0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUser).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * total_item_to_load);


        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                itemPos++;

                if(itemPos == 1){
                    String messageKey = dataSnapshot.getKey();
                    mLastKey = messageKey;
                    prevKey = messageKey;
                }

                messagesList.add(messages);
                mAdapter.notifyDataSetChanged();
                mMessageList.scrollToPosition(messagesList.size() - 1);
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        String message = chatSend.getText().toString();
        if(!TextUtils.isEmpty(message)){

            String current_user_ref = "messages/"+mCurrentUser+"/"+mChatUser;
            String chat_user_ref = "messages/"+mChatUser+"/"+mCurrentUser;

            DatabaseReference user_message_ref = mRootRef.child("messages").child(mCurrentUser).child(mChatUser).push();
            String push_id = user_message_ref.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("message",message);
            messageMap.put("from",mCurrentUser);

            Map userMap = new HashMap();
            userMap.put(current_user_ref+"/"+push_id,messageMap);
            userMap.put(chat_user_ref+"/"+push_id,messageMap);

            chatSend.setText("");

            mRootRef.child("Chat").child(mCurrentUser).child(mChatUser).child("seen").setValue(true);
            mRootRef.child("Chat").child(mCurrentUser).child(mChatUser).child("timestemp").setValue(ServerValue.TIMESTAMP);

            mRootRef.child("Chat").child(mChatUser).child(mCurrentUser).child("seen").setValue(false);
            mRootRef.child("Chat").child(mChatUser).child(mCurrentUser).child("timestemp").setValue(ServerValue.TIMESTAMP);

            mRootRef.updateChildren(userMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError == null){

                }else{
                    String error = databaseError.getMessage();
                    Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show();
                }
                }
            });


        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser mUser = mAuth.getCurrentUser();
        if(mUser!=null)
            mUserRef.child("online").setValue("true");

    }


}
