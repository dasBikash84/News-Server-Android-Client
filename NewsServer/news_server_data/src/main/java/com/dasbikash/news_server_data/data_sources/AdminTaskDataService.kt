/*
 * Copyright 2019 das.bikash.dev@gmail.com. All rights reserved.
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

package com.dasbikash.news_server_data.data_sources

import com.dasbikash.news_server_data.models.ArticleUploaderStatusChangeRequest
import com.dasbikash.news_server_data.models.NewsPaperParserModeChangeRequest

internal interface AdminTaskDataService {
    fun addParserTokenGenerationRequest():Boolean
    fun addDataCoordinatorTokenGenerationRequest():Boolean
    fun addNewsPaperParserModeChangeRequest(newsPaperParserModeChangeRequest: NewsPaperParserModeChangeRequest):Boolean
    fun addArticleUploaderStatusChangeRequest(articleUploaderStatusChangeRequest: ArticleUploaderStatusChangeRequest):Boolean
}