package com.example.myapplication

import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ResepAdapter(
    private val resepList: List<Resep>,
    private val onItemClicked: (Resep) -> Unit
) : RecyclerView.Adapter<ResepAdapter.ResepViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResepViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_resep, parent, false)
        return ResepViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResepViewHolder, position: Int) {
        val resep = resepList[position]
        holder.bind(resep)
        holder.itemView.setOnClickListener { onItemClicked(resep) }
    }

    override fun getItemCount(): Int = resepList.size

    class ResepViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivResepImage)
        private val tvName: TextView = itemView.findViewById(R.id.tvResepName)
        private val tvKalori: TextView = itemView.findViewById(R.id.tvResepKalori)
        private val tvGI: TextView = itemView.findViewById(R.id.tvResepGI)

        fun bind(resep: Resep) {
            tvName.text = resep.nama_resep
            tvKalori.text = "${resep.total_kalori} kkal"
            tvGI.text = "GI ${resep.level_gi}"


            // Logika Gambar Base64 (Sama seperti LogMakanan)
            if (resep.foto_resep_base64.isNotEmpty()) {
                try {
                    val imageBytes = Base64.decode(resep.foto_resep_base64, Base64.DEFAULT)
                    Glide.with(itemView.context)
                        .load(imageBytes)
                        .centerCrop()
                        .placeholder(R.color.gray)
                        .into(ivImage)
                } catch (e: Exception) {
                    ivImage.setImageResource(R.color.gray)
                }
            } else {
                ivImage.setImageResource(R.color.gray)
            }
        }
    }
}