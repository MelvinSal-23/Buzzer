package com.projects.kotlinnbuzzer.fragments

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.annotations.Nullable
import com.projects.kotlinnbuzzer.R
import com.projects.kotlinnbuzzer.databinding.FragmentHomeBinding
import com.projects.kotlinnbuzzer.model.User
import androidx.activity.result.ActivityResultLauncher


class HomeFragment : Fragment() {

    lateinit var binding:FragmentHomeBinding
    lateinit var googleSignInClient:GoogleSignInClient
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var database: FirebaseDatabase
    val RC_SIGN_IN = 11

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            Log.d(ContentValues.TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(requireContext(),"Sign In Failed", Toast.LENGTH_SHORT).show()
            Log.w(ContentValues.TAG, "Google sign in failed", e)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createRequest()
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        binding.createbtn.setOnClickListener {
            if (firebaseAuth.currentUser == null){
                signIn()
            }
            else{
                findNavController().navigate(R.id.action_homeFragment_to_createRoomFragment)
            }
        }
        binding.joinbtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_joinFragment)
        }
    }
    private fun createRequest() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1063486704620-eujoo0f7ro9la3in4648fop10a426gd6.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    @Nullable
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        val usr = User(user.uid, user.displayName ?: "No Name", user.photoUrl?.toString() ?: "")
                        database.reference.child("UserProfiles")
                            .child(user.uid).setValue(usr)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    findNavController().navigate(R.id.action_homeFragment_to_createRoomFragment)
                                } else {
                                    Toast.makeText(requireContext(), "Error in database", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(requireContext(), "User is null after sign-in", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Sign-in failed", Toast.LENGTH_SHORT).show()
                    Log.w(ContentValues.TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }
}