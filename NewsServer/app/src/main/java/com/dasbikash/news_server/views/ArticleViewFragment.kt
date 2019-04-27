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

package com.dasbikash.news_server.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.DisplayUtils
import com.dasbikash.news_server_data.models.room_entity.Article
import com.dasbikash.news_server_data.models.room_entity.ArticleImage
import com.dasbikash.news_server_data.models.room_entity.Language
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class ArticleViewFragment : Fragment(){

    private lateinit var mLanguage: Language
    private lateinit var mArticle: Article
    private var mArticleTextSize:Int? = null
    private var mTransientTextSize:Int? = null

    private lateinit var mArticleTitle:AppCompatTextView
    private lateinit var mArticlePublicationText:AppCompatTextView
    private lateinit var mArticleImageScroller: NestedScrollView
    private lateinit var mArticleText:AppCompatTextView
    private lateinit var mArticleImageHolder:RecyclerView
    private lateinit var mArticleImageListAdapter:ArticleImageListAdapter

    private val mDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_article_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val articleId:String = arguments!!.getString(ARG_ARTICLE_ID)!!
        mLanguage = arguments!!.getSerializable(ARG_LANGUAGE) as Language
        mTransientTextSize = arguments!!.getInt(ARG_TRANSIENT_TEXT_SIZE)

        findViewItems(view)

        mDisposable.add(
                Observable.just(articleId)
                        .subscribeOn(Schedulers.io())
                        .map {
                            val newsDataRepository = RepositoryFactory.getNewsDataRepository(context!!)
                            mArticle = newsDataRepository.findArticleById(articleId)!!
                            mArticle.imageLinkList =
                                    mArticle.imageLinkList
                                            ?.asSequence()
                                            ?.filter {
                                                !(it.link?.isBlank() ?: true)
                                            }?.toList()
                            if (mTransientTextSize!! >= DisplayUtils.MIN_ARTICLE_TEXT_SIZE) {
                                mArticleTextSize = mTransientTextSize
                            }else{
                                mArticleTextSize = DisplayUtils.getArticleTextSize(context!!)
                            }
                            DisplayUtils.getArticlePublicationDateString(mArticle,mLanguage,context!!) ?: ""
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<String?>(){
                            override fun onComplete() {}
                            @SuppressLint("SetTextI18n")
                            override fun onNext(dateString: String) {

                                mArticleText.setTextSize(TypedValue.COMPLEX_UNIT_SP,mArticleTextSize!!.toFloat())

                                mArticleTitle.text = mArticle.title
                                mArticlePublicationText.text = dateString
                                DisplayUtils.displayHtmlText(mArticleText,mArticle.articleText!!)

                                if(mArticle.imageLinkList != null && mArticle.imageLinkList!!.size > 0){
                                    mArticleImageListAdapter = ArticleImageListAdapter()
                                    mArticleImageHolder.adapter = mArticleImageListAdapter
                                    mArticleImageListAdapter.submitList(mArticle.imageLinkList)
                                    mArticleImageScroller.visibility = View.VISIBLE
                                }else{
                                    mArticleImageScroller.visibility = View.GONE
                                }
                            }
                            override fun onError(e: Throwable) {
                                Log.d(TAG,"Error in fetching article data!")
                            }
                        })
        )
    }

    private fun findViewItems(view: View) {
        mArticleTitle = view.findViewById(R.id.article_title)
        mArticlePublicationText = view.findViewById(R.id.article_publication_date_text)
        mArticleImageScroller = view.findViewById(R.id.article_image_scroller)
        mArticleImageHolder = view.findViewById(R.id.article_image_holder)
        mArticleText = view.findViewById(R.id.article_text)
    }

    override fun onPause() {
        super.onPause()
        mDisposable.clear()
    }

    companion object {

        val ARG_ARTICLE_ID = "com.dasbikash.news_server.views.ArticleViewFragment.ARG_ARTICLE_ID"
        val ARG_LANGUAGE = "com.dasbikash.news_server.views.ArticleViewFragment.ARG_LANGUAGE"
        val ARG_TRANSIENT_TEXT_SIZE = "com.dasbikash.news_server.views.ArticleViewFragment.ARG_TRANSIENT_TEXT_SIZE"
        val TAG = "ArticleViewFragment"

        fun getInstance(articleId:String,language: Language,transTextSize:Int): ArticleViewFragment {
            val args = Bundle()
            args.putString(ARG_ARTICLE_ID, articleId)
            args.putSerializable(ARG_LANGUAGE, language)
            args.putInt(ARG_TRANSIENT_TEXT_SIZE, transTextSize)
            val fragment = ArticleViewFragment()
            fragment.setArguments(args)
            return fragment
        }
    }
}

object ArticleImageDiffCallback: DiffUtil.ItemCallback<ArticleImage>(){
    override fun areItemsTheSame(oldItem: ArticleImage, newItem: ArticleImage): Boolean {
        return oldItem.link == newItem.link
    }
    override fun areContentsTheSame(oldItem: ArticleImage, newItem: ArticleImage): Boolean {
        return oldItem.link == newItem.link
    }
}

class ArticleImageListAdapter():
        ListAdapter<ArticleImage,ArticleImageHolder>(ArticleImageDiffCallback){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleImageHolder {
        return ArticleImageHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.view_article_image,parent,false)
        )
    }
    override fun onBindViewHolder(holder: ArticleImageHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ArticleImageHolder(itemView: View):RecyclerView.ViewHolder(itemView){

    val mArticleImage:AppCompatImageView
    val mImageCaption:AppCompatTextView

    init{
        mArticleImage = itemView.findViewById(R.id.article_image)
        mImageCaption = itemView.findViewById(R.id.article_image_caption)
    }

    fun bind(articleImage: ArticleImage){
        if (articleImage.link !=null){
            Picasso.get().load(articleImage.link).into(mArticleImage)
        }else{
            itemView.visibility = View.GONE
        }
        if (articleImage.captin !=null){
            mImageCaption.text = articleImage.captin
        }else{
            mImageCaption.visibility = View.GONE
        }
    }
}