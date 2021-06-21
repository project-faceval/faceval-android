package com.chardon.faceval.android.ui.recordlist

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.chardon.faceval.android.databinding.FragmentItemBinding

/**
 * [RecyclerView.Adapter] that can display a [ListItem].
 */
class DefaultRecordRecyclerViewAdapter(
    private val values: List<ListItem>
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
        holder.dateAddedView.text = SimpleDateFormat("yyyy-MM-dd").format(item.dateAdded)
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