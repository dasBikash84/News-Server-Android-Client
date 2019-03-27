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

package com.dasbikash.news_server.old_app.this_data.image_data;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;

class ImageDataCursorWrapper extends CursorWrapper {

    private static final String TAG = "TextDataCursorWrapper";

    ImageDataCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    ImageData getInstance() {

        try {

            int id = getInt(getColumnIndex(NewsServerDBSchema.ImageTable.Cols.Id.NAME));
            String link = getString(getColumnIndex(NewsServerDBSchema.ImageTable.Cols.WebLink.NAME));
            String diskLocation = getString(getColumnIndex(NewsServerDBSchema.ImageTable.Cols.DiskLocation.NAME));
            String altText = getString(getColumnIndex(NewsServerDBSchema.ImageTable.Cols.AltText.NAME));
            double sizeKB = getDouble(getColumnIndex(NewsServerDBSchema.ImageTable.Cols.SizeKB.NAME));

            if (id == 0 ||
                id == -1 ||
                link == null)   return null;

            return new ImageData(id,link,diskLocation,altText,sizeKB) ;

        } catch (Exception ex){
            //Log.d("Error:", ""+ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }
}