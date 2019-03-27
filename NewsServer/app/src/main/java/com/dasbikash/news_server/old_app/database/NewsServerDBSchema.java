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

package com.dasbikash.news_server.old_app.database;

public final class NewsServerDBSchema {

    public static final int IS_WEEKLY_FLAG = 1;
    public static final int NOT_WEEKLY_FLAG = 0;

    public static final int SAVED_LOCALLY_FLAG = 1;
    public static final int NOT_SAVED_LOCALLY_FLAG = 0;

    public static final int GROUP_CATEGORY_IDENTIFIER_FOR_HOMEPAGE = -1;
    public static final int GROUP_CATEGORY_IDENTIFIER_FOR_CUSTOM_GROUP = 0;

    public static final int ITEM_ACTIVE_FLAG = 1;
    public static final int ITEM_INACTIVE_FLAG = 0;

    public static final int NULL_PARENT_FEATURE_ID = 0;

    public static final String DEFAULT_LINK_TRAILING_FORMAT = "page_num";

    public static final int NEWSPAPER_ID_THE_GURDIAN = 1;
    public static final int NEWSPAPER_ID_PROTHOM_ALO = 2;
    public static final int NEWSPAPER_ID_ANANDO_BAZAR = 3;
    public static final int NEWSPAPER_ID_THE_DAILY_STAR = 4;
    public static final int NEWSPAPER_ID_THE_INDIAN_EXPRESS = 5;
    public static final int NEWSPAPER_ID_DOINICK_ITTEFAQ = 6;
    public static final int NEWSPAPER_ID_THE_TIMES_OF_INDIA = 7;
    public static final int NEWSPAPER_ID_DAILY_MIRROR = 8;
    public static final int NEWSPAPER_ID_DHAKA_TRIBUNE = 9;
    public static final int NEWSPAPER_ID_BD_PROTIDIN = 10;
    public static final int NEWSPAPER_ID_DAWN_PAK = 11;
    public static final int NEWSPAPER_ID_KALER_KANTHO = 12;
    public static final int NEWSPAPER_ID_JUGANTOR = 13;
    public static final int NEWSPAPER_ID_THE_FINANCIAL_EXPRESS = 14;
    public static final int NEWSPAPER_ID_BONIK_BARTA = 15;
    public static final int NEWSPAPER_ID_BHORER_KAGOJ = 16;
    public static final int NEWSPAPER_ID_NEW_AGE = 17;
    public static final int NEWSPAPER_ID_DAILY_SUN = 18;

    public static final int IMAGE_DOWNLOAD_ON_DN_ENABLE_FLAG = 1;
    public static final int IMAGE_DOWNLOAD_ON_DN_DISABLE_FLAG = 0;
    public static final int IMAGE_DOWNLOAD_ON_DN_SETTINGS_ENTRY_ID = 1;

    public static final int NAVIGATION_MENU_DISPLAY_ENABLE_FLAG = 1;
    public static final int NAVIGATION_MENU_DISPLAY_DISABLE_FLAG = 0;
    public static final int NAVIGATION_MENU_DISPLAY_SETTINGS_ENTRY_ID = 2;

    public static final int FREQUENTLY_VIEWED_LIST_SIZE_SETTINGS_ENTRY_ID = 3;
    public static final int FREQUENTLY_VIEWED_LIST_SIZE_DEFAULT_VALUE = -1;

    public static final int MANUAL_IMAGE_DOWNLOAD_COUNT_SETTINGS_ENTRY_ID = 4;
    public static final int MIN_MANUAL_IMAGE_DL_COUNT_FOR_PROMPT2 = 7;

    public static final int LANGUAGE_CODE_BANGLA_BD= 1;
    public static final int LANGUAGE_CODE_BANGLA_IN= 2;
    public static final int LANGUAGE_CODE_ENGLISH_UK= 3;
    public static final int LANGUAGE_CODE_ENGLISH_US= 4;

    public static final int ARTICLE_TEXT_FONT_SIZE_SETTINGS_ENTRY_ID = 5;
    public static final int ARTICLE_TEXT_FONT_SIZE_SMALL = 14;
    public static final int ARTICLE_TEXT_FONT_SIZE_REGULAR = 16;
    public static final int ARTICLE_TEXT_FONT_SIZE_LARGE = 18;
    public static final int ARTICLE_TEXT_FONT_SIZE_EXTRA_LARGE = 20;

    /*public enum ARTICLE_TEXT_FONT_SIZE {
        SMALL(ARTICLE_TEXT_FONT_SIZE_SMALL),
        REGULAR(ARTICLE_TEXT_FONT_SIZE_REGULAR),
        LARGE(ARTICLE_TEXT_FONT_SIZE_LARGE),
        EXTRA_LARGE(ARTICLE_TEXT_FONT_SIZE_EXTRA_LARGE);

        private final int fontSize;
        ARTICLE_TEXT_FONT_SIZE(int fontSize) {
            this.fontSize = fontSize;
        }

        public int getFontSize() {
            return this.fontSize;
        }
    }*/

    public static final String NOTIFICATION_FILTER_INFO_SEPERATOR = "\\&\\$\\%\\&\\$\\%";
    public static final int POSITIVE_STATUS = 1;
    public static final int NEGETIVE_STATUS = 0;


    public static final class ContinentTable{

        public static final String NAME = "continent";

        public static final class Cols {
            public static final class Id {
                public static final String NAME = "id";
                public static final String TYPE = "integer primary key autoincrement";
            }
            public static final class Name {
                public static final String NAME = "name";
                public static final String TYPE = "text not null";
            }
        }
    }

    public static final class CountryTable{

        public static final String NAME = "country";

        public static final class Cols {
            public static final class Name {
                public static final String NAME = "name";
                public static final String TYPE = "text primary key not null";
            }
            public static final class CodeName {
                public static final String NAME = "code_name";
                public static final String TYPE = "text not null";
            }
            public static final class ContinentId {
                public static final String NAME = "continent_id";
                public static final String TYPE = "integer not null";
                public static final String FOREIGN_KEY="continent(id)";
            }
            public static final class TimeZoneId {
                public static final String NAME = "timezone_id";
                public static final String TYPE = "text not null";
            }
        }
    }

    public static final class LanguageTable{

        public static final String NAME = "language";

        public static final class Cols {
            public static final class Id {
                public static final String NAME = "id";
                public static final String TYPE = "integer primary key autoincrement";
            }
            public static final class Name {
                public static final String NAME = "name";
                public static final String TYPE = "text not null";
            }
        }
    }

    public static final class NewsPaperTable{

        public static final String NAME = "news_paper";

        public static final class Cols {
            public static final class Id {
                public static final String NAME = "id";
                public static final String TYPE = "integer primary key autoincrement";
            }
            public static final class Name {
                public static final String NAME = "name";
                public static final String TYPE = "text not null";
            }
            public static final class CountryName{
                public static final String NAME = "country_name";
                public static final String TYPE = "text not null";
                public static final String FOREIGN_KEY="country(name)";
            }
            public static final class LanguageId{
                public static final String NAME = "language_id";
                public static final String TYPE = "integer not null";
                public static final String FOREIGN_KEY="language(id)";
            }
            public static final class IsActive{
                public static final String NAME = "is_active";
                public static final String TYPE = "integer default "+ ITEM_ACTIVE_FLAG;
            }
        }
    }

    public static final class FeatureGroupTable {

        public static final String NAME = "feature_group";

        public static final class Cols {
            public static final class Id {
                public static final String NAME = "id";
                public static final String TYPE = "integer primary key autoincrement";
            }
            public static final class Title {
                public static final String NAME = "title";
                public static final String TYPE = "text not null";
            }
            //For Home page feature group category id will be -1
            //For any newspaper home page category id will be corresponding newspaper id
            //For custom group category id will be 0
            public static final class GroupCategoryidentifier {
                public static final String NAME = "category_identifier";
                public static final String TYPE = "integer default "+ GROUP_CATEGORY_IDENTIFIER_FOR_CUSTOM_GROUP;
            }
            public static final class IsActive{
                public static final String NAME = "is_active";
                public static final String TYPE = "integer default "+ ITEM_ACTIVE_FLAG;
            }
        }
    }

    public static final class FeatureGroupEntryTable {

        public static final String NAME = "feature_group_entry";

        public static final class Cols {
            public static final class Id {
                public static final String NAME = "id";
                public static final String TYPE = "integer primary key autoincrement";
            }
            public static final class GroupId {
                public static final String NAME = "group_id";
                public static final String TYPE = "integer not null";
                public static final String FOREIGN_KEY="feature_group(id)";
            }
            public static final class MemberFeatureId {
                public static final String NAME = "member_feature_id";
                public static final String TYPE = "integer not null";
                public static final String FOREIGN_KEY="feature(id)";
            }
        }
    }

    public static final class FeatureTable{

        public static final String NAME = "feature";

        public static final class Cols{

            public static final class Id{
                public static final String NAME="id";
                public static final String TYPE="integer primary key autoincrement";
            }
            public static final class NewsPaperId{
                public static final String NAME="news_paper_id";
                public static final String TYPE="integer not null";
                public static final String FOREIGN_KEY="news_paper(id)";
            }
            public static final class ParentFeatureId{
                public static final String NAME="parent_feature_id";
                public static final String TYPE="integer default "+NULL_PARENT_FEATURE_ID;
            }
            public static final class Title{
                public static final String NAME="title";
                public static final String TYPE="text NOT NULL";
            }
            public static final class IsWeekly{
                public static final String NAME="is_Weekly";
                public static final String TYPE="integer default "+NOT_WEEKLY_FLAG;
            }
            public static final class WeeklyPublicationDay{
                public static final String NAME="weekly_pub_day";
                public static final String TYPE="integer default 0";
            }
            public static final class LinkFormat{
                public static final String NAME="link_format";
                public static final String TYPE="text default NULL";
            }
            public static final class LinkVariablePartFormat {
                public static final String NAME="link_variable_part_format";
                public static final String TYPE="text default '"+DEFAULT_LINK_TRAILING_FORMAT+"'";
            }
            public static final class FirstEditionDateString{
                public static final String NAME="first_edition_date_string";
                public static final String TYPE="text default NULL";
            }
            public static final class IsActive{
                public static final String NAME = "is_active";
                public static final String TYPE = "integer default "+ ITEM_ACTIVE_FLAG;
            }
            public static final class ArticleReadCount{
                public static final String NAME="article_read_count";
                public static final String TYPE="integer default 0";
            }
            public static final class IsFavourite{
                public static final String NAME = "is_favourite";
                public static final String TYPE = "integer default "+ ITEM_INACTIVE_FLAG;
            }
        }
    }

    public static final class TextTable{
        public static final String NAME = "texts";
        public static final class Cols{
            public static final class Id{
                public static final String NAME="id";
                public static final String TYPE="integer primary key autoincrement";
            }
            public static final class Content{
                public static final String NAME="content";
                public static final String TYPE="text";
            }
        }
    }

    public static final class ImageTable{

        public static final String NAME = "images";

        public static final class Cols{
            public static final class Id{
                public static final String NAME="id";
                public static final String TYPE="integer primary key autoincrement";
            }
            public static final class WebLink{
                public static final String NAME="link";
                public static final String TYPE="text NOT NULL ";
            }
            public static final class DiskLocation{
                public static final String NAME="disk_location";
                public static final String TYPE="text default null";
            }
            public static final class AltText{
                public static final String NAME="alt_text";
                public static final String TYPE="text default null";
            }
            public static final class SizeKB{
                public static final String NAME="size_in_KB";
                public static final String TYPE="real default 0";
            }
            /*public static final class LastUpdateTimeStamp{
                public static final String NAME="last_update_ts";
                public static final String TYPE="TIMESTAMP default CURRENT_TIMESTAMP";
            }*/
        }
    }

    public static final class ArticleTable{
        public static final String NAME = "articles";

        public static final class Cols{
            public static final class Id{
                public static final String NAME="id";
                public static final String TYPE="integer primary key autoincrement";
            }
            public static final class FeatureId{
                public static final String NAME="feature_id";
                public static final String TYPE="integer not NULL";
                public static final String FOREIGN_KEY="feature(id)";
            }
            public static final class ArticleLink{
                public static final String NAME="link";
                public static final String TYPE="text NOT NULL ";
            }
            public static final class LinkHashCode{
                public static final String NAME="link_hashcode";
                public static final String TYPE="integer default 0";
            }
            public static final class ArticleTitle{
                public static final String NAME="title";
                public static final String TYPE="text not null";
            }
            public static final class PreviewImageId{
                public static final String NAME="preview_image_id";
                public static final String TYPE="integer DEFAULT null";
            }
            public static final class PublicationTimeStamp{
                public static final String NAME="publication_ts";
                public static final String TYPE="integer default 0";
            }

            public static final class LastModificationTimeStamp{
                public static final String NAME="last_modification_ts";
                public static final String TYPE="integer default 0";
            }
            public static final class SavedLocally{
                public static final String NAME = "saved_locally";
                public static final String TYPE="integer default "+ NOT_SAVED_LOCALLY_FLAG;
            }
        }
    }

    public static final class ArticleFragmentTable{

        public static final String NAME = "article_fragments";

        public static final class Cols{
            public static final class Id{
                public static final String NAME="id";
                public static final String TYPE="integer primary key autoincrement";
            }
            public static final class ArticleLinkHashCode{
                public static final String NAME="article_link_hash_code";
                public static final String TYPE="integer not NULL";
            }
            public static final class TextId{
                public static final String NAME="text_id";
                public static final String TYPE="integer default null";
                public static final String FOREIGN_KEY="texts(id)";
            }
            public static final class ImageId{
                public static final String NAME="image_id";
                public static final String TYPE="integer default null";
                public static final String FOREIGN_KEY="images(id)";
            }
        }
    }

    public static final class SettingsDataTable {

        public static final String NAME = "settings_value";

        public static final class Cols{
            public static final class Id{
                public static final String NAME="id";
                public static final String TYPE="integer primary key autoincrement";
            }
            public static final class ItemDescription{
                public static final String NAME="description";
                public static final String TYPE="text not NULL";
            }
            public static final class ItemValue{
                public static final String NAME="value";
                public static final String TYPE="integer not NULL";
            }
        }
    }

    public static final class NotificationInfoTable{

        public static final String NAME = "notification_info";

        public static final class Cols{
            public static final class FeatureId{
                public static final String NAME="feature_id";
                public static final String TYPE="integer primary key not null";
                public static final String FOREIGN_KEY="feature(id)";
            }
            public static final class IsActive{
                public static final String NAME = "is_active";
                public static final String TYPE = "integer default "+ ITEM_ACTIVE_FLAG;
            }
            public static final class InclusionFilter{
                public static final String NAME = "inclution_filter";
                public static final String TYPE = "text default null";
            }
            public static final class ExclusionFilter{
                public static final String NAME = "exclution_filter";
                public static final String TYPE = "text default null";
            }
        }
    }

    public static final class ConfigIntegrityCheckReportTable {

        public static final String NAME = "config_integrity_check_report";

        public static final class Cols {
            public static final class Id {
                public static final String NAME = "id";
                public static final String TYPE = "integer primary key autoincrement";
            }
            public static final class EntryTs {
                public static final String NAME = "entry_time_stamp";
                public static final String TYPE = "integer not null";
            }
            public static final class ReportDetails {
                public static final String NAME = "report_details";
                public static final String TYPE = "text not null";
            }
        }
    }

    public static final class ParentFeatureCheckLog{
        public static final String NAME = "parent_feature_check_log";
        public static final class Cols{
            public static final class Id {
                public static final String NAME = "id";
                public static final String TYPE = "integer primary key autoincrement";
            }
            public static final class EntryTs {
                public static final String NAME = "entry_time_stamp";
                public static final String TYPE = "integer not null";
            }
            public static final class ParentFeatureId {
                public static final String NAME = "parent_feature_id";
                public static final String TYPE = "integer not null";
            }
            public static final class CheckedFeatureId {
                public static final String NAME = "checked_feature_id";
                public static final String TYPE = "integer not null";
            }
            public static final class CheckStatus{
                public static final String NAME = "checking_status";
                public static final String TYPE = "integer default "+ NewsServerDBSchema.POSITIVE_STATUS;
            }
        }
    }

}
