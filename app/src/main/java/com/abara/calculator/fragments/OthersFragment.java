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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.abara.calculator.CalcActivity;
import com.abara.calculator.R;
import com.abara.calculator.util.PreferenceIds;
import com.abara.calculator.util.User;
import com.abara.calculator.util.Utils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

/**
 * Created by abara on 9/11/15.
 *
 * Fragment to display a card with all departments and semesters.
 * No need for activity communication in this fragment.
 *
 */
public class OthersFragment extends Fragment implements View.OnClickListener {

    private AppCompatSpinner courseSpinner, deptSpinner, semSpinner;
    private SharedPreferences prefs;

    public OthersFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_others, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        courseSpinner = (AppCompatSpinner) v.findViewById(R.id.course_spinner);
        semSpinner = (AppCompatSpinner) v.findViewById(R.id.sem_spinner);
        deptSpinner = (AppCompatSpinner) v.findViewById(R.id.dept_spinner);

        FloatingActionButton mFab = (FloatingActionButton) v.findViewById(R.id.next_btn);

        courseSpinner.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.simple_list_item, Utils.getCourse()));
        semSpinner.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.simple_list_item, Utils.getSemesters()));

        // Populate the departments according to the course selected (BE, BTECH)
        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        deptSpinner.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.simple_list_item, Utils.getDeptBE()));
                        break;
                    case 1:
                        deptSpinner.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.simple_list_item, Utils.getDeptBTECH()));
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mFab.setOnClickListener(this);

    }

    /*
    * Method to handle the FAB click listener.
    *
    * Check for network connection. If available then pass the selected course, department, semester and regulation
    * to the CalcActivity for displaying the subjects.
    * */
    @Override
    public void onClick(View v) {

        // Convert the json from SharedPreferences into Java Object.
        User user = new Gson().fromJson(prefs.getString(PreferenceIds.USER_JSON_KEY, "{}"), User.class);

        String regulation = user.getRegulation();
        int course = courseSpinner.getSelectedItemPosition();
        int dept = deptSpinner.getSelectedItemPosition();
        int sem = semSpinner.getSelectedItemPosition();

        if (Utils.isNetAvailable(getActivity().getApplicationContext())) {
            Intent nextIntent = new Intent(getActivity(), CalcActivity.class);
            nextIntent.putExtra("course_pos", course);
            nextIntent.putExtra("dept_pos", dept);
            nextIntent.putExtra("sem_pos", sem);
            nextIntent.putExtra("regulation", regulation);
            startActivity(nextIntent);
        } else {

            //No internet connection, request for Offline Package if not downloaded.
            if (getView() != null && !prefs.getBoolean(PreferenceIds.IS_OFFLINE_PACKAGE_DOWNLOADED, false)) {
                Utils.showOfflinePackageDialog(getActivity(), user.getRegulation());
                Snackbar.make(getView(), "No internet connection", Snackbar.LENGTH_SHORT).show();
            } else {
                Intent nextIntent = new Intent(getActivity(), CalcActivity.class);
                nextIntent.putExtra("course_pos", course);
                nextIntent.putExtra("dept_pos", dept);
                nextIntent.putExtra("sem_pos", sem);
                nextIntent.putExtra("regulation", regulation);
                startActivity(nextIntent);
            }
        }

        // Analytics for analyzing the user engagement with the app.
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "CalculateFab Others Tried");
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "CalculateGPAOthers");
        FirebaseAnalytics.getInstance(getActivity()).logEvent("try_calc_gpa_others", bundle);

    }
}
