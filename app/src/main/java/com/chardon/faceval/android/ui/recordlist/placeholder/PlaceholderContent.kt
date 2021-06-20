package com.chardon.faceval.android.ui.recordlist.placeholder

import java.util.*

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object PlaceholderContent {

    /**
     * An array of sample (placeholder) items.
     */
    val ITEMS: MutableList<PlaceholderItem> = ArrayList()

    /**
     * A map of sample (placeholder) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, PlaceholderItem> = HashMap()

    private val COUNT = 25

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createPlaceholderItem(i))
        }
    }

    private fun addItem(item: PlaceholderItem) {
        ITEMS.add(item)
        ITEM_MAP.put(item.imageSrc, item)
    }

    private fun createPlaceholderItem(position: Int): PlaceholderItem {
        return PlaceholderItem("https://static.vecteezy.com/system/resources/previews/000/420/940/original/avatar-icon-vector-illustration.png", "Title", 8.6, Date())
    }

    /**
     * A placeholder item representing a piece of content.
     */
    data class PlaceholderItem(
        val imageSrc: String,
        val title: String = "No title",
        val score: Double,
        val dateAdded: Date,
    ) {
        override fun toString(): String = "$title,$dateAdded"
    }
}