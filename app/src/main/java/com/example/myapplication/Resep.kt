package com.example.myapplication

import com.google.firebase.firestore.DocumentId

data class Resep(
    @DocumentId
    val id: String = "", // ID Dokumen

    val nama_resep: String = "",
    val kategori: String = "", // Sarapan, Makan Siang, Makan Malam, Snack
    val foto_resep_base64: String = "", // Teks Base64 dari foto resep
    val total_kalori: Int = 0,
    val total_karbo: Double = 0.0,
    val total_serat: Double = 0.0, // Tambahkan ini
    val level_gi: String = "Rendah", // Rendah, Sedang, Tinggi

    // Kita gunakan List<String> agar mudah
    val bahan: List<String> = emptyList(),
    val langkah_langkah: List<String> = emptyList()
)