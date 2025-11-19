package com.example.myapplication

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class LogGulaAdapter(private val logList: List<LogGula>) :
    RecyclerView.Adapter<LogGulaAdapter.LogViewHolder>() {

    // Ini membuat ViewHolder (penampung) untuk setiap item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_log_gula, parent, false)
        return LogViewHolder(view)
    }

    // Ini mengikat data dari list Anda ke komponen di layout item
    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logList[position]
        holder.bind(log)
    }

    // Ini memberi tahu RecyclerView berapa banyak item yang ada
    override fun getItemCount(): Int = logList.size

    /**
     * ViewHolder: Menghubungkan ID dari item_log_gula.xml ke variabel
     */
    class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNilaiGula: TextView = itemView.findViewById(R.id.tvItemNilaiGula)
        private val tvStatusHasil: TextView = itemView.findViewById(R.id.tvItemStatusHasil)
        private val tvTanggalWaktu: TextView = itemView.findViewById(R.id.tvItemTanggalWaktu)
        private val tvJenisPengukuran: TextView = itemView.findViewById(R.id.tvItemJenisPengukuran)

        // Format tanggal yang kita inginkan
        private val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

        fun bind(log: LogGula) {
            tvNilaiGula.text = log.nilai_gula.toString()
            tvJenisPengukuran.text = log.jenis_pengukuran

            // Format tanggal dari Date ke String
            if (log.tgl_input != null) {
                tvTanggalWaktu.text = sdf.format(log.tgl_input)
            } else {
                tvTanggalWaktu.text = "Tanggal tidak diketahui"
            }

            // Atur Status dan Warna
            tvStatusHasil.text = log.status_hasil
            when (log.status_hasil) {
                "Normal" -> tvStatusHasil.setTextColor(Color.parseColor("#008000")) // Hijau
                "Prediabetes" -> tvStatusHasil.setTextColor(Color.parseColor("#FFA500")) // Oranye
                "Diabetes" -> tvStatusHasil.setTextColor(Color.parseColor("#FF0000")) // Merah
                else -> tvStatusHasil.setTextColor(Color.BLACK)
            }
        }
    }
}