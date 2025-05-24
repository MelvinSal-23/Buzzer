package com.projects.kotlinnbuzzer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.projects.kotlinnbuzzer.R
import com.projects.kotlinnbuzzer.model.RoomModel

class RoomAdapter(private val context:Context) : RecyclerView.Adapter<RoomAdapter.ViewHolder>(){
    val detailsofQuiz:ArrayList<RoomModel> = ArrayList<RoomModel>()

    fun add(s: RoomModel){
        detailsofQuiz.add(s)
        notifyDataSetChanged()
    }
    fun clearList(){
        detailsofQuiz.clear()
        notifyDataSetChanged()
    }
    fun getSize():Int{
        return detailsofQuiz.size
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView= LayoutInflater.from(parent.context).inflate(R.layout.room_recitem,parent,false)
        return RoomAdapter.ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem=detailsofQuiz[position]
        holder.name.text = currentItem.name
        holder.kickbtn.setOnClickListener {
            FirebaseDatabase.getInstance().getReference(
                    "AvailableRooms")
                .child(currentItem.roomCode)
                .child(currentItem.androidid)
                .removeValue()
            FirebaseDatabase.getInstance().getReference(
                "AvailableRooms")
                .child(currentItem.roomCode)
                .child(currentItem.androidid)
                .addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.childrenCount == 0L){
                            notifyDataSetChanged()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }
    }

    override fun getItemCount(): Int {
        return detailsofQuiz.size
    }



    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val name: TextView =itemView.findViewById(R.id.name)
        val kickbtn:Button = itemView.findViewById(R.id.kickbtn)

    }
}