package com.projects.kotlinnbuzzer

import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.projects.kotlinnbuzzer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var android_id: String
    lateinit var database: FirebaseDatabase
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.mlottiez.visibility = View.GONE
//        setTheme(R.style.Kotlinnbuzzer)
        database = FirebaseDatabase.getInstance()
        android_id =
            Settings.Secure.getString(this?.getContentResolver(), Settings.Secure.ANDROID_ID)
    }

    override fun onStop() {
        super.onStop()
    }
}