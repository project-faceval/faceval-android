package com.chardon.faceval.android.ui.recordlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RecordViewModelFactory : ViewModelProvider.Factory  {
    companion object Store {
        @Volatile
        var viewModel: RecordViewModel? = null
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordViewModel::class.java)) {
            synchronized(Store) {
                if (viewModel == null) {
                    viewModel = RecordViewModel()
                }

                return viewModel as T
            }
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}