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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.abara.calculator.util.PreferenceIds;
import com.abara.calculator.util.VolleySingleTon;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by abara on 9/30/2015.
 * <p>
 * Activity to change the student's password.
 * Analytics enabled!
 */
public class ChangePassActivity extends AppCompatActivity {

    private AppCompatEditText oneBox, twoBox, currBox;
    private AppCompatButton changeBtn;

    private SharedPreferences prefs;
    //private Firebase firebase;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);

        Toolbar toolbar = (Toolbar) findViewById(R.id.change_pass_app_bar);
        toolbar.setTitle(R.string.change_pass_txt);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //firebase = new Firebase("https://cgpa-calculator.firebaseio.com/users");
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        inVars();

        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetAvailable())
                    changePassword();
                else
                    Toast.makeText(ChangePassActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
            }
        });

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Password change viewed");
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "ChangePassword");
        FirebaseAnalytics.getInstance(ChangePassActivity.this).logEvent("change_pass_view", bundle);

    }

    /*
    * Method to validate and change the password.
    * */
    private void changePassword() {

        String one = oneBox.getText().toString();
        String two = twoBox.getText().toString();
        String currP = currBox.getText().toString();

        String userJson = prefs.getString(PreferenceIds.USER_JSON_KEY, "{}");
        String univno = "0";
        String oldPass = "null";

        try {
            JSONObject jsonObject = new JSONObject(userJson);
            univno = jsonObject.getString(PreferenceIds.USER_OBJECT_UNIVNO);
            oldPass = jsonObject.getString(PreferenceIds.USER_OBJECT_PASSWORD);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(one) && TextUtils.isEmpty(two) && TextUtils.isEmpty(currP)) {
            Toast.makeText(ChangePassActivity.this, "Fields are missing", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(one) && TextUtils.isEmpty(two)) {
            Toast.makeText(ChangePassActivity.this, "New password required", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(one)) {
            Toast.makeText(ChangePassActivity.this, "Enter a new password", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(two)) {
            Toast.makeText(ChangePassActivity.this, "Retype your new password", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(currP)) {
            Toast.makeText(ChangePassActivity.this, "Current password required", Toast.LENGTH_SHORT).show();
        } else {

            if (currP.matches(oldPass)) {

                if (one.matches(two)) {
                    //sendRequest(univno, one);
                    //User user = new User(univno, name, pass, email);
                    changePassWithFirebase(univno, one);
                } else {
                    Toast.makeText(ChangePassActivity.this, "Password mismatch!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ChangePassActivity.this, "Password incorrect!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*
    * Method to update the password at Firebase database.
    * */
    private void changePassWithFirebase(String univno, String one) {

        reference.child(univno).child("password").setValue(one, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(ChangePassActivity.this, "Password changed!", Toast.LENGTH_SHORT).show();

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Password changed");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "ChangePassword");
                    FirebaseAnalytics.getInstance(ChangePassActivity.this).logEvent("change_pass", bundle);

                    onBackPressed();
                } else {
                    Toast.makeText(ChangePassActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    // Not needed
    private void sendRequest(String univno, String pass) {

        String URL = "http://cgpacalc.xyz/app/change_pass.php?univno=" + univno + "&pass=" + pass;

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, (String) null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    switch (response.getString("success")) {
                        case "1":
                            Toast.makeText(ChangePassActivity.this, "Password changed!", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                            break;
                        case "2":
                        case "0":
                            Toast.makeText(ChangePassActivity.this, "Couldn't change password!", Toast.LENGTH_SHORT).show();
                            break;
                        case "-1":
                            Toast.makeText(ChangePassActivity.this, "Cannot make connection to server!", Toast.LENGTH_SHORT).show();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        VolleySingleTon.getInstance().getRequestQueue().add(objectRequest);

    }

    /*
    * Method to check for internet connectivity.
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

    /*
    * Initialize variables
    * */
    private void inVars() {

        oneBox = (AppCompatEditText) findViewById(R.id.curr_pass_box);
        twoBox = (AppCompatEditText) findViewById(R.id.new_pass_box);
        currBox = (AppCompatEditText) findViewById(R.id.old_pass_box);
        changeBtn = (AppCompatButton) findViewById(R.id.change_btn);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
