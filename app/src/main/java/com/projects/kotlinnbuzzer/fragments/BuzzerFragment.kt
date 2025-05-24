package com.projects.kotlinnbuzzer.fragments

import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.projects.kotlinnbuzzer.databinding.FragmentBuzzerBinding
import com.projects.kotlinnbuzzer.utils.RoomDataManager


class BuzzerFragment : Fragment() {

    lateinit var binding: FragmentBuzzerBinding
    lateinit var code: String
    lateinit var name: String
    lateinit var database: FirebaseDatabase
    lateinit var android_id: String
    lateinit var roomDataManager: RoomDataManager
    private var isUserRemoved = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentBuzzerBinding.inflate(layoutInflater)
        roomDataManager = RoomDataManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var bundle = arguments
        if (bundle != null) {
            code = bundle.getString("code", "")
            name = bundle.getString("name", "")
        }
        database = FirebaseDatabase.getInstance()
        android_id = Settings.Secure.getString(
            getContext()?.getContentResolver(),
            Settings.Secure.ANDROID_ID
        )

        setupRoomPresence()
    }

    private fun setupRoomPresence() {
        val roomRef = database.getReference("AvailableRooms").child(code).child(android_id)

        // Set up user data in room
        roomRef.child("name").setValue(name)

        // Set up presence system
        val userStatusRef = roomRef.child("online")
        val connectedRef = database.getReference(".info/connected")

        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    // When user disconnects, remove them from room
                    userStatusRef.onDisconnect().setValue(false)
                    // When user connects/reconnects, set them as online
                    userStatusRef.setValue(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        // Monitor if user is removed from room
        roomRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() && !isUserRemoved) {
                    roomDataManager.clearRoomData()
                    findNavController().popBackStack()
                    Toast.makeText(
                        requireActivity(),
                        "You have been removed from the room",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Don't clear room data on normal view destruction
        if (!isUserRemoved) {
            roomDataManager.saveRoomData(code, name)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isUserRemoved = true
        database.getReference(
            "AvailableRooms"
        )
            .child(code)
            .child(android_id)
            .removeValue()
    }

    override fun onStop() {
        super.onStop()
        isUserRemoved = true
        database.getReference(
            "AvailableRooms"
        )
            .child(code)
            .child(android_id)
            .removeValue()
    }

    override fun onDetach() {
        super.onDetach()
        isUserRemoved = true
        database.getReference(
            "AvailableRooms"
        )
            .child(code)
            .child(android_id)
            .removeValue()

    }

    override fun onResume() {
        super.onResume()
        database.getReference(
            "AvailableRooms"
        )
            .child(code)
            .child(android_id)
            .child("name")
            .setValue(name)
    }
}