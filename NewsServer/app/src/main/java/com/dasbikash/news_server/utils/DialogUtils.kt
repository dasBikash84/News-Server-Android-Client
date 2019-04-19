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

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

object DialogUtils {

    fun createAlertDialog(context: Context,alertDialogDetails: AlertDialogDetails):AlertDialog{
        return getAlertDialogBuilder(context,alertDialogDetails).create()
    }

    fun getAlertDialogBuilder(context: Context,alertDialogDetails: AlertDialogDetails):AlertDialog.Builder{
        val dialogBuilder = AlertDialog.Builder(context)

        if (alertDialogDetails.title.isNotBlank()){
            dialogBuilder.setTitle(alertDialogDetails.title.trim())
        }
        if (alertDialogDetails.message.isNotBlank()){
            dialogBuilder.setMessage(alertDialogDetails.message.trim())
        }
        dialogBuilder.setPositiveButton(alertDialogDetails.positiveButtonText.trim(),
                {dialog: DialogInterface?, which: Int -> alertDialogDetails.doOnPositivePress() })
        dialogBuilder.setNegativeButton(alertDialogDetails.negetiveButtonText.trim(),
                {dialog: DialogInterface?, which: Int -> alertDialogDetails.doOnNegetivePress() })
        return dialogBuilder
    }


    data class AlertDialogDetails(
            val title:String = "",
            val message:String = "",
            val positiveButtonText:String = "Ok",
            val negetiveButtonText:String = "Cancel",
            val doOnPositivePress:()->Unit = {},
            val doOnNegetivePress:()->Unit = {}
    )
}