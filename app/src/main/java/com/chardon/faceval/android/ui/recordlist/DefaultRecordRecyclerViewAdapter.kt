package com.chardon.faceval.android.ui.recordlist

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.chardon.faceval.android.ui.recordlist.placeholder.PlaceholderContent.PlaceholderItem
import com.chardon.faceval.android.databinding.FragmentItemBinding

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 */
class DefaultRecordRecyclerViewAdapter(
    private val values: List<PlaceholderItem>
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
        holder.imageView.setImageURI(Uri.parse(item.imageSrc))
        holder.titleView.text = item.title
        holder.scoreView.text = item.score.toString()
        holder.dateAddedView.text = SimpleDateFormat("yyyy-mm-dd").format(item.dateAdded)
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView: ImageView = binding.imageView
        val titleView: TextView = binding.titleLabel
        val scoreView: TextView = binding.scoreLabel
        val dateAddedView: TextView = binding.dateLabel

        override fun toString(): String {
            return "${super.toString()} '${titleView.text}' '${dateAddedView.text}'"
        }
    }

}