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

package com.dasbikash.news_server.display_models.entity;

import java.util.List;

public class DefaultAppSettings {

    private List<Country> countries;
    private List<Language> languages;
    private List<Newspaper> newspapers;
    private List<Page> pages;
    private List<PageGroup> page_groups;

    public DefaultAppSettings() {
    }

    public List<Country> getCountries() {
        return countries;
    }

    public void setCountries(List<Country> countries) {
        this.countries = countries;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    public List<Newspaper> getNewspapers() {
        return newspapers;
    }

    public void setNewspapers(List<Newspaper> newspapers) {
        this.newspapers = newspapers;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public List<PageGroup> getPage_groups() {
        return page_groups;
    }

    public void setPage_groups(List<PageGroup> page_groups) {
        this.page_groups = page_groups;
    }

    @Override
    public String toString() {
        return "DefaultAppSettings{" +
                "countries=" + countries +
                ", languages=" + languages +
                ", newspapers=" + newspapers +
                ", pages=" + pages +
                ", page_groups=" + page_groups +
                '}';
    }
}
