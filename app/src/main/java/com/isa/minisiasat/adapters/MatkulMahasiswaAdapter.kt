package com.isa.minisiasat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isa.minisiasat.databinding.ItemMatkulMahasiswaBinding
import com.isa.minisiasat.models.Matkul

class MatkulMahasiswaAdapter : RecyclerView.Adapter<MatkulMahasiswaAdapter.MatkulViewHolder>() {
    
    private var matkulList = listOf<Matkul>()
    
    fun updateData(newList: List<Matkul>) {
        matkulList = newList.sortedBy { it.jamMulai }
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatkulViewHolder {
        val binding = ItemMatkulMahasiswaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MatkulViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MatkulViewHolder, position: Int) {
        holder.bind(matkulList[position])
    }
    
    override fun getItemCount(): Int = matkulList.size
    
    inner class MatkulViewHolder(
        private val binding: ItemMatkulMahasiswaBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(matkul: Matkul) {
            binding.apply {
                tvKodeMatkul.text = matkul.kodeMatkul
                tvNamaMatkul.text = matkul.namaMatkul
                tvJadwal.text = "${matkul.jamMulai} - ${matkul.jamSelesai}"
                tvDosen.text = "Dosen: ${matkul.dosenNama ?: "TBA"}"
                tvRuang.text = "Ruang: ${matkul.ruang}"
            }
        }
    }
} 