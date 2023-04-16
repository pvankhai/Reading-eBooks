package com.example.app_ebook_mobile.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.app_ebook_mobile.R;
import com.example.app_ebook_mobile.adapters.AdapterComment;
import com.example.app_ebook_mobile.databinding.ActivityBookDetailStoreBinding;

import com.example.app_ebook_mobile.databinding.DialogCommentAddBinding;
import com.example.app_ebook_mobile.models.ModelComment;
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

public class BookDetailStoreActivity extends AppCompatActivity {

    //View binding
    private ActivityBookDetailStoreBinding binding;

    //PDF id, get from intent
    String bookId, bookTitle, bookUrl;

    boolean isInMyFavorite = false;

    // Firebase auth
    private FirebaseAuth firebaseAuth;

    // TAG
    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    // Progress dialog
    private ProgressDialog progressDialog;

    // Arr list to hold comment
    private ArrayList<ModelComment> modelCommentArrayList;

    // Adapter to set to recyclerview
    private AdapterComment adapterComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookDetailStoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get data
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

//        //at start hide download button, bc we need book url that we will load later in function loadBookDetails()
//        binding.downloadBookBtn.setVisibility(View.GONE);


        // Init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Đang xử lý...");
        progressDialog.setCanceledOnTouchOutside(false);


        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            checkIsFavorite();

        }

        loadBookDetails();

        loadComments();


        // Increment book view
        MyApplication.incrementBookViewCountStore(bookId);


        //Handle Click Go/back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        //Handle click: Save to Library
        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               MyApplication.saveBook(BookDetailStoreActivity.this, bookId);
            }
        });



        //handle click, add/remove favorite
        binding.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(BookDetailStoreActivity.this, "You're not logged in", Toast.LENGTH_SHORT).show();
                } else {
                    if (isInMyFavorite) {
                        //in favorite, remove from favorite
                        MyApplication.removeFromFavorite(BookDetailStoreActivity.this, bookId);

                    } else {
                        //not in favorite, add to favorite
                        MyApplication.addToFavorite(BookDetailStoreActivity.this, bookId);
                    }
                }
            }
        });

        // Handle click, show comment add dialog
        binding.addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(BookDetailStoreActivity.this, "You're not logged in... ", Toast.LENGTH_SHORT).show();
                } else {
                    addCommentDialog();
                }
            }
        });

    }

    private void loadComments() {
        // Init arr list
        modelCommentArrayList = new ArrayList<>();

        // Db path to load comment
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");
        databaseReference.child(bookId).child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Clear arr
                        modelCommentArrayList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            // Get data
                            ModelComment modelComment = dataSnapshot.getValue(ModelComment.class);
                            // Add to arr list
                            modelCommentArrayList.add(modelComment);
                        }

                        //Setup Adapter
                        adapterComment = new AdapterComment(BookDetailStoreActivity.this, modelCommentArrayList);

                        //Set Adapter
                        binding.commentRv.setAdapter(adapterComment);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    private String comment = "";

    // Add comment dialog
    private void addCommentDialog() {
        // Interface bind view for dialog
        DialogCommentAddBinding commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this));
        // Setup alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        builder.setView(commentAddBinding.getRoot());

        // Create and show alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // Handle click, dismiss dialog
        commentAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        // handle click, add comment
        commentAddBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get data
                comment = commentAddBinding.commentEt.getText().toString().trim();
                // Validate data
                if (TextUtils.isEmpty(comment)) {
                    Toast.makeText(BookDetailStoreActivity.this, "Nhập nội dung bình luận", Toast.LENGTH_SHORT).show();
                } else {
                    alertDialog.dismiss();
                    addComment();
                }
            }
        });

    }

    private void addComment() {
        // Show progress
        progressDialog.setMessage("Đang thêm bình luận");
        progressDialog.show();

        // Timestamp for comment id & comment time
        String timestamp = "" + System.currentTimeMillis();
        // Setup data to add in DB for comment
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + timestamp);
        hashMap.put("bookId", "" + bookId);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("comment", "" + comment);
        hashMap.put("uid", "" + firebaseAuth.getUid());

        // DB path to add data into it
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");
        databaseReference.child(bookId).child("Comments").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(BookDetailStoreActivity.this, "Đã thêm bình luận", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(BookDetailStoreActivity.this, "Không thể thêm bình luận " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    //request storage permission
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG_DOWNLOAD, "Permission Granted");
                    MyApplication.downloadBook(this, "" + bookId, "" + bookTitle, "" + bookUrl);
                } else {
                    Log.d(TAG_DOWNLOAD, "Permission was denied ...: ");
                    Toast.makeText(this, "Không có quyền", Toast.LENGTH_SHORT).show();
                }
            });

    private void loadBookDetails() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");
        databaseReference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //Get data
                        bookTitle = "" + snapshot.child("title").getValue();
                        String description = "" + snapshot.child("description").getValue();
                        String categoryId = "" + snapshot.child("categoryId").getValue();
                        String viewsCount = "" + snapshot.child("viewsCount").getValue();
                        String downloadsCount = "" + snapshot.child("downloadsCount").getValue();
                        bookUrl = "" + snapshot.child("url").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        Log.d(TAG_DOWNLOAD, "onDataChange: timestamp" + timestamp);

                        //required data is loaded, show download button
//                        binding.downloadBookBtn.setVisibility(View.VISIBLE);

                        //Format Date
                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));
                        MyApplication.loadCategory(
                                "" + categoryId,
                                binding.categoryTv
                        );
                        MyApplication.loadEpubFromUrlSinglePage(
                                "" + bookUrl,
                                "" + bookTitle,
                                binding.pdfView,
                                binding.progressBar,
                                binding.pagesTv
                        );
                        MyApplication.loadEpubSize(
                                "" + bookUrl,
                                "" + bookTitle,
                                binding.sizeTv
                        );

                        MyApplication.loadPdfPageCount(
                                BookDetailStoreActivity.this,
                                "" + bookUrl,
                                binding.pagesTv
                        );

                        //Set data
                        binding.titleTv.setText(bookTitle);
                        binding.descriptionTv.setText(description);
                        binding.viewsTv.setText(viewsCount.replace("null", "N/A"));
                        binding.downloadsTv.setText(downloadsCount.replace("null", "N/A"));
                        binding.dateTv.setText(date);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsFavorite() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyFavorite = snapshot.exists();
                        if (isInMyFavorite) {
                            //exists in favorite
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white, 0, 0);
                            binding.favoriteBtn.setText("Bỏ thích");

                        } else {
                            //not exists in favorite
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white, 0, 0);
                            binding.favoriteBtn.setText("Yêu thích");

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }
}