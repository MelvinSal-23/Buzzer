package com.projects.kotlinnbuzzer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.projects.kotlinnbuzzer.databinding.FragmentBuzzBinding


class BuzzFragment : Fragment() {
   lateinit var binding: FragmentBuzzBinding
   lateinit var code: String
   lateinit var name: String
   lateinit var database: FirebaseDatabase
   lateinit var android_id: String
   lateinit var dbref: DatabaseReference
   lateinit var valuev: ValueEventListener
   var x = 0
   override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?,
   ): View? {
      binding = FragmentBuzzBinding.inflate(layoutInflater)
      return binding.root
   }

   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)


   }
}