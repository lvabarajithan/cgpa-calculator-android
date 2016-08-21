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

/**
 * Created by abara on 12/12/15.
 */
public class Feedback {

    private String feed;
    private String userName;
    private String userEmail;
    private String univNo;
    private String time;

    public Feedback() {
    }

    public Feedback(String feed, String userName, String userEmail, String univNo, String time) {
        this.feed = feed;
        this.userName = userName;
        this.userEmail = userEmail;
        this.univNo = univNo;
        this.time = time;
    }

    public String getFeed() {
        return feed;
    }

    public void setFeed(String feed) {
        this.feed = feed;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUnivNo() {
        return univNo;
    }

    public void setUnivNo(String univNo) {
        this.univNo = univNo;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
