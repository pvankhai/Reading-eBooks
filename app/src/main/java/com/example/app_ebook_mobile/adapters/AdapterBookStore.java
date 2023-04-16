package com.example.app_ebook_mobile.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ebook_mobile.activities.MyApplication;
import com.example.app_ebook_mobile.activities.BookDetailStoreActivity;

import com.example.app_ebook_mobile.databinding.RowBookStoreBinding;
import com.example.app_ebook_mobile.databinding.RowBookUserBinding;
import com.example.app_ebook_mobile.filters.FilterBookStore;
import com.example.app_ebook_mobile.models.ModelBook;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterBookStore extends RecyclerView.Adapter<AdapterBookStore.HolderBookStore> implements Filterable {

        private Context context;
        public ArrayList<ModelBook> pdfArrayList, filterList;
        private FilterBookStore filter;

        private RowBookStoreBinding binding; //create filter class now for search


        private static final String TAG = "ADAPTER_BOOK_STORE_TAG";


        public AdapterBookStore(Context context, ArrayList<ModelBook> pdfArrayList) {
            this.context = context;
            this.pdfArrayList = pdfArrayList;
            this.filterList = pdfArrayList;
        }


        @Override
        public HolderBookStore onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //bind the view
            binding = RowBookStoreBinding.inflate(LayoutInflater.from(context), parent, false);

            return new HolderBookStore(binding.getRoot());
        }

        @Override
        public void onBindViewHolder(@NonNull HolderBookStore holder, int position) {
            /*get data, set data, handle click etc*/

            //get data
            ModelBook model = pdfArrayList.get(position);
            String bookId = model.getId();
            String title = model.getTitle();
            String description = model.getDescription();
            String pdfUrl = model.getUrl();
            Log.d(TAG, "onBindViewHolder: Get URL: "+ model.getUrl());

            String categoryId = model.getCategoryId();
            long timestamp = model.getTimestamp();

            //convert time
            String date = MyApplication.formatTimestamp(timestamp);

            //set data
            holder.titleTv.setText(title);
            holder.descriptionTv.setText(description);
            holder.dataTv.setText(date);


            MyApplication.loadEpubFromUrlSinglePage1(
                    "" + pdfUrl,
                    "" + title,
                    holder.epubView,
                    holder.progressBar,
                    null //we don't need page number here, pass null
            );

            MyApplication.loadCategoryStore(
                    "" + categoryId,
                    holder.categoryTv
            );
            MyApplication.loadEpubSizeStore(
                    "" + pdfUrl,
                    "" + title,
                    holder.sizeTv
            );

            //handle click, show pdf details activity
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, BookDetailStoreActivity.class);
                    intent.putExtra("bookId", bookId);
                    context.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return pdfArrayList.size(); //return List size || number of records
        }

        @Override
        public Filter getFilter() {
            if (filter == null) {
                filter = new FilterBookStore(filterList, this);

            }
            return filter;
        }

        class HolderBookStore extends RecyclerView.ViewHolder {

            TextView titleTv, descriptionTv, categoryTv, sizeTv, dataTv;
            PDFView epubView;
            ProgressBar progressBar;

            public HolderBookStore(@NonNull View itemView) {
                super(itemView);

                titleTv = binding.titleTv;
                descriptionTv = binding.descriptionTv;
                categoryTv = binding.categoryTv;
                sizeTv = binding.sizeTv;
                dataTv = binding.dateTv;
                epubView = binding.epubView;
                progressBar = binding.processBar;


            }
        }
    }



