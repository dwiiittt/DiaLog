package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ActivityRekomendasiBinding
import com.example.myapplication.databinding.ItemRekomendasiGaV2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RekomendasiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRekomendasiBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var allResep = mutableListOf<Resep>()
    private var targetKalori = 2000
    private var targetKarbo = 150.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRekomendasiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (mAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupBottomNavigation()
        loadUserDataAndResep()

        binding.btnAcakUlang.setOnClickListener {
            if (allResep.isNotEmpty()) {
                generateRecommendation()
            }
        }
    }

    private fun loadUserDataAndResep() {
        val userId = mAuth.currentUser?.uid ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)
                if (user != null) {
                    targetKalori = user.target_kalori
                    targetKarbo = user.target_karbo.toDouble()
                }
                fetchAllResep()
            }
    }

    private fun fetchAllResep() {
        db.collection("resep").get()
            .addOnSuccessListener { snapshot ->
                allResep = snapshot.toObjects(Resep::class.java).toMutableList()
                if (allResep.isNotEmpty()) {
                    generateRecommendation()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat resep", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateRecommendation() {
        if (allResep.isEmpty()) {
            Toast.makeText(this, "Data resep kosong", Toast.LENGTH_SHORT).show()
            return
        }

        // --- MEMANGGIL LOGIC GENETIC ALGORITHM ---
        // Pastikan parameter targetKalori dan targetKarbo sudah terisi dari loadUserData
        val optimizer = GeneticOptimizer(allResep, targetKalori, targetKarbo)

        // Jalankan pencarian solusi terbaik
        val bestMenu = optimizer.solve()

        // Pastikan hasil solve mengembalikan 4 menu (Sarapan, Siang, Snack, Malam)
        if (bestMenu.size == 4) {
            displayResult(bestMenu[0], bestMenu[1], bestMenu[2], bestMenu[3])
        } else {
            Toast.makeText(this, "Kombinasi resep tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }
    private fun displayResult(s: Resep, si: Resep, sn: Resep, m: Resep) {
        // Perhitungan Total Nutrisi (Termasuk Serat)
        val totalKal = s.total_kalori + si.total_kalori + sn.total_kalori + m.total_kalori
        val totalKar = s.total_karbo + si.total_karbo + sn.total_karbo + m.total_karbo

        // Asumsi model Resep sudah memiliki field 'serat', jika belum ganti ke 0.0
        val totalSerat = (s.total_serat ?: 0.0) + (si.total_serat ?: 0.0) + (sn.total_serat ?: 0.0) + (m.total_serat ?: 0.0)

        // Update Header Dashboard
        binding.tvTotalKaloriGA.text = totalKal.toString()
        binding.tvTotalKarboGA.text = "${String.format("%.1f", totalKar)}g"
        binding.tvTotalSeratGA.text = "${String.format("%.1f", totalSerat)}g"

        // Isi Data ke Kartu masing-masing waktu makan
        fillCard(ItemRekomendasiGaV2Binding.bind(binding.layoutSarapan.root), s, "SARAPAN")
        fillCard(ItemRekomendasiGaV2Binding.bind(binding.layoutSiang.root), si, "MAKAN SIANG")
        fillCard(ItemRekomendasiGaV2Binding.bind(binding.layoutSnack.root), sn, "SNACK")
        fillCard(ItemRekomendasiGaV2Binding.bind(binding.layoutMalam.root), m, "MAKAN MALAM")
    }

    private fun fillCard(cardBinding: ItemRekomendasiGaV2Binding, resep: Resep, label: String) {
        cardBinding.tvLabelWaktu.text = label
        cardBinding.tvMenuNama.text = resep.nama_resep

        // MENAMPILKAN SERAT DI BAWAH NAMA MENU
        // Format: "250 kkal | Karbo 30g | Serat 5g"
        val seratValue = resep.total_serat ?: 0.0
        val infoNutrisi = "${resep.total_kalori} kkal | Karbo ${resep.total_karbo}g | Serat ${seratValue}g"
        cardBinding.tvMenuNutrisi.text = infoNutrisi

        if (resep.foto_resep_base64.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(resep.foto_resep_base64, Base64.DEFAULT)
                Glide.with(this).load(imageBytes).into(cardBinding.ivMenuFoto)
            } catch (e: Exception) {
                // Jika error load gambar
            }
        }

        cardBinding.root.setOnClickListener {
            val intent = Intent(this, DetailResepActivity::class.java)
            intent.putExtra("NAMA", resep.nama_resep)
            intent.putExtra("KATEGORI", resep.kategori)
            intent.putExtra("KALORI", resep.total_kalori)
            intent.putExtra("KARBO", resep.total_karbo)
            intent.putExtra("SERAT", resep.total_serat ?: 0.0) // Kirim data serat ke detail
            intent.putExtra("GI", resep.level_gi)
            intent.putExtra("FOTO", resep.foto_resep_base64)
            intent.putStringArrayListExtra("BAHAN", ArrayList(resep.bahan))
            intent.putStringArrayListExtra("LANGKAH", ArrayList(resep.langkah_langkah))
            startActivity(intent)
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
                R.id.nav_rekomendasi -> true
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