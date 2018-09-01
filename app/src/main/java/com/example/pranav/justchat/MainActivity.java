package com.example.pranav.justchat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    Toolbar mainToolbar;
    private ViewPager mViewPager;
    private SectionsPagerAdepter  mSectionPager;
    private TabLayout mTabLayout;
    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        if (mAuth.getCurrentUser() != null) {

            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }


        //tabs
        mTabLayout = findViewById(R.id.main_tabs);
        mViewPager = findViewById(R.id.tabPager);
        mSectionPager = new SectionsPagerAdepter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionPager);
        mTabLayout.setupWithViewPager(mViewPager);

        firebaseUser = mAuth.getCurrentUser();
        if(firebaseUser == null){

            sendToStrat();

        }else{

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null){
            mUserRef.child("online").setValue("true");
        }else{
            sendToStrat();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null){
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }
    private void sendToStrat() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.main_logout: {
                mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
                mAuth.signOut();
                sendToStrat();
                return true;
            }
            case R.id.main_account_setting: {
                Intent settingsIntent = new Intent(MainActivity.this, AccountSettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            }

            case R.id.main_user: {
                startActivity(new Intent(MainActivity.this,UsersActivity.class));
                return true;
            }



        }
        return true;
    }
}
