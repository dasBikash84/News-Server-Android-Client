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

package com.dasbikash.news_server_data.utills

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import io.reactivex.disposables.Disposable
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object ImageUtils {

    fun urlToFile(link: String, fileName: String, context: Context): String? {
        try {
            val url = URL(link)
            val bitmap = Picasso.get().load(link).get()
            val imageFile = File(context.filesDir.absolutePath + fileName + ".jpg")
            val os = FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.flush()
            os.close()
            return imageFile.absolutePath
        } catch (e: Exception) {
            LoggerUtils.printStackTrace(e)
            return null
        }
    }

    fun customLoader(imageView: ImageView, url: String? = null,
                     @DrawableRes placeHolderImageResourceId: Int,
                     @DrawableRes defaultImageResourceId: Int, callBack: (() -> Unit)? = null) {
        val picasso = Picasso.get()
        val requestCreator: RequestCreator

        if (url != null) {
            if (url.startsWith("/data")) {
                requestCreator = picasso.load(File(url))
            } else {
                requestCreator = picasso.load(url)
            }
        } else {
            requestCreator = picasso.load(defaultImageResourceId)
        }
        requestCreator
                .error(defaultImageResourceId)
                .placeholder(placeHolderImageResourceId)
                .into(imageView, object : Callback {
                    override fun onSuccess() {
                        callBack?.let { callBack() }
                    }

                    override fun onError(e: java.lang.Exception?) {}
                })
    }

    fun cancelRequestForImageView(imageView: ImageView) {
        Picasso.get().cancelRequest(imageView)
        LoggerUtils.debugLog("cancelRequestForImageView", this::class.java)
    }
}