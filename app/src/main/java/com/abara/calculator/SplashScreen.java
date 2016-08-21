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
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.abara.calculator.intro.IntroActivity;
import com.abara.calculator.util.PreferenceIds;

/*
 * Created by abara on 24/10/15.
 *
 * SplashScreen to display for 2 seconds.
 */
public class SplashScreen extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_splash);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        CountDownTimer timer = new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //Nothing
            }

            /*
            * Method called after 2 seconds.
            * Validate the user login.
            * */
            @Override
            public void onFinish() {

                boolean userLoggedIn = prefs.getBoolean(PreferenceIds.USER_LOGGED_IN_KEY, false);

                if (prefs.getBoolean(PreferenceIds.FIRST_RUN, true)) {
                    prefs.edit().putString(PreferenceIds.USER_JSON_KEY, "{}").clear().commit();
                    prefs.edit().putString(PreferenceIds.USER_RESULTS_JSON_KEY, "{}").clear().commit();
                    prefs.edit().putBoolean(PreferenceIds.USER_LOGGED_IN_KEY, false).commit();
                    finish();
                    startActivity(new Intent(SplashScreen.this, IntroActivity.class));
                } else {
                    if (!userLoggedIn) {
                        startActivity(new Intent(SplashScreen.this, MainActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(SplashScreen.this, HomeActivity.class));
                        finish();
                    }
                }

            }
        };

        //Start the timer.
        timer.start();
    }

}
