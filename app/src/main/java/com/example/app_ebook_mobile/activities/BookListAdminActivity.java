package com.example.app_ebook_mobile.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.example.app_ebook_mobile.adapters.AdapterBookAdmin;
import com.example.app_ebook_mobile.databinding.ActivityBookListAdminBinding;
import com.example.app_ebook_mobile.models.ModelBook;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BookListAdminActivity extends AppCompatActivity {

    //View Binding
    private ActivityBookListAdminBinding binding;

    //ArrList
    private ArrayList<ModelBook> epubArrayList;

    //Adapter
    private AdapterBookAdmin adapterBookAdmin;

    //Intent
    private String categoryId, categoryTitle;

    //TAG
    private static final String TAG = "EPUB_LIST_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookListAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Get data from intent
        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        categoryTitle = intent.getStringExtra("categoryTitle");


        //Set Epub category
        binding.subTitleTv.setText(categoryTitle);

        loadBookList();

        //Search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Search us & when user type each letter
                try {
                    adapterBookAdmin.getFilter().filter(charSequence);

                } catch (Exception e) {
                    Log.d(TAG, "onTextChanged: " + e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        //Handle Click
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadBookList() {
        //Init list before adding data
        epubArrayList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");
        databaseReference.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        epubArrayList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            //Get epub
                            ModelBook modelEpub = dataSnapshot.getValue(ModelBook.class);
                            //Add epub to List
                            epubArrayList.add(modelEpub);

                        }
                        //Setup Adapter
                        adapterBookAdmin = new AdapterBookAdmin(BookListAdminActivity.this, epubArrayList);
                        binding.bookRV.setAdapter(adapterBookAdmin);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}