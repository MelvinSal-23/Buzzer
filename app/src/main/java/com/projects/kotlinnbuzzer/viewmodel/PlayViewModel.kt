package com.projects.kotlinnbuzzer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.projects.kotlinnbuzzer.model.RoomModel

class PlayViewModel : ViewModel() {
    private val _gameState = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> get() = _gameState

    private var currentValueEventListener: ValueEventListener? = null

    fun startListeningToGameState(roomCode: String) {
        // Remove any existing listener to prevent duplicates
        stopListeningToGameState()

        val roomRef = FirebaseDatabase.getInstance().getReference("AvailableRooms").child(roomCode)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val players = mutableListOf<RoomModel>()
                var isGameStarted = false

                // Extract game state
                snapshot.children.forEach { child ->
                    if (child.key == "isStarted") {
                        isGameStarted = child.getValue(Boolean::class.java) ?: false
                    } else if (child.child("name").exists()) {
                        val androidId = child.key ?: return@forEach
                        val name = child.child("name").value?.toString() ?: return@forEach
                        val buzzat = child.child("buzzat").value?.toString()
                        val score = child.child("score").value?.toString()?.toIntOrNull() ?: 0
                        
                        players.add(RoomModel(androidId, name, roomCode, buzzat ?: "0", score))
                    }
                }

                // Sort players by buzz time
                players.sortBy { it.buzzat?.toFloatOrNull() ?: 60000f }

                _gameState.postValue(GameState(
                    isStarted = isGameStarted,
                    players = players,
                    firstBuzzer = players.firstOrNull { it.buzzat?.toFloatOrNull() ?: 0f > 0 }
                ))
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }

        currentValueEventListener = listener
        roomRef.addValueEventListener(listener)
    }

    fun stopListeningToGameState() {
        currentValueEventListener?.let { listener ->
            FirebaseDatabase.getInstance().reference.removeEventListener(listener)
        }
        currentValueEventListener = null
    }

    override fun onCleared() {
        super.onCleared()
        stopListeningToGameState()
    }
}

data class GameState(
    val isStarted: Boolean = false,
    val players: List<RoomModel> = emptyList(),
    val firstBuzzer: RoomModel? = null
)
