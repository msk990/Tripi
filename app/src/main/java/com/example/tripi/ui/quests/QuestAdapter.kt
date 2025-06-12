package com.example.tripi.ui.quests

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tripi.R


class QuestAdapter(
    private val quests: List<Quest>,
    private val onClick: (Quest) -> Unit
) : RecyclerView.Adapter<QuestAdapter.QuestViewHolder>() {

    inner class QuestViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quest, parent, false)
        return QuestViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestViewHolder, position: Int) {
        val quest = quests[position]
        val titleView = holder.view.findViewById<TextView>(R.id.title)
        val imageView = holder.view.findViewById<ImageView>(R.id.image)

        titleView.text = quest.title

        val assetPath = "quests/${quest.image}"
        try {
            val inputStream = holder.view.context.assets.open(assetPath)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Log.w("QuestAdapter", "Missing image asset: $assetPath", e)
            imageView.setImageResource(R.drawable.placeholder_image)
        }
        val pointsBadge = holder.view.findViewById<TextView>(R.id.pointsBadge)
        val statusBadge = holder.view.findViewById<TextView>(R.id.statusBadge)

        pointsBadge.text = "${quest.reward} pts"
        statusBadge.text = when (quest.status) {
            QuestStatus.NOT_STARTED -> "Not Started"
            QuestStatus.IN_PROGRESS -> "In Progress"
            QuestStatus.COMPLETED -> "Completed"
        }

        val statusColor = when (quest.status) {
            QuestStatus.NOT_STARTED -> Color.parseColor("#D32F2F")
            QuestStatus.IN_PROGRESS -> Color.parseColor("#F57C00")
            QuestStatus.COMPLETED -> Color.parseColor("#388E3C")
        }

        statusBadge.background = GradientDrawable().apply {
            cornerRadius = 16f
            setColor(statusColor)
        }



        holder.view.setOnClickListener { onClick(quest) }
    }


    override fun getItemCount() = quests.size
}
