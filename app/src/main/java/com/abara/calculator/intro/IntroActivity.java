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
package com.abara.calculator.intro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.abara.calculator.MainActivity;
import com.abara.calculator.R;
import com.abara.calculator.util.PreferenceIds;
import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

/**
 * Created by abara on 12/24/15.
 *
 * Activity to introduce the app to the new users.
 *
 */
public class IntroActivity extends AppIntro2 {

    // Background colors
    private static final String GREEN = "#4DA207";
    private static final String YELLOW = "#FD9C19";
    private static final String PURPLE = "#8955D1";
    private static final String BLUE = "#00AFDA";
    private static final String ACCENT = "#FF5252";

    @Override
    public void init(Bundle savedInstanceState) {

        addSlide(AppIntroFragment.newInstance("Hi!",
                "Get started by swiping left.",
                R.drawable.ic_logo_splash, Color.parseColor(PURPLE)));
        addSlide(AppIntroFragment.newInstance("Home",
                "Calculate and manage your grades for each semester.",
                R.drawable.ic_page_home, Color.parseColor(YELLOW)));
        addSlide(AppIntroFragment.newInstance("Navigation drawer",
                "You can navigate anywhere in the app from this drawer.",
                R.drawable.ic_page_nav, Color.parseColor(GREEN)));
        addSlide(AppIntroFragment.newInstance("My Details",
                "All of your GPA and CGPA details in one place.",
                R.drawable.ic_page_details, Color.parseColor(BLUE)));
        addSlide(AppIntroFragment.newInstance("Others",
                "Calculate GPA for your friends with this card.",
                R.drawable.ic_page_others, Color.parseColor(ACCENT)));
        addSlide(AppIntroFragment.newInstance("Profile",
                "All of your details are synced to your account. Easy to manage anywhere, anytime.",
                R.drawable.ic_page_student, Color.parseColor(YELLOW)));
        addSlide(AppIntroFragment.newInstance("All done!",
                "You are all set now.",
                R.drawable.ic_all_done, Color.parseColor(GREEN)));

    }

    /*
    * Method to handle when done is pressed.
    * */
    @Override
    public void onDonePressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.edit().putBoolean(PreferenceIds.FIRST_RUN, false).commit();
    }

    @Override
    public void onNextPressed() {
        //Nothing
    }

    @Override
    public void onSlideChanged() {
        //Nothing
    }
}
