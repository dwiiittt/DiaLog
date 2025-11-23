package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast // <-- PENTING: Import ini sebelumnya kurang
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityRekomendasiBinding
import com.google.firebase.auth.FirebaseAuth

class RekomendasiActivity : AppCompatActivity() {

    // --- PERBAIKAN DI SINI ---
    // Sebelumnya: private lateinit: ActivityRekomendasiBinding (Salah)
    // Sekarang:
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

    private fun setupCategoryListeners() {
        binding.cardSarapan.setOnClickListener {
            Toast.makeText(this, "Membuka Resep Sarapan", Toast.LENGTH_SHORT).show()
        }

        binding.cardMakanSiang.setOnClickListener {
            Toast.makeText(this, "Membuka Resep Makan Siang", Toast.LENGTH_SHORT).show()
        }

        binding.cardMakanMalam.setOnClickListener {
            Toast.makeText(this, "Membuka Resep Makan Malam", Toast.LENGTH_SHORT).show()
        }

        binding.cardSnack.setOnClickListener {
            Toast.makeText(this, "Membuka Resep Snack", Toast.LENGTH_SHORT).show()
        }
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