package com.example.catatanhesti.account

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.catatanhesti.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.catatanhesti.databinding.ActivitySignUpBinding


class SignUp : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth //untuk autentikasi firebase
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()

        //fungsi button sign up
        binding.signupBtn.setOnClickListener {

            val email = binding.email.text.toString()
            val pass = binding.password.text.toString()
            val confirmPass = binding.passwordRetype.text.toString()

            if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()){ //proses pengecekan email sama atau tidak
                if(pass.isNotEmpty() && confirmPass.isNotEmpty()){
                    if (pass == confirmPass){
                        binding.progressBar.visibility = View.VISIBLE //menampilkan loading progres bar
                        firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                            if (it.isSuccessful){ //jika login berhasil, maka UI login menuju menu utama
                                val intent = Intent(this, MainActivity::class.java)
                                Toast.makeText(this, "Sign Up Berhasil", Toast.LENGTH_LONG).show()
                                binding.progressBar.visibility = View.GONE
                                startActivity(intent)
                            }else{ // jika gagal maka tampilkan pesan Gagal
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_LONG).show()
                            }
                        }
                    }else{
                        Toast.makeText(this, "Password tidak sesuai", Toast.LENGTH_LONG).show()
                    }
                }else{
                    Toast.makeText(this, "Email dan Password Tidak Boleh Kosong", Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(this, "Email dan Password Tidak Boleh Kosong", Toast.LENGTH_LONG).show()
            }
        }

        binding.haveAccount.setOnClickListener {
            finish()
        }

    }
}