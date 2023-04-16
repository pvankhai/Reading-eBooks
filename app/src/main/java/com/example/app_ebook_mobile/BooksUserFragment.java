package com.example.app_ebook_mobile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.app_ebook_mobile.adapters.AdapterBookUser;
import com.example.app_ebook_mobile.databinding.FragmentBooksUserBinding;
import com.example.app_ebook_mobile.models.ModelBook;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BooksUserFragment extends Fragment {

    //that we passed while creating instance of this fragments
    private String categoryId;
    private String category;
    private String uid;

    private ArrayList<ModelBook> pdfArrayList;
    private AdapterBookUser adapterPdfUser;

    //view binding
    private FragmentBooksUserBinding binding;

    private static final String TAG = "BOOKS_USER_TAG";

    public BooksUserFragment() {
        // Required empty public constructor
    }


    public static BooksUserFragment newInstance(String categoryId, String category, String uid) {
        BooksUserFragment fragment = new BooksUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", category);
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            uid = getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate/bind the layout for this fragment
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(getContext()), container, false);

        Log.d(TAG, "onCreateView: "+category);
        if (category.equals("All")) {
            //load all book
            loadAllBooks();
        }
        else if(category.equals("Most Viewed")){
            //load all view book
            loadMostViewedDownloadedBooks("viewsCount");
        }
        else if (category.equals("Most Downloaded")) {
            //load all download books
            loadMostViewedDownloadedBooks("downloadsCount");
        }
        else {
            //load selected category books
            loadCategorizedBooks();
        }

        //search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                //called as and when user type any letter
                try {
                    adapterPdfUser.getFilter().filter(s);

                }
                catch (Exception e){
                    Log.d(TAG,"onTextChanged: "+e.getMessage());
                }
            }

            @Override
            public void afterTextChanged( Editable s) {

            }
        });
        return binding.getRoot();
    }

    private void loadAllBooks() {
        //init list
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(FirebaseAuth.getInstance().getUid()).child("Books").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before starting adding data into it
                pdfArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    ModelBook model = ds.getValue(ModelBook.class);
                    //add to List
                    pdfArrayList.add(model);
                    Log.d(TAG, "onDataChange: User ID"+uid);
                }
                //setup adapter
                adapterPdfUser = new AdapterBookUser(getContext(), pdfArrayList);
                //set adapter to recyclerview
                binding.booksRv.setAdapter(adapterPdfUser);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMostViewedDownloadedBooks(String orderBy) {
        //init list
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(uid).child("Books").orderByChild(orderBy).limitToLast(10) //load most viewed or downloaded books
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before starting adding data into it
                        pdfArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            //get data
                            ModelBook model = ds.getValue(ModelBook.class);
                            //add to List
                            pdfArrayList.add(model);
                        }
                        //setup adapter
                        adapterPdfUser = new AdapterBookUser(getContext(), pdfArrayList);
                        //set adapter to recyclerview
                        binding.booksRv.setAdapter(adapterPdfUser);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadCategorizedBooks() {

        //init list
        pdfArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(uid).child("Books").orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before starting adding data into it
                        pdfArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            //get data
                            ModelBook model = ds.getValue(ModelBook.class);
                            //add to List
                            pdfArrayList.add(model);
                        }
                        //setup adapter
                        adapterPdfUser = new AdapterBookUser(getContext(), pdfArrayList);
                        //set adapter to recyclerview
                        binding.booksRv.setAdapter(adapterPdfUser);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}