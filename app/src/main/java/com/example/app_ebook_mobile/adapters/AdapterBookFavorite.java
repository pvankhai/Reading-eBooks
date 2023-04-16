package com.example.app_ebook_mobile.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ebook_mobile.activities.MyApplication;
import com.example.app_ebook_mobile.activities.BookDetailStoreActivity;
import com.example.app_ebook_mobile.databinding.RowBookFavoriteUserBinding;
import com.example.app_ebook_mobile.models.ModelBook;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterBookFavorite extends RecyclerView.Adapter<AdapterBookFavorite.HolderPdfFavorite> {


    private Context context;
    private ArrayList<ModelBook> pdfArrayList;
    //view binding
    private RowBookFavoriteUserBinding binding;

    private static final String TAG = "FAV_BOOK_TAG";

    //constructor
    public AdapterBookFavorite(Context context, ArrayList<ModelBook> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfFavorite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind/inflate row_pdf_favorite.xml
        binding = RowBookFavoriteUserBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderPdfFavorite(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfFavorite holder, int position) {
        ////Get, Set data && Handle Click
        ModelBook model = pdfArrayList.get(position);

        loadBookDetail(model, holder);

        //handle click, open pdf details page, already done in previous videos
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, BookDetailStoreActivity.class);
                intent.putExtra("bookId", model.getId());
                context.startActivity(intent);

            }
        });
        //handle click remove from favorite
        holder.removeFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApplication.removeFromFavorite(context, model.getId());
            }
        });

    }

    private void loadBookDetail(ModelBook model, HolderPdfFavorite holder) {
        String bookId = model.getId();
        Log.d(TAG, "LoadBookDetails: Book Details of Book ID: " + bookId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //get book info
                        String bookTitle = "" + snapshot.child("title").getValue();
                        String description = "" + snapshot.child("description").getValue();
                        String categoryId = "" + snapshot.child("categoryId").getValue();
                        String bookUrl = "" + snapshot.child("url").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String uid = "" + snapshot.child("uid").getValue();
                        String viewsCount = "" + snapshot.child("viewsCount").getValue();
                        String downloadsCount = "" + snapshot.child("downloadsCount").getValue();

                        //set to model
                        model.setFavorite(true);
                        model.setTitle(bookTitle);
                        model.setDescription(description);
                        model.setTimestamp(Long.parseLong(timestamp));
                        model.setCategoryId(categoryId);
                        model.setUid(uid);
                        model.setUrl(bookUrl);

                        //format date
                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(categoryId, holder.categoryTv);
                        MyApplication.loadEpubFromUrlSinglePage("" + bookUrl, "" + bookTitle, holder.pdfView, holder.progressBar, null);
                        MyApplication.loadEpubSize("" + bookUrl, "" + bookTitle, holder.sizeTv);


                        //set data to views
                        holder.titleTv.setText(bookTitle);
                        holder.descriptionTv.setText(description);
                        holder.dateTv.setText(date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size(); //return list size
    }


    //ViewHolder
    class HolderPdfFavorite extends RecyclerView.ViewHolder {

        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageButton removeFavBtn;


        public HolderPdfFavorite(@NonNull View iteView) {
            super(iteView);


            //init ui views of row_pdf_favorite.xml
            pdfView = binding.pdfView;
            progressBar = binding.processBar;
            titleTv = binding.titleTv;
            removeFavBtn = binding.removeFavBtn;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;


        }
    }

}
