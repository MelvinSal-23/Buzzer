package com.projects.kotlinnbuzzer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.projects.kotlinnbuzzer.adapter.PlayAdapter
import com.projects.kotlinnbuzzer.databinding.ActivityPlayBinding
import com.projects.kotlinnbuzzer.model.RoomModel

class PlayActivity : AppCompatActivity() {
    lateinit var binding: ActivityPlayBinding
    lateinit var database: DatabaseReference
    lateinit var valuev: ValueEventListener
    lateinit var madapter: PlayAdapter
    var code = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_play)
        binding.mlottiez2.visibility = View.GONE
        madapter = PlayAdapter(this)
        binding.playrecview.adapter = madapter
        binding.playrecview.layoutManager = LinearLayoutManager(this)
        val bundle = intent.extras
        if (bundle != null) {
            code = bundle.getString("code", "")
        }
        FirebaseDatabase.getInstance().getReference("AvailableRooms")
            .child("isStarted")
            .setValue(false)
        binding.roomcode1.text = "Room Code: ${code}"
        FirebaseDatabase.getInstance().getReference("AvailableRooms").child(code)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    madapter.clearList()
                    if (snapshot != null) {
                        if (snapshot.childrenCount == 2L) {
                            finish()
                        }
                        for (data in snapshot.children) {
                            if (data.child("name").value != null) {
                                val androidid = data.key
                                val name = data.child("name").value
                                var buzat = data.child("buzzat").value
                                if (buzat == "" || buzat == null) {
                                    buzat = "0"
                                }
                                madapter.add(
                                    RoomModel(
                                        androidid.toString(),
                                        name.toString(),
                                        code,
                                        buzat.toString()
                                    )
                                )
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        binding.startbtn.setOnClickListener {
            FirebaseDatabase.getInstance().getReference("AvailableRooms")
                .child(code)
                .child("isStarted")
                .setValue(true)
                .addOnSuccessListener {

                    Toast.makeText(this@PlayActivity, "Started", Toast.LENGTH_SHORT).show()
                    binding.startbtn.isClickable = false
                    binding.startbtn.isActivated = false
                    binding.startbtn.setBackgroundColor(Color.GRAY)

                }
                .addOnFailureListener {
                    Toast.makeText(this, "Unable to start ${it.message}", Toast.LENGTH_SHORT).show()
                }


        }
        binding.endbtn.setOnClickListener {
            FirebaseDatabase.getInstance().getReference("AvailableRooms").child(code).removeValue()
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
        binding.resetbtn.setOnClickListener {
            var x = 0
            FirebaseDatabase.getInstance().getReference("AvailableRooms")
                .child(code)
                .child("isStarted")
                .setValue(false)
                .addOnSuccessListener {
                    Toast.makeText(this, "Reset Successful", Toast.LENGTH_SHORT).show()
                    binding.startbtn.isClickable = true
                    binding.startbtn.isActivated = true
                    binding.startbtn.setBackgroundColor(getResources().getColor(R.color.purple_500))
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Unable to reset ${it.message}", Toast.LENGTH_SHORT).show()
                }
            FirebaseDatabase.getInstance().getReference("AvailableRooms")
                .child(code)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot != null) {
                            for (data in snapshot.children) {
                                if (data.child("name").value != null && x == 0) {
                                    data.ref.child("buzzat").setValue("0")
                                }
                            }
                            x = 1
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }

    }

    override fun onStop() {
        super.onStop()
        FirebaseDatabase.getInstance().getReference("AvailableRooms")
            .child(code)
            .child("isStarted")
            .setValue(false)
    }

    override fun onPause() {
        super.onPause()
        FirebaseDatabase.getInstance().getReference("AvailableRooms")
            .child(code)
            .child("isStarted")
            .setValue(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseDatabase.getInstance().getReference("AvailableRooms")
            .child(code)
            .child("isStarted")
            .setValue(false)
    }
}