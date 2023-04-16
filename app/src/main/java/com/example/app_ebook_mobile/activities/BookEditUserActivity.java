package com.example.app_ebook_mobile.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.app_ebook_mobile.databinding.ActivityBookEditUserBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class BookEditUserActivity extends AppCompatActivity {

    //view binding
    private ActivityBookEditUserBinding binding;

    //book id get from intent started from AdapterEpubAdmin
    private String bookId;

    //progress dialog
    private ProgressDialog progressDialog;

    private ArrayList<String> categoryTitleArrayList, categoryIdArraylist;

    private static final String TAG = "BOOK_EDIT_USER_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookEditUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookId = getIntent().getStringExtra("bookId");

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Đang xử lý...");
        progressDialog.setCanceledOnTouchOutside(false);

        loadCategories();
        loadBookInfo();


        //handle click, pick category
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryDialog();
            }
        });


        //handle click, go to previous screen
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        //handle click begin upload
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();

            }
        });
    }

    private void loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading book info");

        DatabaseReference refBook = FirebaseDatabase.getInstance().getReference("users");
        refBook.child(FirebaseAuth.getInstance().getUid()).child("Books").child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //set book info
                        selectedCategoryId = "" + snapshot.child("categoryId").getValue();
                        String description = "" + snapshot.child("description").getValue();
                        String title = "" + snapshot.child("title").getValue();

                        //set to views
                        binding.titleEt.setText(title);
                        binding.descriptionEt.setText(description);

                        Log.d(TAG, "onDataChange: Loading Book Category Info");
                        DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference("Categories");
                        refBookCategory.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //get category
                                        String category = "" + snapshot.child("category").getValue();
                                        //set to category text view
                                        binding.categoryTv.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String title = "", description = "";

    private void validateData() {
        //get data
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Vui lòng nhập tên sách", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Vui lòng nhập mô tả sách", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(selectedCategoryId)) {
            Toast.makeText(this, "Chọn thể loại", Toast.LENGTH_SHORT).show();
        } else {
            updatePdf();
        }
    }

    private void updatePdf() {
        Log.d(TAG, "updatePdf: Starting updating pdf info to db ...");

        //show progress
        progressDialog.setMessage("Đang cập nhật thông tin sách...");
        progressDialog.show();

        //setup data to update to db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("categoryId", "" + selectedCategoryId);

        //start updating
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(FirebaseAuth.getInstance().getUid()).child("Books").child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Book updated...");
                        progressDialog.dismiss();
                        Toast.makeText(BookEditUserActivity.this, "Thông tin sách đã được cập nhật", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update due to " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(BookEditUserActivity.this, "Thông tin sách cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String selectedCategoryId = "", selectedCategoryTitle = "";

    private void categoryDialog() {
        //make string array from arrayList of String
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i < categoryIdArraylist.size(); i++) {
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }


        //Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn thể loại")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedCategoryId = categoryIdArraylist.get(i);
                        selectedCategoryTitle = categoryTitleArrayList.get(i);

                        //set to textview
                        binding.categoryTv.setText(selectedCategoryTitle);
                    }
                })
                .show();
    }

    private void loadCategories() {
        Log.d(TAG, "LoadCategories: Loading categories...");


        categoryIdArraylist = new ArrayList<>();
        categoryTitleArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdArraylist.clear();
                categoryTitleArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String id = "" + ds.child("id").getValue();
                    String category = "" + ds.child("category").getValue();
                    categoryIdArraylist.add(id);
                    categoryTitleArrayList.add(category);

                    Log.d(TAG, "onDataChange: ID: " + id);
                    Log.d(TAG, "onDataChange: Category: " + category);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}