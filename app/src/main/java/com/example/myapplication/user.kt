package com.example.myapplication

data class User(
    val uid: String = "",
    val nama: String = "",
    val jenis_kelamin: String = "",
    val tgl_lahir: String = "",
    val berat_badan: Int = 0,
    val tinggi_badan: Int = 0,
    val tipe_diabetes: String = "",
    val email: String = ""
)