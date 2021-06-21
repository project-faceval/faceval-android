package com.chardon.faceval.android.ui.recordlist

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.chardon.faceval.android.R
import com.chardon.faceval.android.data.LoginDataSource
import com.chardon.faceval.android.data.UserDatabase
import com.chardon.faceval.android.databinding.FragmentItemListBinding
import com.chardon.faceval.android.rest.client.APISet
import com.chardon.faceval.android.rest.client.PhotoClient
import com.chardon.faceval.android.util.Action
import com.chardon.faceval.android.util.MiscExtensions.toListItemList
import com.chardon.faceval.android.util.NotificationUtil.darkPurple
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

/**
 * A fragment representing a list of Items.
 */
class RecordFragment : Fragment() {
    private var columnCount = 1

    private lateinit var recordViewModel: RecordViewModel

    private lateinit var loginDataSource: LoginDataSource

    private lateinit var binding: FragmentItemListBinding

    private val photoClient: PhotoClient by lazy {
        APISet.photoClient
    }

    private val refreshJob = Job()
    private val refreshScope = CoroutineScope(Dispatchers.Main + refreshJob)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recordViewModel = ViewModelProvider(this, RecordViewModelFactory())
            .get(RecordViewModel::class.java)

        loginDataSource = LoginDataSource(UserDatabase.getInstance(requireContext()).userDao)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_item_list, container, false)

        binding.refreshLayout.setOnRefreshListener {
            refreshScope.launch {
                refreshAsync()
            }
        }

        recordViewModel.records.observe(viewLifecycleOwner) {
            binding.list.adapter = DefaultRecordRecyclerViewAdapter(it, requireActivity())
            binding.notePanel.isGone = it.isNotEmpty()
        }

        refreshScope.launch {
            refreshAsync(flushLocalData = false)
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            UPDATE_DETAIL -> {
                if (resultCode != Activity.RESULT_OK) {
                    return
                }

                refreshScope.launch {
                    refreshAsync()
                }
            }
            else -> {}
        }
    }

    @SuppressLint("ShowToast")
    private suspend fun refreshAsync(flushLocalData: Boolean = true,
                                     callback: Action = Action { }) {
        binding.refreshLayout.isRefreshing = true

        // Set the adapter
        with(binding.list) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }

            if (flushLocalData || recordViewModel.records.value == null) {

                val user = loginDataSource.getCurrentUser()

                binding.notePanel.isVisible = user == null

                if (user == null) {
//                    Snackbar.make(requireView(), "Cannot get user info", Snackbar.LENGTH_LONG)
//                        .darkPurple()
//                        .show()
                    binding.refreshLayout.isRefreshing = false
                    return
                }

                recordViewModel.updateRecords(
                    photoClient.getPhotosAsync(
                        photoId = null, userName = user.id, attachBase = !SAFE_MODE
                    ).await().toListItemList()
                )
                
                binding.refreshLayout.isRefreshing = false

                if (SAFE_MODE) {
                    try {
                        var entireList = recordViewModel.records.value ?: return

                        for (index in entireList.indices) {
                            val item = entireList[index]

                            val itemWithBase = photoClient.getPhotosAsync(
                                photoId = item.photoId, userName = user.id, attachBase = true
                            ).await().toListItemList()

                            entireList = entireList.toMutableList().apply {
                                this[index] = itemWithBase[0]
                            }

                            recordViewModel.updateRecords(entireList)
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        }

        binding.refreshLayout.isRefreshing = false

        callback.invoke()
    }

    override fun onResume() {
        super.onResume()

        val navigateAdd = requireActivity().intent.getBooleanExtra("navigate_add", false)

        if (navigateAdd) {
            refreshScope.launch {
                refreshAsync()
            }
        }
    }

    companion object {

        // Safe mode ensures much lower possibility of memory leak caused by long base64 encoding
        // It is recommended to keep it open
        const val SAFE_MODE = true

        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            RecordFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }

        const val UPDATE_DETAIL = 1
    }
}