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

package com.dasbikash.news_server.view_controllers

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.*
import com.dasbikash.news_server_data.data_sources.data_services.web_services.firebase.ArticleCommentLocal
import com.dasbikash.news_server_data.exceptions.NoInternertConnectionException
import com.dasbikash.news_server_data.models.room_entity.*
import com.dasbikash.news_server_data.repositories.AdminTaskRepository
import com.dasbikash.news_server_data.repositories.ArticleCommentRepository
import com.dasbikash.news_server_data.repositories.ArticleNotificationGenerationRepository
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class FragmentArticleView : Fragment(), TextSizeChangeableArticleViewFragment,SignInHandler {

    private lateinit var mLanguage: Language
    private lateinit var mNewspaper: Newspaper
    private lateinit var mPage: Page
    private lateinit var mArticle: Article
    private lateinit var mMainScroller: NestedScrollView

    private lateinit var mDateString: String
//    private var mArticleTextSize: Int? = null

    private lateinit var mArticleTitle: AppCompatTextView
    private lateinit var mArticlePublicationText: AppCompatTextView
    private lateinit var mArticleText: AppCompatTextView
    private lateinit var mArticleImageHolder: RecyclerView
    private lateinit var mArticleImageListAdapter: ArticleImageListAdapter
    private lateinit var mWaitScreen: LinearLayoutCompat

    private lateinit var mCommentsBlock:LinearLayout
    private lateinit var mCommentsHolder: RecyclerView
    private lateinit var mCommentsEditText: EditText
    private lateinit var mCommentsSubmitButton: Button
    private lateinit var mAnonymousCheckBox: CheckBox

    private lateinit var mArticleCommentLocalListAdapter:ArticleCommentLocalListAdapter
    private lateinit var mArticleCommentList:List<ArticleCommentLocal>

    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)

    private var mShowNotificationRequestMenuItem = false


    var actionAfterSuccessfulLogIn: (() -> Unit)? = null

    override fun launchSignInActivity(doOnSignIn: () -> Unit) {
        val intent = RepositoryFactory.getUserSettingsRepository(context!!).getLogInIntent()
        intent?.let {
            startActivityForResult(intent, LOG_IN_REQ_CODE)
        }
        actionAfterSuccessfulLogIn = doOnSignIn
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LOG_IN_REQ_CODE) {
            LogInPostProcessUtils.doLogInPostProcess(
                    mDisposable,context!!,resultCode,data,{showWaitScreen()},
                    {hideWaitScreen()},
                    {
                        actionAfterSuccessfulLogIn?.let { it() }
                        actionAfterSuccessfulLogIn = null
                    })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_article_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mArticle = arguments!!.getSerializable(ARG_ARTICLE) as Article

        findViewItems(view)
        setListners()
        initViewItems()
    }

    private fun initViewItems() {
        mArticleCommentLocalListAdapter = ArticleCommentLocalListAdapter(
                                                RepositoryFactory.getUserSettingsRepository(context!!).getCurrentUserId(),
                                                {commentEditAction(it)},{commentDeleteAction(it)})
        mCommentsHolder.adapter=mArticleCommentLocalListAdapter
        mCommentsBlock.visibility = View.VISIBLE
    }

    private fun findViewItems(view: View) {
        mArticleTitle = view.findViewById(R.id.article_title)
        mArticlePublicationText = view.findViewById(R.id.article_publication_date_text)
        mArticleImageHolder = view.findViewById(R.id.article_image_holder)
        mArticleText = view.findViewById(R.id.article_text)
        mWaitScreen = view.findViewById(R.id.wait_screen_for_data_loading)
        mMainScroller = view.findViewById(R.id.main_scroller)

        mCommentsBlock = view.findViewById(R.id.comments_block)
        mCommentsHolder = view.findViewById(R.id.comments_holder)
        mCommentsEditText = view.findViewById(R.id.new_comment_edit_text)
        mCommentsSubmitButton = view.findViewById(R.id.submit_comment_button)
        mAnonymousCheckBox = view.findViewById(R.id.anonymous_check_box)
    }

    private fun setListners() {
        mWaitScreen.setOnClickListener { }
        mCommentsSubmitButton.setOnClickListener { newCommentPostAction() }
    }

    private fun newCommentPostAction() {
        mCommentsEditText.text.trim().toString().apply {
            if (isBlank()){
                DisplayUtils.showShortToast(context!!, BLANK_COMMENT_MSG)
            }else{
                val userSettingsRepository = RepositoryFactory.getUserSettingsRepository(context!!)
                if (userSettingsRepository.checkIfLoggedIn()) {
                    DialogUtils.createAlertDialog(context!!, DialogUtils.AlertDialogDetails(
                            title = POST_COMMENT_PROMPT,doOnPositivePress = {addNewComment(this)}
                    )).show()
                }else{

                    DialogUtils.createAlertDialog(context!!, DialogUtils.AlertDialogDetails(
                            title = SIGN_IN_PROMPT,doOnPositivePress = {launchSignInActivity{addNewComment(this)}}
                    )).show()
                }
            }
        }
    }

    private fun addNewComment(commentText:String){
        showWaitScreen()
        mDisposable.add(
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .map {
                            ArticleCommentRepository.addNewComment(commentText, mArticle,mAnonymousCheckBox.isChecked)?.let {
                                return@map it
                            }
                            throw IllegalStateException()
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<ArticleCommentLocal?>() {
                            override fun onComplete() {
                                hideWaitScreen()
                            }

                            override fun onNext(data: ArticleCommentLocal) {
                                mCommentsEditText.setText("")
                                refreshComments()
                            }

                            override fun onError(e: Throwable) {
                                if (e is CompositeException) {
                                    e.exceptions.asSequence().forEach {
                                        LoggerUtils.printStackTrace(it)
                                        LoggerUtils.debugLog("Error class: ${it::class.java.canonicalName}", this@FragmentArticleView::class.java)
                                        LoggerUtils.debugLog("Trace: ${it.stackTrace.asList()}", this@FragmentArticleView::class.java)
                                    }
                                    if (e.exceptions.filter { it is NoInternertConnectionException }.count() > 0) {
                                        NetConnectivityUtility.showNoInternetToastAnyWay(this@FragmentArticleView.context!!)
                                    } else if (e.exceptions.filter { it is IllegalStateException }.count() > 0) {
                                        DisplayUtils.showShortToast(context!!, COMMENT_ADDITION_FAILURE_MSG)
                                    }
                                } else if (e is NoInternertConnectionException) {
                                    NetConnectivityUtility.showNoInternetToastAnyWay(this@FragmentArticleView.context!!)
                                } else if (e is IllegalStateException) {
                                    DisplayUtils.showShortToast(context!!, COMMENT_ADDITION_FAILURE_MSG)
                                }
                                hideWaitScreen()
                            }
                        }))
    }

    private fun commentDeleteAction(articleCommentLocal: ArticleCommentLocal) {
        DialogUtils.createAlertDialog(context!!, DialogUtils.AlertDialogDetails(
                title = DELETE_COMMENT_PROMPT,doOnPositivePress = {deleteComment(articleCommentLocal)}
        )).show()
    }

    private fun deleteComment(articleCommentLocal: ArticleCommentLocal) {
        showWaitScreen()
        mDisposable.add(
                Observable.just(articleCommentLocal)
                        .subscribeOn(Schedulers.io())
                        .map {
                            ArticleCommentRepository.deleteComment(it,mArticle)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Boolean>() {
                            override fun onComplete() {
                                hideWaitScreen()
                            }

                            override fun onNext(data: Boolean) {
                                if (data) {
                                    refreshComments()
                                }
                            }

                            override fun onError(e: Throwable) {
                                if (e is CompositeException) {
                                    e.exceptions.asSequence().forEach {
                                        LoggerUtils.printStackTrace(it)
                                        LoggerUtils.debugLog("Error class: ${it::class.java.canonicalName}", this@FragmentArticleView::class.java)
                                        LoggerUtils.debugLog("Trace: ${it.stackTrace.asList()}", this@FragmentArticleView::class.java)
                                    }
                                    if (e.exceptions.filter { it is NoInternertConnectionException }.count() > 0) {
                                        NetConnectivityUtility.showNoInternetToastAnyWay(this@FragmentArticleView.context!!)
                                    } else if (e.exceptions.filter { it is IllegalStateException }.count() > 0) {
                                        DisplayUtils.showShortToast(context!!, COMMENT_DELETION_FAILURE_MSG)
                                    }
                                } else if (e is NoInternertConnectionException) {
                                    NetConnectivityUtility.showNoInternetToastAnyWay(this@FragmentArticleView.context!!)
                                } else if (e is IllegalStateException) {
                                    DisplayUtils.showShortToast(context!!, COMMENT_DELETION_FAILURE_MSG)
                                }
                                hideWaitScreen()
                            }
                        }))
    }

    private fun editComment(newText:String, articleCommentLocal: ArticleCommentLocal) {
        showWaitScreen()
        mDisposable.add(
                Observable.just(articleCommentLocal)
                        .subscribeOn(Schedulers.io())
                        .map {
                            ArticleCommentRepository.editComment(newText,it,mArticle)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Boolean>() {
                            override fun onComplete() {
                                hideWaitScreen()
                            }

                            override fun onNext(data: Boolean) {
                                if (data) {
                                    refreshComments()
                                }
                            }

                            override fun onError(e: Throwable) {
                                if (e is CompositeException) {
                                    e.exceptions.asSequence().forEach {
                                        LoggerUtils.printStackTrace(it)
                                        LoggerUtils.debugLog("Error class: ${it::class.java.canonicalName}", this@FragmentArticleView::class.java)
                                        LoggerUtils.debugLog("Trace: ${it.stackTrace.asList()}", this@FragmentArticleView::class.java)
                                    }
                                    if (e.exceptions.filter { it is NoInternertConnectionException }.count() > 0) {
                                        NetConnectivityUtility.showNoInternetToastAnyWay(this@FragmentArticleView.context!!)
                                    } else if (e.exceptions.filter { it is IllegalStateException }.count() > 0) {
                                        DisplayUtils.showShortToast(context!!, COMMENT_EDIT_FAILURE_MESSAGE)
                                    }
                                } else if (e is NoInternertConnectionException) {
                                    NetConnectivityUtility.showNoInternetToastAnyWay(this@FragmentArticleView.context!!)
                                } else if (e is IllegalStateException) {
                                    DisplayUtils.showShortToast(context!!, COMMENT_EDIT_FAILURE_MESSAGE)
                                }
                                hideWaitScreen()
                            }
                        }))
    }

    private fun commentEditAction(articleCommentLocal: ArticleCommentLocal) {

        val editText = EditText(context!!)
        editText.setText(articleCommentLocal.commentText)
        val dialogBuilder =
                DialogUtils.getAlertDialogBuilder(context!!, DialogUtils.AlertDialogDetails(
                        message = EDIT_COMMENT_PROMPT,
                        doOnPositivePress = {
                            if (editText.text.toString().trim().isNotBlank()) {
                                editComment(editText.text.toString(), articleCommentLocal)
                            }else{
                                DisplayUtils.showShortToast(context!!,BLANK_COMMENT_MSG)
                            }
                        }
                ))
        dialogBuilder.setView(editText)
        dialogBuilder.create().show()
    }

    override fun onResume() {
        super.onResume()
        hideWaitScreen()
        displayArticleData()
        updateNotificationRequestMenuItem()
        displayArticleComments()
    }

    private fun displayArticleComments() {
        if (!::mArticleCommentList.isInitialized) {
            refreshComments()
        }
    }

    private fun refreshComments() {
        mDisposable.add(
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .map {
                            ArticleCommentRepository.getCommentsForArticle(mArticle)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<List<ArticleCommentLocal>>() {
                            override fun onComplete() {}
                            override fun onNext(data: List<ArticleCommentLocal>) {
                                mArticleCommentList = data.sortedByDescending { it.commentTime }
                                mArticleCommentLocalListAdapter.submitList(mArticleCommentList)
                            }

                            override fun onError(e: Throwable) {}
                        }))
    }

    private fun updateNotificationRequestMenuItem() {
        mDisposable.add(
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .map {
                            RepositoryFactory.getUserSettingsRepository(context!!).checkIfLoogedAsAdmin()
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Boolean>() {
                            override fun onComplete() {}
                            override fun onNext(data: Boolean) {
                                mShowNotificationRequestMenuItem = data
                                activity!!.invalidateOptionsMenu()
                            }
                            override fun onError(e: Throwable) {}
                        }))
    }

    private fun showWaitScreen() {
        mWaitScreen.visibility = View.VISIBLE
        mWaitScreen.bringToFront()
    }

    private fun hideWaitScreen() {
        mWaitScreen.visibility = View.GONE
    }

    private fun displayArticleData() {
        mDisposable.add(
                Observable.just(true)
                        .subscribeOn(Schedulers.computation())
                        .map {
                            val appSettingsRepository = RepositoryFactory.getAppSettingsRepository(context!!)
                            mPage = appSettingsRepository.findPageById(mArticle.pageId!!)!!
                            debugLog(mPage.toString())
                            mNewspaper = appSettingsRepository.getNewspaperByPage(mPage)
                            debugLog(mNewspaper.toString())
                            mLanguage = appSettingsRepository.getLanguageByNewspaper(mNewspaper)
                            mArticle.imageLinkList =
                                    mArticle.imageLinkList
                                            ?.asSequence()
                                            ?.filter {
                                                it.link?.isNotBlank() ?: false
                                            }?.toList()
//                            mArticleTextSize = DisplayUtils.getArticleTextSize(context!!)
                            mDateString = DisplayUtils
                                    .getArticlePublicationDateString(mArticle, mLanguage, context!!, true)!!
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Unit>() {
                            override fun onComplete() {}
                            override fun onNext(t: Unit) {
                                (activity!! as AppCompatActivity).supportActionBar!!.setTitle("${mPage.name} | ${mNewspaper.name}")
                                displayArticle()
                            }

                            override fun onError(e: Throwable) {
                                LoggerUtils.printStackTrace(e)
                            }
                        }))
    }

    private fun displayArticle() {

        setTextSize()

        mArticleTitle.text = mArticle.title
        mArticlePublicationText.text = mDateString
        DisplayUtils.displayHtmlText(mArticleText, mArticle.articleText!!)

        if (mArticle.imageLinkList != null && mArticle.imageLinkList!!.size > 0) {
            mArticleImageListAdapter = ArticleImageListAdapter(this, /*DisplayUtils.DEFAULT_ARTICLE_TEXT_SIZE.toFloat(),*/ true)
            mArticleImageHolder.adapter = mArticleImageListAdapter
            mArticleImageListAdapter.submitList(mArticle.imageLinkList!!.filter { it.link!=null })
            mArticleImageHolder.visibility = View.VISIBLE
        } else {
            mArticleImageHolder.visibility = View.GONE
        }
    }

    private fun setTextSize() {
        val fontSize = DisplayUtils.getArticleTextSize(context!!).toFloat()
        mArticleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
        mArticlePublicationText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize-3)
        mArticleTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize+1)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_fragment_article_view, menu)
        menu.findItem(R.id.add_notification_gen_request_menu_item).setVisible(mShowNotificationRequestMenuItem)
        menu.findItem(R.id.add_token_generation_request_menu_item).setVisible(mShowNotificationRequestMenuItem)
        mDisposable.add(
                Observable.just(mArticle)
                        .subscribeOn(Schedulers.computation())
                        .map {
                            val newsDataRepository = RepositoryFactory.getNewsDataRepository(context!!)
                            newsDataRepository.checkIfAlreadySaved(it)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Boolean>() {
                            override fun onComplete() {}
                            override fun onNext(alreadySaved: Boolean) {
                                menu.findItem(R.id.save_article_locally_menu_item).setVisible(!alreadySaved)
                            }

                            override fun onError(e: Throwable) {}
                        })
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.itemId == R.id.save_article_locally_menu_item) {
            DialogUtils.createAlertDialog(context!!,
                    DialogUtils.AlertDialogDetails(message = SAVE_ARTICLE_PROMPT, doOnPositivePress = { saveArticleLocallyAction() }))
                    .show()
            return true
        }else if (item.itemId == R.id.add_notification_gen_request_menu_item) {
            launchNotificationGenerationRequestDialog()
        }else if (item.itemId == R.id.add_token_generation_request_menu_item) {
            addTokenGenerationRequestAction()
        }
        return false
    }

    private fun addTokenGenerationRequestAction() {

        DialogUtils.createAlertDialog(context!!, DialogUtils.AlertDialogDetails(
                TOKEN_GENERATION_REQ_PROMPT,
                doOnPositivePress = { addTokenGenerationRequest() }
        )).show()
    }

    private fun addTokenGenerationRequest() {
        showWaitScreen()
        mDisposable.add(
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .map { AdminTaskRepository.addDataCoordinatorTokenGenerationRequest() }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Boolean>() {
                            override fun onComplete() {hideWaitScreen()}

                            override fun onNext(result: Boolean) {
                                if (result) {
                                    DisplayUtils.showShortToast(context!!, TOKEN_GENERATION_SUCCESS_MESSAGE)
                                } else {
                                    DisplayUtils.showShortToast(context!!, TOKEN_GENERATION_FAILURE_MESSAGE)
                                }
                            }
                            override fun onError(e: Throwable) {hideWaitScreen()}
                        })
        )
    }

    private fun launchNotificationGenerationRequestDialog() {
        val editText = EditText(context!!)
        editText.inputType = (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        editText.hint = AUTH_TOKEN_HINT
        val dialogBuilder =
                DialogUtils.getAlertDialogBuilder(context!!, DialogUtils.AlertDialogDetails(
                        message = NOTIFICATION_REQUEST_PROMPT,
                        doOnPositivePress = {
                            addNotificationGenerationRequest(editText.text.trim().toString())
                        }
                ))
        dialogBuilder.setView(editText)
        dialogBuilder.create().show()
    }

    private fun addNotificationGenerationRequest(authToken:String) {
        mDisposable.add(
                Observable.just(mArticle)
                        .subscribeOn(Schedulers.io())
                        .map {
                            ArticleNotificationGenerationRepository.addNotificationGenerationRequestForArticle(mArticle,authToken)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Boolean>() {
                            override fun onComplete() {}
                            override fun onNext(data: Boolean) {
                                if (data){
                                    showShortSnack(NOTIFICATION_REQ_ADDED_MESSAGE)
                                }else{
                                    showShortSnack(NOTIFICATION_REQ_ADD_FAILURE)
                                }
                            }
                            override fun onError(e: Throwable) {}
                        }))
    }

    private fun saveArticleLocallyAction() {
        showWaitScreen()
        mDisposable.add(
                Observable.just(mArticle)
                        .subscribeOn(Schedulers.io())
                        .map {
                            val newsDataRepository = RepositoryFactory.getNewsDataRepository(context!!)

                            if (!newsDataRepository.checkIfAlreadySaved(it)) {
                                val savedArticleImage = newsDataRepository.saveArticleToLocalDisk(it, context!!)
                                LoggerUtils.debugLog(savedArticleImage.toString(), this@FragmentArticleView::class.java)
                                return@map true
                            }
                            false
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Boolean>() {
                            override fun onComplete() {
                                hideWaitScreen()
                            }

                            override fun onNext(result: Boolean) {
                                if (result) {
                                    DisplayUtils.showShortSnack(view as CoordinatorLayout, ARTICLE_SAVED_MSG)
                                    activity!!.invalidateOptionsMenu()
                                }
                            }

                            override fun onError(e: Throwable) {
                                hideWaitScreen()
                            }
                        })
        )
    }

    fun refreshCommentsTextSize(){
        if (mArticleCommentList.isNotEmpty()) {
            mArticleCommentLocalListAdapter.submitList(emptyList())
            Handler(Looper.getMainLooper()).postDelayed(
                    { mArticleCommentLocalListAdapter.submitList(mArticleCommentList) }, 50L)
        }
    }

    fun refreshArticleImageDisplay(){
        if (mArticle.imageLinkList != null && mArticle.imageLinkList!!.size > 0) {
            mArticleImageListAdapter.submitList(emptyList())
            Handler(Looper.getMainLooper()).postDelayed(
                    { mArticleImageListAdapter.submitList(mArticle.imageLinkList!!.filter { it.link != null }) }, 50L)
        }
    }

    override fun setArticleTextSpSizeTo(fontSize: Int) {
        setTextSize()
        refreshArticleImageDisplay()
        refreshCommentsTextSize()
    }

    companion object {

        private const val TOKEN_GENERATION_REQ_PROMPT = "Add token generation request?"
        private const val TOKEN_GENERATION_SUCCESS_MESSAGE = "Token generation request added."
        private const val TOKEN_GENERATION_FAILURE_MESSAGE = "Token generation request addition failure."
        private const val NOTIFICATION_REQUEST_PROMPT = "Add notification generation request"
        private const val NOTIFICATION_REQ_ADDED_MESSAGE = "Notification request added"
        private const val NOTIFICATION_REQ_ADD_FAILURE = "Notification request addition failure"
        private const val AUTH_TOKEN_HINT = "Auth Token"
        private const val ARG_ARTICLE = "com.dasbikash.news_server.views.FragmentArticleView.ARG_ARTICLE"
        private const val ARTICLE_SAVED_MSG = "Article Saved."
        private const val BLANK_COMMENT_MSG = "Blank Comment!!"
        private const val SAVE_ARTICLE_PROMPT="Save Article?"
        private const val EDIT_COMMENT_PROMPT = "Edit comment and hit ok to save."
        private const val COMMENT_EDIT_FAILURE_MESSAGE = "Comment Edit failure."
        private const val COMMENT_DELETION_FAILURE_MSG = "Comment deletion failure."
        private const val DELETE_COMMENT_PROMPT = "Delete Comment?"
        private const val COMMENT_ADDITION_FAILURE_MSG = "Comment addition failure."
        private const val POST_COMMENT_PROMPT = "Post Comment?"
        private const val SIGN_IN_PROMPT = "Sign in and continue."
        private const val LOG_IN_REQ_CODE = 5456

        fun getInstance(article: Article): FragmentArticleView {
            val args = Bundle()
            args.putSerializable(ARG_ARTICLE, article)
            val fragment = FragmentArticleView()
            fragment.setArguments(args)
            return fragment
        }
    }
}

object ArticleImageDiffCallback : DiffUtil.ItemCallback<ArticleImage>() {
    override fun areItemsTheSame(oldItem: ArticleImage, newItem: ArticleImage): Boolean {
        return oldItem.link == newItem.link
    }

    override fun areContentsTheSame(oldItem: ArticleImage, newItem: ArticleImage): Boolean {
        return oldItem.link == newItem.link
    }
}

class ArticleImageListAdapter(lifecycleOwner: LifecycleOwner, /*val mArticleTextSize: Float,*/ val enableImageDownload: Boolean = false) :
        ListAdapter<ArticleImage, ArticleImageHolder>(ArticleImageDiffCallback), DefaultLifecycleObserver {

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    val mDisposable = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleImageHolder {
        return ArticleImageHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.view_article_image, parent, false),
                mDisposable, /*mArticleTextSize,*/ enableImageDownload
        )
    }

    override fun onBindViewHolder(holder: ArticleImageHolder, position: Int) {
        holder.bind(getItem(position), position + 1, itemCount)
    }

    override fun onViewRecycled(holder: ArticleImageHolder) {
        super.onViewRecycled(holder)
        holder.disposeImageLoader()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        LoggerUtils.debugLog("Disposing", this::class.java)
        mDisposable.clear()
    }

    override fun onResume(owner: LifecycleOwner) {
        LoggerUtils.debugLog("onResume", this::class.java)
        notifyDataSetChanged()
    }

    override fun onPause(owner: LifecycleOwner) {
        LoggerUtils.debugLog("Disposing", this::class.java)
        mDisposable.clear()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        LoggerUtils.debugLog("Disposing", this::class.java)
        mDisposable.clear()
    }
}

class ArticleImageHolder(itemView: View, val compositeDisposable: CompositeDisposable, /*textFontSize: Float,*/
                         val enableImageDownload: Boolean = false)
    : RecyclerView.ViewHolder(itemView) {

    val mArticleImage: AppCompatImageView
    val mImageCaption: AppCompatTextView
    val mCurrentImagePositionText: AppCompatTextView
    var imageLoadingDisposer: Disposable? = null

    init {
        mArticleImage = itemView.findViewById(R.id.article_image)
        mImageCaption = itemView.findViewById(R.id.article_image_caption)
        mCurrentImagePositionText = itemView.findViewById(R.id.current_image_position)
    }

    fun disposeImageLoader() {
        imageLoadingDisposer?.dispose()
    }

    fun bind(articleImage: ArticleImage, currentImagePosition: Int, totalImageCount: Int) {

            mCurrentImagePositionText.text = StringBuilder("${currentImagePosition}")
                    .append("/")
                    .append("${totalImageCount}")
                    .toString()

            mCurrentImagePositionText.bringToFront()
            imageLoadingDisposer = ImageLoadingDisposer(mArticleImage)
            compositeDisposable.add(imageLoadingDisposer!!)

            ImageUtils.customLoader(mArticleImage, articleImage.link,
                    R.drawable.pc_bg, R.drawable.app_big_logo,
                    {
                        compositeDisposable.delete(imageLoadingDisposer!!)
                        imageLoadingDisposer = null
                        if (enableImageDownload) {
                            mArticleImage.setOnLongClickListener {
                                DialogUtils.createAlertDialog(itemView.context, DialogUtils.AlertDialogDetails(
                                        message = "Download Image?", doOnPositivePress = { downloadImageAction(articleImage.link!!) }
                                )).show()
                                true
                            }
                        }
                    })

            if (articleImage.caption != null) {
                mImageCaption.text = articleImage.caption
            } else {
                mImageCaption.visibility = View.GONE
            }

        val textFontSize = DisplayUtils.getArticleTextSize(itemView.context).toFloat()
        mImageCaption.setTextSize(TypedValue.COMPLEX_UNIT_SP, textFontSize)
        mCurrentImagePositionText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textFontSize)
    }

    private fun downloadImageAction(link: String) {
        FileDownloaderUtils.downloadImageInExternalFilesDir(itemView.context, link)
    }
}

object ArticleCommentLocalDiffCallback : DiffUtil.ItemCallback<ArticleCommentLocal>() {
    override fun areItemsTheSame(oldItem: ArticleCommentLocal, newItem: ArticleCommentLocal): Boolean {
        return oldItem.commentId == newItem.commentId
    }

    override fun areContentsTheSame(oldItem: ArticleCommentLocal, newItem: ArticleCommentLocal): Boolean {
        return oldItem == newItem
    }
}

class ArticleCommentLocalListAdapter(val currentUserId:String,val commentEditAction:(ArticleCommentLocal)->Unit,
                                     val commentDeleteAction:(ArticleCommentLocal)->Unit) :
        ListAdapter<ArticleCommentLocal, ArticleCommentLocalViewHolder>(ArticleCommentLocalDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleCommentLocalViewHolder {
        return ArticleCommentLocalViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.view_comment_display, parent, false),currentUserId)
    }

    override fun onBindViewHolder(holder: ArticleCommentLocalViewHolder, position: Int) {
        val articleCommentLocal = getItem(position)
        holder.editButton.setOnClickListener { commentEditAction(articleCommentLocal) }
        holder.deleteButton.setOnClickListener { commentDeleteAction(articleCommentLocal) }
        holder.bind(articleCommentLocal)
    }
}

class ArticleCommentLocalViewHolder(itemView: View,val currentUserId:String):
        RecyclerView.ViewHolder(itemView) {

    val userImage:ImageView
    val displayName:AppCompatTextView
    val commentText:AppCompatTextView
    val commentDate:AppCompatTextView
    val editBlock:ViewGroup
    val editButton:ImageButton
    val deleteButton:ImageButton

    init {
        userImage = itemView.findViewById(R.id.user_image)
        displayName = itemView.findViewById(R.id.display_name)
        commentText = itemView.findViewById(R.id.comment_text)
        commentDate = itemView.findViewById(R.id.comment_date)
        editBlock = itemView.findViewById(R.id.comment_edit_view_holder)
        editButton = itemView.findViewById(R.id.edit_comment)
        deleteButton = itemView.findViewById(R.id.delete_comment)
    }

    fun bind(articleCommentLocal: ArticleCommentLocal){
        if (articleCommentLocal.userId == currentUserId){
            editBlock.visibility = View.VISIBLE
        }else{
            editBlock.visibility = View.GONE
        }
        if (articleCommentLocal.dislayName!=null){
            displayName.text = articleCommentLocal.dislayName!!
        }else{
            displayName.text = ANONYMOUS_USER_DISPLAY_NAME
        }
        commentText.text = articleCommentLocal.commentText
        commentDate.text = DisplayUtils.getFormatedShortDateString(itemView.context!!,articleCommentLocal.commentTime)
        ImageUtils.customLoader(userImage,articleCommentLocal.imageUrl,R.drawable.account_circle_black_48,
                                    R.drawable.account_circle_black_48)

        val textFontSize = DisplayUtils.getArticleTextSize(itemView.context).toFloat()
        commentText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textFontSize)
        commentDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, textFontSize-3)
        displayName.setTextSize(TypedValue.COMPLEX_UNIT_SP, textFontSize+1)
    }
    companion object{
        private const val ANONYMOUS_USER_DISPLAY_NAME = "Anonymous"
    }
}