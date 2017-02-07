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

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by abara on 23/10/15.
 */
public class User implements Parcelable {


    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    private String univno;
    private String name;
    private String password;
    private String email;
    private String dept;
    private Grade grade;
    private String regulation;
    private String status;
    private ArrayList<Subject> arrears;
    private int avatar;

    public User() {

    }

    public User(Parcel parcel) {
        univno = parcel.readString();
        name = parcel.readString();
        password = parcel.readString();
        email = parcel.readString();
        dept = parcel.readString();
        status = parcel.readString();
        avatar = parcel.readInt();
    }

    public User(String univno, String name, String password, String email, String dept, String regulation, String status, int avatar) {
        this.univno = univno;
        this.name = name;
        this.password = password;
        this.email = email;
        this.dept = dept;
        this.regulation = regulation;
        this.status = status;
        this.avatar = avatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUnivno() {
        return univno;
    }

    public void setUnivno(String univno) {
        this.univno = univno;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof User) {

            return this.univno.matches(((User) o).getUnivno());

        }
        return false;
    }

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(univno);
        dest.writeString(name);
        dest.writeString(password);
        dest.writeString(email);
        dest.writeString(dept);
        dest.writeString(status);
        dest.writeInt(avatar);
    }

    public ArrayList<Subject> getArrears() {
        return arrears;
    }

    public void setArrears(ArrayList<Subject> arrears) {
        this.arrears = arrears;
    }

    public String getRegulation() {
        return regulation;
    }

    public void setRegulation(String regulation) {
        this.regulation = regulation;
    }

    public void setOnline(String online) {
        status = online;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }
}
