package com.example.catatanhesti

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.catatanhesti.databinding.ActivityMainBinding
import com.example.catatanhesti.feature.InsertionActivity
import com.example.catatanhesti.fragments.AccountFragment
import com.example.catatanhesti.fragments.StatisticFragment
import com.example.catatanhesti.fragments.TransactionFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //tombol navigasi
        val transactionFragment = TransactionFragment()
        val statisticFragment = StatisticFragment()
        val accountFragment = AccountFragment()

        binding.chipAppBar.setItemSelected(R.id.ic_transaction,true)
        makeCurrentFragment(transactionFragment)
        binding.chipAppBar.setOnItemSelectedListener {
            when (it){
                R.id.ic_transaction -> makeCurrentFragment(transactionFragment)
                R.id.ic_static -> makeCurrentFragment(statisticFragment)
                R.id.ic_account -> makeCurrentFragment(accountFragment)
            }
            val b = true
            b
        }

    }

    private fun makeCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
    }

    fun floating_button(view: View){
        val intent = Intent(this, InsertionActivity::class.java)
        startActivity(intent)
    }
}