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

class JoinFragment : Fragment() {

    lateinit var binding:FragmentJoinBinding
    lateinit var name:String
    lateinit var code:String
    lateinit var dbref: DatabaseReference
    lateinit var valuev:ValueEventListener
    var x=1
    var android_id=""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentJoinBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        name=""
        code=""
        android_id = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID)

        dbref =  FirebaseDatabase.getInstance().getReference("AvailableRooms")
        valuev = dbref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        binding.joinbtn.setOnClickListener {
            name = binding.nameEditText.text.toString()
            code = binding.RoomCodeEditText.text.toString()
            hideKeybord()
            if(name.isNotEmpty() and code.isNotEmpty()){
               valuev= dbref
                    .addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot!=null){
                                for (codes in snapshot.children){
                                    if(codes.key == code){
                                        val i = Intent(requireActivity(), BuzzerActivity::class.java)
                                        i.putExtra("code",code)
                                        i.putExtra("name",name)
                                        startActivity(i)

                                        x=0
                                    }
                                }
                                if (x == 1){
                                    Snackbar.make(view,"No Matching Room Available",Snackbar.LENGTH_SHORT).show()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
            }
            else{
                Snackbar.make(view,"Please fill all details",Snackbar.LENGTH_SHORT).show()

            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (valuev != null && dbref != null) {
            dbref.removeEventListener(valuev)
        }
    }

    override fun onPause() {
        super.onPause()
        if (valuev != null && dbref != null) {
            dbref.removeEventListener(valuev)
        }
    }

    override fun onStop() {
        super.onStop()
        if (valuev != null && dbref != null) {
            dbref.removeEventListener(valuev)
        }
    }
    fun hideKeybord() {
        val view = requireActivity().currentFocus
        if (view != null) {
            val hideKey = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideKey.hideSoftInputFromWindow(view.windowToken, 0)
        } else {
            requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        }
    }
}