package com.example.app_ebook_mobile.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.app_ebook_mobile.BooksUserFragment;
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

public class DashboardUserActivity extends AppCompatActivity {

    //to show in tabs
    public ArrayList<ModelCategory> categoryArrayList;
    public ViewPagerAdapter viewPagerAdapter;

    //view binding
    private ActivityDashboardUserBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        setupViewPagerAdapter(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);


        // Handle click , logout
        binding.LogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                startActivity(new Intent(DashboardUserActivity.this, MainActivity.class));
                finish();
            }
        });

        // Handle click: Open profile
        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardUserActivity.this, ProfileActivity.class));

            }
        });

        // Handle click: Add book your library
        binding.addBookLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardUserActivity.this, BookAddUserActivity.class));
            }
        });

        // Handle click: Open book store
        binding.openStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardUserActivity.this, BookStoreActivity.class));
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
                ModelCategory modelAll = new ModelCategory("01", "Tất cả sách", "", 1);

                //add models to List
                categoryArrayList.add(modelAll);

                //add data to view pager adapter
                viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
                        "" + modelAll.getId(),
                        "" + "All",
                        "" + modelAll.getUid()
                ), modelAll.getCategory());

                //  refresh List
                viewPagerAdapter.notifyDataSetChanged();

                //now load from firebase
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    //add data to List
                    categoryArrayList.add(model);
                    //add data to viewPagerAdapter
                    viewPagerAdapter.addFragment(BooksUserFragment.newInstance(
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
        private ArrayList<BooksUserFragment> fragmentList = new ArrayList<>();
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

        private void addFragment(BooksUserFragment fragment, String title) {
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


    private void checkUser() {
        //get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            // not logged in, go to main screen
            binding.subTitleTv.setText("Tài khoản khách");
        } else {
            //logged in, get user info
            String email = firebaseUser.getEmail();
            //set in textview of toolbar
            binding.subTitleTv.setText(email);
        }
    }
}