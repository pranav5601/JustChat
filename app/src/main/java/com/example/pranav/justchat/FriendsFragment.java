package com.example.pranav.justchat;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

RecyclerView mFriendsList;
DatabaseReference mFriendsDatabase;
DatabaseReference mUsersDatabase;

FirebaseAuth mAuth;
String mCurrent_user_id;
View mView;
private FirebaseRecyclerAdapter<Friends,FriendsViewHolder> firebaseRecyclerAdapter;



    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendsList = mView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Query query = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        FirebaseRecyclerOptions<Friends> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(query,Friends.class)
                .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                firebaseRecyclerOptions
        ) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {
                holder.setDate(model.getDate());


                final String list_user_id = getRef(position).getKey();
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String display_name = dataSnapshot.child("name").getValue().toString();
                        final String display_image = dataSnapshot.child("thumb_image").getValue().toString();

                        holder.setName(display_name);
                        holder.setDisplayImage(display_image);
                        if(dataSnapshot.hasChild("online")) {
                            String user_online_status =  dataSnapshot.child("online").getValue().toString();
                            holder.setUserOnline(user_online_status);
                        }

                        holder.fView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence option[] = new CharSequence[]{"Open Profile","Send Message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Option");
                                builder.setItems(option, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(which == 0){
                                            Intent profileInten = new Intent(getContext(),ProfileActivity.class);
                                            profileInten.putExtra("user_id",list_user_id);
                                            profileInten.putExtra("user_name",display_name);
                                            profileInten.putExtra("user_image",display_image);
                                            startActivity(profileInten);
                                        }
                                        if(which == 1){
                                            Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("user_id",list_user_id);
                                            chatIntent.putExtra("chat_user_name",display_name);
                                            chatIntent.putExtra("chat_user_image",display_image);
                                            startActivity(chatIntent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_user_info, parent, false);
                return new FriendsViewHolder(v);
            }
        };
        mFriendsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{


        View fView;
        public FriendsViewHolder(View itemView) {
            super(itemView);
            fView = itemView;
        }

        public void setDate(String date) {
            TextView userStatus = (TextView) fView.findViewById(R.id.user_status);
            userStatus.setText(date);
        }
        public void setName(String name){
            TextView userName = (TextView) fView.findViewById(R.id.user_name);
            userName.setText(name);
        }
        public void setDisplayImage(String thumb_image){
            CircleImageView userImageView = fView.findViewById(R.id.user_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.pro_pic).into(userImageView);
        }

        public void setUserOnline(String online){
            ImageView online_icon = fView.findViewById(R.id.user_online_icon);
            if(online.equals("true")){
                online_icon.setVisibility(View.VISIBLE);
            }else{
                online_icon.setVisibility(View.INVISIBLE);
            }
        }
    }
}
