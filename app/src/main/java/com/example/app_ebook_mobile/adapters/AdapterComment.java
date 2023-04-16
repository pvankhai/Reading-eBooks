package com.example.app_ebook_mobile.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.ColorSpace;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.app_ebook_mobile.R;
import com.example.app_ebook_mobile.activities.MyApplication;
import com.example.app_ebook_mobile.databinding.RowCommentBinding;
import com.example.app_ebook_mobile.models.ModelCategory;
import com.example.app_ebook_mobile.models.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.HolderComment> {

    // Context
    private Context context;

    // Arr list to hold comment
    private ArrayList<ModelComment> commentArrayList;

    //View binding
    private RowCommentBinding binding;

    // Firebase auth
    private FirebaseAuth firebaseAuth;

    // Construct
    public AdapterComment(Context context, ArrayList<ModelComment> commentArrayList) {
        this.context = context;
        this.commentArrayList = commentArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderComment onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate bind the view xml
        binding = RowCommentBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderComment(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderComment holder, int position) {
        //// Get data
        // Get data
        ModelComment modelComment = commentArrayList.get(position);
        String id = modelComment.getId();
        String bookId = modelComment.getBookId();
        String comment = modelComment.getComment();
        String uid = modelComment.getUid();
        String timestamp = modelComment.getTimestamp();

        // Format date, already made function
        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

        // Set data
        holder.dateTv.setText(date);
        holder.commentTv.setText(comment);

        //No User's name
        loadUserDetails(modelComment, holder);

        //Handle click, show option delete
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseAuth.getCurrentUser() != null && uid.equals(firebaseAuth.getUid())) {
                    deleteComment(modelComment, holder);
                }
            }
        });


    }

    private void deleteComment(ModelComment modelComment, HolderComment holder) {
        // Show confirm dialog before deleting comment
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Xóa bình luận")
                .setMessage("Bạn có chắc muốn xoá bình luận này?")
                .setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Delete from dialog clicked
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");
                        databaseReference.child(modelComment.getBookId())
                                .child("Comments")
                                .child(modelComment.getId())
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context, "Xóa bình luận thành công", Toast.LENGTH_SHORT).show();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Xóa bình luận thất bại " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Cancel clicked
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    private void loadUserDetails(ModelComment modelComment, HolderComment holder) {
        String uid = modelComment.getUid();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = "" + snapshot.child("name").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();


                        // Set data
                        holder.nameTv.setText(name);
                        try {
                            Glide.with(context)
                                    .load(profileImage)
                                    .placeholder(R.drawable.ic_person_gray)
                                    .into(holder.profileIv);
                        } catch (Exception e) {
                            holder.profileIv.setImageResource(R.drawable.ic_person_gray);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return commentArrayList.size(); //Return comment size
    }

    // View holder class for row_comment.xml
    class HolderComment extends RecyclerView.ViewHolder {

        // UI
        ShapeableImageView profileIv;
        TextView nameTv, dateTv, commentTv;


        public HolderComment(@NonNull View itemView) {
            super(itemView);

            // Init UI view
            profileIv = binding.profileIv;
            nameTv = binding.nameTv;
            dateTv = binding.dateTv;
            commentTv = binding.commentTv;


        }
    }

}
