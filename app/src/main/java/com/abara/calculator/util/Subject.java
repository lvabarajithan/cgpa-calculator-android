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
 * Created by abara on 9/17/2015.
 */
public class Subject implements Parcelable {

    private int id;
    private String code;
    private String name;
    private String credit;
    private boolean elective;

    public Subject() {
    }

    protected Subject(Parcel in) {
        id = in.readInt();
        code = in.readString();
        name = in.readString();
        credit = in.readString();
        elective = in.readByte() != 0;
    }

    public static final Creator<Subject> CREATOR = new Creator<Subject>() {
        @Override
        public Subject createFromParcel(Parcel in) {
            return new Subject(in);
        }

        @Override
        public Subject[] newArray(int size) {
            return new Subject[size];
        }
    };

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isElective() {
        return elective;
    }

    public void setElective(boolean elective) {
        this.elective = elective;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Subject) {
            if (code.equals(((Subject) o).getCode())) {
                return true;
            }
        }
        return super.equals(o);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(code);
        dest.writeString(name);
        dest.writeString(credit);
        dest.writeByte((byte) (elective ? 1 : 0));
    }
}
