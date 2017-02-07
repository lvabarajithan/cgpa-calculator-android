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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abara.calculator.adapter.AvatarListAdapter;
import com.abara.calculator.adapter.SubjectAdapter;
import com.abara.calculator.fragments.GPAFragment;
import com.abara.calculator.fragments.HomeFragment;
import com.abara.calculator.fragments.OthersFragment;
import com.abara.calculator.util.Grade;
import com.abara.calculator.util.OnAvatarClickListener;
import com.abara.calculator.util.PreferenceIds;
import com.abara.calculator.util.Result;
import com.abara.calculator.util.Semester;
import com.abara.calculator.util.Subject;
import com.abara.calculator.util.User;
import com.abara.calculator.util.Utils;
import com.bumptech.glide.Glide;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/*
 * Created by abara on 8/30/2015.
 *
 * Activity to display as the Dashboard.
 */
public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
        , GPAFragment.GradesFragmentListener, HomeFragment.HomeSubjectListener {

    private static final String SELECTED_FRAGMENT_KEY = "selected_fragment";
    private static final int PERMISSION_WRITE_CODE = 101;
    private static final int HOME = 0;
    private static final int OTHERS = 1;
    private static final int GPA = 2;
    private static final String TAG = HomeActivity.class.getName();

    //DetailsFragment
    private double cgpa;
    private String gpa;

    //All fragments
    private HomeFragment homeFragment;
    private GPAFragment gpaFragment;
    private OthersFragment othersFragment;

    //Activity Components
    private Toolbar mAppBar;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private AppCompatTextView headerUserText, headerUserDesc;
    private CircleImageView headerImage;
    private ImageView editProfile;
    private View navHeader;

    //Global components
    private SharedPreferences prefs;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private String number, name, userJson, regulation;
    private int selectedFragment = HOME;
    private User currUser;

    //HomeFragment components
    private String deptname, selectedCourse;
    private int deptPos;
    private ArrayList<Subject> subjects, arrearList;
    private SubjectAdapter subjectAdapter;
    private Result userGradesResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        reference.keepSynced(true);
        userJson = prefs.getString(PreferenceIds.USER_JSON_KEY, "{}");
        currUser = new Gson().fromJson(userJson, User.class);


        // Ads API
        AdBuddiz.setPublisherKey(getResources().getString(R.string.ad_public_key));
        AdBuddiz.cacheAds(this);

        initAll();
        initVariables();

        //init Home components
        subjects = new ArrayList<>();
        arrearList = new ArrayList<>();

        homeFragment = new HomeFragment();
        gpaFragment = new GPAFragment();
        othersFragment = new OthersFragment();

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });

        if (savedInstanceState != null) {
            selectedFragment = savedInstanceState.getInt(SELECTED_FRAGMENT_KEY);
        } else {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String data = bundle.getString("page_number");
                selectedFragment = (data == null) ? HOME : Integer.parseInt(data);
                Log.d(TAG, "onCreate: Bundle is not null with value " + selectedFragment);
            } else {
                selectedFragment = HOME;
                Log.d(TAG, "onCreate: Bundle is null");
            }
        }
        showFragment(selectedFragment);

        if (Utils.isNetAvailable(this)) {
            Utils.showOfflinePackageDialog(this, regulation);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_FRAGMENT_KEY, selectedFragment);
    }

    /*
    * Method to choose the avatar image.
    * */
    private void choosePicture() {

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Choose Picture icon");
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Choose Picture");
        FirebaseAnalytics.getInstance(this).logEvent("choose_pic", bundle);

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_choose_picture, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Dialog).setView(view);
        final AlertDialog dialog = builder.create();

        RecyclerView avatarList = (RecyclerView) view.findViewById(R.id.avatars_list);
        avatarList.setLayoutManager(new GridLayoutManager(this, 4));
        avatarList.setItemAnimator(new DefaultItemAnimator());
        avatarList.setHasFixedSize(true);
        avatarList.setAdapter(new AvatarListAdapter(this, new OnAvatarClickListener() {
            @Override
            public void onAvatarClick(int position) {

                int avatar = Utils.avatars[position];
                Glide.with(HomeActivity.this).load(avatar).fitCenter().into(headerImage);
                Toast.makeText(HomeActivity.this, "Avatar updated!", Toast.LENGTH_SHORT).show();
                currUser.setAvatar(position);

                prefs.edit().putString(PreferenceIds.USER_JSON_KEY, new Gson().toJson(currUser)).apply();
                reference.child("users").child(currUser.getUnivno()).child("avatar").setValue(position, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null)
                            Log.e(TAG, "onComplete: Image not set!, " + databaseError.getMessage());
                    }
                });
                dialog.dismiss();


            }
        }));
        dialog.show();
    }

    /*
    * Method to initialize variables.
    * */
    private void initVariables() {

        headerImage = (CircleImageView) navHeader.findViewById(R.id.header_user_image);
        headerUserText = (AppCompatTextView) navHeader.findViewById(R.id.header_user_name);
        headerUserDesc = (AppCompatTextView) navHeader.findViewById(R.id.header_user_desc);
        editProfile = (ImageView) navHeader.findViewById(R.id.action_edit_profile);

    }

    /*
    * Method to initialize and setup components.
    * */
    private void initAll() {

        mAppBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mAppBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Home");

        mNavigationView = (NavigationView) findViewById(R.id.navigation_drawer);
        mNavigationView.setNavigationItemSelectedListener(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, R.string.open_nav_drawer, R.string.close_nav_drawer) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        navHeader = LayoutInflater.from(this).inflate(R.layout.navigation_header, null);
        mNavigationView.addHeaderView(navHeader);

    }

    /*
    * Self explanatory.
    * */
    private void closeNavigationDrawer() {
        mDrawerLayout.closeDrawer(mNavigationView);
    }

    /*
    * Method to display the fragment.
    * */
    private void showFragment(int position) {

        Fragment fragment = null;
        selectedFragment = position;

        switch (position) {
            case HOME:
                fragment = homeFragment;
                getSupportActionBar().setTitle("Home");
                break;
            case OTHERS:
                fragment = othersFragment;
                getSupportActionBar().setTitle("Others");
                break;
            case GPA:
                fragment = gpaFragment;
                getSupportActionBar().setTitle("Details");
                break;
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment, String.valueOf(position)).commitAllowingStateLoss();
        }
    }

    /*
    * validation and updating of the student profile.
    * */
    @Override
    protected void onResume() {
        super.onResume();

        boolean userLoggedIn = prefs.getBoolean(PreferenceIds.USER_LOGGED_IN_KEY, true);

        if (!userLoggedIn) {

            finish();

        } else {

            name = "no name";
            number = "0";
            regulation = "2013";

            name = currUser.getName();
            deptname = currUser.getDept();
            number = currUser.getUnivno();
            regulation = currUser.getRegulation();

            //Bitmap img = ImageUtils.loadImageFromLocal(this);
            int avatarId = currUser.getAvatar();
            if (avatarId != -1) {
                //headerImage.setImageDrawable(new BitmapDrawable(getResources(), img));
                Glide.with(this).load(Utils.avatars[avatarId]).fitCenter().into(headerImage);
            } else {
                //headerImage.setImageResource(R.drawable.ic_avatar);
                Glide.with(this).load(R.drawable.ic_avatar).fitCenter().into(headerImage);
            }

            headerUserText.setText(name);
            headerUserDesc.setText(number);

            // Set the user status to Online
            Utils.setUserOnline(currUser.getUnivno(), true);

        }

    }

    @Override
    protected void onStop() {
        boolean userLoggedIn = prefs.getBoolean(PreferenceIds.USER_LOGGED_IN_KEY, true);

        if (userLoggedIn) {

            // Set the user status to Offline
            Utils.setUserOnline(currUser.getUnivno(), false);

        }
        super.onStop();
    }

    /*
    * Self explanatory.
    * */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                //do logout
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Dialog);
                builder.setTitle("Logout");
                builder.setMessage("Are you sure ?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //ImageUtils.deleteImageFromLocal();
                        //ImageUtils.initMediaScanner(HomeActivity.this);

                        //Utils.setUserOnline(currUser.getUnivno(), false);
                        prefs.edit().putString(PreferenceIds.USER_JSON_KEY, "{}").clear()
                                .putString(PreferenceIds.USER_RESULTS_JSON_KEY, "{}").clear()
                                .putBoolean(PreferenceIds.USER_LOGGED_IN_KEY, false).apply();
                        finish();
                        startActivity(new Intent(HomeActivity.this, SplashScreen.class));

                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                return true;
        }
        mDrawerToggle.onOptionsItemSelected(item);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
            closeNavigationDrawer();
        } else {
            super.onBackPressed();
        }
    }

    /*
    * Called when NavigationItem is clicked.
    * */
    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        int item = menuItem.getItemId();

        switch (item) {
            case R.id.action_home:
                showFragment(HOME);
                closeNavigationDrawer();
                break;
            case R.id.action_others:
                showFragment(OTHERS);
                closeNavigationDrawer();
                break;
            case R.id.action_details:
                showFragment(GPA);
                closeNavigationDrawer();
                break;
            case R.id.action_about:
                startActivity(new Intent(HomeActivity.this, AboutActivity.class));
                break;
            case R.id.action_help:
                startActivity(new Intent(HomeActivity.this, HelpActivity.class));
                break;
            case R.id.action_change_pass:
                startActivity(new Intent(HomeActivity.this, ChangePassActivity.class));
                break;
            case R.id.action_profile:
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                break;
        }
        //mNavigationView.setCheckedItem(item);

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //GPAFragment communication
    @Override
    public void updateGrades(TextView gpaText, TextView cgpaText, TextView points, TextView arrears) {
        refreshGrades(gpaText, cgpaText, points, arrears);
    }

    /*
    * Sync the student's grades from Firebase Database and update user's json to SharedPreference.
    * */
    private void refreshGrades(TextView gpaText, TextView cgpaText, TextView points, TextView arrear) {

        Utils.updateLocalFromFirebase(FirebaseDatabase.getInstance().getReference(), number, prefs);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Update offline package");
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "UserProfile");
        FirebaseAnalytics.getInstance(this).logEvent("offile_package", bundle);

        String userJson = prefs.getString(PreferenceIds.USER_JSON_KEY, "{}");
        Log.d(Utils.TAG, userJson);

        int earned = 0, total = 0;

        User user = new Gson().fromJson(userJson, User.class);
        ArrayList<Subject> arrearList = user.getArrears();
        Grade grade = user.getGrade();
        StringBuilder stringBuilder = new StringBuilder();

        if (arrearList != null) {
            if (arrearList.size() != 0) {
                for (int i = 0; i < arrearList.size(); i++) {
                    Subject aSub = arrearList.get(i);
                    String name = aSub.getName() + ((i == arrearList.size() - 1) ? "" : "\n");
                    stringBuilder.append(name);
                }
                arrear.setText(stringBuilder.toString());

            } else {
                arrear.setText(getResources().getString(R.string.details_no_arrear_txt));
            }
        } else {
            arrear.setText(getResources().getString(R.string.details_no_arrear_txt));
        }
        this.cgpa = grade.getCgpa();
        StringBuilder sBuilder = new StringBuilder();
        for (int i = 1; i <= 8; i++) {
            Semester sem = null;
            switch (i) {
                case 1:
                    sem = grade.getSem1();
                    break;
                case 2:
                    sem = grade.getSem2();
                    break;
                case 3:
                    sem = grade.getSem3();
                    break;
                case 4:
                    sem = grade.getSem4();
                    break;
                case 5:
                    sem = grade.getSem5();
                    break;
                case 6:
                    sem = grade.getSem6();
                    break;
                case 7:
                    sem = grade.getSem7();
                    break;
                case 8:
                    sem = grade.getSem8();
                    break;
            }
            earned += sem.getPoints();
            total += sem.getTotalPoints();
            String appendingText = "Semester " + i + " : " + new DecimalFormat("#.##").format(sem.getGpa()) + ((i == 8) ? "" : "\n");
            sBuilder.append(appendingText);
        }
        this.gpa = sBuilder.toString();

        points.setText(getResources().getString(R.string.pointstxt_display).replace("%d", earned + "").replace("%f", total + ""));
        gpaText.setText(this.gpa);
        cgpaText.setText(getResources().getString(R.string.gpa_frag_cgpa_text).replace("%f", new DecimalFormat("#.##").format(this.cgpa)));

    }

    //HomeFragment communication
    /*
    * Update the user's person subject list according to selected semester.
    * */
    @Override
    public void updateHomeSubjects(final int position, final RecyclerView homeList,
                                   final LinearLayout homeProgressLayout) {

        homeProgressLayout.setVisibility(View.VISIBLE);
        homeList.setVisibility(View.INVISIBLE);
        subjects.clear();
        //arrearList.clear();

        selectedCourse = Utils.getSelectedCourseByName(deptname);
        deptPos = Utils.getDeptPositionByName(deptname);

        String[] theDept = selectedCourse.matches("BE") ? Utils.getDeptBENickNames() : Utils.getDeptBTechNickNames();

        userGradesResult = new Gson().fromJson(prefs.getString(PreferenceIds.USER_RESULTS_JSON_KEY, "{}"), Result.class);

        int choice = (position + 1);
        ArrayList<Integer> gradesForSemList = null;
        switch (choice) {
            case 1:
                gradesForSemList = userGradesResult.getSem1();
                break;
            case 2:
                gradesForSemList = userGradesResult.getSem2();
                break;
            case 3:
                gradesForSemList = userGradesResult.getSem3();
                break;
            case 4:
                gradesForSemList = userGradesResult.getSem4();
                break;
            case 5:
                gradesForSemList = userGradesResult.getSem5();
                break;
            case 6:
                gradesForSemList = userGradesResult.getSem6();
                break;
            case 7:
                gradesForSemList = userGradesResult.getSem7();
                break;
            case 8:
                gradesForSemList = userGradesResult.getSem8();
                break;
        }

        final ArrayList<Integer> gFSList = gradesForSemList;

        reference.child("database")
                .child("Reg2013").child(selectedCourse.toUpperCase())
                .child(theDept[deptPos].toUpperCase())
                .child("sem" + (position + 1)).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getChildrenCount() > 0) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                Subject subject = snapshot.getValue(Subject.class);
                                subjects.add(subject);

                            }

                            subjectAdapter = new SubjectAdapter(HomeActivity.this, subjects, gFSList, Utils.PADDING_FLAG_NEEDED);
                            homeList.setVisibility(View.VISIBLE);
                            homeList.setAdapter(subjectAdapter);
                            if (homeProgressLayout.getVisibility() == View.VISIBLE)
                                homeProgressLayout.setVisibility(View.INVISIBLE);

                        } else {
                            Toast.makeText(HomeActivity.this, "Nothing found!", Toast.LENGTH_SHORT).show();
                            if (homeProgressLayout.getVisibility() == View.VISIBLE)
                                homeProgressLayout.setVisibility(View.INVISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Utils.TAG, databaseError.getMessage());
                    }
                });


    }

    /*
    * Called when FAB button is clicked.
    * */
    @Override
    public void onCalculateFabClickListener(FloatingActionButton fab, int semPos) {

        int[] gradeList = subjectAdapter.getGradeList();
        subjects = subjectAdapter.getSubjects();
        ArrayList<Integer> gradeResultList = subjectAdapter.getGradesForSemList();

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

        Intent resultIntent = new Intent(HomeActivity.this, ResultActivity.class);
        resultIntent.putExtra("gpa", gpa);
        resultIntent.putExtra("sem", (semPos + 1));
        resultIntent.putExtra("credit", creditsSum);
        resultIntent.putExtra("pEarned", pointsEarned);
        resultIntent.putExtra("pTotal", totalPoints);
        resultIntent.putExtra("isUser", true);

        Bundle arrearBundle = new Bundle();
        arrearBundle.putParcelableArrayList("arrear_list", arrearList);
        arrearBundle.putIntArray("arrear_grade_list", gradeList);
        arrearBundle.putParcelableArrayList("arrear_subject", subjects);
        arrearBundle.putIntegerArrayList("result_grade_list", gradeResultList);

        resultIntent.putExtra("arrear_bundle", arrearBundle);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "CGPA Calculated");
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "CalculateGPA");
        FirebaseAnalytics.getInstance(this).logEvent("calc_gpa", bundle);

        startActivity(resultIntent);
    }

    /*
    * Update the student's personal subjects when no network is available.
    * */
    @Override
    public void updateOfflineHomeSubjects(final int position, final RecyclerView homeList,
                                          final LinearLayout homeProgressLayout) {

        homeProgressLayout.setVisibility(View.VISIBLE);
        homeList.setVisibility(View.INVISIBLE);
        subjects.clear();
        arrearList.clear();

        selectedCourse = Utils.getSelectedCourseByName(deptname);
        deptPos = Utils.getDeptPositionByName(deptname);

        String[] theDept = selectedCourse.matches("BE") ? Utils.getDeptBENickNames() : Utils.getDeptBTechNickNames();

        userGradesResult = new Gson().fromJson(prefs.getString(PreferenceIds.USER_RESULTS_JSON_KEY, "{}"), Result.class);

        int choice = (position + 1);
        ArrayList<Integer> gradesForSemList = null;
        switch (choice) {
            case 1:
                gradesForSemList = userGradesResult.getSem1();
                break;
            case 2:
                gradesForSemList = userGradesResult.getSem2();
                break;
            case 3:
                gradesForSemList = userGradesResult.getSem3();
                break;
            case 4:
                gradesForSemList = userGradesResult.getSem4();
                break;
            case 5:
                gradesForSemList = userGradesResult.getSem5();
                break;
            case 6:
                gradesForSemList = userGradesResult.getSem6();
                break;
            case 7:
                gradesForSemList = userGradesResult.getSem7();
                break;
            case 8:
                gradesForSemList = userGradesResult.getSem8();
                break;
        }

        final ArrayList<Integer> gFSList = gradesForSemList;

        try {

            FileInputStream inputStream = openFileInput(prefs.getString(PreferenceIds.OFFLINE_PACKAGE_FILE, "0") + ".json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();
            String aLine;
            while ((aLine = reader.readLine()) != null) {
                builder.append(aLine);
            }

            String offlineDbJson = builder.toString();

            JSONObject offlineDbObject = new JSONObject(offlineDbJson);
            JSONObject courseObject = offlineDbObject.getJSONObject(selectedCourse.toUpperCase());
            JSONObject deptObject = courseObject.getJSONObject(theDept[deptPos].toUpperCase());
            JSONObject semObject = deptObject.getJSONObject("sem" + (position + 1));

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

                subjectAdapter = new SubjectAdapter(HomeActivity.this, subjects, gFSList, Utils.PADDING_FLAG_NEEDED);
                homeList.setVisibility(View.VISIBLE);
                homeList.setAdapter(subjectAdapter);
                if (homeProgressLayout.getVisibility() == View.VISIBLE)
                    homeProgressLayout.setVisibility(View.INVISIBLE);

            } else {
                Toast.makeText(HomeActivity.this, "Nothing found!", Toast.LENGTH_SHORT).show();
                if (homeProgressLayout.getVisibility() == View.VISIBLE)
                    homeProgressLayout.setVisibility(View.INVISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
