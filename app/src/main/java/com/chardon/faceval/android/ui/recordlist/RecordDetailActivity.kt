package com.chardon.faceval.android.ui.recordlist

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.chardon.faceval.android.R
import com.chardon.faceval.android.data.LoginDataSource
import com.chardon.faceval.android.data.UserDatabase
import com.chardon.faceval.android.databinding.ActivityRecordDetailBinding
import com.chardon.faceval.android.rest.client.APISet
import com.chardon.faceval.android.rest.client.PhotoClient
import com.chardon.faceval.android.util.BitmapUtil.toBitmap
import com.chardon.faceval.android.util.MiscExtensions.toMap
import com.chardon.faceval.entity.PhotoInfoUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.lang.Exception

class RecordDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordDetailBinding

    private lateinit var recordDetailViewModel: RecordDetailViewModel

    private val infoLoadJob = Job()
    private val infoLoadScope = CoroutineScope(Dispatchers.Main + infoLoadJob)

    private lateinit var loginDataSource: LoginDataSource

    private val photoClient: PhotoClient by lazy {
        APISet.photoClient
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_record_detail)

        recordDetailViewModel = ViewModelProvider(this).get(RecordDetailViewModel::class.java)
        binding.recordDetailViewModel = recordDetailViewModel
        binding.lifecycleOwner = this

        loginDataSource = LoginDataSource(UserDatabase.getInstance(applicationContext).userDao)

        recordDetailViewModel.apply {
            image.observe(this@RecordDetailActivity) {
                binding.detailImageView.setImageBitmap(it)
            }

            title.observe(this@RecordDetailActivity) {
                binding.detailTitleEntry.setText(it)
            }

            description.observe(this@RecordDetailActivity) {
                binding.descriptionEntry.setText(it)
            }
        }

        val photoId = intent.getLongExtra("photo_id", -1)
        val imageName = intent.getStringExtra("image_name")

        val bitmap: Bitmap

        File(filesDir, imageName!!).apply {
            FileInputStream(this).use { inputStream ->
                bitmap = inputStream.toBitmap()
            }
        }

        infoLoadScope.launch {
            binding.loading.root.visibility = View.VISIBLE

            val user = loginDataSource.getCurrentUser()

            val photoInfo = photoClient.getPhotosAsync(
                photoId = photoId, userName = user!!.id, attachBase = false).await()[0]

            recordDetailViewModel.updateInfo(
                bitmap, title = photoInfo.title ?: "", description = photoInfo.description ?: "")

            binding.loading.root.visibility = View.GONE
        }

        binding.detailCancelButton.setOnClickListener {
            setResult(RESULT_CANCELED, intent)
            finish()
        }

        binding.detailConfirmButton.setOnClickListener {
            val uploadJob = Job()
            val uploadScope = CoroutineScope(Dispatchers.Main + uploadJob)

            uploadJob.invokeOnCompletion {
                setResult(RESULT_OK, intent)
                finish()
            }

            uploadScope.launch {
                val user = loginDataSource.getCurrentUser() ?: return@launch

                try {
                    val photoInfo = photoClient.updatePhotoInfoAsync(
                        PhotoInfoUpdate(
                            id = user.id,
                            password = user.password!!,
                            photoId = photoId,
                            title = binding.detailTitleEntry.text.toString(),
                            description = binding.descriptionEntry.text.toString(),
                            score = null,
                        ).toMap()
                    )
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, "Update failed", Toast.LENGTH_LONG)
                        .show()
                    return@launch
                }

                Toast.makeText(applicationContext, "Update success", Toast.LENGTH_LONG)
                    .show()

                uploadJob.complete()
            }
        }

        binding.deleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Do you really want to delete?")
                .setPositiveButton(R.string.yes) { _, _ ->
                    infoLoadScope.launch {
                        val user = loginDataSource.getCurrentUser() ?: return@launch

                        binding.loading.root.visibility = View.VISIBLE

                        try {
                            val status = photoClient.deletePhotoAsync(user.id, user.password!!, photoId)
                        } catch (e: Exception) {
                            Toast.makeText(applicationContext, "Failed", Toast.LENGTH_LONG)
                                .show()
                            binding.loading.root.visibility = View.GONE
                            return@launch
                        }

                        binding.loading.root.visibility = View.GONE

                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }
                .setNegativeButton(R.string.no) { _, _ ->

                }
                .show()
        }
    }
}