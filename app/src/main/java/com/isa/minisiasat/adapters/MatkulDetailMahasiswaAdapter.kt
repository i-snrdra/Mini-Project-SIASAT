package com.isa.minisiasat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isa.minisiasat.databinding.ItemMatkulDetailMahasiswaBinding
import com.isa.minisiasat.models.Matkul

class MatkulDetailMahasiswaAdapter(
    private val onItemClick: (Matkul) -> Unit
) : RecyclerView.Adapter<MatkulDetailMahasiswaAdapter.MatkulViewHolder>() {
    
    private var matkulList = listOf<Matkul>()
    
    fun updateData(newList: List<Matkul>) {
        matkulList = newList.sortedBy { it.kodeMatkul }
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatkulViewHolder {
        val binding = ItemMatkulDetailMahasiswaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MatkulViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MatkulViewHolder, position: Int) {
        holder.bind(matkulList[position])
    }
    
    override fun getItemCount(): Int = matkulList.size
    
    inner class MatkulViewHolder(
        private val binding: ItemMatkulDetailMahasiswaBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(matkul: Matkul) {
            binding.apply {
                tvKodeMatkul.text = matkul.kodeMatkul
                tvNamaMatkul.text = matkul.namaMatkul
                tvSks.text = "${matkul.sks} SKS"
                tvSemester.text = "Semester ${matkul.semester}"
                tvDosen.text = "Dosen: ${matkul.dosenNama ?: "TBA"}"
                tvJadwal.text = "${matkul.hari}, ${matkul.jamMulai} - ${matkul.jamSelesai}"
                tvRuang.text = "Ruang: ${matkul.ruang}"
                
                root.setOnClickListener {
                    onItemClick(matkul)
                }
            }
        }
    }
} 