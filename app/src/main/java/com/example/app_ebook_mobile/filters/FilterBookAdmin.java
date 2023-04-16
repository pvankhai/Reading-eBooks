package com.example.app_ebook_mobile.filters;

import android.widget.Filter;

import com.example.app_ebook_mobile.adapters.AdapterBookAdmin;
import com.example.app_ebook_mobile.models.ModelBook;

import java.util.ArrayList;

public class FilterBookAdmin extends Filter {

    //arrayList in which we want to search
    ArrayList<ModelBook> filterList;
    //adapter in which filter need to be implemented
    AdapterBookAdmin adapterEpubAdmin;

    //constructor
    public FilterBookAdmin(ArrayList<ModelBook> filterList, AdapterBookAdmin adapterEpubAdmin){
        this.filterList = filterList;
        this.adapterEpubAdmin = adapterEpubAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        //value should not be null and empty
        if (charSequence != null && charSequence.length() > 0) {
            //change to upper case, or lower case to avoid case sensitivity
            charSequence =charSequence.toString().toUpperCase();
            ArrayList<ModelBook> filteredModels = new ArrayList<>();

            for (int i=0; i<filterList.size(); i++){
                //validate
                if (filterList.get(i).getTitle().toUpperCase().contains(charSequence)){
                    //add to filtered List
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;

        }
        else{
            results.count=filterList.size();
            results.values=filterList;

        }

        return results; //dont miss it
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        //apply filter cahnges
        adapterEpubAdmin.epubArrayList= (ArrayList<ModelBook>)filterResults.values;

        //notify cahnges
        adapterEpubAdmin.notifyDataSetChanged();
    }
}
