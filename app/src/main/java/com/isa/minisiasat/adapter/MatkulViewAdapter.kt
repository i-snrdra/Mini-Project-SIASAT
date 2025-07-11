package com.isa.minisiasat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isa.minisiasat.databinding.ItemMatkulViewBinding
import com.isa.minisiasat.models.Matkul

class MatkulViewAdapter(
    private val onEditClick: (Matkul) -> Unit = {},
    private val onDeleteClick: (Matkul) -> Unit = {}
) : RecyclerView.Adapter<MatkulViewAdapter.MatkulViewHolder>() {
    
    private var matkulList = listOf<Matkul>()
    
    fun updateList(newList: List<Matkul>) {
        matkulList = newList
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatkulViewHolder {
        val binding = ItemMatkulViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MatkulViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MatkulViewHolder, position: Int) {
        holder.bind(matkulList[position])
    }
    
    override fun getItemCount(): Int = matkulList.size
    
    inner class MatkulViewHolder(
        private val binding: ItemMatkulViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(matkul: Matkul) {
            binding.apply {
                // Basic info
                tvKodeMatkul.text = matkul.kodeMatkul
                tvNamaMatkul.text = matkul.namaMatkul
                tvHari.text = matkul.hari
                tvWaktu.text = "${matkul.jamMulai} - ${matkul.jamSelesai}"
                tvSks.text = matkul.sks.toString()
                tvKapasitas.text = "${matkul.kapasitas} mahasiswa"
                
                // Dosen status
                if (matkul.dosenId != null && matkul.dosenNama != null) {
                    tvDosenStatus.text = matkul.dosenNama
                    tvDosenStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                } else {
                    tvDosenStatus.text = "Belum ada dosen"
                    tvDosenStatus.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
                }
                
                // Button clicks
                btnEdit.setOnClickListener {
                    onEditClick(matkul)
                }
                
                btnDelete.setOnClickListener {
                    onDeleteClick(matkul)
                }
            }
        }
    }
} 