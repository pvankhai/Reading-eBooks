package com.example.app_ebook_mobile.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.app_ebook_mobile.R;
import com.example.app_ebook_mobile.adapters.AdapterComment;

import com.example.app_ebook_mobile.databinding.ActivityBookDetailUserBinding;
import com.example.app_ebook_mobile.databinding.DialogCommentAddBinding;
import com.example.app_ebook_mobile.models.ModelComment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BookDetailUserActivity extends AppCompatActivity {

    //View binding
    private ActivityBookDetailUserBinding binding;

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
        binding = ActivityBookDetailUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get data
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        //at start hide download button, bc we need book url that we will load later in function loadBookDetails()
        binding.downloadBookBtn.setVisibility(View.GONE);


        // Init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Đang xử lý...");
        progressDialog.setCanceledOnTouchOutside(false);


        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {


        }

        loadBookDetails();



        // Increment book view
        MyApplication.incrementBookViewCountLibrary(bookId);


        //Handle Click Go/back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        //Handle click, Open to view PDF
        binding.readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(BookDetailUserActivity.this, BookViewActivity.class);
                intent1.putExtra("bookId", bookId);
                startActivity(intent1);
            }
        });

        //handle click, download pdf
        binding.downloadBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG_DOWNLOAD, "onClick: Checking permission");
                if (ContextCompat.checkSelfPermission(BookDetailUserActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG_DOWNLOAD, "onClick: Permission already granted, can download book");
                    MyApplication.downloadBook(BookDetailUserActivity.this, "" + bookId, "" + bookTitle, "" + bookUrl);

                } else {
                    Log.d(TAG_DOWNLOAD, "onClick: Permission was not granted, request permission ...");
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }

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
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(FirebaseAuth.getInstance().getUid()).child("Books").child(bookId)
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
                        Log.d(TAG_DOWNLOAD, "onDataChange: timestamp" + bookTitle + "timestamp" + timestamp);

                        //required data is loaded, show download button
                        binding.downloadBookBtn.setVisibility(View.VISIBLE);

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
                                BookDetailUserActivity.this,
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
}