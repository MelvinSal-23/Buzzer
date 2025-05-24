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


class BuzzerFragment : Fragment() {

    lateinit var binding: FragmentBuzzerBinding
    lateinit var code: String
    lateinit var name: String
    lateinit var database: FirebaseDatabase
    lateinit var android_id: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentBuzzerBinding.inflate(layoutInflater)
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

        database.getReference(
            "AvailableRooms"
        )
            .child(code)
            .child(android_id)
            .child("name")
            .setValue(name)
        database.getReference(
            "AvailableRooms"
        )
            .child(code)
            .child(android_id).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.childrenCount == 0L) {
                        findNavController().popBackStack()
                        Toast.makeText(
                            requireActivity(),
                            "You have been removed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })


    }

    override fun onDestroy() {
        super.onDestroy()
        database.getReference(
            "AvailableRooms"
        )
            .child(code)
            .child(android_id)
            .removeValue()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        database.getReference(
            "AvailableRooms"
        )
            .child(code)
            .child(android_id)
            .removeValue()
    }

    override fun onStop() {
        super.onStop()
        database.getReference(
            "AvailableRooms"
        )
            .child(code)
            .child(android_id)
            .removeValue()
    }

    override fun onDetach() {
        super.onDetach()
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