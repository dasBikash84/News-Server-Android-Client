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

package com.dasbikash.news_server.display_models.room_converters;

import com.dasbikash.news_server.display_models.ImageLinkList;
import com.dasbikash.news_server.display_models.NewsCategoryEntry;

import androidx.room.TypeConverter;

public class ImageLinkListConverter {

    private static String DATA_BRIDGE = "!@#$";

    @TypeConverter
    public static String fromImageLinkList(ImageLinkList imageLinkList){

        StringBuilder stringBuilder = new StringBuilder("");

        for (int i = 0; i < imageLinkList.getImageLinks().size(); i++) {
            stringBuilder.append(imageLinkList.getImageLinks().get(i));
            if (i!= imageLinkList.getImageLinks().size()-1){
                stringBuilder.append(DATA_BRIDGE);
            }
        }
        return stringBuilder.toString();
    }

    @TypeConverter
    public static ImageLinkList toImageLinkList(String imageLinkListString){

        ImageLinkList imageLinkList = new ImageLinkList();

        if (imageLinkListString!=null){
            for (String entryStr : imageLinkListString.split(DATA_BRIDGE)){
                imageLinkList.getImageLinks().add(entryStr);
            }
        }

        return imageLinkList;
    }
}
