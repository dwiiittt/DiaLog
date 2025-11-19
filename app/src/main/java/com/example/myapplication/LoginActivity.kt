package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log // <-- Pastikan import ini ada
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase Auth
        mAuth = FirebaseAuth.getInstance()

        // --- LOGIKA AUTO-LOGIN ---
        // Cek di awal, apakah user sudah login dari sesi sebelumnya?
        if (mAuth.currentUser != null) {
            // Jika ya (tidak null), jangan tampilkan login, langsung ke Dashboard
            Log.d("LoginActivity", "User sudah login: ${mAuth.currentUser!!.email}")
            pindahKeDashboard()
            return // Hentikan eksekusi onCreate agar halaman login tidak disiapkan
        }
        // --- SELESAI LOGIKA AUTO-LOGIN ---

        // Jika currentUser == null, kode akan lanjut ke sini
        // (Tampilkan halaman login seperti biasa)

        // Listener untuk tombol Login
        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        // Listener untuk text "Daftar di sini" (Sign Up)
        binding.tvDaftar.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Listener untuk "Lupa Password"
        binding.tvLupaPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    /**
     * Logika untuk memproses login user
     */
    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        // Tampilkan loading (jika ada)
        // binding.progressBar.visibility = View.VISIBLE

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                // Sembunyikan loading
                // binding.progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    // Login berhasil
                    Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()

                    // Pindahkan ke Halaman Utama (Panggil fungsi helper)
                    pindahKeDashboard()

                } else {
                    // Login gagal
                    Toast.makeText(baseContext, "Login Gagal: ${task.exception?.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
    }

    /**
     * FUNGSI HELPER
     * Untuk pindah ke Dashboard dan membersihkan history
     */
    private fun pindahKeDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        // Hapus semua activity sebelumnya (login/register)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Tutup LoginActivity
    }


    /**
     * Menampilkan dialog untuk reset password
     */
    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Lupa Password?")
        builder.setMessage("Masukkan email Anda untuk menerima link reset password.")

        val input = EditText(this)
        input.hint = "Email"
        input.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        builder.setView(input)

        builder.setPositiveButton("Kirim") { _, _ ->
            val email = input.text.toString().trim()
            if (email.isNotEmpty()) {
                sendPasswordResetEmail(email)
            } else {
                Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    /**
     * Mengirim email reset password menggunakan Firebase Auth
     */
    private fun sendPasswordResetEmail(email: String) {
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Email reset password telah dikirim.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Gagal mengirim email: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}