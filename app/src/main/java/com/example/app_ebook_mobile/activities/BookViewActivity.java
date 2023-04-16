package com.example.app_ebook_mobile.activities;

import static androidx.core.content.FileProvider.getUriForFile;

import static java.security.AccessController.getContext;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.app_ebook_mobile.R;
import com.example.app_ebook_mobile.databinding.ActivityPdfViewBinding;
//import com.github.barteksc.pdfviewer.listener.OnErrorListener;
//import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
//import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pspdfkit.configuration.activity.PdfActivityConfiguration;
import com.pspdfkit.ui.PdfActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BookViewActivity extends AppCompatActivity {

    //View binding
    private ActivityPdfViewBinding binding;

    private String bookId;

    //TAG
    private static final String TAG = "PDF_VIEW_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Get bookId from Intent
        Intent intent = getIntent();
        intent.getData();
        bookId = intent.getStringExtra("bookId");
        Log.d(TAG, "onCreate: " + bookId);


        loadBookDetail();

        //Handle click Go/Back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    private void loadBookDetail() {
        //Get book detail
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(FirebaseAuth.getInstance().getUid()).child("Books").child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Get bookUrl
                        String pdfUrl = "" + snapshot.child("url").getValue();
                        Log.d(TAG, "onDataChange: BookID: " + pdfUrl);
                        // Step 2: Load PDF using bookUrl
                        loadBookFromUrl(pdfUrl);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    // Load book from Storage and Add library pspdfkit
    private void loadBookFromUrl(String pdfUrl) {
        String path = bookId + ".pdf";
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        storageReference.getBytes(Constants.MAX_BYTES_EPUB)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {

                        try {
                            Log.d(TAG, "onSuccess: Cache: "+ "download");
                            File downloadFolder = new File(getFilesDir(), "download");

//                            Log.d(TAG, "onSuccess: ");
//                            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//                            downloadsFolder.mkdirs();
                            downloadFolder.mkdirs();

//                            String filePath = "D:\\"+bookId+ ".pdf";
//                            File.createTempFile(path, null, BookViewActivity.this.getCacheDir());
                            File newFile = new File(downloadFolder, path);
                            Log.d(TAG, "onSuccess: File: " + newFile);
                            FileOutputStream out = new FileOutputStream(newFile);
                            Log.d(TAG, "onSuccess: "+path);
                            out.write(bytes);
                            Log.d(TAG, "onSuccess: Write");
                            out.close();
                            Log.d(TAG, "onSuccess: Close");
//                            File imagePath = new File(getFilesDir(), "download");
//                            Log.d(TAG, "onSuccess: File path: "+imagePath);
//                            File newFile = new File(imagePath, path);

                            Uri contentUri = getUriForFile(BookViewActivity.this, "com.example.app_ebook_mobile.fileprovider", newFile);
                            Log.d(TAG, "onSuccess: "+contentUri);
                            final PdfActivityConfiguration config = new PdfActivityConfiguration.Builder(BookViewActivity.this).build();
                            PdfActivity.showDocument(BookViewActivity.this, contentUri, config);

                            MyApplication.incrementBookViewCountLibrary(bookId);


                        } catch (Exception e) {

                            Toast.makeText(BookViewActivity.this, "", Toast.LENGTH_SHORT).show();

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(BookViewActivity.this, "Failed" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }
}