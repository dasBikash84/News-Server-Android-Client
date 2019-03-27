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

public class FeatureGroupEntry {
    private int mId;
    private int mFeatureGroupId;
    private int mFeatureId;

    FeatureGroupEntry(int id, int featureGroupId, int featureId) {
        mId = id;
        mFeatureGroupId = featureGroupId;
        mFeatureId = featureId;
    }

    public int getId() {
        return mId;
    }

    public int getFeatureGroupId() {
        return mFeatureGroupId;
    }

    public int getFeatureId() {
        return mFeatureId;
    }
}
