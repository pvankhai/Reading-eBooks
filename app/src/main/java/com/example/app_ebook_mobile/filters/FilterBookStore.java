package com.example.app_ebook_mobile.filters;

import android.widget.Filter;

import com.example.app_ebook_mobile.adapters.AdapterBookStore;
import com.example.app_ebook_mobile.models.ModelBook;

import java.util.ArrayList;

public class FilterBookStore extends Filter {

    //arraylist in which we want to search
    ArrayList<ModelBook> filterList;

    //adapter in  which filter need to be implementd
    AdapterBookStore adapterBookStore;

    //constructor
    public FilterBookStore (ArrayList<ModelBook> filterList, AdapterBookStore adapterBookStore){
        this.filterList = filterList;
        this.adapterBookStore = adapterBookStore;

    }
    @Override
    protected FilterResults performFiltering(CharSequence constrain) {
        FilterResults results = new FilterResults();
        //value to be searched should not be null/empty
        if (constrain != null || constrain.length() >0) {
            //not null nor empty
            //change to uppercase or lower case to avoid case sensitivity
            constrain = constrain.toString().toUpperCase();
            ArrayList<ModelBook> filteredModels = new ArrayList<>();

            for (int i =0; i<filterList.size(); i++){
                //validate
                if (filterList.get(i).getTitle().toUpperCase().contains(constrain)) {
                    //search matches, add to List
                    filteredModels.add(filterList.get(i));

                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;

        }
        else {
            //empty or null, make original list/result
            results.count = filterList.size();
            results.values = filterList;
        }
        return results; //dont miss it
    }

    @Override
    protected void publishResults(CharSequence constraints, FilterResults filterResults) {
        //apply filter changes
        adapterBookStore.pdfArrayList = (ArrayList<ModelBook>) filterResults.values;

        //notify changes
        adapterBookStore.notifyDataSetChanged();
    }
}
