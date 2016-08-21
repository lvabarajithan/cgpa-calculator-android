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

/**
 * Created by abara on 9/11/15.
 */
public class Grade implements Parcelable {

    public static final Creator<Grade> CREATOR = new Creator<Grade>() {
        @Override
        public Grade createFromParcel(Parcel in) {
            return new Grade(in);
        }

        @Override
        public Grade[] newArray(int size) {
            return new Grade[size];
        }
    };
    private Semester sem1, sem2, sem3, sem4, sem5, sem6, sem7, sem8;
    private double cgpa;

    public Grade() {
    }


    public Grade(Semester sem1, Semester sem2, Semester sem3, Semester sem4, Semester sem5, Semester sem6, Semester sem7, Semester sem8) {
        this.sem1 = sem1;
        this.sem2 = sem2;
        this.sem3 = sem3;
        this.sem4 = sem4;
        this.sem5 = sem5;
        this.sem6 = sem6;
        this.sem7 = sem7;
        this.sem8 = sem8;
    }

    protected Grade(Parcel in) {
        cgpa = in.readDouble();
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        this.cgpa = cgpa;
    }

    public Semester getSem1() {
        return sem1;
    }

    public void setSem1(Semester sem1) {
        this.sem1 = sem1;
    }

    public Semester getSem2() {
        return sem2;
    }

    public void setSem2(Semester sem2) {
        this.sem2 = sem2;
    }

    public Semester getSem3() {
        return sem3;
    }

    public void setSem3(Semester sem3) {
        this.sem3 = sem3;
    }

    public Semester getSem4() {
        return sem4;
    }

    public void setSem4(Semester sem4) {
        this.sem4 = sem4;
    }

    public Semester getSem5() {
        return sem5;
    }

    public void setSem5(Semester sem5) {
        this.sem5 = sem5;
    }

    public Semester getSem6() {
        return sem6;
    }

    public void setSem6(Semester sem6) {
        this.sem6 = sem6;
    }

    public Semester getSem7() {
        return sem7;
    }

    public void setSem7(Semester sem7) {
        this.sem7 = sem7;
    }

    public Semester getSem8() {
        return sem8;
    }

    public void setSem8(Semester sem8) {
        this.sem8 = sem8;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(cgpa);
    }
}
