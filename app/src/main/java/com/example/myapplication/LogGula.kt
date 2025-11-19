package com.example.myapplication

import java.util.Date

data class LogGula(
    // Kunci untuk menghubungkan ke User
    val uid: String = "",

    // Data dari Form
    val tgl_input: Date? = null, // Akan menyimpan tanggal & jam jadi satu
    val jenis_pengukuran: String = "", // GDP atau GD2PP
    val nilai_gula: Int = 0,
    val aktivitas_fisik: String = "",
    val catatan_lain: String = "",

    // Data yang Dihitung Otomatis
    val status_hasil: String = ""
)