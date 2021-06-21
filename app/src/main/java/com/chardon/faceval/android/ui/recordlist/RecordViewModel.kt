package com.chardon.faceval.android.ui.recordlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecordViewModel : ViewModel() {

    private val _records = MutableLiveData<List<ListItem>>()
    val records: LiveData<List<ListItem>> = _records

    fun updateRecords(listItems: List<ListItem>) {
        _records.value = listItems
    }
}