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
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.abara.calculator.util.PreferenceIds;
import com.abara.calculator.util.User;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.Calendar;

/**
 * Created by abara on 9/20/2015.
 *
 * Activity to display About the application.
 */
public class AboutActivity extends AppCompatActivity {

    private static final String TAG = AboutActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.about_toolbar);
        toolbar.setTitle(R.string.about_txt);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    /*
    * Called when RateNow is clicked.
    * */
    public void onClickRateNow(View view) {

        Intent rating = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName()));
        startActivity(rating);
        Toast.makeText(this, "Thanks for your interest in rating!", Toast.LENGTH_SHORT).show();

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Rate app");
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "AboutActivity");
        FirebaseAnalytics.getInstance(AboutActivity.this).logEvent("click_about", bundle);

    }

    /*
    * Called when More apps is clicked.
    * */
    public void onClickMoreApps(View view) {

        Intent dev = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/dev?id=5738152916341248637"));
        startActivity(dev);
        Toast.makeText(this, "Explore more of my apps!", Toast.LENGTH_SHORT).show();

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "More apps");
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "AboutActivity");
        FirebaseAnalytics.getInstance(AboutActivity.this).logEvent("click_about", bundle);

    }

    /*
    * Called when developer text is clicked.
    * Uses Analytics.
    * */
    public void onAuthorClick(View view) {

        Intent gplus = new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/+AbarajithanLv"));
        startActivity(gplus);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Developer GPlus");
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "AboutActivity");
        FirebaseAnalytics.getInstance(AboutActivity.this).logEvent("click_about", bundle);

    }

    /*
    * Called when contribute is clicked.
    *
    * Display a BottomSheet with custom layout.
    * */
    public void onContributeClick(View view) {

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Contribution Read");
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "AboutActivity");
        FirebaseAnalytics.getInstance(AboutActivity.this).logEvent("contribute_sheet", bundle);

        final BottomSheetDialog sheetDialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.sheet_dialog_contribute, null);
        sheetDialog.setContentView(sheetView);

        AppCompatImageView closeButton = (AppCompatImageView) sheetView.findViewById(R.id.contribute_sheet_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheetDialog.dismiss();
            }
        });

        final TextInputEditText msgBox = (TextInputEditText) sheetView.findViewById(R.id.contribute_quick_send_box);
        AppCompatImageView sendButton = (AppCompatImageView) sheetView.findViewById(R.id.contribute_quick_send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = msgBox.getText().toString();

                if (message.isEmpty()) {
                    Toast.makeText(AboutActivity.this, "Message cannot be empty!", Toast.LENGTH_SHORT).show();
                } else {

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    User user = new Gson().fromJson(prefs.getString(PreferenceIds.USER_JSON_KEY, "{}"), User.class);

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference reference = database.getReference().child("contributors").child(user.getUnivno());

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Contribution QuickMessage");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Contribution");
                    FirebaseAnalytics.getInstance(AboutActivity.this).logEvent("contribute_quick_msg", bundle);

                    String key = reference.push().getKey();

                    reference.child(key).child("message").setValue(message, new DatabaseReference.CompletionListener() {

                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                Toast.makeText(AboutActivity.this, "Thanks!, Will contact you shortly", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e(TAG, "Message : " + databaseError.getMessage());
                                Toast.makeText(AboutActivity.this, "Message failed, Try again!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    final String time = Calendar.getInstance().getTime().toString();

                    reference.child(key).child("timestamp").setValue(time, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                //Toast.makeText(AboutActivity.this, "Thanks!, Will contact you shortly", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "TimeStamp : " + time + " success");
                            } else {
                                Log.e(TAG, "TimeStamp : " + databaseError.getMessage());
                                //Toast.makeText(AboutActivity.this, "Message failed, Try again!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

            }
        });

        LinearLayout gmail = (LinearLayout) sheetView.findViewById(R.id.gmail_layout);
        LinearLayout twitter = (LinearLayout) sheetView.findViewById(R.id.twitter_layout);
        LinearLayout gplus = (LinearLayout) sheetView.findViewById(R.id.gplus_layout);

        gmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent mailIntent = new Intent(Intent.ACTION_SEND);
                mailIntent.setType("text/plain");
                mailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"lvabarajithan@gmail.com"});
                mailIntent.putExtra(Intent.EXTRA_SUBJECT, "REG: CGPA application project development");
                mailIntent.putExtra(Intent.EXTRA_TEXT, "I would like to contribute to this project. Here are my details\n\n");

                startActivity(Intent.createChooser(mailIntent, "Send mail"));

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Contribution Gmail");
                bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Contribution");
                FirebaseAnalytics.getInstance(AboutActivity.this).logEvent("contribute_others", bundle);

            }
        });

        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/imabara"));
                startActivity(twitterIntent);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Contribution Twitter");
                bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Contribution");
                FirebaseAnalytics.getInstance(AboutActivity.this).logEvent("contribute_others", bundle);

            }
        });

        gplus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gplusIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/+AbarajithanLv"));
                startActivity(gplusIntent);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Contribution GPlus");
                bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Contribution");
                FirebaseAnalytics.getInstance(AboutActivity.this).logEvent("contribute_others", bundle);

            }
        });

        sheetDialog.show();

    }

    /*
    * Called when share with friends is clicked.
    *
    * Analytics enabled!
    * */
    public void onShareClick(View view) {

        String URL = "https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey, Checkout this CGPA Calculator app, specially made for Anna University students.\n " + URL +
                "\nHelps you to calculate your GPA and CGPA in seconds. Give it a try!");
        startActivity(Intent.createChooser(shareIntent, "Share with"));

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Share with friends");
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "AboutActivity");
        FirebaseAnalytics.getInstance(AboutActivity.this).logEvent(FirebaseAnalytics.Event.SHARE, bundle);

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

}
