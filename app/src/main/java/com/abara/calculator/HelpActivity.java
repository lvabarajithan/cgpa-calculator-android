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

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.abara.calculator.util.Feedback;
import com.abara.calculator.util.PreferenceIds;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by abara on 9/20/2015.
 *
 * Activity to provide Feedback to the developer.
 */
public class HelpActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = HelpActivity.class.getName();
    private Toolbar appbar;
    private AppCompatButton submitButton;
    private AppCompatEditText feedBox;
    //private Firebase firebase;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        //firebase = new Firebase("https://cgpa-calculator.firebaseio.com/");
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        appbar = (Toolbar) findViewById(R.id.help_bar);
        setSupportActionBar(appbar);
        getSupportActionBar().setTitle("Help and feedback");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setUpAds();

        feedBox = (AppCompatEditText) findViewById(R.id.feed_box);
        submitButton = (AppCompatButton) findViewById(R.id.submit_btn);
        submitButton.setOnClickListener(this);

    }

    // Ads API
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
    * Called when Submit button is clicked.
    * */
    @Override
    public void onClick(View v) {

        if (isNetAvailable()) {

            String feed = feedBox.getText().toString();
            if (!TextUtils.isEmpty(feed)) {
                String userJson = prefs.getString(PreferenceIds.USER_JSON_KEY, "{}");

                try {
                    JSONObject object = new JSONObject(userJson);

                    String name = object.getString(PreferenceIds.USER_OBJECT_NAME);
                    String email = object.getString(PreferenceIds.USER_OBJECT_EMAIL);
                    String univno = object.getString(PreferenceIds.USER_OBJECT_UNIVNO);

                    Calendar calendar = Calendar.getInstance();
                    String timeAndDate = calendar.getTime().toString();
                    Feedback feedback = new Feedback(feed, name, email, univno, timeAndDate);

                    reference.child("feedback").child(univno).push().setValue(feedback, new DatabaseReference.CompletionListener() {

                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                Snackbar.make(findViewById(R.id.help_header_layout), "Thanks for your feedback!", Snackbar.LENGTH_SHORT).show();
                            } else {
                                Log.d(TAG, databaseError.getMessage());
                                Snackbar.make(findViewById(R.id.help_header_layout), "Couldn't send feedback", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Snackbar.make(findViewById(R.id.help_header_layout), "Feed can't be empty", Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Snackbar.make(findViewById(R.id.help_header_layout), "No internet connection", Snackbar.LENGTH_SHORT).show();
        }

    }

    /*
    * Check for internet connectivity.
    * */
    private boolean isNetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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

}
