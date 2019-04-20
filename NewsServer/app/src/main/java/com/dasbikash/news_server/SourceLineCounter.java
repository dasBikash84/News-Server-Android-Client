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

package com.dasbikash.news_server;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SourceLineCounter {

    private static List<File> allFiles = new ArrayList<File>();
    private static COUNT_MODE countMode = COUNT_MODE.EXCLUDE_TEST_FILES;

    enum COUNT_MODE{
        ABSOLUTE_ALL,ALL,EXCLUDE_TEST_FILES
    }

    public static void main(String[] args) {
//        System.out.println(new File("app").getAbsolutePath());
        File file;
        switch (countMode){
            case ALL:
            case ABSOLUTE_ALL:
                file = new File("app/src/");
                break;
            case EXCLUDE_TEST_FILES:
                file = new File("app/src/main/java");
                break;
            default:
                return;
        }

        if (file.exists() && file.isDirectory() && file.canExecute()){
            processFile(file);
        }

        final HashMap<File,Integer> fileLineCountMap = new HashMap<>();

        allFiles.forEach(file1 -> {
            System.out.print("File: "+file1.getAbsolutePath());
            if (file1.getName().matches("\\..+")){
                System.out.println(" is hidden so quiting");
                return;
            }
            try {
                RandomAccessFile fileReader = new RandomAccessFile(file1,"r");
                int lineCount = 0;

                do {
                    String nextLine;
                    try {
                        nextLine = fileReader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                    if (nextLine == null){
                        break;
                    }

                    if (countMode != COUNT_MODE.ABSOLUTE_ALL) {
                        if (nextLine.trim().isEmpty() ||
                                nextLine.contains("import") ||
                                nextLine.contains("package") ||
                                nextLine.matches("^//.+") ||
                                nextLine.matches("^.\\*.+")) {
                            continue;
                        }
                    }
                    lineCount++;
                }while (true);
                System.out.println(" Line count: "+lineCount);
                fileLineCountMap.put(file1,lineCount);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });

        int totalLineCount = 0;
        for (int lineCount :
                fileLineCountMap.values()) {
            totalLineCount += lineCount;
        }

        System.out.println("Total lines: "+totalLineCount);


    }


    private static void processFile(File file){
        String[] childFileNames= file.list();

        if (childFileNames!=null && childFileNames.length>0) {

            for (String childFileName : childFileNames) {
                File childFile = new File(file.getAbsolutePath()+"/"+childFileName);
                if (childFile.exists() && childFile.isDirectory() && childFile.canExecute()) {
                    processFile(childFile);
                } else {
                    allFiles.add(childFile);
                }
            }
        }
    }
}
