/*
 * Copyright (C) 2016 Abarajithan Lv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.abara.calculator.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import com.abara.calculator.HomeActivity;
import com.abara.calculator.R;
import com.abara.calculator.util.MaterialTextView;
import com.abara.calculator.util.PreferenceIds;
import com.abara.calculator.util.Utils;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by abara on 9/11/15.
 * <p>
 * Fragment to display student's department subjects.
 */
public class HomeFragment extends Fragment {

    private AppCompatSpinner semSpinner;
    private RecyclerView homeList;
    private FloatingActionButton fab;
    private LinearLayout homeProgressLayout, noInternetLayout;

    private SharedPreferences prefs;

    private Activity activity;
    private ViewGroup mContainer;

    private int semPos = 0;

    public HomeFragment() {
    }

    /*
     * Initialize activity to it's parent activity (HomeActivity) for communication.
    * */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mContainer = container;
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        homeProgressLayout = (LinearLayout) v.findViewById(R.id.home_progress_bar);
        homeProgressLayout.setVisibility(View.INVISIBLE);
        noInternetLayout = (LinearLayout) v.findViewById(R.id.home_offline_layout);
        noInternetLayout.setVisibility(View.INVISIBLE);
        MaterialTextView tryAgainText = (MaterialTextView) v.findViewById(R.id.home_try_again);

        semSpinner = (AppCompatSpinner) v.findViewById(R.id.sem_spinner_home);
        homeList = (RecyclerView) v.findViewById(R.id.home_subject_list);
        fab = (FloatingActionButton) v.findViewById(R.id.home_done_button);
        homeList.setItemAnimator(new DefaultItemAnimator());
        homeList.setLayoutManager(new LinearLayoutManager(getContext()));
        homeList.setHasFixedSize(false);
        semSpinner.setAdapter(new ArrayAdapter<>(getContext(), R.layout.simple_list_item, Utils.getSemesters()));
        semSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                loadSubjects(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        tryAgainText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int position = semSpinner.getSelectedItemPosition();
                loadSubjects(position);

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "CalculateFab Tried");
                bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "CalculateGPA");
                FirebaseAnalytics.getInstance(getActivity()).logEvent("try_calc_gpa", bundle);

                if (homeList.getAdapter() == null || homeList.getAdapter().getItemCount() <= 0) {
                    Snackbar.make(mContainer, "Cannot initiate calculation", Snackbar.LENGTH_SHORT).show();
                } else {
                    ((HomeActivity) activity).onCalculateFabClickListener(fab, semPos);
                }

            }
        });
    }

    private void loadSubjects(int position) {

        semPos = position;

        if (isNetAvailable()) {
            noInternetLayout.setVisibility(View.INVISIBLE);
            ((HomeActivity) activity).updateHomeSubjects(position, homeList, homeProgressLayout);
        } else {

            if (prefs.getBoolean(PreferenceIds.IS_OFFLINE_PACKAGE_DOWNLOADED, false)) {
                noInternetLayout.setVisibility(View.INVISIBLE);
                ((HomeActivity) activity).updateOfflineHomeSubjects(position, homeList, homeProgressLayout);
            } else {
                noInternetLayout.setVisibility(View.VISIBLE);
                Snackbar.make(mContainer, "No internet connection", Snackbar.LENGTH_SHORT).show();
            }

        }

    }

    private boolean isNetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();

        boolean wifiNet = false;
        boolean mobileNet = false;

        for (NetworkInfo net : networkInfos) {
            if (net.getTypeName().equalsIgnoreCase("WIFI"))
                if (net.isConnected()) {
                    wifiNet = true;
                }
            if (net.getTypeName().equalsIgnoreCase("MOBILE"))
                if (net.isConnected()) {
                    mobileNet = true;
                }
        }
        return wifiNet || mobileNet;
    }

    /*
    * The interface used to communication between the HomeActivity and this fragment
    * */
    public interface HomeSubjectListener {
        public void updateHomeSubjects(int position, RecyclerView homeList, LinearLayout homeProgressLayout);

        public void onCalculateFabClickListener(FloatingActionButton fab, int semPos);

        public void updateOfflineHomeSubjects(int position, RecyclerView homeList, LinearLayout homeProgressLayout);
    }

}
