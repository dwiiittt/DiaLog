package com.example.myapplication

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Auth Guard: Pastikan user sudah login
        if (mAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupBottomNavigation()
        setupFormListeners()

        // Muat data profil yang ada dari Firestore
        loadUserProfile()
    }

    private fun setupFormListeners() {
        // Setup dropdown Tipe Diabetes dari strings.xml
        val tipeDiabetesArray = resources.getStringArray(R.array.tipe_diabetes_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tipeDiabetesArray)
        binding.autoCompleteTipeDiabetes.setAdapter(adapter)

        // Setup listener kalender untuk Tanggal Lahir
        binding.etTglLahir.setOnClickListener {
            showDatePickerDialog()
        }

        // Setup listener tombol Update Profil
        binding.btnUpdateProfil.setOnClickListener {
            validateAndSaveProfile()
        }
    }

    /**
     * Memuat data user dari Firestore dan menampilkannya di form
     */
    private fun loadUserProfile() {
        val userId = mAuth.currentUser!!.uid
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    populateForm(user)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat profil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Mengisi form dengan data dari objek User
     */
    private fun populateForm(user: User) {
        binding.etNama.setText(user.nama)
        binding.etBeratBadan.setText(user.berat_badan.toString())
        binding.etTinggiBadan.setText(user.tinggi_badan.toString())
        binding.etTglLahir.setText(user.tgl_lahir)

        // Atur dropdown
        binding.autoCompleteTipeDiabetes.setText(user.tipe_diabetes, false)

        // Atur RadioButton Jenis Kelamin
        if (user.jenis_kelamin.equals("Laki-laki", ignoreCase = true)) {
            binding.rbLaki.isChecked = true
        } else if (user.jenis_kelamin.equals("Perempuan", ignoreCase = true)) {
            binding.rbPerempuan.isChecked = true
        }
    }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.etTglLahir.setText(sdf.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Batasi agar tidak bisa memilih masa depan
        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }
    // FUNGSI BARU — tambahkan di dalam class ProfileActivity
    private fun hitungTargetSerat(usia: Int, jenisKelamin: String): Pair<Double, Double> {
        val seratAKG = if (jenisKelamin.equals("Perempuan", ignoreCase = true)) {
            when {
                usia <= 12 -> 27.0
                usia <= 15 -> 29.0
                usia <= 18 -> 29.0
                usia <= 29 -> 32.0
                usia <= 49 -> 30.0
                usia <= 64 -> 25.0
                usia <= 80 -> 22.0
                else       -> 20.0
            }
        } else {
            when {
                usia <= 12 -> 28.0
                usia <= 15 -> 34.0
                usia <= 18 -> 37.0
                usia <= 29 -> 37.0
                usia <= 49 -> 36.0
                usia <= 64 -> 30.0
                usia <= 80 -> 25.0
                else       -> 22.0
            }
        }
        val seratMin = seratAKG - 5.0
        val seratMax = seratAKG + 5.0
        return Pair(seratMin, seratMax)
    }
    /**
     * Validasi input, hitung ulang target kalori/karbo, dan simpan ke Firestore
     */
    private fun validateAndSaveProfile() {
        val userId = mAuth.currentUser?.uid ?: return

        // 1. Ambil data dari input
        val nama = binding.etNama.text.toString().trim()
        val beratStr = binding.etBeratBadan.text.toString().trim()
        val tinggiStr = binding.etTinggiBadan.text.toString().trim()
        val tglLahir = binding.etTglLahir.text.toString().trim()
        val tipeDiabetes = binding.autoCompleteTipeDiabetes.text.toString()
        val selectedJenisKelaminId = binding.rgJenisKelamin.checkedRadioButtonId

        // 2. Validasi kelengkapan data
        if (nama.isEmpty() || beratStr.isEmpty() || tinggiStr.isEmpty() || tglLahir.isEmpty() || selectedJenisKelaminId == -1) {
            Toast.makeText(this, "Semua data wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        val jenisKelamin = findViewById<RadioButton>(selectedJenisKelaminId).text.toString()
        val beratBadan = beratStr.toDoubleOrNull() ?: 0.0
        val tinggiBadan = tinggiStr.toDoubleOrNull() ?: 0.0

        // 3. Hitung usia dari tgl_lahir
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val birthDate = sdf.parse(tglLahir)
        val usia = if (birthDate != null) {
            val today = java.util.Calendar.getInstance()
            val birth = java.util.Calendar.getInstance().apply { time = birthDate }
            var age = today.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR)
            if (today.get(java.util.Calendar.DAY_OF_YEAR) < birth.get(java.util.Calendar.DAY_OF_YEAR)) age--
            age
        } else 0

        // 4. Hitung BMI
        val tinggiMeter = tinggiBadan / 100.0
        val bmi = if (tinggiMeter > 0) beratBadan / (tinggiMeter * tinggiMeter) else 0.0

        // 5. Hitung BBi (Broca Modifikasi)
        val bbi = if (jenisKelamin.equals("Perempuan", ignoreCase = true)) {
            0.85 * (tinggiBadan - 100)
        } else {
            0.90 * (tinggiBadan - 100)
        }

        // 6. Tentukan BB untuk kalori
        val bbUntukKalori = if (bmi >= 30.0) {
            bbi + 0.25 * (beratBadan - bbi)
        } else {
            bbi
        }

        // 7. Hitung kalori & karbo
        val kaloriHarian = if (jenisKelamin.equals("Perempuan", ignoreCase = true)) {
            (25 * bbUntukKalori).toInt()
        } else {
            (30 * bbUntukKalori).toInt()
        }
        val batasKarbo = ((0.45 * kaloriHarian) / 4).toInt()

        // 8. Hitung target serat dari AKG
        val (seratMin, seratMax) = hitungTargetSerat(usia, jenisKelamin)

        // 9. Simpan ke Firestore
        val userUpdates = mapOf<String, Any>(
            "nama" to nama,
            "berat_badan" to beratBadan.toInt(),
            "tinggi_badan" to tinggiBadan.toInt(),
            "tgl_lahir" to tglLahir,
            "jenis_kelamin" to jenisKelamin,
            "tipe_diabetes" to tipeDiabetes,
            "target_kalori" to kaloriHarian,
            "target_karbo" to batasKarbo,
            "target_serat_min" to seratMin,
            "target_serat_max" to seratMax
        )

        db.collection("users").document(userId)
            .update(userUpdates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil & Target Nutrisi berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                Log.d("ProfileActivity", "Update success for UID: $userId")
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memperbarui profil: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("ProfileActivity", "Error updating profile", e)
            }
    }
    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_profile
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
                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}