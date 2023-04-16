package com.example.app_ebook_mobile.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.app_ebook_mobile.R;
import com.example.app_ebook_mobile.databinding.ActivityProfileEditBinding;
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

import java.util.HashMap;

public class ProfileEditActivity extends AppCompatActivity {


    //view binding
    private ActivityProfileEditBinding binding;

    // Firebase auth
    private FirebaseAuth firebaseAuth;

    // Progress
    private ProgressDialog progressDialog;

    //TAG
    private static final String TAG = "PROFILE_EDIT_TAG";

    private Uri imageUri = null;

    private String name = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Đang xử lý...");
        progressDialog.setCanceledOnTouchOutside(false);


        // Setup Firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();

        // Handle click goBack
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Handle click pick image
        binding.profileTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageAttachMenu();

            }
        });

        // Handle click, update profile
        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
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
                        binding.nameEt.setText(name);


                        // Set Image, using glide
                        Glide.with(ProfileEditActivity.this)
                                .load(profileImage)
                                .placeholder(R.drawable.ic_person_gray)
                                .into(binding.profileTv);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }


    private void validateData() {
        // Get data
        name = binding.nameEt.getText().toString().trim();

        //Validate data
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Vui lòng nhập tên người dùng", Toast.LENGTH_SHORT).show();

        } else {
            if (imageUri == null) {
                updateProfile("");
            } else {
                uploadImage();
            }
        }
    }


    private void uploadImage() {
        Log.d(TAG, "uploadImage: ");
        progressDialog.setMessage("Đang cập nhật ảnh đại diện...");
        progressDialog.show();

        //image path
        String filePathAndName = "ProfileImages/" + firebaseAuth.getUid();

        //Storage Reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: Profile image uploaded");
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String uploadedImageUrl = "" + uriTask.getResult();

                        Log.d(TAG, "onSuccess: Uploaded image url: " + uploadedImageUrl);
                        updateProfile(uploadedImageUrl);


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed profile image uploaded" + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Thay đổi ảnh đại diện thất bại " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    private void updateProfile(String imageUrl) {
        Log.d(TAG, "updateProfile: Updating user profile");
        progressDialog.setMessage("Đang cập nhật thông tin người dùng...");
        progressDialog.show();

        //Setup data to update in db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "" + name);
        if (imageUri != null) {
            hashMap.put("profileImage", "" + imageUrl);
        }

        //Update data to DB
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Profile updated");
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Đã cập nhật thông tin người dùng ", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to update db due to " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Cập nhât thông tin thất bại " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }


    private void showImageAttachMenu() {
        //Init popup menu
        PopupMenu popupMenu = new PopupMenu(this, binding.profileTv);
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Máy ảnh");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Thư viện ảnh");

        popupMenu.show();

        //Handle menu item clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //Get id of item clicked
                int which = item.getItemId();
                if (which == 0) {
                    // Camera clicked
                    pickImageCamera();

                } else if (which == 1) {
                    // Gallery clicked
                    pickImageGallery();

                }

                return false;
            }
        });

    }


    private void pickImageCamera() {
        // Intent to pick
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Pick");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);

    }

    private void pickImageGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);


    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // Used to handle result of camera intent
                    // Get uri of image
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: Picked from Camera " + imageUri);
                        Intent data = result.getData();
                        binding.profileTv.setImageURI(imageUri);
                    } else {
                        Toast.makeText(ProfileEditActivity.this, "Canceled ", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // Get uri of image
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: Picked from Gallery " + imageUri);
                        Intent data = result.getData();
                        imageUri = data.getData();
                        binding.profileTv.setImageURI(imageUri);
                    } else {
                        Toast.makeText(ProfileEditActivity.this, "Canceled ", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );
}