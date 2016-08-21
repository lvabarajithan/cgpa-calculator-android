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
package com.abara.calculator.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.abara.calculator.R;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by abara on 8/29/2015.
 *
 * The Utility class
 */
public class Utils {

    public static final String TAG = "CGPA Calculator";

    public static final int PADDING_FLAG_NEEDED = 1;
    public static final int PADDING_FLAG_NOT_NEEDED = 0;

    // Avatars array
    public static final int[] avatars = {R.drawable.ic_boy_1, R.drawable.ic_boy_2, R.drawable.ic_boy_3, R.drawable.ic_boy_4,
            R.drawable.ic_girl_1, R.drawable.ic_girl_2, R.drawable.ic_girl_3, R.drawable.ic_girl_4};

    // Nicknames for departments to be used at Firebase Database
    public static final String[] deptBENickNames = {
            "ECE", "EEE", "EIE", "ICE", "CIVIL", "EE", "GIE", "AERO", "AUTO",
            "MECH", "PE", "MSE", "ME", "IEM", "MECHA", "MAE", "IE", "MES", "CSE",
            "BIO", "MED", "PCE", "MARINE", "AGRI", "RA", "CCE"
    };
    public static final String[] deptBTechNickNames = {
            "IT", "CE", "BIO", "POLY", "PLASTIC", "TC", "TT", "FT", "PE", "CEE",
            "PCE", "PT", "FOOD"
    };
    public static final String[] course = {"BE", "BTech"};

    // Course, Departments and Semesters to be displayed to the user.
    public static final String[] deptBE = {"B.E Electronics and Communication Engg",
            "B.E Electrical and Electronics Engg",
            "B.E Electronics and Instrumentation Engg",
            "B.E Instrumentation and Control Engg",
            "B.E Civil Engg",
            "B.E Environmental Engg",
            "B.E Geoinformatics Engg",
            "B.E Aeronautical Engg",
            "B.E Automobile Engg",
            "B.E Mechanical Engg",
            "B.E Production Engg",
            "B.E Material Science and Engg",
            "B.E Manufacturing Engg",
            "B.E Industrial Engg and Management",
            "B.E Mechatronics Engg",
            "B.E Mechanical and Automation Engg",
            "B.E Industrial Engg",
            "B.E Mechanical Engg - Sandwich",
            "B.E Computer Science Engg",
            "B.E BioMedical Engg",
            "B.E Medical Electronics",
            "B.E PetroChemical Engg",
            "B.E Marine Engg",
            "B.E Agriculture Engg",
            "B.E Robotics And Automation",
            "B.E Computer and Communication Engg"
    };
    public static final String[] deptBTECH = {"B.Tech-Information Technology",
            "B.Tech Chemical Engg",
            "B.Tech Biotechnology",
            "B.Tech Polymer Technology",
            "B.Tech Plastic Technology",
            "B.Tech Textile Chemistry",
            "B.Tech Textile Technology",
            "B.Tech Fashion Technology",
            "B.Tech Petroleum Engg",
            "B.Tech Chemical and Electrochemical Engg",
            "B.Tech Petrochemical Technology",
            "B.Tech Pharmaceutical Technology",
            "B.Tech Food Technology"
    };

    public static final String[] semesters = {
            "Semester 1",
            "Semester 2",
            "Semester 3",
            "Semester 4",
            "Semester 5",
            "Semester 6",
            "Semester 7",
            "Semester 8"
    };

    // Not needed
    private static String[] getAllDepts() {
        ArrayList<String> all = new ArrayList<>(Arrays.asList(getDeptBE()));
        all.addAll(Arrays.asList(getDeptBTECH()));
        return (String[]) all.toArray();
    }

    public static String[] getCourse() {
        return course;
    }

    public static String[] getDeptBENickNames() {
        return deptBENickNames;
    }

    public static String[] getDeptBTechNickNames() {
        return deptBTechNickNames;
    }

    public static String[] getSemesters() {
        return semesters;
    }

    public static String[] getDeptBE() {
        return deptBE;
    }

    public static String[] getDeptBTECH() {
        return deptBTECH;
    }

    /*
    * Static method to get the position of the selected department.
    * */
    public static int getDeptPositionByName(String name) {

        int temp = -1;

        for (int i = 0; i < Utils.getDeptBE().length; i++) {
            if (name.matches(Utils.getDeptBE()[i])) {
                temp = i;
                break;
            }
        }

        if (temp == -1) {
            for (int i = 0; i < Utils.getDeptBTECH().length; i++) {
                if (name.matches(Utils.getDeptBTECH()[i])) {
                    temp = i;
                    break;
                }
            }
        }

        return temp;
    }

    /*
    * Static method to get the course nickname of the selected department.
    * */
    public static String getSelectedCourseByName(String deptname) {

        if (deptname.contains("B.E")) {
            return course[0];
        } else {
            return course[1];
        }
    }

    // Not needed
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /*
    * Static method to show the offline package dialog.
    * */
    public static void showOfflinePackageDialog(final Context context, final String regulation) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

        boolean isOfflinePackageDownloaded = prefs.getBoolean(PreferenceIds.IS_OFFLINE_PACKAGE_DOWNLOADED, false);

        if (!isOfflinePackageDownloaded) {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Offline package")
                    .setMessage("You can use the app even when you are offline.")
                    .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (isNetAvailable(context)) {
                                Utils.downloadLocalDb(context, regulation);
                            } else {
                                Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
                            }

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

    /*
    * Static method to check whether the device is connected Internet.
    * */
    public static boolean isNetAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
    * Static method to download Database to local storage.
    * */
    public static void downloadLocalDb(final Context context, String regulation) {

        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        dialog.setMessage("Just a sec...");
        dialog.show();

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

        final String fileName = regulation;

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("database");
        reference.child("Reg" + regulation).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String dbString = new Gson().toJson(dataSnapshot.getValue());

                try {

                    FileOutputStream outputStream = context.openFileOutput(fileName + ".json", Context.MODE_PRIVATE);
                    outputStream.write(dbString.getBytes());
                    outputStream.close();

                    preferences.edit().putString(PreferenceIds.OFFLINE_PACKAGE_FILE, fileName).commit();
                    preferences.edit().putBoolean(PreferenceIds.IS_OFFLINE_PACKAGE_DOWNLOADED, true).commit();


                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Download offline package");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "UserProfile");
                    FirebaseAnalytics.getInstance(context).logEvent("offline_package", bundle);

                    if (dialog.isShowing()) {
                        dialog.hide();
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Download failed!", Toast.LENGTH_SHORT).show();
                    if (dialog.isShowing()) {
                        dialog.hide();
                    }
                    preferences.edit().putBoolean(PreferenceIds.IS_OFFLINE_PACKAGE_DOWNLOADED, false).commit();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Download failed!", Toast.LENGTH_SHORT).show();
                    if (dialog.isShowing()) {
                        dialog.hide();
                    }
                    preferences.edit().putBoolean(PreferenceIds.IS_OFFLINE_PACKAGE_DOWNLOADED, false).commit();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
                if (dialog.isShowing()) {
                    dialog.hide();
                }
            }
        });

    }

    /*
    * Static method to update the local database.
    * */
    public static void updateLocalFromFirebase(DatabaseReference reference, String number, final SharedPreferences prefs) {
        reference.child("users").child(number).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                Gson gson = new Gson();
                String userJsonUpdate = gson.toJson(user);

                prefs.edit().putString(PreferenceIds.USER_JSON_KEY, userJsonUpdate).commit();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(Utils.TAG, databaseError.getMessage());
            }
        });

        final User user = new Gson().fromJson(prefs.getString(PreferenceIds.USER_JSON_KEY, "{}"), User.class);

        reference.child("results").orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    if (snapshot.getKey().equals(user.getUnivno())) {
                        Result result = snapshot.getValue(Result.class);
                        if (result != null)
                            prefs.edit().putString(PreferenceIds.USER_RESULTS_JSON_KEY, new Gson().toJson(result)).commit();
                        else
                            prefs.edit().putString(PreferenceIds.USER_RESULTS_JSON_KEY, "{}").commit();
                        break;
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(Utils.TAG, databaseError.getMessage());
            }
        });

    }

    /*
    * Static method to update the online status of the user.
    * */
    public static void setUserOnline(String univno, boolean isOnline) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(univno).child("status").setValue(isOnline ? "Online" : "Offline", new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.d(TAG, "User status updated!");
                } else {
                    Log.d(TAG, "Cannot update user status");
                }
            }
        });

    }
}
