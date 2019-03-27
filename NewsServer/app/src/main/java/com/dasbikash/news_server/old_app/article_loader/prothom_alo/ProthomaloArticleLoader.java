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

package com.dasbikash.news_server.old_app.article_loader.prothom_alo;

import com.dasbikash.news_server.old_app.article_loader.ArticleLoaderBase;

import org.jsoup.select.Elements;

public class ProthomaloArticleLoader extends ArticleLoaderBase {

    private final String mSiteBaseAddress = "https://www.prothomalo.com";

    @Override
    protected String getSiteBaseAddress() {
        return mSiteBaseAddress;
    }

    @Override
    protected String processLink(String linkText) {
        if (linkText.contains("paimages")){
            linkText = linkText.replace("paimages","paloimages");
        }
        return super.processLink(linkText);
    }

    @Override
    protected String getFeaturedImageCaptionSelectorAttr() {
        return ProthomaloArticleParserInfo.FEATURED_IMAGE_CAPTION_SELECTOR_ATTR;
    }

    @Override
    protected String getFeaturedImageCaptionSelector() {
        return null;
    }

    @Override
    protected String getFeaturedImageLinkSelectorAttr() {
        return ProthomaloArticleParserInfo.FEATURED_IMAGE_LINK_SELECTOR;
    }

    @Override
    protected String getFeaturedImageSelector() {
        return ProthomaloArticleParserInfo.FEATURED_IMAGE_SELECTOR;
    }

    @Override
    protected String getArticleModificationDateString() {
        return null;
    }

    @Override
    protected String[] getArticleModificationDateStringFormats() {
        return new String[0];
    }

    private int mArticleLayoutType = 0;

    @Override
    protected Elements getArticleFragmentBlocks() {
        Elements articleFragments = mDocument.select(
                ProthomaloArticleParserInfo.ARTICLE_FRAGMENT_BLOCK_SELECTOR[mArticleLayoutType]
        );
        if (articleFragments==null || articleFragments.size() == 0){
            mArticleLayoutType = 1;
            articleFragments = mDocument.select(
                    ProthomaloArticleParserInfo.ARTICLE_FRAGMENT_BLOCK_SELECTOR[mArticleLayoutType]
            );
        }

        return articleFragments;
    }

    @Override
    protected String getParagraphImageSelector() {
        return ProthomaloArticleParserInfo.PARAGRAPH_IMAGE_SELECTOR[mArticleLayoutType];
    }

    @Override
    protected String getParagraphImageLinkSelectorAttr() {
        return ProthomaloArticleParserInfo.PARAGRAPH_IMAGE_LINK_SELECTOR_ATTR[mArticleLayoutType];
    }

    @Override
    protected String getParagraphImageCaptionSelectorAttr() {
        if (mArticleLayoutType == 0) {
            return ProthomaloArticleParserInfo.PARAGRAPH_IMAGE_CAPTION_SELECTOR_ATTR[mArticleLayoutType];
        } else {
            return null;
        }
    }

    @Override
    protected String getParagraphImageCaptionSelector() {
        if (mArticleLayoutType == 1) {
            return ProthomaloArticleParserInfo.PARAGRAPH_IMAGE_CAPTION_SELECTOR[mArticleLayoutType];
        } else {
            return null;
        }
    }
    /*@Override
    protected ArrayList<ArticleFragmentPayload> parseArticle() {

        //Log.d(TAG, "ProthomaloArticleLoader parseArticle: ");

        ArrayList<ArticleFragmentPayload> articleFragmentPayloads = new ArrayList<>();

        try {

            //Log.d(TAG, "ProthomaloArticleLoader parseArticle: mArticle.getLink(): "+mArticle.getLink());

            Document document = URLConnectionHelper.getJsopDocument(mArticle.getLink());
            if (document == null) {
                return articleFragmentPayloads;
            }

            Elements featuredImageElements = document.select(ProthomaloArticleParserInfo.getFeaturedImageSelector());

            if (featuredImageElements.size()>0){

                for (Element featuredImage:
                        featuredImageElements) {

                    String featuredImageLink = featuredImage.attr(ProthomaloArticleParserInfo.getFeaturedImageLinkSelectorAttr());

                    if (featuredImageLink.trim().length() > 0) {

                        featuredImageLink = processLink(featuredImageLink);

                        //featuredImageLink = ProthomaloArticleParserInfo.processLink(featuredImageLink);//ProthomaloArticleParserInfo.getSiteHTTPProtocol() + featuredImageLink;

                        String imagecaption = featuredImage.attr(ProthomaloArticleParserInfo.getFeaturedImageCaptionSelector());

                        int featuredImageDataId =
                                ImageDataHelper.saveImageData(
                                        featuredImageLink.hashCode(),
                                        featuredImageLink,
                                        imagecaption
                                );
                        ArticleFragmentPayload articleFragmentPayload = ArticleFragmentPayloadHelper.getInstanceByImageDataId(featuredImageDataId);
                        if (articleFragmentPayload != null) {
                            articleFragmentPayloads.add(articleFragmentPayload);
                        }
                    }
                }
            }


            Elements articleFragments = document.select(ProthomaloArticleParserInfo.getArticleFragmentBlockSelector());

            if (articleFragments.size()>0) {

                for (Element articleFragment :
                        articleFragments) {

                    String paraText = articleFragment.html();

                    //Log.d(TAG, "parseArticle: articleFragment.html():"+paraText);

                    if (paraText.trim().length() == 0) continue;

                    Elements imageChildren = articleFragment.select(ProthomaloArticleParserInfo.getParagraphImageSelector());

                    if (imageChildren.size() > 0) {
                        //Log.d(TAG, "parseArticle: imageChildren.size(): "+imageChildren.size());
                        for (Element imageChild :
                                imageChildren) {

                            String articleImageLink = imageChild.attr(ProthomaloArticleParserInfo.getArticleImageLinkSelectorAttr());

                            if (articleImageLink.length() > 0) {

                                articleImageLink = processLink(articleImageLink);

                                //Log.d(TAG, "articleImageLink: "+articleImageLink);
                                String imageCaption = imageChild.attr(ProthomaloArticleParserInfo.getArticleImageCaptionSelectorAttr());
                                //Log.d(TAG, "imageCaption: "+imageCaption);
                                //articleImageLink = ProthomaloArticleParserInfo.processLink(articleImageLink);
                                int articleImageDataId =
                                        ImageDataHelper.saveImageData(
                                                articleImageLink.hashCode(),
                                                articleImageLink,
                                                imageCaption
                                        );

                                ArticleFragmentPayload articleFragmentPayload = ArticleFragmentPayloadHelper.getInstanceByImageDataId(articleImageDataId);

                                if (articleFragmentPayload != null) {
                                    articleFragmentPayloads.add(articleFragmentPayload);
                                }
                            }
                        }

                        paraText = paraText.replaceAll(
                                ProthomaloArticleParserInfo.getArticleImageRemoverRegex(),
                                ProthomaloArticleParserInfo.getArticleImageRemoverRegexReplacement()
                        );
                        //Log.d(TAG, "paraText after img block replacement: "+paraText);
                    }

                    if (checkIfInvalidParagraph(paraText)) continue;

                    //Log.d(TAG, "ProthomaloArticleLoader parseArticle: paraText: "+paraText);

                    int textDataId = TextDataHelper.saveTextData(paraText);

                    ArticleFragmentPayload articleFragmentPayload = ArticleFragmentPayloadHelper.getInstanceByTextDataId(textDataId);

                    if (articleFragmentPayload != null) {
                        articleFragmentPayloads.add(articleFragmentPayload);
                    }
                }
            }else {
                articleFragments = document.select(ProthomaloArticleParserInfo.getArticleImageBlock2());

                for (Element articleFragment :
                        articleFragments) {

                    Elements articleImageLinks = articleFragment.select(ProthomaloArticleParserInfo.getParagraphImageSelector2());

                    if (articleImageLinks.size()>0) {
                        String articleImageLink = articleImageLinks.get(0).attr(ProthomaloArticleParserInfo.getArticleImageLinkSelectorAttr2());
                        if (articleImageLink.length() > 0) {

                            articleImageLink = processLink(articleImageLink);
                            String imageCaption = "";
                            //Log.d(TAG, "articleImageLink: "+articleImageLink);
                            if (articleFragment.select(ProthomaloArticleParserInfo.getArticleImageCaptionSelector2()).size()>0) {
                                imageCaption = articleFragment.select(ProthomaloArticleParserInfo.getArticleImageCaptionSelector2()).get(0).text();
                            }
                            //Log.d(TAG, "imageCaption: "+imageCaption);
                            //articleImageLink = ProthomaloArticleParserInfo.processLink(articleImageLink);
                            int articleImageDataId =
                                    ImageDataHelper.saveImageData(
                                            articleImageLink.hashCode(),
                                            articleImageLink,
                                            imageCaption
                                    );

                            ArticleFragmentPayload articleFragmentPayload = ArticleFragmentPayloadHelper.getInstanceByImageDataId(articleImageDataId);

                            if (articleFragmentPayload != null) {
                                articleFragmentPayloads.add(articleFragmentPayload);
                            }
                        }
                    }
                }
            }

        } catch (Exception ex){
            //NewsServerUtility.logErrorMessage(TAG+": "+ex.getMessage());
            //Log.d(TAG, "parseArticle: Error: "+ex.getMessage());
            ex.printStackTrace();
        }

        return articleFragmentPayloads;
    }*/
}
