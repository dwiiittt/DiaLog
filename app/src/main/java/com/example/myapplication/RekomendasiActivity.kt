package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityRekomendasiBinding
import com.google.firebase.auth.FirebaseAuth

class RekomendasiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRekomendasiBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRekomendasiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        // Auth Guard
        if (mAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupBottomNavigation()
        setupCategoryListeners()
    }

    /**
     * FUNGSI UTAMA: Mengatur aksi klik tombol kategori
     */
    private fun setupCategoryListeners() {
        // Hapus Toast, ganti dengan pemanggilan fungsi openResepList

        binding.cardSarapan.setOnClickListener {
            openResepList("Sarapan")
        }

        binding.cardMakanSiang.setOnClickListener {
            openResepList("Makan Siang")
        }

        binding.cardMakanMalam.setOnClickListener {
            openResepList("Makan Malam")
        }

        binding.cardSnack.setOnClickListener {
            openResepList("Snack")
        }
    }

    /**
     * FUNGSI BARU: Membuka halaman daftar resep & mengirim kategori
     */
    private fun openResepList(kategori: String) {
        val intent = Intent(this, ResepListActivity::class.java)
        // Kita kirim data teks (misal: "Sarapan") ke halaman sebelah
        // agar halaman sebelah tahu resep mana yang harus ditampilkan
        intent.putExtra("KATEGORI_RESEP", kategori)
        startActivity(intent)
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_rekomendasi
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_input_gula -> {
                    startActivity(Intent(this, InputGulaActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_rekomendasi -> true // Sudah di sini
                R.id.nav_input_makanan -> {
                    startActivity(Intent(this, InputMakananActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}