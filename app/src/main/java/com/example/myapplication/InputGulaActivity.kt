package com.example.myapplication

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityInputGulaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class InputGulaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInputGulaBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var selectedDate = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputGulaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Auth Guard
        if (mAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Setup Listener
        setupListeners()

        // Setup Navigasi Bawah
        setupBottomNavigation()
    }

    private fun setupListeners() {
        // Listener untuk text input Tanggal
        binding.etTanggal.setOnClickListener {
            showDatePickerDialog()
        }

        // Listener untuk text input Waktu
        binding.etWaktu.setOnClickListener {
            showTimePickerDialog()
        }

        // Listener untuk tombol Simpan
        binding.btnSimpanLog.setOnClickListener {
            simpanLogGulaDarah()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                // Format tanggal dan tampilkan di EditText
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.etTanggal.setText(sdf.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedDate.set(Calendar.MINUTE, minute)
                // Format waktu dan tampilkan di EditText
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                binding.etWaktu.setText(sdf.format(selectedDate.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // 24 jam
        )
        timePicker.show()
    }

    private fun simpanLogGulaDarah() {
        // 1. Dapatkan semua data dari form
        val userId = mAuth.currentUser!!.uid
        val tanggalStr = binding.etTanggal.text.toString()
        val waktuStr = binding.etWaktu.text.toString()
        val nilaiGulaStr = binding.etNilaiGula.text.toString()

        // 2. Validasi Input Wajib
        if (tanggalStr.isEmpty() || waktuStr.isEmpty() || nilaiGulaStr.isEmpty()) {
            Toast.makeText(this, "Tanggal, Waktu, dan Nilai Gula tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Dapatkan data sisanya
        val nilaiGula = nilaiGulaStr.toInt()
        val selectedRadioId = binding.rgJenisPengukuran.checkedRadioButtonId
        val jenisPengukuran = findViewById<RadioButton>(selectedRadioId).text.toString()
        val aktivitas = binding.etAktivitasFisik.text.toString()
        val catatan = binding.etCatatanLain.text.toString()

        // 4. Hitung 'status_hasil' (Berdasarkan standar medis)
        val statusHasil = when (jenisPengukuran) {
            "GDP (Puasa)" -> when {
                nilaiGula < 100 -> "Normal"
                nilaiGula < 126 -> "Prediabetes"
                else -> "Diabetes"
            }
            "GD2PP (Setelah Makan)" -> when {
                nilaiGula < 140 -> "Normal"
                nilaiGula < 200 -> "Prediabetes"
                else -> "Diabetes"
            }
            else -> "N/A"
        }

        // 5. Buat Objek LogGula
        val log = LogGula(
            uid = userId,
            tgl_input = selectedDate.time, // Simpan sebagai objek Date
            jenis_pengukuran = jenisPengukuran,
            nilai_gula = nilaiGula,
            aktivitas_fisik = aktivitas,
            catatan_lain = catatan,
            status_hasil = statusHasil
        )

        // 6. Simpan ke Firestore
        db.collection("log_gula_darah")
            .add(log)
            .addOnSuccessListener {
                Toast.makeText(this, "Catatan berhasil disimpan!", Toast.LENGTH_SHORT).show()
                // Kembali ke Dashboard setelah berhasil
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupBottomNavigation() {
        // Tandai item "Input Gula" sebagai aktif
        binding.bottomNavigation.selectedItemId = R.id.nav_input_gula

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish() // Tutup activity ini
                    true
                }
                R.id.nav_input_gula -> {
                    // Sudah di halaman ini
                    true
                }
                R.id.nav_rekomendasi -> {
                    startActivity(Intent(this, RekomendasiActivity::class.java))
                    finish() // Tutup activity ini
                    true
                }
                R.id.nav_input_makanan -> {
                    startActivity(Intent(this, InputMakananActivity::class.java))
                    finish() // Tutup activity ini
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish() // Tutup activity ini
                    true
                }
                else -> false
            }
        }
    }
}