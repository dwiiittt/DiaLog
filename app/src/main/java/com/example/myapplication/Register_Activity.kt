// Pastikan package Anda benar
package com.example.myapplication

// Import library yang diperlukan
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityRegisterBinding // <-- Import class Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterActivity : AppCompatActivity() {

    // 1. Deklarasi variabel untuk View Binding, Firebase Auth, dan Firestore
    private lateinit var binding: ActivityRegisterBinding // Ini menggantikan semua findViewById
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Setup View Binding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 3. Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 4. Panggil fungsi helper untuk setup UI
        setupTipeDiabetesDropdown()
        setupDatePicker()

        // 5. Setup listener untuk tombol Daftar
        binding.btnDaftar.setOnClickListener {
            // Panggil fungsi untuk memproses registrasi
            registerUser()
        }
    }

    /**
     * Mengisi dropdown Tipe Diabetes dengan data dari strings.xml
     */
    private fun setupTipeDiabetesDropdown() {
        val tipeArray = resources.getStringArray(R.array.tipe_diabetes_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tipeArray)
        binding.autoCompleteTipeDiabetes.setAdapter(adapter)
    }

    /**
     * Menampilkan popup kalender saat input Tanggal Lahir diklik
     */
    private fun setupDatePicker() {
        binding.etTglLahir.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Format tanggal (misal: 25/12/2023)
                    val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    binding.etTglLahir.setText(selectedDate)
                },
                year, month, day
            )
            // Batasi agar tidak bisa memilih tanggal di masa depan
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }
    }

    /**
     * Fungsi utama untuk validasi dan registrasi pengguna
     */
    private fun registerUser() {
        // Ambil semua data dari input
        val nama = binding.etNama.text.toString().trim()
        val tglLahir = binding.etTglLahir.text.toString().trim()
        val beratBadanStr = binding.etBeratBadan.text.toString().trim()
        val tinggiBadanStr = binding.etTinggiBadan.text.toString().trim()
        val tipeDiabetes = binding.autoCompleteTipeDiabetes.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Ambil data Jenis Kelamin dari RadioGroup
        val selectedKelaminId = binding.rgJenisKelamin.checkedRadioButtonId
        val jenisKelamin = if (selectedKelaminId != -1) {
            findViewById<RadioButton>(selectedKelaminId).text.toString()
        } else {
            "" // Kosong jika belum dipilih
        }

        // --- VALIDASI SEDERHANA ---
        if (nama.isEmpty() || tglLahir.isEmpty() || beratBadanStr.isEmpty() ||
            tinggiBadanStr.isEmpty() || tipeDiabetes.isEmpty() || email.isEmpty() ||
            password.isEmpty() || jenisKelamin.isEmpty()) {

            Toast.makeText(this, "Semua data harus diisi!", Toast.LENGTH_SHORT).show()
            return // Hentikan fungsi jika ada yang kosong
        }

        // Konversi berat dan tinggi ke Angka (Int)
        val beratBadan = beratBadanStr.toIntOrNull() ?: 0
        val tinggiBadan = tinggiBadanStr.toIntOrNull() ?: 0

        if (password.length < 6) {
            Toast.makeText(this, "Password minimal harus 6 karakter", Toast.LENGTH_SHORT).show()
            return
        }

        // Tampilkan loading (jika ada ProgressBar)
        // binding.progressBar.visibility = View.VISIBLE

        // --- LANGKAH 1: Buat Akun di Firebase Authentication ---
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Auth berhasil
                    val firebaseUser = mAuth.currentUser
                    val userId = firebaseUser?.uid

                    if (userId != null) {
                        // Lanjut ke LANGKAH 2: Simpan data ke Firestore
                        simpanDataTambahan(userId, nama, jenisKelamin, tglLahir, beratBadan, tinggiBadan, tipeDiabetes, email)
                    } else {
                        Toast.makeText(this, "Gagal mendapatkan User ID", Toast.LENGTH_SHORT).show()
                        // Sembunyikan loading
                    }
                } else {
                    // Auth gagal
                    Log.w("RegisterActivity", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Registrasi Gagal: ${task.exception?.message}",
                        Toast.LENGTH_LONG).show()
                    // Sembunyikan loading
                }
            }
    }

    /**
     * Fungsi untuk menyimpan data tambahan user ke Firestore
     */
    private fun simpanDataTambahan(
        userId: String,
        nama: String,
        jenisKelamin: String,
        tglLahir: String,
        beratBadan: Int,
        tinggiBadan: Int,
        tipeDiabetes: String,
        email: String
    ) {
        // Buat objek User menggunakan data class yang sudah kita buat
        val user = User(
            uid = userId,
            nama = nama,
            jenis_kelamin = jenisKelamin,
            tgl_lahir = tglLahir,
            berat_badan = beratBadan,
            tinggi_badan = tinggiBadan,
            tipe_diabetes = tipeDiabetes,
            email = email
        )

        // Simpan objek User ke koleksi "users" dengan ID dokumen = userId
        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                // Sembunyikan loading
                // binding.progressBar.visibility = View.GONE

                Toast.makeText(this, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()


                val intent = Intent(this, LoginActivity::class.java)

                // Hapus semua activity sebelumnya (termasuk register) dari tumpukan
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)

                finish() // Tutup RegisterActivity agar tidak bisa kembali
                // Pindahkan pengguna ke Halaman Utama (HomeActivity)
                // val intent = Intent(this, HomeActivity::class.java)
                // startActivity(intent)
                // finish() // Tutup activity ini
            }
            .addOnFailureListener { e ->
                // Sembunyikan loading
                Log.w("RegisterActivity", "Error adding document", e)
                Toast.makeText(this, "Gagal menyimpan data profil", Toast.LENGTH_SHORT).show()
            }
    }
}