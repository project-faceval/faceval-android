package com.chardon.faceval.android.ui.recordlist

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.drawToBitmap

import com.chardon.faceval.android.databinding.FragmentItemBinding
import com.chardon.faceval.android.util.BitmapUtil.save
import com.chardon.faceval.android.util.BitmapUtil.toBitmap
import com.chardon.faceval.android.util.FileNames
import java.io.File
import java.io.FileOutputStream

/**
 * [RecyclerView.Adapter] that can display a [ListItem].
 */
class DefaultRecordRecyclerViewAdapter(
    private val values: List<ListItem>, private val activity: Activity
) : RecyclerView.Adapter<DefaultRecordRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
//        holder.imageView.setImageURI(Uri.parse(item.imageSrc))
        item.bitmap?.let { holder.imageView.setImageBitmap(it) }
        holder.titleView.text = item.title
        holder.scoreView.text = String.format("%.1f", item.score)
        holder.dateAddedView.text =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.dateAdded)

        holder.rootView.setOnClickListener {
            val intent = Intent(activity.applicationContext, RecordDetailActivity::class.java)

            intent.putExtra("ignore_image", item.bitmap == null)

            if (item.bitmap != null) {
                File(activity.applicationContext.filesDir, FileNames.UPDATE_RECORD_IMG).apply {
                    if (exists()) {
                        delete()
                    }

                    createNewFile()

                    FileOutputStream(this).use { fos ->
//                    holder.imageView.drawToBitmap()
                        item.bitmap.save(fos)
                    }
                }
            }

            intent.putExtra("image_name", FileNames.UPDATE_RECORD_IMG)
            intent.putExtra("photo_id", item.photoId)
            activity.startActivityForResult(intent, RecordFragment.UPDATE_DETAIL)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView: ImageView = binding.imageView
        val titleView: TextView = binding.titleLabel
        val scoreView: TextView = binding.scoreLabel
        val dateAddedView: TextView = binding.dateLabel
        val rootView: ConstraintLayout = binding.root

        override fun toString(): String {
            return "${super.toString()} '${titleView.text}' '${dateAddedView.text}'"
        }
    }

}