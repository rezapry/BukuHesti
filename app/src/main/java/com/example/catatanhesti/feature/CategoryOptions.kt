package com.example.catatanhesti.feature

object CategoryOptions {

    //kategori pada pengeluaran
    fun expenseCategory(): ArrayList<String> {
        val listExpense = ArrayList<String>()
        listExpense.add("Makanan & Minuman")
        listExpense.add("Transportasi")
        listExpense.add("Edukasi")
        listExpense.add("Piutang")
        listExpense.add("Belanja")
        listExpense.add("Investasi")
        listExpense.add("Pengeluaran Lain")

        return listExpense
    }

    //kategori pada pemasukan
    fun incomeCategory(): ArrayList<String> {
        val listIncome = ArrayList<String>()
        listIncome.add("Penjualan")
        listIncome.add("Hutang")
        listIncome.add("Gaji")
        listIncome.add("Pemberian")
        listIncome.add("Hadiah")
        listIncome.add("Pemasukan Lain")

        return listIncome
    }
}