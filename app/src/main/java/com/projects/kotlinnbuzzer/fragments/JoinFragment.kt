package com.projects.kotlinnbuzzer.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.projects.kotlinnbuzzer.BuzzerActivity
import com.projects.kotlinnbuzzer.databinding.FragmentJoinBinding
import com.projects.kotlinnbuzzer.utils.RoomDataManager

class JoinFragment : Fragment() {
   lateinit var binding: FragmentJoinBinding
   lateinit var name: String
   lateinit var code: String
   private var dbref: DatabaseReference? = null
   private var valuev: ValueEventListener? = null
   lateinit var roomDataManager: RoomDataManager
   var x = 1
   var android_id = ""
   override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?,
   ): View? {
      binding = FragmentJoinBinding.inflate(layoutInflater)
      roomDataManager = RoomDataManager(requireContext())
      dbref = FirebaseDatabase.getInstance().getReference("AvailableRooms")
      return binding.root
   }

   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)
      name = ""
      code = ""
      android_id = Settings.Secure.getString(
         requireContext().getContentResolver(), Settings.Secure.ANDROID_ID
      )
      if (roomDataManager.isInRoom()) {
         name = roomDataManager.getUserName()
         code = roomDataManager.getRoomCode()
         binding.nameEditText.setText(name)
         binding.RoomCodeEditText.setText(code)
         checkAndJoinRoom()
      }
      binding.joinbtn.setOnClickListener {
         name = binding.nameEditText.text.toString()
         code = binding.RoomCodeEditText.text.toString()
         hideKeybord()
         if (name.isNotEmpty() and code.isNotEmpty()) {
            checkAndJoinRoom()
         } else {
            Snackbar.make(binding.root, "Fill all details", Snackbar.LENGTH_SHORT).show()
         }
      }
   }

   private fun checkAndJoinRoom() {
      if (dbref == null) {
         dbref = FirebaseDatabase.getInstance().getReference("AvailableRooms")
      }

      valuev?.let { dbref?.removeEventListener(it) }

      valuev = object : ValueEventListener {
         override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.hasChild(code)) {
               val roomRef = snapshot.child(code)
               val android_id = Settings.Secure.getString(
                  requireContext().getContentResolver(), Settings.Secure.ANDROID_ID
               )

               roomDataManager.saveRoomData(code, name)

               val intent = Intent(requireActivity(), BuzzerActivity::class.java)
               intent.putExtra("code", code)
               intent.putExtra("name", name)
               startActivity(intent)
               valuev?.let { dbref?.removeEventListener(it) }
               valuev = null
            } else {
               Snackbar.make(binding.root, "Room not found", Snackbar.LENGTH_SHORT).show()
               roomDataManager.clearRoomData()
            }
         }

         override fun onCancelled(error: DatabaseError) {
            Snackbar.make(binding.root, "Error: ${error.message}", Snackbar.LENGTH_SHORT).show()
         }
      }.also { listener ->
         valuev = listener
         dbref?.addValueEventListener(listener)
      }
   }

   override fun onDestroy() {
      super.onDestroy()
      try {
         val currentName = binding.nameEditText.text.toString()
         val currentCode = binding.RoomCodeEditText.text.toString()
         if (currentName.isNotEmpty() && currentCode.isNotEmpty()) {
            roomDataManager.saveRoomData(currentCode, currentName)
         }
         valuev?.let { dbref?.removeEventListener(it) }
         valuev = null
      } catch (e: Exception) {
      }
   }

   override fun onPause() {
      super.onPause()
      try {
         val currentName = binding.nameEditText.text.toString()
         val currentCode = binding.RoomCodeEditText.text.toString()
         if (currentName.isNotEmpty() && currentCode.isNotEmpty()) {
            roomDataManager.saveRoomData(currentCode, currentName)
         }
         valuev?.let { dbref?.removeEventListener(it) }
         valuev = null
      } catch (e: Exception) {
      }
   }

   override fun onStop() {
      super.onStop()
      try {
         val currentName = binding.nameEditText.text.toString()
         val currentCode = binding.RoomCodeEditText.text.toString()
         if (currentName.isNotEmpty() && currentCode.isNotEmpty()) {
            roomDataManager.saveRoomData(currentCode, currentName)
         }
         valuev?.let { dbref?.removeEventListener(it) }
         valuev = null
      } catch (e: Exception) {
      }
   }

   fun hideKeybord() {
      val view = requireActivity().currentFocus
      if (view != null) {
         val hideKey =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
         hideKey.hideSoftInputFromWindow(view.windowToken, 0)
      } else {
         requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
      }
   }
}