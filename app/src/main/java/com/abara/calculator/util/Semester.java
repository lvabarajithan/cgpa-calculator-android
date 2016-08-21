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
 * Created by abara on 10/11/15.
 */
public class Semester implements Parcelable{

    private int credit = 0;
    private float gpa = 0;
    private int points = 0, totalPoints = 0;

    public Semester() {
    }

    public Semester(int credit, float gpa, int points, int totalPoints) {
        this.credit = credit;
        this.gpa = gpa;
        this.points = points;
        this.totalPoints = totalPoints;
    }

    protected Semester(Parcel in) {
        credit = in.readInt();
        gpa = in.readFloat();
        points = in.readInt();
        totalPoints = in.readInt();
    }

    public static final Creator<Semester> CREATOR = new Creator<Semester>() {
        @Override
        public Semester createFromParcel(Parcel in) {
            return new Semester(in);
        }

        @Override
        public Semester[] newArray(int size) {
            return new Semester[size];
        }
    };

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public float getGpa() {
        return gpa;
    }

    public void setGpa(float gpa) {
        this.gpa = gpa;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(credit);
        dest.writeFloat(gpa);
        dest.writeInt(points);
        dest.writeInt(totalPoints);
    }
}
