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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.dasbikash.news_server.R
import com.dasbikash.news_server.utils.LifeCycleAwareCompositeDisposable
import com.dasbikash.news_server_data.models.room_entity.Newspaper
import com.dasbikash.news_server_data.repositories.RepositoryFactory
import com.dasbikash.news_server_data.utills.LoggerUtils


class   FragmentNPStatusChangeRequest : Fragment(), AdapterView.OnItemSelectedListener {

    private val mNewsPapers = mutableListOf<Newspaper>()
    private val mDisposable = LifeCycleAwareCompositeDisposable.getInstance(this)

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    /**
     *
     * Callback method to be invoked when an item in this view has been
     * selected. This callback is invoked only when the newly selected
     * position is different from the previously selected position or if
     * there was no selected item.
     *
     * Implementers can call getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param parent The AdapterView where the selection happened
     * @param view The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id The row id of the item that is selected
     */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        LoggerUtils.debugLog(mNewsPapers.find { it.name == parent?.getItemAtPosition(position).toString()}?.id ?: "No Np Found",this::class.java)
    }

    private lateinit var mNewsPaperSelectorSpinner: Spinner

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_np_status_change_request,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoggerUtils.debugLog(this::class.java.simpleName,this::class.java,context!!)
        mNewsPaperSelectorSpinner = view.findViewById(R.id.newspaper_selector_spinner)
        mNewsPaperSelectorSpinner.setOnItemSelectedListener(this)

        RepositoryFactory.getAppSettingsRepository(context!!).getNewsPapersLiveData().observe(this,Observer<List<Newspaper>>{

            mNewsPapers.clear()
            mNewsPapers.addAll(it)
            val dataAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item, mNewsPapers.map { it.name }.toList())

            // Drop down layout style - list view with radio button
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // attaching data adapter to spinner
            mNewsPaperSelectorSpinner.setAdapter(dataAdapter)
        })

//        mDisposable.add(
//                Obser
//        )
    }
}