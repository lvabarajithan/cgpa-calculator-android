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

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.abara.calculator.adapter.SubjectAdapter;
import com.abara.calculator.util.PreferenceIds;
import com.abara.calculator.util.Subject;
import com.abara.calculator.util.Utils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by abara on 8/30/2015.
 * <p>
 * Activity to display the subjects and calculate the GPA, points and credits.
 */
public class CalcActivity extends AppCompatActivity {

    private static final String GRADE_LIST_LEY = "grade_list";
    private static final String TAG = CalcActivity.class.getName();

    private int coursePos, deptPos, semPos;
    private String regulation = "2013";
    private String[] selectedDept;

    private Toolbar appBar;
    //private Firebase firebase;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private RecyclerView subjectList;

    private SubjectAdapter subjectAdapter;
    private ArrayList<Subject> subjects;
    private int[] gradeList;

    private ProgressDialog progressDialog;
    private SharedPreferences prefs;

    /*
    * Initialize the components and setup the online database
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        appBar = (Toolbar) findViewById(R.id.calc_bar);
        setSupportActionBar(appBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        coursePos = getIntent().getIntExtra("course_pos", 0);
        deptPos = getIntent().getIntExtra("dept_pos", 0);
        semPos = getIntent().getIntExtra("sem_pos", 0);
        regulation = getIntent().getStringExtra("regulation");

        selectedDept = (coursePos == 0) ? Utils.getDeptBENickNames() : Utils.getDeptBTechNickNames();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        initTheList();
        setUpAds();

        if (savedInstanceState != null) {
            gradeList = savedInstanceState.getIntArray(GRADE_LIST_LEY);
        } else {
            refreshGradeList();
        }
        if (Utils.isNetAvailable(this))
            setupFirebase();
        else
            setupOfflineParsing();

    }

    /*
    * Ads API
    * */
    private void setUpAds() {

        AdBuddiz.setPublisherKey(getResources().getString(R.string.ad_public_key));
        AdBuddiz.cacheAds(this);
        AdBuddiz.showAd(this);

    }

    /*
    * Initialize the subjects list
    * */
    private void initTheList() {

        subjects = new ArrayList<>();

        subjectList = (RecyclerView) findViewById(R.id.subjects_list);
        subjectList.setHasFixedSize(false);
        subjectList.setItemAnimator(new DefaultItemAnimator());
        subjectList.setLayoutManager(new LinearLayoutManager(this));

    }

    /*
    * Initialize the components required for offline database parsing
    * */
    private void setupOfflineParsing() {

        try {

            FileInputStream inputStream = openFileInput(regulation + ".json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();
            String aLine;
            while ((aLine = reader.readLine()) != null) {
                builder.append(aLine);
            }

            String offlineDbJson = builder.toString();

            JSONObject offlineDbObject = new JSONObject(offlineDbJson);
            JSONObject courseObject = offlineDbObject.getJSONObject(Utils.course[coursePos].toUpperCase());
            JSONObject deptObject = courseObject.getJSONObject(selectedDept[deptPos].toUpperCase());
            JSONObject semObject = deptObject.getJSONObject("sem" + (semPos + 1));

            if (semObject.length() > 0) {

                for (int i = 1; i <= semObject.length(); i++) {

                    JSONObject subObject = semObject.getJSONObject("Sub " + i);

                    Subject aSub = new Subject();
                    aSub.setId(subObject.getInt("id"));
                    aSub.setCode(subObject.getString("code"));
                    aSub.setCredit(subObject.getString("credit"));
                    aSub.setName(subObject.getString("name"));
                    aSub.setElective(subObject.getBoolean("elective"));

                    subjects.add(aSub);

                }

                subjectAdapter = new SubjectAdapter(CalcActivity.this, subjects, null, Utils.PADDING_FLAG_NOT_NEEDED);
                subjectList.setAdapter(subjectAdapter);
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

            } else {
                Toast.makeText(CalcActivity.this, "Nothing found!", Toast.LENGTH_SHORT).show();
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    /*
    * Initialize the Firebase database and populate the subjects list.
    * */
    private void setupFirebase() {

        //firebase = new Firebase("https://cgpa-calculator.firebaseio.com/database/Reg" + regulation + "/" + Utils.course[coursePos].toUpperCase());
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("database/Reg" + regulation + "/" + Utils.course[coursePos].toUpperCase());

        reference.child(selectedDept[deptPos].toUpperCase()).child("sem" + (semPos + 1)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        Subject subject = snapshot.getValue(Subject.class);
                        subjects.add(subject);

                    }

                    refreshGradeList();

                    subjectAdapter = new SubjectAdapter(CalcActivity.this, subjects, null, Utils.PADDING_FLAG_NOT_NEEDED);
                    subjectList.setAdapter(subjectAdapter);

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                } else {
                    Toast.makeText(CalcActivity.this, "Nothing found!", Toast.LENGTH_SHORT).show();
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: Error : " + databaseError.getMessage());
            }
        });

    }

    /*
    * Method to refresh the grades list.
    * */
    private void refreshGradeList() {
        gradeList = new int[subjects.size()];
        for (int i = 0; i < subjects.size(); i++)
            this.gradeList[i] = 0;
    }

    /*
    * Method to persist the selected grade when the screen orientation is changed.
    * */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (subjectAdapter != null) {
            outState.putIntArray(GRADE_LIST_LEY, subjectAdapter.getGradeList());
        }
    }

    /*
    * Inflate the menu with a Tick.
    * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calc_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /*
    * Handle the back and tick menu actions.
    * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_done:
                if (subjectAdapter == null || subjectAdapter.getItemCount() <= 0)
                    Toast.makeText(this, "Cannot initiate calculation", Toast.LENGTH_SHORT).show();
                else
                    calculateGPA();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    * Method to calculate the GPA and pass the result to the ResultActivity.
    *
    * Analytics enabled!
    * */
    private void calculateGPA() {

        int[] gradeList = subjectAdapter.getGradeList();

        int creditsSum = 0;
        float creditGPASum = 0;
        int pointsEarned = 0;
        int totalPoints = subjects.size() * 10;

        for (int i = 0; i < subjects.size(); i++) {
            Subject sub = subjects.get(i);
            if (gradeList[i] != 0)
                creditsSum += Integer.parseInt(sub.getCredit());
            pointsEarned += gradeList[i];
            creditGPASum += (Float.parseFloat(sub.getCredit()) * gradeList[i]);
        }

        float gpa = creditGPASum / creditsSum;

        Intent resultIntent = new Intent(this, ResultActivity.class);
        resultIntent.putExtra("gpa", gpa);
        resultIntent.putExtra("sem", (semPos + 1));
        resultIntent.putExtra("credit", creditsSum);
        resultIntent.putExtra("pEarned", pointsEarned);
        resultIntent.putExtra("pTotal", totalPoints);
        resultIntent.putExtra("isUser", false);
        resultIntent.putExtra("isLoggedIn", prefs.getBoolean(PreferenceIds.USER_LOGGED_IN_KEY, true));
        startActivity(resultIntent);


        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "CalculateFab Others");
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "CalculateGPAOthers");
        FirebaseAnalytics.getInstance(this).logEvent("calc_gpa_others", bundle);

    }
}
