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

import java.util.ArrayList;

/**
 * Created by abara on 12/24/15.
 */
public class Result {

    private ArrayList<Integer> sem1;
    private ArrayList<Integer> sem2;
    private ArrayList<Integer> sem3;
    private ArrayList<Integer> sem4;
    private ArrayList<Integer> sem5;
    private ArrayList<Integer> sem6;
    private ArrayList<Integer> sem7;
    private ArrayList<Integer> sem8;

    public Result() {
        sem1 = new ArrayList<>();
        sem2 = new ArrayList<>();
        sem3 = new ArrayList<>();
        sem4 = new ArrayList<>();
        sem5 = new ArrayList<>();
        sem6 = new ArrayList<>();
        sem7 = new ArrayList<>();
        sem8 = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            sem1.add(i, 0);
            sem2.add(i, 0);
            sem3.add(i, 0);
            sem4.add(i, 0);
            sem5.add(i, 0);
            sem6.add(i, 0);
            sem7.add(i, 0);
            sem8.add(i, 0);
        }
    }

    public Result(ArrayList<Integer> sem1, ArrayList<Integer> sem2, ArrayList<Integer> sem3, ArrayList<Integer> sem4, ArrayList<Integer> sem5, ArrayList<Integer> sem6, ArrayList<Integer> sem7, ArrayList<Integer> sem8) {
        this.sem1 = sem1;
        this.sem2 = sem2;
        this.sem3 = sem3;
        this.sem4 = sem4;
        this.sem5 = sem5;
        this.sem6 = sem6;
        this.sem7 = sem7;
        this.sem8 = sem8;
    }

    public ArrayList<Integer> getSem1() {
        return sem1;
    }

    public void setSem1(ArrayList<Integer> sem1) {
        this.sem1 = sem1;
    }

    public ArrayList<Integer> getSem2() {
        return sem2;
    }

    public void setSem2(ArrayList<Integer> sem2) {
        this.sem2 = sem2;
    }

    public ArrayList<Integer> getSem3() {
        return sem3;
    }

    public void setSem3(ArrayList<Integer> sem3) {
        this.sem3 = sem3;
    }

    public ArrayList<Integer> getSem4() {
        return sem4;
    }

    public void setSem4(ArrayList<Integer> sem4) {
        this.sem4 = sem4;
    }

    public ArrayList<Integer> getSem5() {
        return sem5;
    }

    public void setSem5(ArrayList<Integer> sem5) {
        this.sem5 = sem5;
    }

    public ArrayList<Integer> getSem6() {
        return sem6;
    }

    public void setSem6(ArrayList<Integer> sem6) {
        this.sem6 = sem6;
    }

    public ArrayList<Integer> getSem7() {
        return sem7;
    }

    public void setSem7(ArrayList<Integer> sem7) {
        this.sem7 = sem7;
    }

    public ArrayList<Integer> getSem8() {
        return sem8;
    }

    public void setSem8(ArrayList<Integer> sem8) {
        this.sem8 = sem8;
    }
}
