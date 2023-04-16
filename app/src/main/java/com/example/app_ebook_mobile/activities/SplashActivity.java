package com.example.app_ebook_mobile.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.app_ebook_mobile.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    //firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        // start main screen after 2 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUser();
            }
        }, 3000);// 3000 mean 3 seconds
    }

    private void checkUser() {
        //get current user, if logged in
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            //user not logged in
            //start main screen
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        } else {
            //user logged in check user type, sane as done in login screen
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
            ref.child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            //get user type
                            String userType = "" + snapshot.child("userType").getValue();
                            //check user type
                            if (userType.equals("user")) {
                                //this is simple user, open user dashboard
                                startActivity(new Intent(SplashActivity.this, DashboardUserActivity.class));
                                finish();
                            } else if (userType.equals("admin")) {
                                //this is admin, open admin dashboard
                                startActivity(new Intent(SplashActivity.this, DashboardAdminActivity.class));
                                finish();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError error) {

                        }
                    });
        }
    }
}