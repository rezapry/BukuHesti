package com.example.catatanhesti.account

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.catatanhesti.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.catatanhesti.databinding.ActivityLoginBinding


class Login : AppCompatActivity() {

    private var auth: FirebaseAuth = Firebase.auth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.createAccount.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
        }

        emailLogin()

        //Login dengan akun Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("736955148323-64bd50m7kd0gbc7eqmbrqum6bd3h2t53.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googleSignInBtn.setOnClickListener {
            googleSignInClient.signOut()
            signIn()
        }

    }


    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Hasil yang dikembalikan dari peluncuran Intent dari GoogleSignInAPI.getSignInIntent
        if (requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                //login google berhasil, autentikasi dengan firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(ContentValues.TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException){
                // Masuk dengan Google Gagal
                Log.w(ContentValues.TAG, "Google Login Gagal!", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String){
        binding.progressBar.visibility = View.VISIBLE
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful){
                    //Sign in Berhasil
                    Log.d(ContentValues.TAG, "signIn dengan Credential:Berhasil")
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    Toast.makeText(this,"Login Berhasil", Toast.LENGTH_LONG).show()
                    binding.progressBar.visibility = View.GONE
                    startActivity(intent)
                }else{
                    //Sign in gagal
                    Log.w(ContentValues.TAG, "signIn dengan Credential:Gagal", task.exception)
                    Toast.makeText(this, ""+task.exception, Toast.LENGTH_LONG).show()
                }
            }
    }

    companion object{
        const val RC_SIGN_IN = 1001
    }

    //fungsi button login
    private fun emailLogin() {
        binding.loginBtn.setOnClickListener {

            val email = binding.email.text.toString()
            val pass = binding.password.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()){
                binding.progressBar.visibility = View.VISIBLE
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful){ //jika login berhasil, maka UI login menuju menu utama
                        val intent = Intent(this, MainActivity::class.java)
                        Toast.makeText(this,"Login Berhasil", Toast.LENGTH_LONG).show()
                        binding.progressBar.visibility = View.GONE
                        startActivity(intent)
                    }else{
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this,"Maaf, Login Gagal!", Toast.LENGTH_LONG).show()
                    }
                }
            }else{
                Toast.makeText(this, "Email dan Password Tidak Boleh Kosong", Toast.LENGTH_LONG).show()
            }

        }
    }

    //jika pengguna sudah Berhasil Login, maka tidak bisa kembali ke UI Login
    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null){
            Intent(this, MainActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it)
            }
        }
    }

}