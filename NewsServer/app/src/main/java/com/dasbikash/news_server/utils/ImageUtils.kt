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

package com.dasbikash.news_server.utils

import android.widget.ImageView
import com.dasbikash.news_server.R
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import java.io.File

object ImageUtils {

    fun customLoader(imageView: ImageView,url: String?=null) {
        val picasso = Picasso.get()
        val requestCreator:RequestCreator

        if (url != null){
            if (url.startsWith("/data")){
                requestCreator = picasso.load(File(url))
            }else {
                requestCreator = picasso.load(url)
            }
        }else{
            requestCreator = picasso.load(R.drawable.app_big_logo)
        }
        requestCreator.error(R.drawable.app_big_logo).into(imageView)
    }
}
