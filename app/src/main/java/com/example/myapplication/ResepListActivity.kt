package com.example.myapplication

import android.content.Intent // <-- INI YANG KURANG TADI
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityResepListBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList // <-- Ini juga penting untuk pengiriman data list

class ResepListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResepListBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var resepAdapter: ResepAdapter
    private var resepList = mutableListOf<Resep>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResepListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // 1. Ambil Kategori yang dikirim dari halaman sebelumnya
        val kategori = intent.getStringExtra("KATEGORI_RESEP") ?: "Semua"
        binding.tvJudulKategori.text = "Resep $kategori"

        binding.btnBack.setOnClickListener { finish() }

        setupRecyclerView()
        fetchResep(kategori)
    }

    private fun setupRecyclerView() {
        resepAdapter = ResepAdapter(resepList) { resep ->
            // SAAT ITEM DIKLIK:
            val intent = Intent(this, DetailResepActivity::class.java)

            // Kirim semua data resep ke halaman sebelah
            intent.putExtra("NAMA", resep.nama_resep)
            intent.putExtra("KATEGORI", resep.kategori)
            intent.putExtra("KALORI", resep.total_kalori)
            intent.putExtra("KARBO", resep.total_karbo)
            intent.putExtra("GI", resep.level_gi)
            intent.putExtra("FOTO", resep.foto_resep_base64)

            // Kirim List (ArrayList)
            intent.putStringArrayListExtra("BAHAN", ArrayList(resep.bahan))
            intent.putStringArrayListExtra("LANGKAH", ArrayList(resep.langkah_langkah))

            startActivity(intent)
        }
        binding.rvResepList.layoutManager = LinearLayoutManager(this)
        binding.rvResepList.adapter = resepAdapter
    }

    private fun fetchResep(kategori: String) {
        // Query: Cari resep yang field 'kategori'-nya sama dengan yang dipilih
        db.collection("resep")
            .whereEqualTo("kategori", kategori)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    binding.tvResepKosong.visibility = View.VISIBLE
                    binding.rvResepList.visibility = View.GONE
                } else {
                    binding.tvResepKosong.visibility = View.GONE
                    binding.rvResepList.visibility = View.VISIBLE

                    val list = snapshot.toObjects(Resep::class.java)
                    resepList.clear()
                    resepList.addAll(list)
                    resepAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}