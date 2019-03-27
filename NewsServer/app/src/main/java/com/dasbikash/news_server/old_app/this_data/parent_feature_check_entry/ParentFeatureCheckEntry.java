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

package com.dasbikash.news_server.old_app.this_data.parent_feature_check_entry;

public final class ParentFeatureCheckEntry {
    private int mId;
    private long mEntryTimeStamp;
    private int mParentFeatureId;
    private int mCheckedFeatureId;
    private boolean mCheckStatus;

    ParentFeatureCheckEntry(int id, long entryTimeStamp, int parentFeatureId,
                            int checkedFeatureId, boolean checkStatus) {
        mId = id;
        mEntryTimeStamp = entryTimeStamp;
        mParentFeatureId = parentFeatureId;
        mCheckedFeatureId = checkedFeatureId;
        mCheckStatus = checkStatus;
    }

    public int getId() {
        return mId;
    }

    public long getEntryTimeStamp() {
        return mEntryTimeStamp;
    }

    public int getParentFeatureId() {
        return mParentFeatureId;
    }

    public int getCheckedFeatureId() {
        return mCheckedFeatureId;
    }

    public boolean isCheckStatus() {
        return mCheckStatus;
    }
}
