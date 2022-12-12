package com.example.catatanhesti.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.example.catatanhesti.R
import com.example.catatanhesti.feature.TransactionAdapter
import com.example.catatanhesti.feature.TransactionDetails
import com.example.catatanhesti.feature.TransactionModel
import java.util.*
import kotlin.collections.ArrayList

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class TransactionFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var transactionRecyclerView: RecyclerView
    private lateinit var tvNoData: TextView
    private lateinit var noDataImage: ImageView
    private lateinit var tvNoDataTitle: TextView
    private lateinit var tvVisibilityNoData: TextView
    private lateinit var shimmerLoading: ShimmerFrameLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var transactionList: ArrayList<TransactionModel>
    private lateinit var dbRef: DatabaseReference
    private val user = Firebase.auth.currentUser
    private lateinit var typeOption: Spinner
    private lateinit var timeSpanOption: Spinner
    private var selectedType: String = "Semua Tipe" //otomatis ke semua tipe
    private var selectedTimeSpan: String = "Semua Waktu" //otomatis ke semua waktu
    var dateStart: Long = 0
    var dateEnd: Long = 0

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

        return inflater.inflate(R.layout.fragment_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeItems()

        showUserName()

        visibilityOptions()


        transactionRecyclerView = view.findViewById(R.id.rvTransaction)
        transactionRecyclerView.layoutManager = LinearLayoutManager(this.activity)
        transactionRecyclerView.setHasFixedSize(true)

        transactionList = arrayListOf<TransactionModel>()

        getTransactionData()

        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh)
        swipeRefreshLayout.setOnRefreshListener {
            getTransactionData()
            swipeRefreshLayout.isRefreshing = false
        }

    }



    private fun initializeItems() {
        tvNoData = requireView().findViewById(R.id.tvNoData)
        noDataImage = requireView().findViewById(R.id.noDataImage)
        tvNoDataTitle = requireView().findViewById(R.id.tvNoDataTitle)
        tvVisibilityNoData = requireView().findViewById(R.id.visibilityNoData)
        shimmerLoading = requireView().findViewById(R.id.shimmerFrameLayout)
    }

    //menampilkan username di top bar
    private fun showUserName() {
        user?.reload()
        val tvUserName: TextView = requireView().findViewById(R.id.userNameTV)
        val email = user!!.email
        val userName = user.displayName


        val name = if (userName == null || userName == ""){
            val splitValue = email?.split("@")
            splitValue?.get(0).toString()
        }else{
            userName
        }

        tvUserName.text = "Hai, ${name}!"
    }

    //filter data berdasarkan tipe
    private fun visibilityOptions (){
        typeOption = requireView().findViewById(R.id.typeSpinner) as Spinner
        val typeList = arrayOf("Semua Tipe", "Pengeluaran", "Pemasukan")

        val typeSpinnerAdapter = ArrayAdapter<String>(this.requireActivity(),R.layout.selected_spinner,typeList)
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        typeOption.adapter = typeSpinnerAdapter

        timeSpanOption = requireView().findViewById(R.id.timeSpanSpinner) as Spinner
        val timeSpanList = arrayOf("Semua Waktu", "Bulan Ini", "Minggu Ini", "Hari Ini")
        val timeSpanAdapter = ArrayAdapter<String>(this.requireActivity(),R.layout.selected_spinner, timeSpanList)
        timeSpanAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        timeSpanOption.adapter = timeSpanAdapter

        typeOption.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when(typeList[p2]){
                    "Semua Tipe" -> selectedType = "Semua Tipe"
                    "Pengeluaran" -> selectedType = "Pengeluaran"
                    "Pemasukan" -> selectedType = "Pemasukan"
                }
                getTransactionData()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        //set up filter waktu
        timeSpanOption.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when(timeSpanList[p2]){
                    "Semua Waktu" -> selectedTimeSpan = "Semua Waktu"
                    "Bulan Ini" -> {
                        selectedTimeSpan = "Bulan Ini"
                        getRangeDate(Calendar.DAY_OF_MONTH)
                    }
                    "Minggu Ini" -> {
                        selectedTimeSpan = "Minggu Ini"
                        getRangeDate(Calendar.DAY_OF_WEEK)
                    }
                    "Hari Ini" -> {
                        selectedTimeSpan = "Hari Ini"
                        dateStart = System.currentTimeMillis()
                        dateEnd = System.currentTimeMillis()
                    }
                }
                getTransactionData()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun getRangeDate(rangeType: Int) {
        val currentDate = Date()
        val cal: Calendar = Calendar.getInstance(TimeZone.getDefault())
        cal.time = currentDate

        val startDay = cal.getActualMinimum(rangeType)
        cal.set(rangeType, startDay)
        val startDate = cal.time
        dateStart = startDate.time

        val endDay = cal.getActualMaximum(rangeType)
        cal.set(rangeType, endDay)
        val endDate = cal.time
        dateEnd= endDate.time
    }

    //menampilkan data berdasarkan filter
    private fun getTransactionData() {
        shimmerLoading.startShimmer()
        shimmerLoading.visibility = View.VISIBLE
        tvVisibilityNoData.visibility = View.GONE
        transactionRecyclerView.visibility = View.GONE
        tvNoData.visibility = View.GONE
        noDataImage.visibility = View.GONE
        tvNoDataTitle.visibility = View.GONE

        val uid = user?.uid //mendapatkan user id dari database
        if (uid != null) {
            dbRef = FirebaseDatabase.getInstance().getReference(uid)
        }
        val query: Query = dbRef.orderByChild("invertedDate") //sortir data discending
        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                transactionList.clear()
                if (snapshot.exists()){
                    when (selectedType) {
                        "Semua Tipe" -> {
                            for (transactionSnap in snapshot.children){
                                val transactionData = transactionSnap.getValue(TransactionModel::class.java)
                                if (selectedTimeSpan == "Semua Waktu"){
                                    transactionList.add(transactionData!!)
                                }else{
                                    if (transactionData!!.date!! > dateStart-86400000 &&
                                        transactionData.date!!<= dateEnd){
                                        transactionList.add(transactionData)
                                    }
                                }
                            }
                        }
                        "Pengeluaran" -> {
                            for (transactionSnap in snapshot.children){
                                val transactionData = transactionSnap.getValue(TransactionModel::class.java)
                                if (transactionData!!.type == 1){
                                    if (selectedTimeSpan == "Semua Waktu"){
                                        transactionList.add(transactionData)
                                    }else{
                                        if (transactionData.date!! > dateStart-86400000 &&
                                            transactionData.date!! <= dateEnd){
                                            transactionList.add(transactionData)
                                        }
                                    }
                                }
                            }
                        }
                        "Pemasukan" -> {
                            for (transactionSnap in snapshot.children){
                                val transactionData = transactionSnap.getValue(TransactionModel::class.java)
                                if (transactionData!!.type == 2){
                                    if (selectedTimeSpan == "Semua Waktu"){
                                        transactionList.add(transactionData)
                                    }else{
                                        if (transactionData.date!! > dateStart-86400000 &&
                                            transactionData.date!! <= dateEnd){
                                            transactionList.add(transactionData)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (transactionList.isEmpty()){ //jika belum ada data yang ditambah
                        noDataImage.visibility = View.VISIBLE
                        tvNoDataTitle.visibility = View.VISIBLE
                        tvVisibilityNoData.visibility = View.VISIBLE
                        tvVisibilityNoData.text = "Tidak ada $selectedType $selectedTimeSpan"
                    }else{
                        val mAdapter = TransactionAdapter(transactionList)
                        transactionRecyclerView.adapter = mAdapter

                        mAdapter.setOnItemClickListener(object: TransactionAdapter.onItemClickListener{
                            override fun onItemClick(position: Int) {
                                val intent = Intent(this@TransactionFragment.activity, TransactionDetails::class.java)

                                //Detail Data Transaksi
                                intent.putExtra("transaksiID", transactionList[position].transactionID)
                                intent.putExtra("tipe", transactionList[position].type)
                                intent.putExtra("judul", transactionList[position].title)
                                intent.putExtra("kategori", transactionList[position].category)
                                intent.putExtra("jumlah", transactionList[position].amount)
                                intent.putExtra("tanggal", transactionList[position].date)
                                intent.putExtra("note", transactionList[position].note)
                                startActivity(intent)
                            }
                        })
                        transactionRecyclerView.visibility = View.VISIBLE
                    }
                    shimmerLoading.stopShimmer()
                    shimmerLoading.visibility = View.GONE
                }else{ //jika tidak ada data di database
                    shimmerLoading.stopShimmer()
                    shimmerLoading.visibility = View.GONE

                    noDataImage.visibility = View.VISIBLE
                    tvNoDataTitle.visibility = View.VISIBLE
                    tvNoData.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                print("Pemanggilan data gagal")
            }

        })
    }

    override fun onResume() {
        super.onResume()

        getTransactionData()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TransactionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
