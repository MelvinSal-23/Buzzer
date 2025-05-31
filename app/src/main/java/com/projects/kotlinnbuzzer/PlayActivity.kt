package com.projects.kotlinnbuzzer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.projects.kotlinnbuzzer.adapter.PlayAdapter
import com.projects.kotlinnbuzzer.databinding.ActivityPlayBinding
import com.projects.kotlinnbuzzer.model.RoomModel
import com.projects.kotlinnbuzzer.utils.GameStateManager

class PlayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayBinding
    private lateinit var code: String
    private lateinit var madapter: PlayAdapter
    private lateinit var gameStateManager: GameStateManager
    private var currentDialogPlayerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_play)

        // Initialize managers
        gameStateManager = GameStateManager(this)

        // Setup RecyclerView
        madapter = PlayAdapter(this)
        binding.playrecview.adapter = madapter
        binding.playrecview.layoutManager = LinearLayoutManager(this)

        // Get or restore room code
        code = gameStateManager.getRoomCode().ifEmpty {
            intent.extras?.getString("code", "") ?: ""
        }

        // Restore adapter state if available
        val savedScores = gameStateManager.getPlayerScores()
        if (savedScores.isNotEmpty()) {
            savedScores.values.forEach { player ->
                madapter.add(player)
            }
        }

        // Start listening for game state changes
        setupFirebaseListener()
        setupClickListeners()
    }

    private fun setupFirebaseListener() {
        FirebaseDatabase.getInstance().getReference("AvailableRooms").child(code)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.childrenCount == 2L) {
                        finish()
                        return
                    }

                    // Keep existing scores to prevent flicker
                    val existingPlayers = madapter.detailsofQuiz.associateBy { it.androidid }

                    // Track active buzzers
                    var activeBuzzer: RoomModel? = null
                    var activeBuzzTime = Float.MAX_VALUE

                    // Create new list of players
                    val updatedPlayers = mutableListOf<RoomModel>()

                    // Process all players
                    val currentScores = mutableMapOf<String, RoomModel>()
                    for (data in snapshot.children) {
                        if (data.child("name").value == null) continue

                        val androidId = data.key ?: continue
                        val name = data.child("name").value?.toString() ?: continue
                        val buzzat = data.child("buzzat").value?.toString()
                        val score = data.child("score").value?.toString()?.toIntOrNull() ?: 0
                        val buzzTime = buzzat?.toFloatOrNull()
                            ?: 0f                        // Use existing player data if available to preserve score
                        val existingPlayer = existingPlayers[androidId]
                        val finalScore = existingPlayer?.score ?: score

                        val player = RoomModel(androidId, name, code, buzzat ?: "0", finalScore)
                        updatedPlayers.add(player)
                        currentScores[androidId] = player

                        // If player has buzzed (non-zero buzz time), check if they're the earliest buzzer
                        if (buzzTime > 0) {
                            if (buzzTime < activeBuzzTime) {
                                activeBuzzTime = buzzTime
                                activeBuzzer = player
                            }
                        }
                    }                    // Update adapter with all players at once to prevent flicker
                    madapter.clearList()
                    updatedPlayers.forEach { player ->
                        madapter.add(player)
                    }

                    // Show score dialog for active buzzer
                    activeBuzzer?.let { showScoreDialog(it) }

                    // Save current state
                    gameStateManager.saveGameState(
                        code,
                        true,
                        true, // Always in playing state
                        currentScores,
                        activeBuzzTime.toString(),
                        activeBuzzer?.name.orEmpty(),
                        activeBuzzer?.androidid.orEmpty()
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@PlayActivity, "Error: ${error.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun setupClickListeners() {
        binding.randombtn.setOnClickListener {
            if (madapter.getCount() == 0) {
                Toast.makeText(this, "No players available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val randomPlayer = madapter.detailsofQuiz.random()
            showScoreDialog(randomPlayer)
        }

        binding.endbtn.setOnClickListener {
            AlertDialog.Builder(this).setTitle("End Game")
                .setMessage("Do you want to end the game?").setPositiveButton("OK") { _, _ ->
                    FirebaseDatabase.getInstance().getReference("AvailableRooms").child(code)
                        .removeValue()
                    val i = Intent(this, MainActivity::class.java)
                    startActivity(i)
                    finish()
                }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }

        binding.resetbtn.setOnClickListener {
            AlertDialog.Builder(this).setTitle("Reset")
                .setMessage("Do you want to reset?\nAll scores will be 0")
                .setPositiveButton("OK") { _, _ ->
                    FirebaseDatabase.getInstance().getReference("AvailableRooms").child(code).get()
                        .addOnSuccessListener { snapshot ->
                            snapshot.children.forEach { data ->
                                if (data.child("name").exists()) {
                                    data.ref.child("buzzat").setValue("0")
                                    data.ref.child("score").setValue(0)
                                }
                            }
                            Toast.makeText(this, "Reset Successful", Toast.LENGTH_SHORT).show()
                        }
                }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }

        // Set up recycler view item click listener
        madapter.setOnItemClickListener { player ->
            showScoreDialog(player)
        }
    }

    private fun showScoreDialog(player: RoomModel) {
        // Prevent showing dialog for same player
        if (currentDialogPlayerId == player.androidid) {
            return
        }
        // Update current player being scored
        currentDialogPlayerId = player.androidid

        val dialogView = layoutInflater.inflate(R.layout.dialog_score, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).setTitle("Update Score")
            .setPositiveButton("Update", null)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }.create()

        // Get dialog views
        val playerNameText = dialogView.findViewById<TextView>(R.id.player_name)
        val scoreInput = dialogView.findViewById<EditText>(R.id.score_input)
        val btnPlus5 = dialogView.findViewById<Button>(R.id.btn_plus_5)
        val btnPlus10 = dialogView.findViewById<Button>(R.id.btn_plus_10)
        val btnPlus15 = dialogView.findViewById<Button>(R.id.btn_plus_15)
        val btnMinus5 = dialogView.findViewById<Button>(R.id.btn_minus_5)
        val btnMinus10 = dialogView.findViewById<Button>(R.id.btn_minus_10)
        val btnMinus15 = dialogView.findViewById<Button>(R.id.btn_minus_15)

        // Set initial values
        playerNameText.text = player.name
        scoreInput.setText(player.score.toString())

        // Quick score buttons
        btnPlus5.setOnClickListener { updateScoreInput(scoreInput, 5) }
        btnPlus10.setOnClickListener { updateScoreInput(scoreInput, 10) }
        btnPlus15.setOnClickListener { updateScoreInput(scoreInput, 15) }
        btnMinus5.setOnClickListener { updateScoreInput(scoreInput, -5) }
        btnMinus10.setOnClickListener { updateScoreInput(scoreInput, -10) }
        btnMinus15.setOnClickListener { updateScoreInput(scoreInput, -15) }

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val newScore = scoreInput.text.toString().toIntOrNull()
                if (newScore != null) {
                    updatePlayerScore(player, newScore)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Please enter a valid score", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun updateScoreInput(scoreInput: EditText, change: Int) {
        val currentScore = scoreInput.text.toString().toIntOrNull() ?: 0
        scoreInput.setText((currentScore + change).toString())
    }

    private fun updatePlayerScore(player: RoomModel, newScore: Int) {
        val playerRef = FirebaseDatabase.getInstance().getReference("AvailableRooms")
            .child(code)
            .child(player.androidid)

        // Update both score and reset buzz time in a single update
        val updates = mapOf(
            "score" to newScore,
            "buzzat" to "0"  // Reset buzz time to allow player to buzz again
        )

        // Update adapter immediately for instant feedback
        val updatedPlayer = player.copy(score = newScore)
        madapter.updatePlayer(updatedPlayer)

        playerRef.updateChildren(updates).addOnSuccessListener {
            Toast.makeText(this, "Score updated successfully", Toast.LENGTH_SHORT).show()
            // Save score in local state
            gameStateManager.savePlayerScore(player.androidid, newScore)
            // Reset current dialog player to allow new dialogs
            currentDialogPlayerId = null
        }.addOnFailureListener {
            // Revert the score on failure
            madapter.updatePlayer(player)
            Toast.makeText(this, "Failed to update score", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Current state is already saved in GameStateManager
    }

    override fun onPause() {
        super.onPause()
        // Save current state
        gameStateManager.saveGameState(
            code,
            true,
            true, // Always in playing state
            madapter.detailsofQuiz.associateBy { it.androidid },
            "0",
            madapter.detailsofQuiz.firstOrNull()?.name ?: "",
            madapter.detailsofQuiz.firstOrNull()?.androidid ?: ""
        )
    }

    override fun onResume() {
        super.onResume()
        // Restore state if needed
        if (madapter.getCount() == 0) {
            val savedScores = gameStateManager.getPlayerScores()
            savedScores.values.forEach { player ->
                madapter.add(player)
            }
        }
    }
}