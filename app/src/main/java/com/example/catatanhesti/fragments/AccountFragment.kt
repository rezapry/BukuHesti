package com.example.catatanhesti.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.example.catatanhesti.R
import com.example.catatanhesti.feature.TransactionModel
import com.example.catatanhesti.account.Login
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AccountFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    // inisialisasi Firebase Auth dan database
    private var auth: FirebaseAuth = Firebase.auth
    private var user = Firebase.auth.currentUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logout()  //tombol logout

        accountDetails() //Output detail akun dari firebase

        Handler().postDelayed({ //untuk setup PieChart() dan menampilkan semua TimeRecap(), dll

        }, 200)

        swipeRefresh()
    }

    //refresh
    private fun swipeRefresh() {
        val swipeRefreshLayout: SwipeRefreshLayout = requireView().findViewById(R.id.swipeRefresh)
        swipeRefreshLayout.setOnRefreshListener {
            accountDetails()
            swipeRefreshLayout.isRefreshing = false
        }
    }


    //menu top bar
    private fun accountDetails() {
        val tvName: TextView = requireView().findViewById(R.id.tvName)
        val tvEmail: TextView = requireView().findViewById(R.id.tvEmail)
        val tvPicture: TextView = requireView().findViewById(R.id.picture)
        val verified: CardView = requireView().findViewById(R.id.verified)
        val notVerified: CardView = requireView().findViewById(R.id.notVerified)

        user?.reload()
        user?.let {

            val userName = user!!.displayName
            val email = user!!.email

            if (user!!.isEmailVerified){
                verified.visibility = View.VISIBLE
                notVerified.visibility = View.GONE

                verified.setOnClickListener {
                    Toast.makeText(this@AccountFragment.activity, "Akun telah terverifikasi!", Toast.LENGTH_LONG).show()
                }
            }else{
                notVerified.visibility = View.VISIBLE
                verified.visibility = View.GONE

                notVerified.setOnClickListener {
                    user?.sendEmailVerification()?.addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(this@AccountFragment.activity, "Periksa Email Masuk!", Toast.LENGTH_LONG).show()
                        }else{
                            Toast.makeText(this@AccountFragment.activity, "${it.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

            val splitValue = email?.split("@") //
            val name = splitValue?.get(0)
            tvName.text = name.toString()
            tvPicture.text = name?.get(0).toString().uppercase()
            tvEmail.text = email.toString()

            if (userName != null) {
                tvName.text = userName.toString()
                tvPicture.text = userName[0].toString().uppercase()
            }

        }
    }

    //fungsi tombol logut
    private fun logout() {
        val btnLogout: ImageButton = requireView().findViewById(R.id.btnLogout)
        btnLogout.setOnClickListener {
            auth.signOut()
            Intent(this.activity, Login::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                activity?.startActivity(it)
            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AccountFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
