package com.example.app_ebook_mobile.activities;

import static android.content.ContentValues.TAG;
import static com.example.app_ebook_mobile.activities.Constants.MAX_BYTES_EPUB;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MyApplication extends Application {

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static final String formatTimestamp(long timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        // Format dd/MM/yyyy
        String date = DateFormat.format("dd/MM/yyyy", calendar).toString();
        return date;
    }


    // Save to Library
    public  static  void saveBook(Context context, String bookId){
        String TAG = "SAVE_BOOK_TAG";
        //1) Get Bô

        DatabaseReference refBook = FirebaseDatabase.getInstance().getReference("Books");
        refBook.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("uid", "" + FirebaseAuth.getInstance().getUid());
                        hashMap.put("id", "" + snapshot.child("timestamp").getValue());
                        hashMap.put("title", "" + snapshot.child("title").getValue());
                        hashMap.put("description", "" + snapshot.child("description").getValue());
                        hashMap.put("categoryId", "" + snapshot.child("categoryId").getValue());
                        hashMap.put("url", "" + snapshot.child("url").getValue());
                        hashMap.put("timestamp", snapshot.child("timestamp").getValue());
                        hashMap.put("viewsCount", snapshot.child("viewsCount").getValue());
                        hashMap.put("downloadsCount", snapshot.child("downloadsCount").getValue());

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
                        ref.child(FirebaseAuth.getInstance().getUid()).child("Books").child(bookId)
                                .setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context, "Đã thêm vào thư viện cá nhân", Toast.LENGTH_SHORT).show();
//                                        Log.d(TAG, "onSuccess: ạihdjkas "+ bookId+" "+hashMap.get("uid")+" "+ hashMap.get("id"));
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Không thể thêm vào thư viện cá nhân", Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        }




    //Delete book from Store
    public static void deleteBookStore(Context context, String bookId, String bookUrl, String bookTitle) {
        String TAG = "DELETE_BOOK_TAG";

        Log.d(TAG, "DeleteBook: Deleting...");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Đang xử lý...");
        progressDialog.setMessage("Đang xóa " + bookTitle + " ...");//deleting book
        progressDialog.show();

        Log.d(TAG, "DeleteBook: Deleting...");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "OnSuccess: Deleted from storage");

                        Log.d(TAG, "onSuccess: now deleting info from database");
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Deleted from database too");
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Xóa sách thành công", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Failed to delete from db due to " + e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Xóa sách thất bại" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to delete from storage due to " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    //Delete book from Library
    public static void deleteBookUser(Context context, String bookId, String bookUrl, String bookTitle) {
        String TAG = "DELETE_BOOK_TAG";

        Log.d(TAG, "DeleteBook: Deleting...");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Đang xử lý...");
        progressDialog.setMessage("Đang xóa " + bookTitle + " ...");//deleting book
        progressDialog.show();

        Log.d(TAG, "DeleteBook: Deleting...");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "OnSuccess: Deleted from storage");

                        Log.d(TAG, "onSuccess: now deleting info from database");
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                        reference.child(FirebaseAuth.getInstance().getUid()).child("Books").child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Deleted from database too");
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Xóa sách thành công", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Failed to delete from db due to " + e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Xóa sách thất bại" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to delete from storage due to " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    //LoadPDFSize
    public static void loadEpubSize(String pdfUrl, String pdfTitle, TextView sizeTv) {
        String TAG = "PDF_SIZE_TAG";

        // Using Url Get file and its metadata from Firebase Storage

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        storageReference.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        //Get Size
                        double bytes = storageMetadata.getSizeBytes();

                        Log.d(TAG, "onSuccess: " + pdfTitle + "" + bytes);

                        //Convert Bytes to KB, MB
                        double kb = bytes / 1204;
                        double mb = kb / 1024;

                        if (mb >= 1) {
                            sizeTv.setText(String.format("%.2f", mb) + " MB");
                        } else if (kb >= 1) {
                            sizeTv.setText(String.format("%.2f", kb) + " KB");
                        } else {
                            sizeTv.setText(String.format("%.2f", bytes) + " Bytes");
                        }
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed Get Metadata
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });
    }

    //LoadPDFSize-Store
    public static void loadEpubSizeStore(String pdfUrl, String pdfTitle, TextView sizeTv) {
        String TAG = "PDF_SIZE_TAG";

        // Using Url Get file and its metadata from Firebase Storage

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        storageReference.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        //Get Size
                        double bytes = storageMetadata.getSizeBytes();

                        Log.d(TAG, "onSuccess: " + pdfTitle + "" + bytes);

                        //Convert Bytes to KB, MB
                        double kb = bytes / 1204;
                        double mb = kb / 1024;

                        if (mb >= 1) {
                            sizeTv.setText(String.format("%.2f", mb) + " MB");
                        } else if (kb >= 1) {
                            sizeTv.setText(String.format("%.2f", kb) + " KB");
                        } else {
                            sizeTv.setText(String.format("%.2f", bytes) + " Bytes");
                        }
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed Get Metadata
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });
    }

    //LoadCategory
    public static void loadCategoryStore(String categoryId, TextView categoryTv) {
        //Get Category using categoryId

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Categories");
        databaseReference.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //Get Category
                        String category = "" + snapshot.child("category").getValue();

                        //Set Category text view
                        categoryTv.setText(category);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    //LoadPDF-Store
    public static void loadEpubFromUrlSinglePage1(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar, TextView pagesTv) {
        String TAG = "PDF_LOAD_SINGLE_TAG";

        // Using Url get file data & its metadata
        Log.d(TAG, "loadEpubFromUrlSinglePage1: PDF URL" + pdfUrl);
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);


        Log.d(TAG, "loadEpubFromUrlSinglePage1: URL" + storageReference);
        storageReference.getBytes(MAX_BYTES_EPUB)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: " + pdfTitle + " successfully");
                        // Set Epub View
                        pdfView.fromBytes(bytes)
                                .pages(0)  //Show first page
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        //Hide Process
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onError: " + t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        //Hide Process
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onPageError: " + t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        //Hide Process
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "loadComplete: pdf loaded");

                                        //if pagesTv param is not full then set page numbers
                                        if (pagesTv != null) {
                                            pagesTv.setText("" + nbPages);

                                        }
                                    }
                                })
                                .load();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Hide Process
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });
    }

    //LoadPDF
    public static void loadEpubFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar, TextView pagesTv) {
        String TAG = "PDF_LOAD_SINGLE_TAG";

        // Using Url get file data & its metadata

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        storageReference.getBytes(MAX_BYTES_EPUB)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: " + pdfTitle + " successfully");
                        // Set Epub View
                        pdfView.fromBytes(bytes)
                                .pages(0)  //Show first page
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        //Hide Process
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onError: " + t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        //Hide Process
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onPageError: " + t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        //Hide Process
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "loadComplete: pdf loaded");

                                        //if pagesTv param is not full then set page numbers
                                        if (pagesTv != null) {
                                            pagesTv.setText("" + nbPages);

                                        }
                                    }
                                })
                                .load();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Hide Process
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });
    }


    //LoadCategory
    public static void loadCategory(String categoryId, TextView categoryTv) {
        //Get Category using categoryId

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Categories");
        databaseReference.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //Get Category
                        String category = "" + snapshot.child("category").getValue();

                        //Set Category text view
                        categoryTv.setText(category);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    // Book View Count in Library
    public static void incrementBookViewCountLibrary(String bookId) {
        // (1) Get book views count
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(FirebaseAuth.getInstance().getUid()).child("Books").child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Get views count
                        String viewsCount = "" + snapshot.child("viewsCount").getValue();
                        // In case of null replace  with 0
                        if (viewsCount.equals("") || viewsCount.equals("null")) {
                            viewsCount = "0";
                        }

                        //Increment views count
                        try {
                            long newViewsCount = Long.parseLong(viewsCount) + 1;


                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("viewsCount", newViewsCount);

                            DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("users");
                            databaseReference1.child(FirebaseAuth.getInstance().getUid()).child("Books").child(bookId)
                                    .updateChildren(hashMap);
                        } catch (NumberFormatException e) {

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    // Book View Count in Store
    public static void incrementBookViewCountStore(String bookId) {
        // (1) Get book views count
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");
        databaseReference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Get views count
                        String viewsCount = "" + snapshot.child("viewsCount").getValue();
                        // In case of null replace  with 0
                        if (viewsCount.equals("") || viewsCount.equals("null")) {
                            viewsCount = "0";
                        }

                        //Increment views count
                        try {
                            long newViewsCount = Long.parseLong(viewsCount) + 1;


                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("viewsCount", newViewsCount);

                            DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Books");
                            databaseReference1.child(bookId)
                                    .updateChildren(hashMap);
                        } catch (NumberFormatException e) {

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    //Download
    public static void downloadBook(Context context, String bookId, String bookTitle, String bookUrl) {
        Log.d(TAG_DOWNLOAD, "DownloadBook: downloading book...");
        String nameWithExtension = bookTitle + ".pdf";
        Log.d(TAG_DOWNLOAD, "downloadBook: NAME: " + nameWithExtension);

        //Progress dialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Đang xử lý...");
        progressDialog.setMessage("Đang tải " + nameWithExtension + "...");// tai file abc.pdf
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //download from firebase storage using url
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.getBytes(MAX_BYTES_EPUB)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG_DOWNLOAD, "onSuccess: Book Downloaded");
                        Log.d(TAG_DOWNLOAD, "onSuccess: Saving book...");
                        saveDownloadBook(context, progressDialog, bytes, nameWithExtension, bookId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG_DOWNLOAD, "onFailure: Failed to download due to" + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "Tải xuống thất bại " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private static void saveDownloadBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String bookId) {
        Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Saving downloaded book");
        try {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadsFolder.mkdirs();

            String filePath = downloadsFolder.getPath() + "/" + nameWithExtension;
            Log.d(TAG, "saveDownloadBook: " + filePath);

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();

            Toast.makeText(context, "Đã lưu vào thư mục tải xuống ", Toast.LENGTH_SHORT).show();
            Log.d(TAG_DOWNLOAD, "SaveDownloadBook: Saved to Download Folder");
            progressDialog.dismiss();

            incrementBookDownloadCount(bookId);

        } catch (Exception e) {
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Failed saving to Download Folder due to " + e.getMessage());
            Toast.makeText(context, "Không thể lưu vào thư mục " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();

        }

    }

    private static void incrementBookDownloadCount(String bookId) {
        Log.d(TAG_DOWNLOAD, "incrementBookDownloadCount: increment Book Download Count");

        //step 1 : Get previous download count
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(FirebaseAuth.getInstance().getUid()).child("Books").child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String downloadCount = "" + snapshot.child("downloadsCount").getValue();
                        Log.d(TAG_DOWNLOAD, "onDataChange: Downloads Count: " + downloadCount);

                        if (downloadCount.equals("") || downloadCount.equals("null")) {
                            downloadCount = "0";

                        }

                        //convert to long and increment 1
                        long newDownloadsCount = Long.parseLong(downloadCount) + 1;
                        Log.d(TAG_DOWNLOAD, "onDataChange: Downloads Count: " + newDownloadsCount);

                        //setup data to update
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("downloadsCount", newDownloadsCount);

                        //step 2) Update new incremented downloads count to db
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                        reference.child(FirebaseAuth.getInstance().getUid()).child("Books").child(bookId).updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG_DOWNLOAD, "onSuccess: Downloads count updated ...");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG_DOWNLOAD, "onFailure: Failed to update Downloads Count due to" + e.getMessage());
                                    }
                                });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    public static void loadPdfPageCount(Context context, String pdfUrl, TextView pagesTv) {
        //load pdf file from firebase storage using url
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        storageReference
                .getBytes(MAX_BYTES_EPUB)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        //file received

                        //load pdf pages using pdfView library
                        PDFView pdfView = new PDFView(context, null);
                        pdfView.fromBytes(bytes)
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        //pdf loaded from byte we got from firebase storage, we can now show number of pages
                                        pagesTv.setText("" + nbPages);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }


    public static void addToFavorite(Context context, String bookId) {
        //we can add only if user is logged in
        //1) check if user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            //not logged in, cant add to fav
            Toast.makeText(context, "Bạn chưa đăng nhập ", Toast.LENGTH_SHORT).show();

        } else {
            long timestamp = System.currentTimeMillis();

            //setup data to add in firebase db of current user for favorite book
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("bookId", "" + bookId);
            hashMap.put("timestamp", "" + timestamp);

            //save to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Không thể thêm vào danh sách yêu thích ", Toast.LENGTH_SHORT).show();

                        }
                    });
        }
    }

    public static void removeFromFavorite(Context context, String bookId) {

        //we can add remove if user is logged in
        //1) check if user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            //not logged in, cant remove to fav
            Toast.makeText(context, "Vui lòng đăng nhập ", Toast.LENGTH_SHORT).show();

        } else {


            //remove to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Không thể xóa khỏi danh sách yêu thích ", Toast.LENGTH_SHORT).show();

                        }
                    });
        }
    }
}
