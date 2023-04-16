package com.example.app_ebook_mobile.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.app_ebook_mobile.R;
import com.example.app_ebook_mobile.databinding.ActivityForgotPasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    //View binding
    private ActivityForgotPasswordBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //init/setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Đang xử lý...");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //handle click, begin recovery password
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

    }

    private String email = "";

    private void validateData() {
        //get data email
        email = binding.emailEt.getText().toString().trim();

        //validate data e.g should empty and should be valid format
        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ email", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không đúng định dạng vui lòng nhập lại", Toast.LENGTH_SHORT).show();
        } else {
            recoverPassword();
        }
    }

    private void recoverPassword() {
        //show progress
        progressDialog.setMessage("Đang gửi hướng dẫn khôi phục mật khẩu tới " + email);
        progressDialog.show();

        //begin sending recovery
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //sent
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this, "Hướng dẫn đặt lại mật khẩu đã gửi đến " + email, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to sed
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this, "Gửi hướng dẫn thất bại " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}