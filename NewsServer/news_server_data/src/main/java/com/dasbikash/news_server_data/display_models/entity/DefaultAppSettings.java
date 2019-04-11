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

package com.dasbikash.news_server_data.display_models.entity;


import java.util.HashMap;

public class DefaultAppSettings {

    private HashMap<String, Country> countries;
    private HashMap<String, Language> languages;
    private HashMap<String, Newspaper> newspapers;
    private HashMap<String, Page> pages;
    private HashMap<String, PageGroup> page_groups;
    private HashMap<String,Long> update_time;

    public DefaultAppSettings() {
    }

    public HashMap<String, Country> getCountries() {
        return countries;
    }

    public void setCountries(HashMap<String, Country> countries) {
        this.countries = countries;
    }

    public HashMap<String, Language> getLanguages() {
        return languages;
    }

    public void setLanguages(HashMap<String, Language> languages) {
        this.languages = languages;
    }

    public HashMap<String, Newspaper> getNewspapers() {
        return newspapers;
    }

    public void setNewspapers(HashMap<String, Newspaper> newspapers) {
        this.newspapers = newspapers;
    }

    public HashMap<String, Page> getPages() {
        return pages;
    }

    public void setPages(HashMap<String, Page> pages) {
        this.pages = pages;
    }

    public HashMap<String, PageGroup> getPage_groups() {
        return page_groups;
    }

    public void setPage_groups(HashMap<String, PageGroup> page_groups) {
        this.page_groups = page_groups;
    }

    public HashMap<String, Long> getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(HashMap<String, Long> update_time) {
        this.update_time = update_time;
    }

    @Override
    public String toString() {
        return "DefaultAppSettings{" +
                "countries=" + countries +
                ", languages=" + languages +
                ", newspapers=" + newspapers +
                ", pages=" + pages +
                ", pageGroups=" + page_groups +
                '}';
    }
}
