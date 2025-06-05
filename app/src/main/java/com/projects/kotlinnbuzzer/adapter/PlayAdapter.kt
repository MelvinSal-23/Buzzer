package com.projects.kotlinnbuzzer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.projects.kotlinnbuzzer.R
import com.projects.kotlinnbuzzer.model.RoomModel

class PlayAdapter(private val context: Context) : RecyclerView.Adapter<PlayAdapter.ViewHolder>() {
   val detailsofQuiz: ArrayList<RoomModel> = ArrayList<RoomModel>()
   private var onItemClickListener: ((RoomModel) -> Unit)? = null

   fun setOnItemClickListener(listener: (RoomModel) -> Unit) {
      onItemClickListener = listener
   }

   fun updatePlayer(updatedPlayer: RoomModel) {
      val index = detailsofQuiz.indexOfFirst { it.androidid == updatedPlayer.androidid }
      if (index != -1) {
         detailsofQuiz[index] = updatedPlayer
         detailsofQuiz.sortByDescending { it.score }
         notifyItemChanged(index)
         val newIndex = detailsofQuiz.indexOfFirst { it.androidid == updatedPlayer.androidid }
         if (newIndex != index) {
            notifyDataSetChanged()
         }
      }
   }

   fun add(s: RoomModel) {
      val existingIndex = detailsofQuiz.indexOfFirst { it.androidid == s.androidid }
      if (existingIndex != -1) {
         if (detailsofQuiz[existingIndex].score != s.score) {
            updatePlayer(s)
         }
      } else {
         detailsofQuiz.add(s)
         detailsofQuiz.sortByDescending { it.score }
         notifyDataSetChanged()
      }
   }

   fun clearList() {
      detailsofQuiz.clear()
      notifyDataSetChanged()
   }

   fun getCount(): Int = detailsofQuiz.size

   fun reset() {
      for (c in detailsofQuiz) {
         c.buzzat = "0"
         c.score = 0
      }
      notifyDataSetChanged()
   }

   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val itemView =
         LayoutInflater.from(parent.context).inflate(R.layout.play_recitem, parent, false)
      return ViewHolder(itemView)
   }

   override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      val currentItem = detailsofQuiz[position]
      holder.name.text = currentItem.name
      holder.scoreText.text = "${currentItem.score} pts"

      holder.itemView.setOnClickListener {
         onItemClickListener?.invoke(currentItem)
      }
   }

   override fun getItemCount(): Int = detailsofQuiz.size

   class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
      val name: TextView = itemView.findViewById(R.id.name_part)
      val scoreText: TextView = itemView.findViewById(R.id.score_text)
   }
}

