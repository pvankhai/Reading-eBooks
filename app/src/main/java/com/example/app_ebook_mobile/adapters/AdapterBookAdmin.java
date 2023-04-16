package com.example.app_ebook_mobile.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ebook_mobile.activities.MyApplication;
import com.example.app_ebook_mobile.activities.BookDetailStoreActivity;
import com.example.app_ebook_mobile.activities.BookEditAdminActivity;
import com.example.app_ebook_mobile.databinding.RowBookAdminBinding;
import com.example.app_ebook_mobile.filters.FilterBookAdmin;
import com.example.app_ebook_mobile.models.ModelBook;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterBookAdmin extends RecyclerView.Adapter<AdapterBookAdmin.HolderEpubAdmin> implements Filterable {
    // Context
    private Context context;

    // ArrList to Hold
    public ArrayList<ModelBook> epubArrayList, filterList;

    //View Binding row_epub_admin.xml
    private RowBookAdminBinding binding;

    private static final String TAG = "EPUB_ADAPTER_TAG";

    private FilterBookAdmin filterEpubAdmin;

    //progress
    private ProgressDialog progressDialog;

    // Construct
    public AdapterBookAdmin(Context context, ArrayList<ModelBook> epubArrayList) {
        this.context = context;
        this.epubArrayList = epubArrayList;
        this.filterList = epubArrayList;

        //init progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Đang xử lý...");
        progressDialog.setCanceledOnTouchOutside(false);
    }


    @NonNull
    @Override
    public HolderEpubAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Bind layout using Binding
        binding = RowBookAdminBinding.inflate(LayoutInflater.from(context), parent, false);


        return new HolderEpubAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderEpubAdmin holder, int position) {
        ////Get, Set data && Handle Click
        //Get data
        ModelBook modelEpub = epubArrayList.get(position);
        String pdfId = modelEpub.getId();
        String categoryId = modelEpub.getCategoryId();
        String title = modelEpub.getTitle();
        String description = modelEpub.getDescription();
        String pdfUrl = modelEpub.getUrl();
        long timestamp = modelEpub.getTimestamp();

        // Convert timestamp to dd/MM/yyyy format
        String formattedDate = MyApplication.formatTimestamp(timestamp);

        //Set data
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);

        //Load further detail like category
        MyApplication.loadCategory(
                "" + categoryId,
                holder.categoryTv
        );
        MyApplication.loadEpubFromUrlSinglePage(
                "" + pdfUrl,
                "" + title,
                holder.epubView,
                holder.processBar,
                null
        );
        MyApplication.loadEpubSize(
                "" + pdfUrl,
                "" + title,
                holder.sizeTv
        );

        //Handle Click, show dialog with option 1) edit, 2) Delete
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsDialog(modelEpub, holder);
            }
        });

        //Handle Book/PDF Click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, BookDetailStoreActivity.class);
                intent.putExtra("bookId", pdfId);
                context.startActivity(intent);
            }
        });
    }

    private void moreOptionsDialog(ModelBook modelEpub, HolderEpubAdmin holder) {
        String bookId = modelEpub.getId();
        String bookUrl = modelEpub.getUrl();
        String bookTitle = modelEpub.getTitle();

        //options to show in dialog
        String[] options = {"Chỉnh sửa", "Xóa"};

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Tùy chọn")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //handle dialog option click 
                        if (i == 0) {
                            //edit clicked, Open PdfEditActivity to edit the book info
                            Intent intent = new Intent(context, BookEditAdminActivity.class);
                            intent.putExtra("bookId", bookId);
                            context.startActivity(intent);
                        } else if (i == 1) {
                            //delete clicked
                            MyApplication.deleteBookStore(context,
                                    "" + bookId,
                                    "" + bookUrl,
                                    "" + bookTitle
                            );
//                            deleteBook(modelEpub, holder);
                        }
                    }
                })
                .show();
    }


    @Override
    public int getItemCount() {
        return epubArrayList.size(); //Return List size

    }

    @Override
    public Filter getFilter() {
        if (filterEpubAdmin == null) {
            filterEpubAdmin = new FilterBookAdmin(filterList, this);
        }
        return filterEpubAdmin;
    }


    // View Holder class row_epub_admin.xml
    class HolderEpubAdmin extends RecyclerView.ViewHolder {
        // UI View
        PDFView epubView;
        ProgressBar processBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageButton moreBtn;


        public HolderEpubAdmin(@NonNull View itemView) {
            super(itemView);

            //init UI
            epubView = binding.epubView;
            processBar = binding.processBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            moreBtn = binding.moreBtn;
        }
    }
}
