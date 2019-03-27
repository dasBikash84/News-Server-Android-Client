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

package com.dasbikash.news_server.display_models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NewsCategoryEntry implements Serializable{

    private List<Integer> entries=new ArrayList<>();

    public NewsCategoryEntry() {
    }

    public NewsCategoryEntry(List<Integer> entries) {
        this.entries = entries;
    }

    public List<Integer> getEntries() {
        return entries;
    }

    public void setEntries(List<Integer> entries) {
        this.entries = entries;
    }
}
