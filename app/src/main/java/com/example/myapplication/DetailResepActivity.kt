package com.example.myapplication

import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ActivityDetailResepBinding

class DetailResepActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailResepBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailResepBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data dari Intent
        val nama = intent.getStringExtra("NAMA")
        val kategori = intent.getStringExtra("KATEGORI")
        val kalori = intent.getIntExtra("KALORI", 0)
        val karbo = intent.getDoubleExtra("KARBO", 0.0)
        val gi = intent.getStringExtra("GI")
        val fotoBase64 = intent.getStringExtra("FOTO")

        // Ambil List String (Bahan & Langkah)
        val bahanList = intent.getStringArrayListExtra("BAHAN") ?: arrayListOf()
        val langkahList = intent.getStringArrayListExtra("LANGKAH") ?: arrayListOf()

        // Tampilkan Data ke Layar
        binding.tvDetailNama.text = nama
        binding.tvDetailKategori.text = kategori
        binding.tvDetailKalori.text = "$kalori kkal"
        binding.tvDetailKarbo.text = "$karbo g"
        binding.tvDetailGI.text = gi

        // Format List menjadi Teks berbaris
        // Bahan: "- Ayam\n- Garam"
        val bahanText = StringBuilder()
        for (b in bahanList) {
            bahanText.append("• $b\n")
        }
        binding.tvDetailBahan.text = bahanText.toString()

        // Langkah: "1. Cuci ayam\n2. Rebus"
        val langkahText = StringBuilder()
        for ((index, l) in langkahList.withIndex()) {
            langkahText.append("${index + 1}. $l\n\n") // \n\n agar ada jarak
        }
        binding.tvDetailLangkah.text = langkahText.toString()

        // Tampilkan Foto
        if (!fotoBase64.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(fotoBase64, Base64.DEFAULT)
                Glide.with(this)
                    .load(imageBytes)
                    .placeholder(R.color.gray)
                    .into(binding.ivDetailFoto)
            } catch (e: Exception) {
                binding.ivDetailFoto.setImageResource(R.color.gray)
            }
        }
    }
}