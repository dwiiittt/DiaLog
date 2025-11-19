// Di file LogMakanan.kt
package com.example.myapplication

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class LogMakanan(
    @DocumentId
    val id: String = "",

    val uid: String = "",
    val tgl_input: Date? = null,
    val nama_makanan: String = "",
    val porsi: Double = 0.0,
    val jenis_makanan: String = "",

    // --- UBAH BARIS INI ---
    val foto_makanan_base64: String = "" // Dari foto_makanan_url
)