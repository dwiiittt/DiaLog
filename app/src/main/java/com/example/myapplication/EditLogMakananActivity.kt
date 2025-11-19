package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap // <-- IMPORT BARU
import android.graphics.BitmapFactory // <-- IMPORT BARU
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64 // <-- IMPORT BARU
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ActivityEditLogMakananBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
// import com.google.firebase.storage.FirebaseStorage // <-- HAPUS IMPORT INI
import java.io.ByteArrayOutputStream // <-- IMPORT BARU
import java.util.*

class EditLogMakananActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditLogMakananBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    // private lateinit var storage: FirebaseStorage // <-- HAPUS INI

    // Variabel untuk menampung data
    private var currentLogId: String? = null
    private var imageUri: Uri? = null // URI untuk gambar BARU
    private var existingFotoBase64: String? = null // String Base64 LAMA (saat edit)
    private lateinit var userId: String

    // (Semua launcher kamera & galeri Anda tidak berubah)
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(this, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
            }
        }
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && imageUri != null) {
                binding.ivFotoPreview.setImageURI(imageUri)
            }
        }
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri = result.data?.data
                binding.ivFotoPreview.setImageURI(imageUri)
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditLogMakananBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        // storage = FirebaseStorage.getInstance() // <-- HAPUS INI

        // Auth Guard
        if (mAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        userId = mAuth.currentUser!!.uid
        currentLogId = intent.getStringExtra("LOG_ID")

        setupForm()
        setupListeners()
    }

    private fun setupForm() {
        val jenisArray = resources.getStringArray(R.array.jenis_makanan_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, jenisArray)
        binding.autoCompleteJenisMakanan.setAdapter(adapter)

        if (currentLogId != null) {
            binding.tvJudulEditLog.text = "Edit Catatan Makanan"
            binding.btnHapusLogMakanan.visibility = View.VISIBLE
            loadLogData(currentLogId!!)
        } else {
            binding.tvJudulEditLog.text = "Catatan Makanan Baru"
            binding.btnHapusLogMakanan.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        binding.btnPilihFoto.setOnClickListener { showImagePickDialog() }
        binding.btnSimpanLogMakanan.setOnClickListener { validateAndSave() }
        binding.btnHapusLogMakanan.setOnClickListener { showDeleteConfirmation() }
    }

    /**
     * LOGIKA DIUBAH: Memuat data dari Firestore
     */
    private fun loadLogData(logId: String) {
        db.collection("log_makanan").document(logId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val log = doc.toObject(LogMakanan::class.java)
                    if (log != null) {
                        binding.etNamaMakanan.setText(log.nama_makanan)
                        binding.etPorsi.setText(log.porsi.toString())
                        binding.autoCompleteJenisMakanan.setText(log.jenis_makanan, false)

                        existingFotoBase64 = log.foto_makanan_base64 // Ambil string Base64
                        if (existingFotoBase64!!.isNotEmpty()) {
                            // Ubah Base64 ke gambar dan tampilkan
                            val imageBytes = Base64.decode(existingFotoBase64, Base64.DEFAULT)
                            Glide.with(this).load(imageBytes).into(binding.ivFotoPreview)
                        }
                    }
                }
            }
    }

    // (Fungsi showImagePickDialog, checkCameraPermission, openCamera, openGallery tidak berubah)
    private fun showImagePickDialog() {
        val options = arrayOf("Ambil Foto (Kamera)", "Pilih dari Galeri")
        AlertDialog.Builder(this)
            .setTitle("Pilih Sumber Gambar")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpenCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }
    private fun checkCameraPermissionAndOpenCamera() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    private fun openCamera() {
        val newImageUri = MyFileProvider.getImageUri(this)
        imageUri = newImageUri
        cameraLauncher.launch(newImageUri)
    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }


    /**
     * LOGIKA DIUBAH TOTAL: Validasi dan kompres gambar
     */
    private fun validateAndSave() {
        val nama = binding.etNamaMakanan.text.toString().trim()
        val porsiStr = binding.etPorsi.text.toString().trim()
        val jenis = binding.autoCompleteJenisMakanan.text.toString()

        if (nama.isEmpty() || porsiStr.isEmpty() || jenis.isEmpty()) {
            Toast.makeText(this, "Nama, Porsi, dan Jenis Makanan wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Menyimpan...", Toast.LENGTH_SHORT).show()

        val localImageUri = imageUri
        var fotoBase64 = "" // Siapkan string kosong

        try {
            if (localImageUri != null) {
                // 1. Ada gambar BARU. Kompres gambar.
                val bitmap = uriToBitmap(localImageUri) // Ubah Uri ke Bitmap
                val resizedBitmap = resizeBitmap(bitmap, 400) // Perkecil jadi 400px
                fotoBase64 = bitmapToBase64(resizedBitmap) // Ubah ke String Base64

            } else if (currentLogId != null) {
                // 2. Mode Edit, TIDAK ganti gambar. Pakai Base64 yang lama.
                fotoBase64 = existingFotoBase64 ?: ""
            }
            // 3. Mode Create, TIDAK pilih gambar. fotoBase64 tetap string kosong.

            // 4. Simpan data ke Firestore
            saveData(fotoBase64, nama, porsiStr.toDouble(), jenis)

        } catch (e: Exception) {
            Toast.makeText(this, "Gagal memproses gambar: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("EditLog", "Error processing image", e)
        }
    }

    /**
     * FUNGSI DIUBAH: Tidak ada lagi upload, langsung simpan Base64
     */
    private fun saveData(fotoBase64: String, nama: String, porsi: Double, jenis: String) {

        val log = LogMakanan(
            uid = userId,
            tgl_input = Date(),
            nama_makanan = nama,
            porsi = porsi,
            jenis_makanan = jenis,
            foto_makanan_base64 = fotoBase64 // Simpan string Base64
        )

        if (currentLogId == null) {
            // --- MODE CREATE ---
            db.collection("log_makanan")
                .add(log)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menyimpan: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            // --- MODE UPDATE ---
            val updatedLog = log.copy(id = currentLogId!!)
            db.collection("log_makanan").document(currentLogId!!)
                .set(updatedLog)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal update: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    /**
     * FUNGSI DIUBAH: Logika hapus tidak perlu hapus dari storage
     */
    private fun deleteLog() {
        if (currentLogId == null) return

        db.collection("log_makanan").document(currentLogId!!)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menghapus: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // (Fungsi showDeleteConfirmation tidak berubah)
    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Data")
            .setMessage("Apakah Anda yakin ingin menghapus catatan makanan ini?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteLog()
            }
            .setNegativeButton("Batal", null)
            .show()
    }


    // --- FUNGSI HELPER BARU UNTUK GAMBAR ---

    private fun uriToBitmap(uri: Uri): Bitmap {
        return BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        var width = bitmap.width
        var height = bitmap.height
        val bitmapRatio = width.toFloat() / height.toFloat()

        if (bitmapRatio > 1) { // Gambar landscape
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else { // Gambar portrait
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream) // Kompresi JPEG 80%
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}