package com.example.app_ebook_mobile.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.app_ebook_mobile.BooksStoreFragment;
import com.example.app_ebook_mobile.BooksUserFragment;
import com.example.app_ebook_mobile.databinding.ActivityBookDetailUserBinding;
import com.example.app_ebook_mobile.databinding.ActivityBookStoreBinding;
import com.example.app_ebook_mobile.databinding.ActivityDashboardUserBinding;
import com.example.app_ebook_mobile.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BookStoreActivity extends AppCompatActivity {

    //to show in tabs
    public ArrayList<ModelCategory> categoryArrayList;
    public ViewPagerAdapter viewPagerAdapter;

    //view binding
    private ActivityBookStoreBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookStoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();


        setupViewPagerAdapter(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);


        // Handle click , logout
        binding.LogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                startActivity(new Intent(BookStoreActivity.this, MainActivity.class));
                finish();
            }
        });

        // Handle click: Back
        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }

    private void setupViewPagerAdapter(ViewPager viewPager) {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this);

        categoryArrayList = new ArrayList<>();

        //load categories from firebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("categories");//ba carefull of spellings
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //clear before adding to List
                categoryArrayList.clear();
                /*load categories - static e.g all most Viewed, Downloaded*/
                //add data to models
                ModelCategory modelAll = new ModelCategory("01", "Tất cả", "", 1);
                ModelCategory modelMostViewed = new ModelCategory("02", "Lượt xem nhiều nhất", "", 1);
                ModelCategory modelMostDownloaded = new ModelCategory("03", "Lượt tải nhiều nhất", "", 1);
                //add models to List
                categoryArrayList.add(modelAll);
                categoryArrayList.add(modelMostViewed);
                categoryArrayList.add(modelMostDownloaded);
                //add data to view pager adapter
                viewPagerAdapter.addFragment(BooksStoreFragment.newInstance(
                        "" + modelAll.getId(),
                        "" + "All",
                        "" + modelAll.getUid()
                ), modelAll.getCategory());

                viewPagerAdapter.addFragment(BooksStoreFragment.newInstance(
                        "" + modelMostViewed.getId(),
                        "" + "Most Viewed",
                        "" + modelMostViewed.getUid()
                ), modelMostViewed.getCategory());

                viewPagerAdapter.addFragment(BooksStoreFragment.newInstance(
                        "" + modelMostDownloaded.getId(),
                        "" + "Most Downloaded",
                        "" + modelMostDownloaded.getUid()
                ), modelMostDownloaded.getCategory());
                //  refresh List
                viewPagerAdapter.notifyDataSetChanged();

                //now load from firebase
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    //add data to List
                    categoryArrayList.add(model);
                    //add data to viewPagerAdapter
                    viewPagerAdapter.addFragment(BooksStoreFragment.newInstance(
                            "" + model.getId(),
                            "" + model.getCategory(),
                            "" + model.getUid()
                    ), model.getCategory());

                    //refresh List
                    viewPagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

        //set adapter to view pager
        viewPager.setAdapter(viewPagerAdapter);
    }


    public class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<BooksStoreFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;

        public ViewPagerAdapter(FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        private void addFragment(BooksStoreFragment fragment, String title) {
            //add fragment passed as parameter in fragmentList
            fragmentList.add(fragment);
            //add title passed as parameter in  fragmentTitleList
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

}