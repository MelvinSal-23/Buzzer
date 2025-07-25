package com.projects.kotlinnbuzzer

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.projects.kotlinnbuzzer.databinding.ActivityBuzzerBinding
import com.projects.kotlinnbuzzer.utils.GameStateManager


class BuzzerActivity : AppCompatActivity() {
   private lateinit var binding: ActivityBuzzerBinding
   private lateinit var code: String
   private lateinit var name: String
   private lateinit var database: FirebaseDatabase
   private lateinit var android_id: String
   private lateinit var dbref: DatabaseReference
   private var valuev: ValueEventListener? = null
   private lateinit var mediaPlayer: MediaPlayer
   private lateinit var gameStateManager: GameStateManager
   private var isHostPlaying = false

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      binding = DataBindingUtil.setContentView(this, R.layout.activity_buzzer)
      binding.mlottiez1.visibility = View.GONE

      gameStateManager = GameStateManager(this)

      code = gameStateManager.getRoomCode().ifEmpty {
         intent.extras?.getString("code", "") ?: ""
      }
      name = gameStateManager.getPlayerName().ifEmpty {
         intent.extras?.getString("name", "") ?: ""
      }
      android_id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

      database = FirebaseDatabase.getInstance()
      setupFirebaseListeners()

      binding.buzzerbtn.setOnClickListener {
         clicked()
         handleBuzz()
      }
   }

   private fun setupFirebaseListeners() {
      database.getReference("AvailableRooms")
         .child(code)
         .child("isHostPlaying")
         .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
               isHostPlaying = snapshot.getValue(Boolean::class.java) ?: false
               updateBuzzerState()
            }

            override fun onCancelled(error: DatabaseError) {
            }
         })

      dbref = database.getReference("AvailableRooms")
         .child(code)
         .child(android_id)

      valuev = dbref.addValueEventListener(object : ValueEventListener {
         override fun onDataChange(snapshot: DataSnapshot) {
            if (!snapshot.exists()) {
               Toast.makeText(this@BuzzerActivity, "You have been removed", Toast.LENGTH_SHORT)
                  .show()
               gameStateManager.clearGameState()
               finish()
               return
            }

            val score = snapshot.child("score").value?.toString()?.toIntOrNull() ?: 0
            gameStateManager.savePlayerScore(android_id, score)

            binding.buzzScore.text = "Score: $score"
         }

         override fun onCancelled(error: DatabaseError) {
         }
      })

      val scores = gameStateManager.getPlayerScores()
      val playerState = scores[android_id]

      dbref.updateChildren(
         mapOf(
            "name" to name,
            "buzzat" to "0",
            "score" to (playerState?.score ?: 0)
         )
      )
   }

   private fun handleBuzz() {
      if (isHostPlaying) {
         val currentTime = System.currentTimeMillis()
         dbref.child("buzzat")
            .setValue(currentTime.toString())
            .addOnFailureListener {
               Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
      }
   }

   private fun updateBuzzerState() {
      if (isHostPlaying) {
         binding.buzzerbtn.setCardBackgroundColor(Color.RED)
         binding.buzzerbtn.isClickable = true
      } else {
         binding.buzzerbtn.setCardBackgroundColor(Color.GRAY)
         binding.buzzerbtn.isClickable = false
      }
   }

   override fun onPause() {
      super.onPause()
      gameStateManager.saveGameState(
         code,
         true,
         isHostPlaying,
         gameStateManager.getPlayerScores(),
         "0",
         name,
         android_id
      )
   }

   override fun onResume() {
      super.onResume()
      code = gameStateManager.getRoomCode()
      name = gameStateManager.getPlayerName()
      updateBuzzerState()
   }

   override fun onDestroy() {
      super.onDestroy()
      valuev?.let { dbref.removeEventListener(it) }
      if (!isFinishing && ::mediaPlayer.isInitialized) {
         mediaPlayer.release()
      }
   }

   private fun clicked() {
      mediaPlayer = MediaPlayer.create(applicationContext, R.raw.click)
      mediaPlayer.start()
   }
}