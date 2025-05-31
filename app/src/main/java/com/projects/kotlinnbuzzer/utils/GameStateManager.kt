package com.projects.kotlinnbuzzer.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.projects.kotlinnbuzzer.model.RoomModel

class GameStateManager(context: Context) {
    private val PREF_NAME = "buzzer_game_state"
    private val KEY_ROOM_CODE = "room_code"
    private val KEY_IS_GAME_STARTED = "is_game_started"
    private val KEY_IS_HOST_PLAYING = "is_host_playing"
    private val KEY_PLAYER_SCORES = "player_scores"
    private val KEY_BUZZ_TIME = "buzz_time"
    private val KEY_PLAYER_NAME = "player_name"
    private val KEY_ANDROID_ID = "android_id"
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    fun saveGameState(
        roomCode: String,
        isGameStarted: Boolean,
        isHostPlaying: Boolean,
        playerScores: Map<String, RoomModel>,
        buzzTime: String,
        playerName: String,
        androidId: String
    ) {
        prefs.edit().apply {
            putString(KEY_ROOM_CODE, roomCode)
            putBoolean(KEY_IS_GAME_STARTED, isGameStarted)
            putBoolean(KEY_IS_HOST_PLAYING, isHostPlaying)
            putString(KEY_PLAYER_SCORES, gson.toJson(playerScores))
            putString(KEY_BUZZ_TIME, buzzTime)
            putString(KEY_PLAYER_NAME, playerName)
            putString(KEY_ANDROID_ID, androidId)
            apply()
        }
    }
    
    fun isHostPlaying(): Boolean = prefs.getBoolean(KEY_IS_HOST_PLAYING, false)

    fun getRoomCode(): String = prefs.getString(KEY_ROOM_CODE, "") ?: ""

    fun isGameStarted(): Boolean = prefs.getBoolean(KEY_IS_GAME_STARTED, false)

    fun getPlayerScores(): Map<String, RoomModel> {
        val json = prefs.getString(KEY_PLAYER_SCORES, "{}")
        val type = object : TypeToken<Map<String, RoomModel>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }

    fun getBuzzTime(): String? = prefs.getString(KEY_BUZZ_TIME, null)

    fun getPlayerName(): String = prefs.getString(KEY_PLAYER_NAME, "") ?: ""

    fun getAndroidId(): String = prefs.getString(KEY_ANDROID_ID, "") ?: ""

    fun clearGameState() {
        prefs.edit().clear().apply()
    }

    fun savePlayerScore(androidId: String, score: Int) {
        val scores = getPlayerScores().toMutableMap()
        scores[androidId]?.score = score
        prefs.edit().putString(KEY_PLAYER_SCORES, gson.toJson(scores)).apply()
    }

    fun saveBuzzTime(androidId: String, buzzTime: String) {
        val scores = getPlayerScores().toMutableMap()
        scores[androidId]?.buzzat = buzzTime
        prefs.edit()
            .putString(KEY_PLAYER_SCORES, gson.toJson(scores))
            .putString(KEY_BUZZ_TIME, buzzTime)
            .apply()
    }
}
