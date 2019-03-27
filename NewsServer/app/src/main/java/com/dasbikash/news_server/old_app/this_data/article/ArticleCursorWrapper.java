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

package com.dasbikash.news_server.old_app.this_data.article;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.dasbikash.news_server.old_app.database.NewsServerDBSchema;

import static com.dasbikash.news_server.old_app.database.NewsServerDBSchema.NOT_SAVED_LOCALLY_FLAG;

class ArticleCursorWrapper extends CursorWrapper {

    private static final String TAG = "ArticleCursorWrapper";

    ArticleCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    Article getInstance() {

        try {
            int id = getInt(getColumnIndex(NewsServerDBSchema.ArticleTable.Cols.Id.NAME));
            int featureId = getInt(getColumnIndex(NewsServerDBSchema.ArticleTable.Cols.FeatureId.NAME));
            String link = getString(getColumnIndex(NewsServerDBSchema.ArticleTable.Cols.ArticleLink.NAME));
            int linkHashCode = getInt(getColumnIndex(NewsServerDBSchema.ArticleTable.Cols.LinkHashCode.NAME));
            String title = getString(getColumnIndex(NewsServerDBSchema.ArticleTable.Cols.ArticleTitle.NAME));
            //String summary = getString(getColumnIndex(NewsServerDBSchema.ArticleTable.Cols.ArticleSummary.NAME));
            int previewImageId = getInt(getColumnIndex(NewsServerDBSchema.ArticleTable.Cols.PreviewImageId.NAME));
            long publicationTS = getLong(getColumnIndex(NewsServerDBSchema.ArticleTable.Cols.PublicationTimeStamp.NAME));
            long lastModificationTS = getLong(getColumnIndex(NewsServerDBSchema.ArticleTable.Cols.LastModificationTimeStamp.NAME));
            boolean savedLocally = getInt(getColumnIndex(NewsServerDBSchema.ArticleTable.Cols.SavedLocally.NAME)) == NOT_SAVED_LOCALLY_FLAG ? false:true;

            if (id<1||
                featureId<1 ||
                link.trim().length()<1 ||
                title.trim().length()<1 )   return null;

            return new Article(id, featureId, link, linkHashCode,title, previewImageId,
                                publicationTS, lastModificationTS,savedLocally) ;

        } catch (Exception ex){
            //Log.d("Error", ""+ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

}
