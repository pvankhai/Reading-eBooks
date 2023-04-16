package com.example.app_ebook_mobile.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.app_ebook_mobile.databinding.ActivityAddBookUserBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class BookAddUserActivity extends AppCompatActivity{

    //Setup view Binding
    private ActivityAddBookUserBinding binding;

    //Firebase Auth
    private FirebaseAuth firebaseAuth;

    //Progress dialog
    private ProgressDialog progressDialog;

    // ArrList to hold epub categories
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    //Uri of Pick
    private Uri epubUri;

    private static final int EPUB_PICK_CODE = 1000;

    //TAG for debugging
    private static final String TAG = "TAG_ADD_EPUB";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddBookUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Init Firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadEpubCategories();

        //Setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Đang xử lý...");
        progressDialog.setCanceledOnTouchOutside(false);


        //Handle click, go to previous activity *
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        //Handle click, attach Epub
        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                epubPickIntent();
            }
        });

        //handle click, pick category
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPickDialog();
            }
        });

        //handle click, upload Epub
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Validate
                validateData();
            }
        });


    }

    private String title = "", description = "";

    private void validateData() {
        //// Step 1: Validate Data
        Log.d(TAG, "ValidateData: validating data...");

        // Get Data
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        //Validate Data
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Vui lòng nhập tên sách", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Vui lòng nhập mô tả sách", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectCategoryTitle)) {
            Toast.makeText(this, "Vui lòng chọn thể loại sách", Toast.LENGTH_SHORT).show();
        } else if (epubUri == null) {
            Toast.makeText(this, "Vui lòng chọn tệp tải lên", Toast.LENGTH_SHORT).show();
        } else {
            // All Data is Valid, can upload now
            uploadEpubToStorage();
        }
    }

    private void uploadEpubToStorage() {
        //// Step 2: Upload data to Firebase Storage
        Log.d(TAG, "uploadEpubToStorage: Uploading to storage...");

        //Show progress
        progressDialog.setMessage("Đang thêm sách...");
        progressDialog.show();

        //Timestamp
        long timestamp = System.currentTimeMillis();

        //Path of Epub in Firebase storage
        String filePathAndName = "Library/" + timestamp; // URL den quyen sach

        //Storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(epubUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: Epub uploaded to storage...");
                        Log.d(TAG, "onSuccess: Getting Epub URL");

                        //Get Url Epub
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String uploadedEpubUrl = "" + uriTask.getResult();

                        //Upload to Firebase DB
                        UploadEpubInfoToDB(uploadedEpubUrl, timestamp);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Epub upload failed due to " + e.getMessage());
                        Toast.makeText(BookAddUserActivity.this, "Thêm sách không thành công" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void UploadEpubInfoToDB(String uploadedEpubUrl, long timestamp) {
        //// Step 3: Upload Epub info to Firebase DB
        Log.d(TAG, "uploadEpubToStorage: Uploading to storage...");

        progressDialog.setMessage("Đang thêm thông tin sách...");

        String uid = firebaseAuth.getUid();

        // Setup Data to Upload
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + uid);
        hashMap.put("id", "" + timestamp);
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("categoryId", "" + selectCategoryId);
        hashMap.put("url", "" + uploadedEpubUrl);
        hashMap.put("timestamp", timestamp);
        hashMap.put("viewsCount", 0);
        hashMap.put("downloadsCount", 0);


        // DB reference: DB > Books
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(firebaseAuth.getUid()).child("Books").child("" + timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: Successfully upload...");
                        Toast.makeText(BookAddUserActivity.this, "Thêm sách thành công", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Failed to Upload Data to DB due to " + e.getMessage());
                        Toast.makeText(BookAddUserActivity.this, "Thêm thông tin sách vào DB thất bại" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    private void loadEpubCategories() {
        Log.d(TAG, "loadEpubCategories: Loading Epub Categories...");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        // DB reference to load categories ... db > Categories
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();  //clear before add data
                categoryIdArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    //Get Id & Title of Category
                    String categoryId = "" + dataSnapshot.child("id").getValue();
                    String categoryTitle = "" + dataSnapshot.child("category").getValue();

                    //Add to respective arraylist
                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    // Selected category Id & Title
    private String selectCategoryId, selectCategoryTitle;

    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing category pick dialog");

        //get String arr Category from arr list
        String[] categoriesArr = new String[categoryTitleArrayList.size()];
        for (int i = 0; i < categoryTitleArrayList.size(); i++) {
            categoriesArr[i] = categoryTitleArrayList.get(i);

        }

        //Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn thể loại")
                .setItems(categoriesArr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Handle Item Click
                        // Get Clicked item from Dialog list
                        selectCategoryTitle = categoryTitleArrayList.get(i);
                        selectCategoryId = categoryIdArrayList.get(i);


                        //set category textView
                        binding.categoryTv.setText(selectCategoryTitle);

                        Log.d(TAG, "onClick: Selected Category " + selectCategoryId + "" + selectCategoryTitle);
                    }
                })
                .show();

    }

    private void epubPickIntent() {
        Log.d(TAG, "epubPickIntent: starting Epub pick intent");

        Intent intent = new Intent();
        intent.setType("application/epub+zip");
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Epub"), EPUB_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == EPUB_PICK_CODE) {
                Log.d(TAG, "onActivityResult: EPUB Pick Add");

                epubUri = data.getData();

                Log.d(TAG, "onActivityResult: " + epubUri);
                Toast.makeText(this, "Thêm tệp thành công", Toast.LENGTH_SHORT).show();

            }
        } else {
            Log.d(TAG, "onActivityResult: Canceling Picking Epub");
            Toast.makeText(this, "Dừng thêm tệp", Toast.LENGTH_SHORT).show();
        }
    }
}
