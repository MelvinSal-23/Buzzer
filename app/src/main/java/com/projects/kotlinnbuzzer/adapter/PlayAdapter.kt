package com.projects.kotlinnbuzzer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.projects.kotlinnbuzzer.R
import com.projects.kotlinnbuzzer.model.RoomModel

class PlayAdapter(private val context: Context) : RecyclerView.Adapter<PlayAdapter.ViewHolder>(){
    val detailsofQuiz:ArrayList<RoomModel> = ArrayList<RoomModel>()

    fun add(s: RoomModel){
        detailsofQuiz.add(s)
        detailsofQuiz.sortByDescending { it.buzzat?.toFloat() }

        notifyDataSetChanged()
    }
    fun clearList(){
        detailsofQuiz.clear()
        notifyDataSetChanged()
    }
    fun getCount():Int{
        return detailsofQuiz.size
    }
    fun reset(){
        for(c in detailsofQuiz){
            c.buzzat = "0"
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView= LayoutInflater.from(parent.context).inflate(R.layout.play_recitem,parent,false)
        return PlayAdapter.ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem=detailsofQuiz[position]
        holder.name.text = currentItem.name
        if(!currentItem.buzzat.isNullOrEmpty()){
            holder.buzzat.setText("${(60000 - currentItem.buzzat.toFloat())/1000.0}s")
        }
        else{
            holder.buzzat.setText("${60}s")
        }
//        holder.kickbtn.setOnClickListener {
//            FirebaseDatabase.getInstance().getReference(
//                "AvailableRooms")
//                .child(currentItem.roomCode)
//                .child(currentItem.androidid)
//                .removeValue()
//            FirebaseDatabase.getInstance().getReference(
//                "AvailableRooms")
//                .child(currentItem.roomCode)
//                .child(currentItem.androidid)
//                .addValueEventListener(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        if (snapshot.childrenCount == 0L){
//                            notifyDataSetChanged()
//                        }
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//
//                    }
//
//                })
//        }
    }

    override fun getItemCount(): Int {
        return detailsofQuiz.size
    }



    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val name: TextView =itemView.findViewById(R.id.name_part)
        val buzzat: TextView = itemView.findViewById(R.id.buzat)

    }
}

