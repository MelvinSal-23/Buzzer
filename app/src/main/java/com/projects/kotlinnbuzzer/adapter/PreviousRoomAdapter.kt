package com.projects.kotlinnbuzzer.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.projects.kotlinnbuzzer.R

class PreviousRoomAdapter(private val context:Context) : RecyclerView.Adapter<PreviousRoomAdapter.ViewHolder>(){
    private val detailsofQuiz:ArrayList<String> = ArrayList<String>()
    fun add(s:String){
        detailsofQuiz.add(s)
        notifyDataSetChanged()
    }
    fun clearList(){
        detailsofQuiz.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView= LayoutInflater.from(parent.context).inflate(R.layout.prevroom_item,parent,false)
        return PreviousRoomAdapter.ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem=detailsofQuiz[position]
        holder.name.text = currentItem
        holder.itemView.setOnClickListener {
            var bundle = Bundle()
            bundle.putString("code",currentItem)
            holder.itemView.findNavController().navigate(R.id.action_createRoomFragment_to_roomFragment,bundle)
        }
    }

    override fun getItemCount(): Int {
        return detailsofQuiz.size
    }



    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val name: TextView =itemView.findViewById(R.id.code)

    }
}