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

package com.dasbikash.news_server.display_models.room_converters;

import com.dasbikash.news_server.display_models.PageGroupEntry;

import androidx.room.TypeConverter;

public class PageGroupEntryConverter {

    private static String DATA_BRIDGE = ",";

    @TypeConverter
    public static String fromNewsCategoryEntry(PageGroupEntry entry){
        StringBuilder stringBuilder = new StringBuilder("");
        for (int i = 0; i < entry.getEntries().size(); i++) {
            stringBuilder.append(entry.getEntries().get(i));
            if (i!= entry.getEntries().size()-1){
                stringBuilder.append(DATA_BRIDGE);
            }
        }
        return stringBuilder.toString();
    }

    @TypeConverter
    public static PageGroupEntry toNewsCategoryEntry(String entryListString){
        PageGroupEntry entry = new PageGroupEntry();

        if (entryListString!=null){
            for (String entryStr : entryListString.split(DATA_BRIDGE)){
                try {
                    entry.getEntries().add(Integer.parseInt(entryStr));
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }
        }

        return entry;
    }
}
