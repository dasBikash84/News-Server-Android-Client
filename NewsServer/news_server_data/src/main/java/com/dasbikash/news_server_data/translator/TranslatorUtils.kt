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

package com.dasbikash.news_server_data.translator

object TranslatorUtils {
    /*private val WAITING_MS_FOR_NET_RESPONSE: Long = 60000L
    private val mEnglishToBanglaTranslator:FirebaseTranslator
    private val mBanglaToEnglishTranslator:FirebaseTranslator
    private val mModelDownloadConditions = FirebaseModelDownloadConditions.Builder().build()
    init {
        val englishToBanglaTranslatorOptions = FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(FirebaseTranslateLanguage.EN)
                .setTargetLanguage(FirebaseTranslateLanguage.BN)
                .build()
        mEnglishToBanglaTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(englishToBanglaTranslatorOptions)

        val banglaToEnglishTranslatorOptions = FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(FirebaseTranslateLanguage.BN)
                .setTargetLanguage(FirebaseTranslateLanguage.EN)
                .build()
        mBanglaToEnglishTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(banglaToEnglishTranslatorOptions)
    }

    private fun translateText(text:String,translator: FirebaseTranslator):String {

        ExceptionUtils.checkRequestValidityBeforeNetworkAccess()

        var resultText: String? = null
        val lock = Object()

        var translatorException: TranslatorException? = null

        translator.downloadModelIfNeeded(mModelDownloadConditions)
                .addOnSuccessListener {
                    LoggerUtils.debugLog("downloadModelIfNeeded >> addOnSuccessListener", this::class.java)
                    translator.translate(text)
                            .addOnSuccessListener { translatedText ->
                                resultText = translatedText
                                synchronized(lock) { lock.notify() }
                            }
                            .addOnFailureListener { exception ->
                                LoggerUtils.debugLog("translate >> addOnFailureListener", this::class.java)
                                translatorException = TranslatorException(exception)
                                synchronized(lock) { lock.notify() }
                            }
                }
                .addOnFailureListener { exception ->
                    LoggerUtils.debugLog("downloadModelIfNeeded >> addOnFailureListener", this::class.java)
                    translatorException = TranslatorException(exception)
                    synchronized(lock) { lock.notify() }
                }

        synchronized(lock) { lock.wait(WAITING_MS_FOR_NET_RESPONSE) }

        translatorException?.let { throw it }

        if (resultText == null) {
            throw TranslatorException()
        }

        return resultText!!
    }

    fun translateToBangla(text:String)=
            translateText(text, mEnglishToBanglaTranslator)

    fun translateToEnglish(text:String)=
            translateText(text, mBanglaToEnglishTranslator)*/
}