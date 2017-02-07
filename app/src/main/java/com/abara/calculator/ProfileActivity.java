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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.abara.calculator.util.Grade;
import com.abara.calculator.util.ImageUtils;
import com.abara.calculator.util.PreferenceIds;
import com.abara.calculator.util.Result;
import com.abara.calculator.util.Semester;
import com.abara.calculator.util.User;
import com.abara.calculator.util.Utils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;

/**
 * Created by abara on 8/30/2015.
 * <p>
 * Activity to Profile settings.
 */
public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private AppCompatButton resetButton, deleteButton, downloadButton;
    private SharedPreferences prefs;
    private DatabaseReference reference;
    private User user;

    private String number = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Toolbar toolBar = (Toolbar) findViewById(R.id.profile_app_bar);
        toolBar.setTitle("My Profile");
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //firebase = new Firebase("https://cgpa-calculator.firebaseio.com/");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        setUpAds();

        resetButton = (AppCompatButton) findViewById(R.id.profile_reset_button);
        deleteButton = (AppCompatButton) findViewById(R.id.profile_delete_button);
        downloadButton = (AppCompatButton) findViewById(R.id.profile_download_button);

        resetButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        downloadButton.setOnClickListener(this);

        String userJson = prefs.getString(PreferenceIds.USER_JSON_KEY, "{}");

        user = new Gson().fromJson(userJson, User.class);
        number = user.getUnivno();

        if (prefs.getBoolean(PreferenceIds.IS_OFFLINE_PACKAGE_DOWNLOADED, false)) {
            downloadButton.setText(getResources().getString(R.string.profile_update_download_button));
        } else {
            downloadButton.setText(getResources().getString(R.string.profile_download_button));
        }

    }

    /*
    * Ads API
    * */
    private void setUpAds() {

        AdBuddiz.setPublisherKey(getResources().getString(R.string.ad_public_key));
        AdBuddiz.cacheAds(this);
        AdBuddiz.showAd(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    * Method handling button clicks.
    * */
    @Override
    public void onClick(View v) {

        View container = findViewById(R.id.profile_container);

        if (v == resetButton) {
            if (Utils.isNetAvailable(this))
                resetAccount();
            else
                Snackbar.make(container, "No Internet connection", Snackbar.LENGTH_SHORT).show();
        }

        if (v == deleteButton) {
            if (Utils.isNetAvailable(this))
                deleteAccount();
            else
                Snackbar.make(container, "No Internet connection", Snackbar.LENGTH_SHORT).show();
        }

        if (v == downloadButton) {

            String regulation = user.getRegulation();

            if (Utils.isNetAvailable(this))
                Utils.downloadLocalDb(this, regulation);
            else
                Snackbar.make(container, "No Internet connection", Snackbar.LENGTH_SHORT).show();
        }

    }

    /*
    * Method to delete the current student's account from Firebase database and logout.
    * */
    private void deleteAccount() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Dialog);
        builder.setTitle("Delete?")
                .setMessage("Account will be removed permanently.")
                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        reference.child("results").child(number).removeValue();

                        reference.child("users").child(number).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    Toast.makeText(ProfileActivity.this, "Account deleted successfully!", Toast.LENGTH_SHORT).show();

                                    ImageUtils.deleteImageFromLocal();
                                    ImageUtils.initMediaScanner(ProfileActivity.this);

                                    prefs.edit().putString(PreferenceIds.USER_JSON_KEY, "{}").clear()
                                            .putString(PreferenceIds.USER_RESULTS_JSON_KEY, "{}").clear()
                                            .putBoolean(PreferenceIds.USER_LOGGED_IN_KEY, false).apply();

                                    Bundle bundle = new Bundle();
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Account Deleted");
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "UserProfile");
                                    FirebaseAnalytics.getInstance(ProfileActivity.this).logEvent("delete_account", bundle);

                                    Intent i = new Intent(getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName()));
                                    //i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    finish();
                                    startActivity(i);


                                } else {
                                    Log.e(Utils.TAG, databaseError.getMessage());
                                    Toast.makeText(ProfileActivity.this, "Can't delete account!", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /*
    * Method to reset the current student's account.
    * */
    private void resetAccount() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Dialog);
        builder.setTitle("Reset?")
                .setMessage("This action cannot be reversed.")
                .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Semester sems = new Semester();
                        Grade newGrade = new Grade(sems, sems, sems, sems, sems, sems, sems, sems);

                        Result result = new Result();

                        user.setGrade(newGrade);
                        if (user.getArrears() != null)
                            user.getArrears().clear();

                        String resetedUser = new Gson().toJson(user);
                        prefs.edit().putString(PreferenceIds.USER_JSON_KEY, resetedUser).commit();
                        prefs.edit().putString(PreferenceIds.USER_RESULTS_JSON_KEY, new Gson().toJson(result)).commit();

                        reference.child("results").child(number).removeValue();
                        reference.child("results").child(number).setValue(result);
                        reference.child("users").child(number).child(PreferenceIds.USER_OBJECT_GRADE).setValue(newGrade, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if (databaseError == null) {
                                    Toast.makeText(ProfileActivity.this, "Reset successful!", Toast.LENGTH_SHORT).show();
                                    //Utils.updateLocalFromFirebase(firebase, number, prefs);

                                    Bundle bundle = new Bundle();
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Account Reseted");
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "UserProfile");
                                    FirebaseAnalytics.getInstance(ProfileActivity.this).logEvent("reset_account", bundle);
                                    onBackPressed();
                                } else {
                                    Log.e(Utils.TAG, databaseError.getMessage());
                                    Toast.makeText(ProfileActivity.this, "Can't reset account!", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                        reference.child("users").child(number).child("arrears").setValue(null);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();

    }


}
