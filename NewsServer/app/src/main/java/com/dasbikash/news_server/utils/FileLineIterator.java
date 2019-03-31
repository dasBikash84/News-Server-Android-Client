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

package com.dasbikash.news_server.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

final class FileLineIterator implements Iterator<String> {

    private BufferedReader mBufferedReader;
    private String nextLine;

    public static Iterator<String> getFileLineIteratorFromRawResource(@NonNull Context context, @RawRes int rawResId) {
        try {
            InputStream inputStream = context.getResources().openRawResource(rawResId);
            FileLineIterator fileLineIterator =
                    new FileLineIterator(
                            new BufferedReader(new InputStreamReader(inputStream))
                    );
            return fileLineIterator;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private FileLineIterator(BufferedReader bufferedReader) {
        mBufferedReader = bufferedReader;
        try {
            nextLine = mBufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return nextLine !=null;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public String next() {
        String currentline = new String(nextLine);
        try {
            nextLine = mBufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return currentline;
    }
}