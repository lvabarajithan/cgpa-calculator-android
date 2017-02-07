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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.TransitionDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.abara.calculator.util.Grade;
import com.abara.calculator.util.MaterialTextView;
import com.abara.calculator.util.PreferenceIds;
import com.abara.calculator.util.Result;
import com.abara.calculator.util.Semester;
import com.abara.calculator.util.User;
import com.abara.calculator.util.Utils;
import com.abara.calculator.util.VolleySingleTon;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by abara on 18/11/15.
 * <p>
 * Activity to handle Login, Register and Forget password.
 */
public class LoginActivity extends AppCompatActivity implements Animation.AnimationListener {

    //cards
    private CardView loginCard, registerCard, forgotPassCard, currCard;

    //Login card
    private AppCompatButton loginButton;
    private AppCompatEditText loginUnivBox, loginPassBox;
    private FloatingActionButton registerButton;

    //Register card
    private AppCompatEditText regNameBox, regNoBox, regPassBox, regEmailBox;
    private AppCompatSpinner regSpinner;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseAnalytics firebaseAnalytics;

    //Forgot pass card
    private AppCompatEditText fpNoBox, fpEmailBox;

    //this class
    private CoordinatorLayout container;
    private TransitionDrawable transitionDrawable;
    private User tempUser;
    private SharedPreferences prefs;
    private Animation slideToDownAnim, slideFromDownAnim;
    private int loginFlag = 0;
    private boolean regFlag = false, forgetFlag = false;
    private ProgressDialog progressDialog;

    /*
    * Handling initialization of components and animations.
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        container = (CoordinatorLayout) findViewById(R.id.main_container);

        loginCard = (CardView) findViewById(R.id.login_card);
        registerCard = (CardView) findViewById(R.id.register_card);
        forgotPassCard = (CardView) findViewById(R.id.forgot_pass_card);

        slideToDownAnim = AnimationUtils.loadAnimation(this, R.anim.slide_to_down);
        slideFromDownAnim = AnimationUtils.loadAnimation(this, R.anim.slide_from_down);

        slideToDownAnim.setAnimationListener(this);
        slideFromDownAnim.setAnimationListener(this);

        initLoginCardComponents();
        initRegisterCardComponents();
        initForgotPassCardComponents();

    }

    /*
    * Initialize the Forgot password Card.
    * */
    private void initForgotPassCardComponents() {

        fpNoBox = (AppCompatEditText) findViewById(R.id.card_forgot_pass_box_no);
        fpEmailBox = (AppCompatEditText) findViewById(R.id.card_forgot_pass_box_email);
        AppCompatButton fpReqBtn = (AppCompatButton) findViewById(R.id.card_forgot_pass_button);
        ImageView fpClose = (ImageView) findViewById(R.id.card_forgot_pass_button_close);

        fpReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isNetAvailable()) {
                    sendPassRequest();

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "SendMailRequest");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "ForgotPassword");
                    firebaseAnalytics.logEvent("try_forgot_password", bundle);

                } else {
                    Snackbar.make(container, "No internet connection", Snackbar.LENGTH_SHORT).show();
                }

            }
        });

        fpClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                enableLoginFormControls(true);
                currCard = forgotPassCard;
                hideCard();

            }
        });

    }

    /*
    * Toggles controls of the Login Card.
    * */
    private void enableLoginFormControls(boolean value) {
        loginUnivBox.setEnabled(value);
        loginPassBox.setEnabled(value);
        loginButton.setEnabled(value);
    }

    /*
    * Method to validate the university number and emailId.
    * */
    private void sendPassRequest() {

        String num = fpNoBox.getText().toString();
        String email = fpEmailBox.getText().toString();

        if (TextUtils.isEmpty(num) && TextUtils.isEmpty(email)) {
            Snackbar.make(container, "Fields are missing", Snackbar.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(num)) {
            Snackbar.make(container, "Enter your university number", Snackbar.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(email)) {
            Snackbar.make(container, "Enter your email id", Snackbar.LENGTH_SHORT).show();
        } else {

            showProgress("Just a sec...");

            User user = new User();
            user.setUnivno(num);
            user.setEmail(email);
            sendMail(user);

        }

    }

    /*
    * Method to send the mail from cgpacalc.xyz server
    * */
    private void sendMail(final User paramUser) {

        reference.orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.d(Utils.TAG, dataSnapshot.toString());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    User aUser = snapshot.getValue(User.class);
                    if (paramUser.getUnivno().matches(aUser.getUnivno()) && paramUser.getEmail().matches(aUser.getEmail())) {
                        forgetFlag = true;
                        tempUser = aUser;
                        break;
                    }
                }

                if (forgetFlag) {
                    String URL = "http://cgpacalc.xyz/app/forget_pass.php?univno=" + tempUser.getUnivno()
                            + "&email=" + tempUser.getEmail() + "&pass=" + tempUser.getPassword();

                    JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, (String) null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Snackbar.make(container, "Check your inbox!", Snackbar.LENGTH_LONG).show();
                            enableLoginFormControls(true);
                            currCard = forgotPassCard;
                            hideCard();

                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Mail sent");
                            bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "ForgotPassword");
                            firebaseAnalytics.logEvent("try_forgot_password", bundle);

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(Utils.TAG, error.toString());
                        }
                    });

                    VolleySingleTon.getInstance().getRequestQueue().add(objectRequest);
                    Snackbar.make(container, "Check your inbox!", Snackbar.LENGTH_LONG).show();
                    enableLoginFormControls(true);
                    currCard = forgotPassCard;
                    hideCard();
                    hideProgress();
                } else {
                    Snackbar.make(container, "Account with associated email doesn't exist", Snackbar.LENGTH_LONG).show();
                    hideProgress();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(Utils.TAG, "Firebase Error : " + databaseError.getMessage());
            }
        });

    }


    /*
    * Initialize the Register Card.
    * */
    private void initRegisterCardComponents() {

        regNameBox = (AppCompatEditText) findViewById(R.id.card_register_box_name);
        regNoBox = (AppCompatEditText) findViewById(R.id.card_register_box_no);
        regEmailBox = (AppCompatEditText) findViewById(R.id.card_register_box_email);
        regPassBox = (AppCompatEditText) findViewById(R.id.card_register_box_pass);

        ImageView regButton = (ImageView) findViewById(R.id.card_register_button);
        ImageView regClose = (ImageView) findViewById(R.id.card_register_button_close);

        regSpinner = (AppCompatSpinner) findViewById(R.id.card_register_spinner);

        ArrayList<String> all = new ArrayList<>(Arrays.asList(Utils.getDeptBE()));
        all.addAll(Arrays.asList(Utils.getDeptBTECH()));

        regSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.simple_list_item, all));

        regSpinner.setSelection(0);

        regButton.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {

                                             if (isNetAvailable()) {

                                                 String n = regNameBox.getText().toString();
                                                 final String no = regNoBox.getText().toString();
                                                 String p = regPassBox.getText().toString();
                                                 String eid = regEmailBox.getText().toString();
                                                 String d = (String) regSpinner.getSelectedItem();
                                                 String regulation = "2013";

                                                 if (TextUtils.isEmpty(n) || TextUtils.isEmpty(no) || TextUtils.isEmpty(p) || TextUtils.isEmpty(eid)) {
                                                     Snackbar.make(container, "Detail(s) are missing for registration", Snackbar.LENGTH_SHORT).show();
                                                 } else if (!eid.contains("@") && !eid.contains(".")) {
                                                     Snackbar.make(container, "Invalid email address", Snackbar.LENGTH_SHORT).show();
                                                 } else if (no.length() < 10) {
                                                     Snackbar.make(container, "Invalid Registration Number, Please use your full number!", Snackbar.LENGTH_LONG).show();
                                                 } else {

                                                     showProgress("Just a sec...");

                                                     tempUser = new User(no, n, p, eid, d, regulation, "Online", -1);
                                                     Semester sems = new Semester();
                                                     tempUser.setGrade(new Grade(sems, sems, sems, sems, sems, sems, sems, sems));

                                                     reference.orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
                                                         @Override
                                                         public void onDataChange(DataSnapshot dataSnapshot) {

                                                             Log.d(Utils.TAG, dataSnapshot.toString());

                                                             for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                                                 User user = snapshot.getValue(User.class);
                                                                 if (tempUser.getUnivno().equals(user.getUnivno())) {
                                                                     regFlag = true;
                                                                     break;
                                                                 }
                                                             }

                                                             if (regFlag) {
                                                                 hideProgress();
                                                                 Snackbar.make(container, "User already exists!", Snackbar.LENGTH_SHORT).show();
                                                             } else {
                                                                 progressDialog.dismiss();
                                                                 progressDialog.setMessage("Setting up your profile...");
                                                                 progressDialog.show();

                                                                 Result result = new Result();
                                                                 DatabaseReference resultReference = database.getReference();
                                                                 resultReference.child("results").child(no).setValue(result);

                                                                 reference.child(no).setValue(tempUser, new DatabaseReference.CompletionListener() {
                                                                     @Override
                                                                     public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                         if (databaseError == null) {
                                                                             Snackbar.make(container, "Registration success!", Snackbar.LENGTH_SHORT).show();
                                                                             regNameBox.setText("");
                                                                             regNoBox.setText("");
                                                                             regPassBox.setText("");
                                                                             regEmailBox.setText("");
                                                                             enableLoginFormControls(true);
                                                                             currCard = registerCard;
                                                                             hideCard();
                                                                             Bundle registerBundle = new Bundle();
                                                                             registerBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "RegisterButton");
                                                                             registerBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Register");
                                                                             firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, registerBundle);
                                                                         } else {
                                                                             Log.d(Utils.TAG, databaseError.getMessage());
                                                                         }
                                                                     }
                                                                 });
                                                                 hideProgress();
                                                             }

                                                         }

                                                         @Override
                                                         public void onCancelled(DatabaseError databaseError) {
                                                             Log.e(Utils.TAG, "Firebase Error : " + databaseError.getMessage());
                                                         }
                                                     });


                                                 }
                                             } else
                                                 Snackbar.make(container, "No internet connection", Snackbar.LENGTH_SHORT).show();

                                         }
                                     }

        );

        regClose.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            enableLoginFormControls(true);
                                            currCard = registerCard;
                                            hideCard();

                                        }
                                    }

        );

    }


    /*
    * Initialize the Login Card.
    * */
    private void initLoginCardComponents() {

        loginButton = (AppCompatButton) findViewById(R.id.fragment_login_btn);
        registerButton = (FloatingActionButton) findViewById(R.id.fragment_login_reg_btn);

        loginUnivBox = (AppCompatEditText) findViewById(R.id.fragment_login_univbox);
        loginPassBox = (AppCompatEditText) findViewById(R.id.fragment_login_passbox);
        MaterialTextView loginForgotPassBtn = (MaterialTextView) findViewById(R.id.fragment_login_forgot_pass);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (registerCard.getVisibility() == View.INVISIBLE) {
                    slideFromDownAnim.setDuration(800);
                    currCard = registerCard;
                    registerCard.startAnimation(slideFromDownAnim);
                    registerCard.setVisibility(View.VISIBLE);
                    enableLoginFormControls(false);
                    Bundle registerBundle = new Bundle();
                    registerBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "RegisterFabButton");
                    registerBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Register");
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, registerBundle);
                }

            }
        });

        loginForgotPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (forgotPassCard.getVisibility() == View.INVISIBLE) {
                    currCard = forgotPassCard;
                    forgotPassCard.startAnimation(slideFromDownAnim);
                    forgotPassCard.setVisibility(View.VISIBLE);
                    enableLoginFormControls(false);
                    Bundle forgotPassBundle = new Bundle();
                    forgotPassBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "ForgotPasswordLabel");
                    forgotPassBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "ForgotPassword");
                    firebaseAnalytics.logEvent("try_forgot_password", forgotPassBundle);
                }

            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {

                                               final String univno = loginUnivBox.getText().toString();
                                               final String password = loginPassBox.getText().toString();

                                               if (isNetAvailable()) {

                                                   if (TextUtils.isEmpty(univno) && TextUtils.isEmpty(password)) {
                                                       Snackbar.make(container, "Fields cannot be empty", Snackbar.LENGTH_SHORT).show();
                                                   } else if (TextUtils.isEmpty(univno)) {
                                                       Snackbar.make(container, "Enter your university no", Snackbar.LENGTH_SHORT).show();
                                                   } else if (TextUtils.isEmpty(password)) {
                                                       Snackbar.make(container, "Enter the password", Snackbar.LENGTH_SHORT).show();
                                                   } else {

                                                       showProgress("Logging in...");

                                                       reference.child(univno).orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
                                                           @Override
                                                           public void onDataChange(DataSnapshot dataSnapshot) {

                                                               //Log.d(Utils.TAG, dataSnapshot.toString());

                                                               //for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                                               User user = dataSnapshot.getValue(User.class);

                                                               if (user == null) {
                                                                   hideProgress();
                                                                   Snackbar.make(container, "You are not yet registered!", Snackbar.LENGTH_SHORT).show();
                                                                   return;
                                                               }

                                                               if (user.getUnivno() != null && user.getUnivno().contentEquals(univno)) {
                                                                   loginFlag = 1;
                                                                   if (user.getPassword().contentEquals(password)) {
                                                                       loginFlag = 2;
                                                                       tempUser = user;
                                                                   }
                                                                   //break;
                                                               } else {
                                                                   hideProgress();
                                                                   Toast.makeText(LoginActivity.this, "Something is wrong!", Toast.LENGTH_SHORT).show();
                                                                   loginFlag = 0;
                                                                   //break;
                                                               }

                                                               //}

                                                               if (loginFlag == 2) {

                                                                   prefs.edit().putBoolean(PreferenceIds.USER_LOGGED_IN_KEY, true).apply();

                                                                   DatabaseReference resultReference = database.getReference();

                                                                   resultReference.child("results").orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
                                                                       @Override
                                                                       public void onDataChange(DataSnapshot dataSnapshot) {
                                                                           for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                                                               if (snapshot.getKey().equals(tempUser.getUnivno())) {
                                                                                   Result result = snapshot.getValue(Result.class);
                                                                                   if (result != null)
                                                                                       prefs.edit().putString(PreferenceIds.USER_RESULTS_JSON_KEY, new Gson().toJson(result)).apply();
                                                                                   else
                                                                                       prefs.edit().putString(PreferenceIds.USER_RESULTS_JSON_KEY, "{}").apply();
                                                                                   break;
                                                                               }

                                                                           }
                                                                       }

                                                                       @Override
                                                                       public void onCancelled(DatabaseError databaseError) {
                                                                           Log.d(Utils.TAG, databaseError.getMessage());
                                                                       }
                                                                   });


                                                                   Gson gson = new Gson();
                                                                   String userJson = gson.toJson(tempUser);

                                                                   prefs.edit().putString(PreferenceIds.USER_JSON_KEY, userJson).apply();

                                                                   Toast.makeText(LoginActivity.this, "Hi, " + tempUser.getName(), Toast.LENGTH_SHORT).show();

                                                                   finish();
                                                                   Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
                                                                   homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                   startActivity(homeIntent);

                                                                   Bundle loginBundle = new Bundle();
                                                                   loginBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Login");
                                                                   loginBundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Login");
                                                                   firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, loginBundle);

                                                               } else if (loginFlag == 1) {
                                                                   Snackbar.make(container, "Incorrect password!", Snackbar.LENGTH_SHORT).show();
                                                               }

                                                               hideProgress();

                                                               //Log.d(Utils.TAG, "Found " + users.size() + " users registered");

                                                           }

                                                           @Override
                                                           public void onCancelled(DatabaseError databaseError) {
                                                               Log.e(Utils.TAG, "Firebase Error : " + databaseError.getMessage());
                                                               hideProgress();
                                                           }
                                                       });

                                                   }

                                               } else {
                                                   Snackbar.make(container, "No internet connection", Snackbar.LENGTH_LONG).show();
                                               }

                                           }
                                       }

        );


    }

    /*
    * Method to show progress bar with message.
    * */
    private void showProgress(String msg) {
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    /*
    * Method to hide progress bar.
    * */
    private void hideProgress() {
        if (progressDialog.isShowing())
            progressDialog.hide();
    }

    /*
    * Method to check for network connectivity.
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
    * Method to handle hide animation for current Card.
    * */
    private void hideCard() {

        slideToDownAnim.setDuration(800);
        //currCard.setAnimation(slideToDownAnim);
        currCard.startAnimation(slideToDownAnim);

    }

    /*
    * Called when animation is started.
    * */
    @Override
    public void onAnimationStart(Animation animation) {

        if (animation == slideToDownAnim) {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                registerButton.setVisibility(View.VISIBLE);
            else
                registerButton.show();

            transitionDrawable.reverseTransition(500);

            loginCard.setCardElevation(8);

        }

        if (animation == slideFromDownAnim) {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                registerButton.setVisibility(View.GONE);
            else
                registerButton.hide();

            transitionDrawable = (TransitionDrawable) container.getBackground();
            transitionDrawable.startTransition(500);

            loginCard.setCardElevation(2);

        }

    }

    /*
    * Called when animation ends.
    * */
    @Override
    public void onAnimationEnd(Animation animation) {

        if (animation == slideToDownAnim) {

            currCard.setVisibility(CardView.INVISIBLE);

        }

        if (animation == slideFromDownAnim) {

            currCard.setVisibility(CardView.VISIBLE);

        }

    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // Nothing
    }
}
