package com.example.app_ebook_mobile.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ebook_mobile.activities.BookListAdminActivity;
import com.example.app_ebook_mobile.databinding.RowCategoryBinding;
import com.example.app_ebook_mobile.filters.FilterCategory;
import com.example.app_ebook_mobile.models.ModelCategory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HoderCategory> implements Filterable {

    private Context context;
    public ArrayList<ModelCategory> categoryArrayList, filterList;

    //view binding
    private RowCategoryBinding binding;

    //instance of our filter class
    private FilterCategory filter;

    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.filterList = categoryArrayList;
    }

    @NonNull
    @Override
    public HoderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind row_category.xml\
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HoderCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterCategory.HoderCategory holder, int position) {
        //get data
        ModelCategory model = categoryArrayList.get(position);
        String id = model.getId();
        String category = model.getCategory();
        String uid = model.getUid();
        long timestamp = model.getTimestamp();

        //set data
        holder.categoryTv.setText(category);

        //handle click, delete category
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //confirm delete dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Xóa thể loại")
                        .setMessage("Bạn có chắc chắn muốn xóa thể loại này?")
                        .setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //begin delete
                                Toast.makeText(context, "Đang xóa thể loại...", Toast.LENGTH_SHORT).show();
                                deleteCategory(model, holder);
                            }
                        })
                        .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        });

        //handle item click, go to EpubListAdminActivity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, BookListAdminActivity.class);
                intent.putExtra("categoryId", id);
                intent.putExtra("categoryTitle", category);
                context.startActivity(intent);
            }
        });

    }

    private void deleteCategory(ModelCategory model, HoderCategory holder) {
        //get id of category to delete
        String id = model.getId();
        //Firebase DB > categories > categoryId
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //deleted successfully
                        Toast.makeText(context, "Đã xóa thể loại thành công", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to delete
                        Toast.makeText(context, "Xóa thể loại thất bại" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterCategory(filterList, this);
        }
        return filter;
    }

    /*View holder class to hold Ui views for row_category*/
    class HoderCategory extends RecyclerView.ViewHolder {
        //ui views of row_category.xml
        TextView categoryTv;
        ImageButton deleteBtn;

        public HoderCategory(@NonNull View itemView) {
            super(itemView);

            //init ui views
            categoryTv = binding.categoryTv;
            deleteBtn = binding.deleteBtn;
        }
    }

}
