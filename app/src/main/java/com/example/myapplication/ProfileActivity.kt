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

        // Auth Guard
        if (mAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupBottomNavigation()
        setupFormListeners()

        // Muat data profil yang ada
        loadUserProfile()
    }

    private fun setupFormListeners() {
        // Setup dropdown Tipe Diabetes
        val tipeDiabetesArray = resources.getStringArray(R.array.tipe_diabetes_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tipeDiabetesArray)
        binding.autoCompleteTipeDiabetes.setAdapter(adapter)

        // Setup listener kalender untuk Tanggal Lahir
        binding.etTglLahir.setOnClickListener {
            showDatePickerDialog()
        }

        // Setup listener tombol Simpan
        binding.btnUpdateProfil.setOnClickListener {
            validateAndSaveProfile()
        }
    }

    /**
     * Langkah 1: Memuat data user dari Firestore dan menampilkannya di form
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
     * Helper untuk mengisi form dengan data yang ada
     */
    private fun populateForm(user: User) {
        binding.etNama.setText(user.nama)
        binding.etBeratBadan.setText(user.berat_badan.toString())
        binding.etTinggiBadan.setText(user.tinggi_badan.toString())
        binding.etTglLahir.setText(user.tgl_lahir)

        // Atur AutoComplete (dropdown)
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
                // Format tanggal dan tampilkan di EditText
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.etTglLahir.setText(sdf.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    /**
     * Langkah 2: Validasi input dan simpan kembali ke Firestore
     */
    private fun validateAndSaveProfile() {
        val userId = mAuth.currentUser!!.uid

        // Ambil semua data dari form
        val nama = binding.etNama.text.toString().trim()
        val beratStr = binding.etBeratBadan.text.toString().trim()
        val tinggiStr = binding.etTinggiBadan.text.toString().trim()
        val tglLahir = binding.etTglLahir.text.toString().trim()
        val tipeDiabetes = binding.autoCompleteTipeDiabetes.text.toString()

        val selectedJenisKelaminId = binding.rgJenisKelamin.checkedRadioButtonId

        // Validasi
        if (nama.isEmpty() || beratStr.isEmpty() || tinggiStr.isEmpty() || tglLahir.isEmpty() || selectedJenisKelaminId == -1) {
            Toast.makeText(this, "Semua data wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        val jenisKelamin = findViewById<RadioButton>(selectedJenisKelaminId).text.toString()

        // Siapkan data untuk di-update
        val userUpdates = mapOf<String, Any>(
            "nama" to nama,
            "berat_badan" to beratStr.toInt(),
            "tinggi_badan" to tinggiStr.toInt(),
            "tgl_lahir" to tglLahir,
            "jenis_kelamin" to jenisKelamin,
            "tipe_diabetes" to tipeDiabetes
        )

        // Kirim update ke Firestore
        db.collection("users").document(userId)
            .update(userUpdates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                Log.d("ProfileActivity", "Profil berhasil di-update.")
                // Opsional: pindah ke dashboard
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memperbarui profil: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("ProfileActivity", "Error updating profile", e)
            }
    }

    private fun setupBottomNavigation() {
        // Tandai item "Profile" sebagai aktif
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
                R.id.nav_profile -> true // Sudah di sini
                else -> false
            }
        }
    }
}