package com.example.pranav.justchat;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

//import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    Toolbar usersToolbar;
    RecyclerView mUsersList;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;
    FirebaseListAdapter firebaseListAdapter;
    FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mAuth = FirebaseAuth.getInstance();


        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        usersToolbar = findViewById(R.id.users_toolbar);
        setSupportActionBar(usersToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersList = findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
        if (mAuth.getCurrentUser() != null) {

            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }
    }




    @Override
    protected void onStart() {
        super.onStart();
        Query query = FirebaseDatabase.getInstance().getReference().child("Users");
        FirebaseRecyclerOptions<Users> firebaseRecyclerOptions = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(query,Users.class)
                .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users,UsersViewHolder>(firebaseRecyclerOptions) {
            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_user_info, parent, false);
                return new UsersViewHolder(v);

            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model) {
                holder.setDisplayName(model.getName());
                holder.setDisplayStatus(model.getStatus());
                holder.setDisplayImage(model.getThumb_image());


                final String user_id = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileInten = new Intent(UsersActivity.this,ProfileActivity.class);
                        profileInten.putExtra("user_id",user_id);
                        startActivity(profileInten);
                    }
                });
            }


        };
        mUsersList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();


    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();

    }

    public class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setDisplayName(String name) {
            TextView userName = (TextView) mView.findViewById(R.id.user_name);
            userName.setText(name);

        }
        public void setDisplayStatus(String status){
            TextView userStatus = (TextView) mView.findViewById(R.id.user_status);
            userStatus.setText(status);
        }
        public void setDisplayImage(String thumb_image){
            CircleImageView userImageView = mView.findViewById(R.id.user_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.pro_pic).into(userImageView);
        }


    }

}
