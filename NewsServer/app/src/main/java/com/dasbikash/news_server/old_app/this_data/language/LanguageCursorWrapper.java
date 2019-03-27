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

package com.dasbikash.news_server.old_app.this_data.language;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;

class LanguageCursorWrapper extends CursorWrapper {

    private static final String TAG = "LanguageCursorWrapper";

    LanguageCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    Language getInstance() {

        try {

            int id = getInt(getColumnIndex(NewsServerDBSchema.LanguageTable.Cols.Id.NAME));
            String name = getString(getColumnIndex(NewsServerDBSchema.LanguageTable.Cols.Name.NAME));

            if (id<1||
                name.trim().length()<1)   return null;

            return new Language(id,name) ;

        } catch (Exception ex){
            //Log.d("Error", ""+ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }
}
