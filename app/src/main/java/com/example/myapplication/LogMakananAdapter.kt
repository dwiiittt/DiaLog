package com.example.myapplication

import android.util.Base64 // <-- IMPORT BARU
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class LogMakananAdapter(
    private var logList: List<LogMakanan>,
    private val onItemClicked: (LogMakanan) -> Unit
) : RecyclerView.Adapter<LogMakananAdapter.LogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_log_makanan, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logList[position]
        holder.bind(log)
        holder.itemView.setOnClickListener {
            onItemClicked(log)
        }
    }

    override fun getItemCount(): Int = logList.size

    fun updateData(newList: List<LogMakanan>) {
        logList = newList
        notifyDataSetChanged()
    }

    class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivFoto: ImageView = itemView.findViewById(R.id.ivItemFoto)
        private val tvNama: TextView = itemView.findViewById(R.id.tvItemNamaMakanan)
        private val tvJenis: TextView = itemView.findViewById(R.id.tvItemJenisMakanan)
        private val tvTanggal: TextView = itemView.findViewById(R.id.tvItemTanggal)

        private val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

        fun bind(log: LogMakanan) {
            tvNama.text = log.nama_makanan
            tvJenis.text = log.jenis_makanan

            if (log.tgl_input != null) {
                tvTanggal.text = sdf.format(log.tgl_input)
            } else {
                tvTanggal.text = ""
            }

            // --- LOGIKA GAMBAR YANG DIUBAH ---
            if (log.foto_makanan_base64.isNotEmpty()) {
                // Ubah String Base64 kembali menjadi gambar
                val imageBytes = Base64.decode(log.foto_makanan_base64, Base64.DEFAULT)
                Glide.with(itemView.context)
                    .load(imageBytes) // Muat byte gambar
                    .placeholder(R.color.gray)
                    .into(ivFoto)
            } else {
                // Jika tidak ada foto, tampilkan placeholder
                ivFoto.setImageResource(R.color.gray)
            }
        }
    }
}