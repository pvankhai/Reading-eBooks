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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ebook_mobile.activities.BookDetailUserActivity;
import com.example.app_ebook_mobile.activities.BookEditUserActivity;
import com.example.app_ebook_mobile.activities.MyApplication;
import com.example.app_ebook_mobile.databinding.RowBookUserBinding;

import com.example.app_ebook_mobile.filters.FilterBookUser;
import com.example.app_ebook_mobile.models.ModelBook;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterBookUser extends RecyclerView.Adapter<AdapterBookUser.HolderBookUser> implements Filterable {
    private Context context;
    public ArrayList<ModelBook> pdfArrayList, filterList;
    private FilterBookUser filter;

    private RowBookUserBinding binding; //create filter class now for search


    private static final String TAG = "ADAPTER_PDF_USER_TAG";


    public AdapterBookUser(Context context, ArrayList<ModelBook> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
    }


    @Override
    public HolderBookUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind the view
        binding = RowBookUserBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderBookUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderBookUser holder, int position) {
        /*get data, set data, handle click etc*/

        //get data
        ModelBook model = pdfArrayList.get(position);
        String bookId = model.getId();
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        String categoryId = model.getCategoryId();
        long timestamp = model.getTimestamp();



        //convert time
        String date = MyApplication.formatTimestamp(timestamp);

        //set data
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dataTv.setText(date);


        MyApplication.loadEpubFromUrlSinglePage(
                "" + pdfUrl,
                "" + title,
                holder.epubView,
                holder.progressBar,
                null //we don't need page number here, pass null
        );
        MyApplication.loadCategory(
                "" + categoryId,
                holder.categoryTv
        );
        MyApplication.loadEpubSize(
                "" + pdfUrl,
                "" + title,
                holder.sizeTv
        );

        //handle click, show pdf details activity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, BookDetailUserActivity.class);
                intent.putExtra("bookId", bookId);
                context.startActivity(intent);
            }
        });

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsDialog(model, holder);
            }
        });
    }

    private void moreOptionsDialog(ModelBook modelEpub, HolderBookUser holder) {
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
                            Intent intent = new Intent(context, BookEditUserActivity.class);
                            intent.putExtra("bookId", bookId);
                            context.startActivity(intent);
                        } else if (i == 1) {
                            //delete clicked
                            MyApplication.deleteBookUser(context,
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
        return pdfArrayList.size(); //return List size || number of records
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterBookUser(filterList, this);

        }
        return filter;
    }

    class HolderBookUser extends RecyclerView.ViewHolder {

        TextView titleTv, descriptionTv, categoryTv, sizeTv, dataTv;
        PDFView epubView;
        ProgressBar progressBar;
        ImageButton moreBtn;

        public HolderBookUser(@NonNull View itemView) {
            super(itemView);

            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dataTv = binding.dateTv;
            epubView = binding.epubView;
            progressBar = binding.processBar;
            moreBtn = binding.moreBtn;


        }
    }
}
