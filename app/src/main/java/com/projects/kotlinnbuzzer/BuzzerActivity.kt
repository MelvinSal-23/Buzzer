package com.projects.kotlinnbuzzer

import android.graphics.Color
import android.media.MediaPlayer
import android.os.*
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.projects.kotlinnbuzzer.databinding.ActivityBuzzerBinding


class BuzzerActivity : AppCompatActivity() {
    lateinit var binding: ActivityBuzzerBinding
    lateinit var code: String
    lateinit var name: String
    lateinit var database: FirebaseDatabase
    lateinit var android_id: String
    lateinit var dbref: DatabaseReference
    lateinit var valuev: ValueEventListener
    var timer: CountDownTimer? = null
    private lateinit var mediaPlayer: MediaPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_buzzer)
        binding.mlottiez1.visibility = View.GONE
        var bundle = intent.extras
        if (bundle != null) {
            code = bundle.getString("code", "")
            name = bundle.getString("name", "")
        }
        database = FirebaseDatabase.getInstance()
        android_id =
            Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID)

        database.getReference(
            "AvailableRooms"
        )
            .child(code)
            .child(android_id)
            .child("name")
            .setValue(name)
            .addOnSuccessListener {
                database.getReference(
                    "AvailableRooms"
                )
                    .child(code)
                    .child(android_id)
                    .child("buzzat")
                    .setValue("0")
            }

        dbref = database.getReference(
            "AvailableRooms"
        )
            .child(code)
            .child(android_id)
        valuev = dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.childrenCount == 0L) {
                    Toast.makeText(this@BuzzerActivity, "You have been removed", Toast.LENGTH_SHORT)
                        .show()
                    dbref.removeEventListener(valuev)
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
        database.getReference("AvailableRooms").child(code).child("isStarted")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot != null) {
                        if (snapshot.value == true) {
                            disabled()
//                        Toast.makeText(this@BuzzerActivity,"Button is Enabled",Toast.LENGTH_SHORT).show()
                            binding.buzzerbtn.setCardBackgroundColor(Color.RED)
                            binding.buzzerbtn.isClickable = true

                            timer = object : CountDownTimer(60000, 50) {
                                override fun onTick(millisUntilFinished: Long) {
                                    binding.timertxt.setText("Timer: Started")
                                    binding.buzzerbtn.setOnClickListener {

                                        clicked()
                                        database.getReference(
                                            "AvailableRooms"
                                        )
                                            .child(code)
                                            .child(android_id)
                                            .child("buzzat")
                                            .setValue("${millisUntilFinished}")
                                            .addOnSuccessListener {
                                                cancel()
                                                binding.timertxt.setText("Timer: Sent")
                                                binding.buzzerbtn.setCardBackgroundColor(Color.GRAY)
                                                binding.buzzerbtn.isClickable = false
                                            }
                                            .addOnFailureListener {
                                                cancel()
                                                Toast.makeText(
                                                    this@BuzzerActivity,
                                                    "Error: ${it.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }

                                }

                                override fun onFinish() {

                                    database.getReference(
                                        "AvailableRooms"
                                    )
                                        .child(code)
                                        .child(android_id)
                                        .child("buzzat")
                                        .setValue("${60000L / 1000L}")
                                        .addOnSuccessListener {
                                            cancel()
                                            binding.timertxt.setText("Timer: Sent")
                                            binding.buzzerbtn.setCardBackgroundColor(Color.GRAY)
                                            disabled()
                                            binding.buzzerbtn.isClickable = false
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                this@BuzzerActivity,
                                                "Error: ${it.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            binding.buzzerbtn.setCardBackgroundColor(Color.GRAY)
                                            binding.buzzerbtn.isClickable = false
                                        }
                                }
                            }.start()
                        } else {
                            disabled()
                            binding.timertxt.setText("Timer: Disabled")
                            binding.buzzerbtn.setCardBackgroundColor(Color.GRAY)
                            binding.buzzerbtn.isClickable = false
                            timer?.cancel()
                        }
                    } else {
                        disabled()
                        binding.timertxt.setText("Timer: Disabled")

                        binding.buzzerbtn.setCardBackgroundColor(Color.GRAY)
                        binding.buzzerbtn.isClickable = false
                        timer?.cancel()

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@BuzzerActivity,
                        "Some error Occurred: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
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
        binding.buzzerbtn.setCardBackgroundColor(Color.GRAY)
        binding.buzzerbtn.isClickable = false
        timer?.cancel()


    }

    override fun onStop() {
        super.onStop()
        database.getReference(
            "AvailableRooms"
        )
            .child(code)
            .child(android_id)
            .removeValue()
        binding.buzzerbtn.setCardBackgroundColor(Color.GRAY)
        binding.buzzerbtn.isClickable = false
        timer?.cancel()

    }

    private fun disabled() {
        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.stop)
        mediaPlayer.start()
    }

    private fun clicked() {
        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.click)
        mediaPlayer.start()
    }

}