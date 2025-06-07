package com.example.tripi.ui.collection.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tripi.R
import java.io.IOException

class StickerCollectionAdapter(
    private val context: Context,
    private val stickers: List<Pair<String, String>> // label to image path
) : RecyclerView.Adapter<StickerCollectionAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.stickerGridImage)
        val label: TextView = itemView.findViewById(R.id.stickerGridLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_sticker_grid, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = stickers.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (label, path) = stickers[position]
        holder.label.text = label

        try {
            val inputStream = context.assets.open(path)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            holder.image.setImageBitmap(bitmap)
            inputStream.close()
        } catch (e: IOException) {
            holder.image.setImageResource(R.drawable.ic_farm_black_24dp) // fallback
        }
    }
}
