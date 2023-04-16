package com.example.app_ebook_mobile.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.app_ebook_mobile.R;


import com.example.app_ebook_mobile.adapters.AdapterBookFavorite;
import com.example.app_ebook_mobile.databinding.ActivityProfileBinding;
import com.example.app_ebook_mobile.models.ModelBook;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ProfileActivity extends AppCompatActivity {

    // View binding
    private ActivityProfileBinding binding;

    // Firebase
    private FirebaseAuth firebaseAuth;

    //arrayList to hold the books
    private ArrayList<ModelBook> pdfArrayList;

    //adapter to set in recyclerview
    private AdapterBookFavorite adapterPdfFavorite;

    //TAG
    private static final String TAG = "PROFILE_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        //Set firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();

        loadFavoriteBooks();

        // Handle cLick start user edit page
        binding.profileEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class));
            }
        });

        //Handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }

    private void loadUserInfo() {
        Log.d(TAG, "loadUserInfo: Loading user info..." + firebaseAuth.getUid());
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Get all info
                        String email = "" + snapshot.child("email").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String uid = "" + snapshot.child("uid").getValue();
                        String userType = "" + snapshot.child("userType").getValue();

                        // Format data
                        String formattedDate = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        // Set data UI
                        binding.emailTv.setText(email);
                        binding.nameTv.setText(name);
                        binding.memberDateTv.setText(formattedDate);
                        binding.accountTypeTv.setText(userType);

                        // Set Image, using glide
                        Glide.with(ProfileActivity.this)
                                .load(profileImage)
                                .placeholder(R.drawable.ic_person_gray)
                                .into(binding.profileTv);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void loadFavoriteBooks() {
        //init lint
        pdfArrayList = new ArrayList<>();

        //load favorite books database
        //users -> userId -> favorites
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(firebaseAuth.getUid()).child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before starting adding data
                        pdfArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //we will only get the bookId here, and we got other details in adapter using that bookId
                            String bookId = "" + ds.child("bookId").getValue();

                            //set id to model
                            ModelBook modelPdf = new ModelBook();
                            modelPdf.setId(bookId);

                            //add model toi list
                            pdfArrayList.add(modelPdf);


                        }

                        //set number of favorite Books
                        binding.favoriteBookCountTv.setText("" + pdfArrayList.size());
                        //setup adapter
                        adapterPdfFavorite = new AdapterBookFavorite(ProfileActivity.this, pdfArrayList);
                        //set adapter to recyclerview
                        binding.bookRV.setAdapter(adapterPdfFavorite);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}