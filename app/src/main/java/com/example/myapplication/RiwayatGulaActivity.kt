package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityRiwayatGulaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RiwayatGulaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiwayatGulaBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var logAdapter: LogGulaAdapter
    private var logList = mutableListOf<LogGula>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiwayatGulaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Auth Guard
        if (mAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupRecyclerView()
        setupBottomNavigation()
        fetchRiwayatGula()
    }

    private fun setupRecyclerView() {
        // Siapkan adapter dengan list kosong dulu
        logAdapter = LogGulaAdapter(logList)
        binding.rvRiwayatGula.adapter = logAdapter
        binding.rvRiwayatGula.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchRiwayatGula() {
        val userId = mAuth.currentUser!!.uid

        // Query ke Firestore, ambil SEMUA data, urutkan dari yang TERBARU
        db.collection("log_gula_darah")
            .whereEqualTo("uid", userId)
            .orderBy("tgl_input", Query.Direction.DESCENDING) // <-- DESCENDING agar terbaru di atas
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Toast.makeText(this, "Belum ada riwayat", Toast.LENGTH_SHORT).show()
                } else {
                    // Konversi semua dokumen ke List<LogGula>
                    val list = snapshot.toObjects(LogGula::class.java)

                    // Bersihkan list lama, tambahkan data baru
                    logList.clear()
                    logList.addAll(list)

                    // Beri tahu adapter bahwa data telah berubah
                    logAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                Log.e("RiwayatGula", "Error fetching history", e)
                Toast.makeText(this, "Gagal mengambil riwayat: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBottomNavigation() {
        // Halaman ini tidak ada di menu, jadi kita tidak set 'selectedItemId'
        // Kita hanya siapkan listener-nya saja

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
                R.id.nav_rekomendasi -> {
                    startActivity(Intent(this, RekomendasiActivity::class.java))
                    finish()
                    true
                }
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