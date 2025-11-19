package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityInputMakananBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class InputMakananActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInputMakananBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var logMakananAdapter: LogMakananAdapter
    private var logList = mutableListOf<LogMakanan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputMakananBinding.inflate(layoutInflater)
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

        // Listener untuk tombol "Tambah" (+)
        binding.fabTambahMakanan.setOnClickListener {
            // Buka EditLogMakananActivity dalam mode "Create" (tanpa ID)
            startActivity(Intent(this, EditLogMakananActivity::class.java))
        }

        // Mulai mendengarkan data riwayat
        fetchRiwayatMakanan()
    }

    private fun setupRecyclerView() {
        // Setup adapter dengan listener klik
        logMakananAdapter = LogMakananAdapter(logList) { log ->
            // Aksi saat item diklik: Buka mode "Edit"
            val intent = Intent(this, EditLogMakananActivity::class.java)
            intent.putExtra("LOG_ID", log.id) // Kirim ID Dokumen ke halaman edit
            startActivity(intent)
        }

        binding.rvRiwayatMakanan.adapter = logMakananAdapter
        binding.rvRiwayatMakanan.layoutManager = LinearLayoutManager(this)
    }

    /**
     * Menggunakan addSnapshotListener agar daftar otomatis update
     * jika ada data baru, edit, atau hapus.
     */
    private fun fetchRiwayatMakanan() {
        val userId = mAuth.currentUser!!.uid

        db.collection("log_makanan")
            .whereEqualTo("uid", userId)
            .orderBy("tgl_input", Query.Direction.DESCENDING) // Terbaru di atas
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("InputMakananActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    binding.tvRiwayatKosong.visibility = View.GONE
                    binding.rvRiwayatMakanan.visibility = View.VISIBLE

                    val list = snapshot.toObjects(LogMakanan::class.java)
                    logMakananAdapter.updateData(list)
                } else {
                    Log.d("InputMakananActivity", "No data found")
                    binding.tvRiwayatKosong.visibility = View.VISIBLE
                    binding.rvRiwayatMakanan.visibility = View.GONE
                    logMakananAdapter.updateData(emptyList()) // Kosongkan list
                }
            }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_input_makanan

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
                R.id.nav_input_makanan -> true // Sudah di sini
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