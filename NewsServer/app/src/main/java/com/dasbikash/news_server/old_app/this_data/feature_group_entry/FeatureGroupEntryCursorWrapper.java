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

package com.dasbikash.news_server.old_app.this_data.feature_group_entry;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;

public class FeatureGroupEntryCursorWrapper extends CursorWrapper {

    private static final String TAG = "FeatureGroupEntryCursorWrapper";

    FeatureGroupEntryCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    FeatureGroupEntry getInstance() {

        try {

            int id = getInt(getColumnIndex(NewsServerDBSchema.FeatureGroupEntryTable.Cols.Id.NAME));
            int featureGroupId = getInt(getColumnIndex(NewsServerDBSchema.FeatureGroupEntryTable.Cols.GroupId.NAME));
            int featureId = getInt(getColumnIndex(NewsServerDBSchema.FeatureGroupEntryTable.Cols.MemberFeatureId.NAME));

            if (id<1 || featureGroupId<1 || featureId<1)   return null;

            return new FeatureGroupEntry(id,featureGroupId,featureId) ;

        } catch (Exception ex){
            //Log.d("Error", ""+ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }
}
