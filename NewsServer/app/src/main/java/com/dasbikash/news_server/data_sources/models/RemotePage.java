/*
 * Copyright 2019 www.dasbikash.org. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dasbikash.news_server.data_sources.models;

public class RemotePage {
    private int id;
    private int newsPaperId;
    private int parentPageId;
    private String title;
    private boolean isWeekly;
    private String weeklyPubDay;
    private String linkFormat;
    private String linkVariablePartFormat;
    private String firstEditionDateString;
    private boolean isActive;

    public RemotePage() {
    }

    public RemotePage(int id, int newsPaperId, int parentPageId, String title, boolean isWeekly, String weeklyPubDay, String linkFormat, String linkVariablePartFormat, String firstEditionDateString, boolean isActive) {
        this.id = id;
        this.newsPaperId = newsPaperId;
        this.parentPageId = parentPageId;
        this.title = title;
        this.isWeekly = isWeekly;
        this.weeklyPubDay = weeklyPubDay;
        this.linkFormat = linkFormat;
        this.linkVariablePartFormat = linkVariablePartFormat;
        this.firstEditionDateString = firstEditionDateString;
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNewsPaperId() {
        return newsPaperId;
    }

    public void setNewsPaperId(int newsPaperId) {
        this.newsPaperId = newsPaperId;
    }

    public int getParentPageId() {
        return parentPageId;
    }

    public void setParentPageId(int parentPageId) {
        this.parentPageId = parentPageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isWeekly() {
        return isWeekly;
    }

    public void setWeekly(boolean weekly) {
        isWeekly = weekly;
    }

    public String getWeeklyPubDay() {
        return weeklyPubDay;
    }

    public void setWeeklyPubDay(String weeklyPubDay) {
        this.weeklyPubDay = weeklyPubDay;
    }

    public String getLinkFormat() {
        return linkFormat;
    }

    public void setLinkFormat(String linkFormat) {
        this.linkFormat = linkFormat;
    }

    public String getLinkVariablePartFormat() {
        return linkVariablePartFormat;
    }

    public void setLinkVariablePartFormat(String linkVariablePartFormat) {
        this.linkVariablePartFormat = linkVariablePartFormat;
    }

    public String getFirstEditionDateString() {
        return firstEditionDateString;
    }

    public void setFirstEditionDateString(String firstEditionDateString) {
        this.firstEditionDateString = firstEditionDateString;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
