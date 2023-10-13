package com.example.firebaseauthrec

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etRegisterEmail: EditText
    private lateinit var etRegisterPassword: EditText
    private lateinit var etLoginEmail: EditText
    private lateinit var etLoginPassword: EditText
    private lateinit var etUpdateUsername: EditText
    private lateinit var ivImage: ImageView
    private lateinit var tvDisplay: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()
        val btnRegisterUser = findViewById<Button>(R.id.btnRegister)
        val btnLoginUser = findViewById<Button>(R.id.btnLogin)
        val btnUpdateProfile = findViewById<Button>(R.id.btnUpdateProfile)
        val btnSignOut = findViewById<Button>(R.id.btnSignOut)

        etRegisterEmail = findViewById(R.id.etEmail)
        etRegisterPassword = findViewById(R.id.etPassword)
        etLoginEmail = findViewById(R.id.etEmailLogin)
        etLoginPassword = findViewById(R.id.etPasswordLogin)
        etUpdateUsername = findViewById(R.id.etUpdateProfile)
        ivImage = findViewById(R.id.ivImage)
        tvDisplay = findViewById(R.id.tvLoginMessage)

        btnRegisterUser.setOnClickListener {
            if (etRegisterEmail.text.toString().equals("") || etRegisterPassword.text.toString()
                    .equals("")
            ) {
                Toast.makeText(
                    this@MainActivity,
                    "Please provide a valid input",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val email = etRegisterEmail.text.toString()
                val password = etRegisterPassword.text.toString()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        auth.createUserWithEmailAndPassword(email, password).await()
                        withContext(Dispatchers.Main){
                            etRegisterEmail.text.clear()
                            etRegisterPassword.text.clear()
                            checkLogginState()
                        }



                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }

            }

        }
        btnLoginUser.setOnClickListener {
            val email = etLoginEmail.text.toString()
            val password = etLoginPassword.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        auth.signInWithEmailAndPassword(email, password).await()
                        withContext(Dispatchers.Main){
                            checkLogginState()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Plese provide a valid input", Toast.LENGTH_SHORT).show()
            }

        }
        btnUpdateProfile.setOnClickListener {
            auth.currentUser?.let { user ->
                val displayName = etUpdateUsername.text.toString()
                val imageUrl = Uri.parse("android.resource://$packageName/${R.drawable.arthas}")
                if(displayName.isNotEmpty()){
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .setPhotoUri(imageUrl)
                    .build()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        user.updateProfile(profileUpdates).await()

                        withContext(Dispatchers.Main) {
                            checkLogginState()
                            Toast.makeText(
                                this@MainActivity,
                                "You updated your profile",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                        }
                    }

                }
            }}

        }
        btnSignOut.setOnClickListener {
            auth.signOut()
            etLoginEmail.text.clear()
            etLoginPassword.text.clear()
            etUpdateUsername.text.clear()
            ivImage.setImageResource(0)
            checkLogginState()

        }

    }

    override fun onStop() {
        super.onStop()
        auth.signOut()
        etLoginEmail.text.clear()
        etLoginPassword.text.clear()
        etUpdateUsername.text.clear()
        ivImage.setImageResource(0)
        checkLogginState()

    }

    private fun checkLogginState() {
        val user = auth.currentUser
        if (user == null) {
            tvDisplay.setText("You are not logged in")
        } else {
            tvDisplay.setText("You are logged in")
            etUpdateUsername.setText(user.displayName)
            ivImage.setImageURI(user.photoUrl)
        }
    }
}