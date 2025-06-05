package com.projects.kotlinnbuzzer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.projects.kotlinnbuzzer.R
import com.projects.kotlinnbuzzer.adapter.PreviousRoomAdapter
import com.projects.kotlinnbuzzer.databinding.FragmentCreateRoomBinding


class CreateRoomFragment : Fragment() {

   lateinit var binding: FragmentCreateRoomBinding
   lateinit var code: String
   lateinit var database: FirebaseDatabase
   lateinit var codeArrayList: kotlin.collections.ArrayList<String>
   lateinit var dbref: DatabaseReference
   lateinit var valuev: ValueEventListener
   lateinit var madapter: PreviousRoomAdapter
   override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?,
   ): View? {
      binding = FragmentCreateRoomBinding.inflate(layoutInflater)
      return binding.root
   }

   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)
      code = createRandom()
      binding.randomCodetxt.text = code
      codeArrayList = ArrayList()
      database = FirebaseDatabase.getInstance()
      madapter = PreviousRoomAdapter(requireContext())
      binding.prevRecview.adapter = madapter
      binding.prevRecview.layoutManager = LinearLayoutManager(requireActivity())
      binding.createroom.setOnClickListener {
         var bundle = Bundle()
         bundle.putString("code", code)
         findNavController().navigate(R.id.action_createRoomFragment_to_roomFragment, bundle)
      }
      binding.mprogress.visibility = View.VISIBLE
      binding.backbtn.setOnClickListener {
         findNavController().popBackStack()
      }
      dbref = database.getReference("UserProfiles");
      valuev = dbref.child(FirebaseAuth.getInstance().currentUser!!.uid)
         .child("Rooms")
         .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
               if (snapshot != null) {
                  binding.mprogress.visibility = View.GONE
                  madapter.clearList()
                  for (codes in snapshot.children) {
                     madapter.add(codes.key.toString())
                  }
               }
            }

            override fun onCancelled(error: DatabaseError) {
               TODO("Not yet implemented")
            }

         })

   }

   override fun onStop() {
      super.onStop()

   }

   fun createRandom(): String {
      val numbers: MutableList<Int> = ArrayList()
      for (i in 0..9) {
         numbers.add(i)
      }
      numbers.shuffle()
      var result = ""
      for (i in 0..3) {
         result += numbers[i].toString()
      }
      return result
   }

}