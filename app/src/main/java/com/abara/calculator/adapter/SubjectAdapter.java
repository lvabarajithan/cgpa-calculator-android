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

package com.abara.calculator.adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abara.calculator.R;
import com.abara.calculator.util.Subject;
import com.abara.calculator.util.Utils;

import java.util.ArrayList;

/**
 * Created by abara on 7/11/15.
 *
 * Adapter for list of subjects to display with grades in the spinner
 *
 */
public class SubjectAdapter extends RecyclerView.Adapter<SubjectHolder> {

    private Context mContext;
    private ArrayList<Subject> mSubjects;

    private String[] grades = {"S", "A", "B", "C", "D", "E", "U"};
    private ArrayList<Integer> gradesForSemList;
    private int gradeValues[] = {10, 9, 8, 7, 6, 5, 0};
    private int paddingFlag;

    private int[] gradeList;

    public SubjectAdapter(Context context, ArrayList<Subject> subjects, ArrayList<Integer> gradesForSemList, int paddingFlag) {
        this.mContext = context;
        this.mSubjects = subjects;
        this.gradesForSemList = gradesForSemList;
        this.gradeList = new int[subjects.size()];
        this.paddingFlag = paddingFlag;
    }

    @Override
    public SubjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.subject_single_item, parent, false);
        return new SubjectHolder(view);
    }

    @Override
    public void onBindViewHolder(SubjectHolder holder, final int position) {

        if (position == 0 && paddingFlag == Utils.PADDING_FLAG_NEEDED) {
            holder.subjectContainer.setPadding(0, 32, 0, 0);
        }

        Subject sub = mSubjects.get(position);
        holder.code.setText(sub.getCode());
        holder.name.setText(sub.getName());
        holder.gradeSpinner.setAdapter(new ArrayAdapter<>(mContext, R.layout.single_grade_item, grades));
        if (gradesForSemList != null && gradesForSemList.get(position) != null)
            holder.gradeSpinner.setSelection(gradesForSemList.get(position));

        holder.gradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                gradeList[position] = gradeValues[pos];
                if (gradesForSemList != null)
                    if (gradesForSemList.size() > position)
                        gradesForSemList.set(position, pos);
                    else
                        gradesForSemList.add(position, pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


    }

    public ArrayList<Subject> getSubjects() {
        return mSubjects;
    }

    public int[] getGradeList() {
        return gradeList;
    }

    public ArrayList<Integer> getGradesForSemList() {
        return gradesForSemList;
    }

    @Override
    public int getItemCount() {
        return mSubjects.size();
    }
}

/**
 * View holder for each subject
 */
class SubjectHolder extends RecyclerView.ViewHolder {

    TextView code, name;
    AppCompatSpinner gradeSpinner;
    LinearLayout subjectContainer;

    public SubjectHolder(View itemView) {
        super(itemView);

        code = (TextView) itemView.findViewById(R.id.subject_code_text_item);
        name = (TextView) itemView.findViewById(R.id.subject_name_text_item);
        gradeSpinner = (AppCompatSpinner) itemView.findViewById(R.id.subject_grade_item);

        subjectContainer = (LinearLayout) itemView.findViewById(R.id.subject_container);

    }
}