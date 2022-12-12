package com.example.catatanhesti.feature

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.catatanhesti.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.util.*

class InsertionActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etCategory: AutoCompleteTextView
    private lateinit var etAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var btnSaveData: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var rbExpense: RadioButton
    private lateinit var rbIncome: RadioButton
    private lateinit var etNote: EditText
    private lateinit var toolbarLinear: LinearLayout
    private var type: Int = 1
    private var amount: Double = 0.0
    private var date: Long = 0
    private var invertedDate: Long = 0

    private lateinit var dbRef: DatabaseReference //inisialisasi database
    private lateinit var auth: FirebaseAuth


    private var isSubmitted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insertion)

        //tombol kembali
        val backButton: ImageButton = findViewById(R.id.backBtn)
        backButton.setOnClickListener {
            finish()
        }


        initItem()

        //inisialisasi Firebase Auth dan firebase database
        val user = Firebase.auth.currentUser
        val uid = user?.uid
        if (uid != null) {
            dbRef = FirebaseDatabase.getInstance().getReference(uid) //inisialisasi database dengan uid
        }
        auth = Firebase.auth


        //insert kategori
        etCategory = findViewById(R.id.category)
        val listExpense =
            CategoryOptions.expenseCategory() //mendapatkan data arrayList dari file CategoryOptions
        val expenseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listExpense)
        etCategory.setAdapter(expenseAdapter)

        radioGroup.setOnCheckedChangeListener { _, checkedID ->
            etCategory.text.clear()
            if (checkedID == R.id.rbExpense) {
                type = 1
                setBackgroundColor()
                etCategory.setAdapter(expenseAdapter)
            }
            if (checkedID == R.id.rbIncome){
                type = 2
                setBackgroundColor()

                val listIncome = CategoryOptions.incomeCategory()
                val incomeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listIncome)
                etCategory.setAdapter(incomeAdapter)
            }
        }

        //insert tanggal
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val currentDate = sdf.parse(sdf.format(System.currentTimeMillis()))
        date = currentDate!!.time
        etDate.setOnClickListener {
            clickDatePicker()
        }



        //button simpan
        btnSaveData.setOnClickListener {
            if (!isSubmitted){
                saveTransactionData()
            }else{
                Snackbar.make(findViewById(android.R.id.content), "Transaksi Telah Tersimpan", Snackbar.LENGTH_LONG).show()
            }

        }
    }

    //background atas menu insert
    private fun setBackgroundColor() {
        if (type == 1){
            rbExpense.setBackgroundResource(R.drawable.radio_selected_expense)
            rbIncome.setBackgroundResource(R.drawable.radio_not_selected)
            toolbarLinear.setBackgroundResource(R.drawable.bg_insert_expense)
            btnSaveData.backgroundTintList = getColorStateList(R.color.redExpense)
            window.statusBarColor = ContextCompat.getColor(this, R.color.orangePrimary)
        }else{
            rbIncome.setBackgroundResource(R.drawable.radio_selected_income)
            rbExpense.setBackgroundResource(R.drawable.radio_not_selected)
            toolbarLinear.setBackgroundResource(R.drawable.bg_insert_income)
            btnSaveData.backgroundTintList = getColorStateList(R.color.greenIncome)
            window.statusBarColor = ContextCompat.getColor(this, R.color.orangePrimary)
        }
    }


    //fungsi insert field
    private fun initItem() {
        etTitle = findViewById(R.id.title)
        etCategory = findViewById(R.id.category)
        etAmount = findViewById(R.id.amount)
        etDate = findViewById(R.id.date)
        btnSaveData = findViewById(R.id.saveButton)
        radioGroup = findViewById(R.id.typeRadioGroup)
        rbExpense = findViewById(R.id.rbExpense)
        rbIncome = findViewById(R.id.rbIncome)
        etNote = findViewById(R.id.note)
        toolbarLinear = findViewById(R.id.toolbarLinear)
    }

    //fungsi pengambilan tanggal
    private fun clickDatePicker() {
        val myCalendar = Calendar.getInstance()
        val year = myCalendar.get(Calendar.YEAR)
        val month = myCalendar.get(Calendar.MONTH)
        val day = myCalendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->

                val selectedDate = "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"
                etDate.text = null
                etDate.hint = selectedDate

                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate = sdf.parse(selectedDate)
                date = theDate!!.time

            },
            year,
            month,
            day
        )
        dpd.show()
    }

    //kondisi saat field tidak terisi
    private fun saveTransactionData() {
        //mendapatkan values dari input user:
        val title = etTitle.text.toString()
        val category = etCategory.text.toString()
        val amountEt = etAmount.text.toString()
        val note = etNote.text.toString()

        if(amountEt.isEmpty()){
            etAmount.error = "Mohon Masukan Jumlah"
        }else if(title.isEmpty()) {
            etTitle.error = "Mohon Masukan Judul"
        }else if(category.isEmpty()){
            etCategory.error = "Mohon Masukan Kategori"
        }else{
            amount = etAmount.text.toString().toDouble() //convert ke tipe double

            val transactionID = dbRef.push().key!!
            invertedDate = date * -1
            val transaction = TransactionModel(transactionID, type, title, category, amount, date, note, invertedDate)

            dbRef.child(transactionID).setValue(transaction)
                .addOnCompleteListener {
                    Toast.makeText(this, "Data Berhasil Ditambah", Toast.LENGTH_LONG).show()
                    finish()
                }.addOnFailureListener { err ->
                    Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_LONG).show()
                }

            isSubmitted = true
        }
    }
}
