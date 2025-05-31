package com.projects.kotlinnbuzzer.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.projects.kotlinnbuzzer.PlayActivity
import com.projects.kotlinnbuzzer.adapter.RoomAdapter
import com.projects.kotlinnbuzzer.databinding.FragmentRoomBinding
import com.projects.kotlinnbuzzer.model.RoomModel
import java.text.SimpleDateFormat
import java.util.Calendar


class RoomFragment : Fragment() {
    private lateinit var binding: FragmentRoomBinding
    private lateinit var code: String
    private lateinit var database: FirebaseDatabase
    private lateinit var datetime: String
    private lateinit var madapter: RoomAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentRoomBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        datetime = getDateTime()
        var bundle = arguments
        madapter = RoomAdapter(requireContext())
        binding.roomrecview.adapter = madapter
        binding.roomrecview.layoutManager = LinearLayoutManager(requireActivity())
        if (bundle != null) {
            code = bundle.getString("code", "")
        }
        database = FirebaseDatabase.getInstance()
        binding.roomcode.text = "Room Code: ${code}"
        database.getReference("UserProfiles").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Rooms").child(code).child("createdOn").setValue(datetime)
        database.getReference("AvailableRooms").child(code).child("createdAt").setValue(datetime)
        binding.playbtn.setOnClickListener {
            if (madapter.getSize() == 0) {
                Snackbar.make(requireView(), "Nobody to play.", Snackbar.LENGTH_SHORT).show()
            } else {
                // Set initial play state to true
                database.getReference("AvailableRooms")
                    .child(code)
                    .child("isHostPlaying")
                    .setValue(true)
                    .addOnSuccessListener {
                        // Start PlayActivity
                        val intent = Intent(requireActivity(), PlayActivity::class.java)
                        intent.putExtra("code", code)
                        startActivity(intent)
                    }
            }
        }

        setupFirebaseListener()
    }

    private fun setupFirebaseListener() {
        binding.mprogress1.visibility = View.VISIBLE
        FirebaseDatabase.getInstance().getReference("AvailableRooms")
            .child(code)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    madapter.clearList()
                    binding.mprogress1.visibility = View.GONE
                    if (snapshot != null) {
                        if (snapshot.childrenCount == 0L) {
                            madapter.clearList()
                        } else {
                            for (childs in snapshot.children) {
                                if (childs.child("name").value != null) {
                                    val androidid = childs.key
                                    val name = childs.child("name").value
                                    Log.e("leaderboard", androidid.toString())
                                    madapter.add(
                                        RoomModel(
                                            androidid.toString(),
                                            name.toString(),
                                            code
                                        )
                                    )
                                }
                            }
                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Cancel", error.toString())
                }

            })
    }

    fun getDateTime(): String {
        var c: Calendar = Calendar.getInstance()
        var df: SimpleDateFormat? = null
        var formattedDate = ""
        df = SimpleDateFormat("dd-MM-yyyy HH:mm a")
        formattedDate = df!!.format(c.time)
        return formattedDate
    }

}