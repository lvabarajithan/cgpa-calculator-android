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
package com.abara.calculator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.abara.calculator.util.Grade;
import com.abara.calculator.util.MaterialTextView;
import com.abara.calculator.util.PreferenceIds;
import com.abara.calculator.util.Result;
import com.abara.calculator.util.Semester;
import com.abara.calculator.util.Subject;
import com.abara.calculator.util.User;
import com.abara.calculator.util.Utils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by abara on 9/11/15.
 *
 * Activity to display result.
 */
public class ResultActivity extends AppCompatActivity {

    //private Firebase firebase, arrearFirebase;
    private FirebaseDatabase database;
    private DatabaseReference reference, arrearReference;
    private SharedPreferences prefs;

    private MaterialTextView gpaText, creditText, currCgpaText, pEarnedText, pTotalText;
    private LinearLayout creditsLayout, cgpaLayout, headerLayout, earnedLayout, totalLayout;
    private Button saveBtn;

    private int semPos, myCredit, pEarned, pTotal;
    private float myGPA;
    private double currCgpa;
    private String number = "0", userJson;

    private int[] gradeList;
    private ArrayList<Subject> subjects, arrearList;
    private ArrayList<Integer> gradesForSemList;

    /*
    * Initialize the components.
    * Handle the animations.
    * Validate the user.
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        //firebase = new Firebase("https://cgpa-calculator.firebaseio.com/");
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        semPos = getIntent().getIntExtra("sem", 1);
        myGPA = getIntent().getFloatExtra("gpa", 0.0f);
        myCredit = getIntent().getIntExtra("credit", 0);
        pEarned = getIntent().getIntExtra("pEarned", 0);
        pTotal = getIntent().getIntExtra("pTotal", 0);
        boolean isUser = getIntent().getBooleanExtra("isUser", false);
        boolean isLoggedIn = getIntent().getBooleanExtra("isLoggedIn", true);

        if (isUser) {
            Bundle bundle = getIntent().getBundleExtra("arrear_bundle");
            gradeList = bundle.getIntArray("arrear_grade_list");
            subjects = bundle.getParcelableArrayList("arrear_subject");
            arrearList = bundle.getParcelableArrayList("arrear_list");
            gradesForSemList = bundle.getIntegerArrayList("result_grade_list");
        }

        creditsLayout = (LinearLayout) findViewById(R.id.credits_layout);
        cgpaLayout = (LinearLayout) findViewById(R.id.cgpa_layout);
        headerLayout = (LinearLayout) findViewById(R.id.header_results_container);
        earnedLayout = (LinearLayout) findViewById(R.id.point_earned_layout);
        totalLayout = (LinearLayout) findViewById(R.id.point_total_layout);
        saveBtn = (Button) findViewById(R.id.save_results_btn);

        // Display Save button only when user is logged-in.
        if (isUser) {
            saveBtn.setVisibility(View.VISIBLE);
            cgpaLayout.setVisibility(View.VISIBLE);
        } else {
            saveBtn.setVisibility(View.INVISIBLE);
            cgpaLayout.setVisibility(View.INVISIBLE);
        }

        gpaText = (MaterialTextView) findViewById(R.id.results_gpa_text);
        creditText = (MaterialTextView) findViewById(R.id.result_grades_earned);
        currCgpaText = (MaterialTextView) findViewById(R.id.result_current_cgpa);
        pEarnedText = (MaterialTextView) findViewById(R.id.result_points_earned);
        pTotalText = (MaterialTextView) findViewById(R.id.result_points_total);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_trans_from_top);
        animation.start();
        if (cgpaLayout.getVisibility() == View.VISIBLE)
            cgpaLayout.setAnimation(animation);
        creditsLayout.setAnimation(animation);
        headerLayout.setAnimation(animation);
        earnedLayout.setAnimation(animation);
        totalLayout.setAnimation(animation);

        float formattedGpa = Float.valueOf(new DecimalFormat("#.##").format(myGPA));

        userJson = prefs.getString(PreferenceIds.USER_JSON_KEY, "{}");
        User user = new Gson().fromJson(userJson, User.class);
        double numerator = 0, denominator = 0;

        number = user.getUnivno();
        Grade grade = user.getGrade();
        for (int i = 1; i <= 8; i++) {
            Semester semester = null;
            switch (i) {
                case 1:
                    semester = grade.getSem1();
                    break;
                case 2:
                    semester = grade.getSem2();
                    break;
                case 3:
                    semester = grade.getSem3();
                    break;
                case 4:
                    semester = grade.getSem4();
                    break;
                case 5:
                    semester = grade.getSem5();
                    break;
                case 6:
                    semester = grade.getSem6();
                    break;
                case 7:
                    semester = grade.getSem7();
                    break;
                case 8:
                    semester = grade.getSem8();
                    break;
            }
            if (i == semPos) {
                numerator += myGPA;
                denominator++;
            } else {
                numerator += semester.getGpa();
            }
            if (semester.getGpa() != 0d && i != semPos) {
                denominator++;
            }
        }

        Log.d(Utils.TAG, "Num : " + numerator + "/" + "Deno : " + denominator);
        arrearReference = reference.child("users").child(number).child("arrears");
        currCgpa = Double.valueOf(new DecimalFormat("#.##").format(numerator / denominator));

        gpaText.setText(String.valueOf(formattedGpa));
        creditText.setText(String.valueOf(myCredit));
        currCgpaText.setText(String.valueOf(currCgpa));
        pEarnedText.setText(String.valueOf(pEarned));
        pTotalText.setText(String.valueOf(pTotal));


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSemAndSave();
                updateLocally();
                onBackPressed();
            }
        });

        if (!isLoggedIn) {
            findViewById(R.id.login_to_layout).setVisibility(View.VISIBLE);
            Button loginNowButton = (Button) findViewById(R.id.login_to_btn);
            loginNowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(ResultActivity.this, LoginActivity.class));
                }
            });
        }

    }

    /*
    * Update the changes locally when not network is available.
    * */
    private void updateLocally() {

        Result result = new Gson().fromJson(prefs.getString(PreferenceIds.USER_RESULTS_JSON_KEY, "{}"), Result.class);

        Semester semester = new Semester(myCredit, myGPA, pEarned, pTotal);
        updateArrears();

        User user = new Gson().fromJson(userJson, User.class);
        user.setArrears(arrearList);
        user.getGrade().setCgpa(currCgpa);

        switch (semPos) {
            case 1:
                user.getGrade().setSem1(semester);
                result.setSem1(gradesForSemList);
                break;
            case 2:
                user.getGrade().setSem2(semester);
                result.setSem2(gradesForSemList);
                break;
            case 3:
                user.getGrade().setSem3(semester);
                result.setSem3(gradesForSemList);
                break;
            case 4:
                user.getGrade().setSem4(semester);
                result.setSem4(gradesForSemList);
                break;
            case 5:
                user.getGrade().setSem5(semester);
                result.setSem5(gradesForSemList);
                break;
            case 6:
                user.getGrade().setSem6(semester);
                result.setSem6(gradesForSemList);
                break;
            case 7:
                user.getGrade().setSem7(semester);
                result.setSem7(gradesForSemList);
                break;
            case 8:
                user.getGrade().setSem8(semester);
                result.setSem8(gradesForSemList);
                break;
        }

        String editedUserJson = new Gson().toJson(user);
        prefs.edit().putString(PreferenceIds.USER_JSON_KEY, editedUserJson).commit();
        prefs.edit().putString(PreferenceIds.USER_RESULTS_JSON_KEY, new Gson().toJson(result)).commit();

    }

    /*
    * Update the arrears.
    * */
    private void updateArrears() {

        for (int i = 0; i < subjects.size(); i++) {
            Subject sub = subjects.get(i);
            if (gradeList[i] == 0) {

                if (!arrearList.contains(sub)) {
                    String code = sub.getCode();
                    sub.setCode("*" + code);
                    sub.setId(semPos + 1);
                    arrearList.add(sub);
                    arrearReference.setValue(arrearList);
                }

            } else {
                if (arrearList.contains(sub)) {
                    arrearList.remove(sub);
                    arrearReference.setValue(arrearList);
                }
            }
        }

    }

    /*
    * Update the semester result and save it to both Firebase database and local.
    *
    * Analytics enabled!
    * */
    private void updateSemAndSave() {

        Semester semester = new Semester(myCredit, myGPA, pEarned, pTotal);

        reference.child("results").child(number).child("sem" + semPos).setValue(gradesForSemList, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null)
                    Log.d(Utils.TAG, "Results List saved successfully - Semester " + semPos);
                else {
                    Toast.makeText(ResultActivity.this, "Cannot save!", Toast.LENGTH_SHORT).show();
                    Log.e(Utils.TAG, databaseError.getMessage());
                }
            }
        });

        reference.child("users").child(number).child(PreferenceIds.USER_OBJECT_GRADE)
                .child(PreferenceIds.USER_OBJECT_CGPA).setValue(currCgpa, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null)
                    Log.d(Utils.TAG, "GPA saved successfully - Semester " + semPos);
                else {
                    Toast.makeText(ResultActivity.this, "Cannot save!", Toast.LENGTH_SHORT).show();
                    Log.e(Utils.TAG, databaseError.getMessage());
                }
            }
        });


        reference.child("users").child(number).child(PreferenceIds.USER_OBJECT_GRADE)
                .child("sem" + semPos).setValue(semester, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError == null) {
                    Log.d(Utils.TAG, "Result saved successfully - Semester " + semPos);
                } else {
                    Toast.makeText(ResultActivity.this, "Cannot save!", Toast.LENGTH_SHORT).show();
                    Log.e(Utils.TAG, databaseError.getMessage());
                }

            }
        });


        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "GPA Saved");
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Save GPA");
        FirebaseAnalytics.getInstance(this).logEvent("save_gpa", bundle);

    }
}
