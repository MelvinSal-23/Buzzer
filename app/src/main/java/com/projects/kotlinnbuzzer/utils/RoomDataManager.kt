package com.projects.kotlinnbuzzer.utils

import android.content.Context
import android.content.SharedPreferences

class RoomDataManager(context: Context) {
    private val PREF_NAME = "buzzer_room_data"
    private val PREF_ROOM_CODE = "room_code"
    private val PREF_USER_NAME = "user_name"
    private val PREF_IS_IN_ROOM = "is_in_room"
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    fun saveRoomData(roomCode: String, userName: String) {
        prefs.edit().apply {
            putString(PREF_ROOM_CODE, roomCode)
            putString(PREF_USER_NAME, userName)
            putBoolean(PREF_IS_IN_ROOM, true)
            apply()
        }
    }
    
    fun clearRoomData() {
        prefs.edit().apply {
            remove(PREF_ROOM_CODE)
            remove(PREF_USER_NAME)
            putBoolean(PREF_IS_IN_ROOM, false)
            apply()
        }
    }
    
    fun isInRoom(): Boolean = prefs.getBoolean(PREF_IS_IN_ROOM, false)
    
    fun getRoomCode(): String = prefs.getString(PREF_ROOM_CODE, "") ?: ""
    
    fun getUserName(): String = prefs.getString(PREF_USER_NAME, "") ?: ""
}
