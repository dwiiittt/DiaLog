package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.QuerySnapshot
import com.example.myapplication.databinding.ActivityDashboardBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date
import kotlin.math.pow

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (mAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Panggil semua fungsi untuk memuat data
        fetchUserProfile()
        fetchDataLogGula()
        fetchSaranTerakhir() // <-- FUNGSI BARU DITAMBAHKAN

        // Listeners
        setupListeners()
        setupBottomNavigation()
    }

    private fun setupListeners() {
        binding.btnTambahDataAwal.setOnClickListener {
            startActivity(Intent(this, InputGulaActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.tvLihatRiwayat.setOnClickListener {
            startActivity(Intent(this, RiwayatGulaActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        // ... (Fungsi ini tidak berubah) ...
        binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_input_gula -> {
                    startActivity(Intent(this, InputGulaActivity::class.java))
                    true
                }
                R.id.nav_rekomendasi -> {
                    startActivity(Intent(this, RekomendasiActivity::class.java))
                    true
                }
                R.id.nav_input_makanan -> {
                    startActivity(Intent(this, InputMakananActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchUserProfile() {
        // ... (Fungsi ini tidak berubah) ...
        val userId = mAuth.currentUser!!.uid
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        displayProfile(user)
                    }
                } else {
                    Log.d("Dashboard", "User data not found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Dashboard", "Error fetching user data", e)
            }
    }

    private fun displayProfile(user: User) {
        // ... (Fungsi ini tidak berubah) ...
        val kaloriHarian = if (user.jenis_kelamin.equals("Perempuan", ignoreCase = true)) {
            user.berat_badan * 25
        } else {
            user.berat_badan * 30
        }
        val batasKarbo = (0.45 * kaloriHarian) / 4
        val tinggiMeter = user.tinggi_badan / 100.0
        val bmi = if (tinggiMeter > 0) user.berat_badan / (tinggiMeter.pow(2)) else 0.0
        val statusBmi = when {
            bmi < 18.5 -> "Kurus"
            bmi < 25.0 -> "Normal"
            bmi < 30.0 -> "Gemuk"
            else -> "Obesitas"
        }
        val anjuranSerat = "25-30 gram"
        binding.tvHaloUser.text = "Halo, ${user.nama}"
        binding.tvBeratBadanValue.text = "${user.berat_badan} kg"
        binding.tvTinggiBadanValue.text = "${user.tinggi_badan} cm"
        binding.tvTipeDiabetesValue.text = user.tipe_diabetes
        binding.tvKaloriValue.text = "$kaloriHarian kkal"
        binding.tvKarboValue.text = "${batasKarbo.toInt()} gram"
        binding.tvBmiValue.text = String.format("%.1f", bmi)
        binding.tvStatusBmiValue.text = statusBmi
        binding.tvSeratValue.text = anjuranSerat
    }


    /**
     * =====================================
     * FUNGSI BARU UNTUK SARAN CERDAS
     * =====================================
     */
    private fun fetchSaranTerakhir() {
        val userId = mAuth.currentUser!!.uid

        // Ambil 1 log gula terakhir, diurutkan dari yang terbaru
        db.collection("log_gula_darah")
            .whereEqualTo("uid", userId)
            .orderBy("tgl_input", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    // Tidak ada data log, sembunyikan card saran
                    binding.cardSaran.visibility = View.GONE
                } else {
                    // Ambil data log pertama (dan satu-satunya)
                    val logTerakhir = snapshot.documents[0].toObject(LogGula::class.java)
                    if (logTerakhir != null) {
                        displaySaran(logTerakhir)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Dashboard", "Error fetching saran", e)
                binding.cardSaran.visibility = View.GONE
            }
    }

    /**
     * =====================================
     * FUNGSI HELPER BARU UNTUK MENAMPILKAN SARAN
     * =====================================
     */
    private fun displaySaran(log: LogGula) {
        val saranTeks: String = when (log.status_hasil) {
            "Diabetes" -> {
                "Gula darah Anda terakhir tercatat tinggi. Coba jalan kaki 15 menit setelah makan dan kurangi porsi karbohidrat Anda hari ini."
            }
            "Prediabetes" -> {
                "Hasil Anda di batas waspada. Perhatikan pilihan makanan Anda, pilih yang berserat tinggi dan indeks glikemik rendah."
            }
            "Normal" -> {
                "Kerja bagus! Hasil gula darah Anda normal. Pertahankan pola makan sehat dan aktivitas fisik Anda."
            }
            else -> {
                "Data terakhir Anda: ${log.nilai_gula} mg/dL. Terus catat data Anda untuk mendapatkan saran yang lebih baik."
            }
        }

        binding.tvSaranText.text = saranTeks
        binding.cardSaran.visibility = View.VISIBLE
    }


    private fun fetchDataLogGula() {
        // ... (Fungsi ini tidak berubah, tetap untuk chart) ...
        val userId = mAuth.currentUser!!.uid
        val gdpTask = db.collection("log_gula_darah")
            .whereEqualTo("uid", userId)
            .whereEqualTo("jenis_pengukuran", "GDP (Puasa)")
            .orderBy("tgl_input", Query.Direction.ASCENDING)
            .limit(10)
            .get()
        val gd2ppTask = db.collection("log_gula_darah")
            .whereEqualTo("uid", userId)
            .whereEqualTo("jenis_pengukuran", "GD2PP (Setelah Makan)")
            .orderBy("tgl_input", Query.Direction.ASCENDING)
            .limit(10)
            .get()

        Tasks.whenAllSuccess<QuerySnapshot>(gdpTask, gd2ppTask)
            .addOnSuccessListener { results ->
                val gdpSnapshot = results[0]
                val gd2ppSnapshot = results[1]

                if (gdpSnapshot.isEmpty && gd2ppSnapshot.isEmpty) {
                    Log.d("Dashboard", "Tidak ada data gula darah sama sekali.")
                    showNoDataView(true)
                    return@addOnSuccessListener
                }
                showNoDataView(false)

                val gdpEntries = ArrayList<Entry>()
                val gdpList = gdpSnapshot.toObjects(LogGula::class.java)
                for ((index, log) in gdpList.withIndex()) {
                    gdpEntries.add(Entry(index.toFloat(), log.nilai_gula.toFloat()))
                }

                val gd2ppEntries = ArrayList<Entry>()
                val gd2ppList = gd2ppSnapshot.toObjects(LogGula::class.java)
                for ((index, log) in gd2ppList.withIndex()) {
                    gd2ppEntries.add(Entry(index.toFloat(), log.nilai_gula.toFloat()))
                }

                setupChart(gdpEntries, gd2ppEntries)
            }
            .addOnFailureListener { e ->
                Log.e("Dashboard", "Error fetching combined data. PERIKSA INDEKS ANDA!", e)
                Toast.makeText(this, "Gagal mengambil data chart: ${e.message}", Toast.LENGTH_LONG).show()
                showNoDataView(true)
            }
    }


    private fun showNoDataView(show: Boolean) {
        // ... (Fungsi ini tidak berubah) ...
        if (show) {
            binding.layoutNoData.visibility = View.VISIBLE
            binding.chartGulaDarah.visibility = View.GONE
        } else {
            binding.layoutNoData.visibility = View.GONE
            binding.chartGulaDarah.visibility = View.VISIBLE
        }
    }

    private fun setupChart(gdpEntries: ArrayList<Entry>, gd2ppEntries: ArrayList<Entry>) {
        // ... (Fungsi ini tidak berubah) ...
        val gdpDataSet = LineDataSet(gdpEntries, "GDP (Puasa)")
        gdpDataSet.color = Color.BLUE
        gdpDataSet.valueTextColor = Color.BLACK
        gdpDataSet.lineWidth = 2.5f
        gdpDataSet.setCircleColor(Color.BLUE)

        val gd2ppDataSet = LineDataSet(gd2ppEntries, "GD2PP (Setelah Makan)")
        gd2ppDataSet.color = Color.RED
        gd2ppDataSet.valueTextColor = Color.BLACK
        gd2ppDataSet.lineWidth = 2.5f
        gd2ppDataSet.setCircleColor(Color.RED)

        val lineData = LineData(gdpDataSet, gd2ppDataSet)
        binding.chartGulaDarah.data = lineData
        binding.chartGulaDarah.description.text = "Riwayat Gula Darah"
        binding.chartGulaDarah.setDrawGridBackground(false)
        binding.chartGulaDarah.legend.isEnabled = true
        binding.chartGulaDarah.legend.textColor = Color.BLACK
        binding.chartGulaDarah.invalidate()
    }
}