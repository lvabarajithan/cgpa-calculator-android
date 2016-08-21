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
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abara.calculator.HomeActivity;
import com.abara.calculator.R;
import com.abara.calculator.util.MaterialTextViewRegular;

/*
 * Created by abara on 9/19/2015.
 *
 * Fragment to display the student's GPA, CGPA, points and arrears.
 *
 */
public class GPAFragment extends Fragment {

    private MaterialTextViewRegular gpa, cgpa, points, arrears;
    private Activity activity;
    private SwipeRefreshLayout swipeRefreshLayout;

    public GPAFragment() {
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
        return inflater.inflate(R.layout.fragment_gpa, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View v = getView();

        gpa = (MaterialTextViewRegular) v.findViewById(R.id.gpa_text);
        cgpa = (MaterialTextViewRegular) v.findViewById(R.id.cgpa_text);
        points = (MaterialTextViewRegular) v.findViewById(R.id.points_text_gpa);
        arrears = (MaterialTextViewRegular) v.findViewById(R.id.arrears_text_gpa);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.gpa_swipe_to_refresh);

        //communication between the HomeActivity and this fragment
        ((HomeActivity) activity).updateGrades(gpa, cgpa, points, arrears);

        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#FF5252"));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                //After refreshing, we need to update the grades and hide the SwipeToRefreshLayout
                ((HomeActivity) activity).updateGrades(gpa, cgpa, points, arrears);

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

            }
        });

    }


    /*
    * The interface used to communication between the HomeActivity and this fragment
    * */
    public interface GradesFragmentListener {
        public void updateGrades(TextView gpa, TextView gpaText, TextView cgpaText, TextView arrears);
    }

}
