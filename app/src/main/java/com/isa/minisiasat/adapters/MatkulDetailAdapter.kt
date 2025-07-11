package com.isa.minisiasat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isa.minisiasat.databinding.ItemMatkulDetailBinding
import com.isa.minisiasat.models.Matkul

class MatkulDetailAdapter(
    private val onDetailClick: (Matkul) -> Unit
) : RecyclerView.Adapter<MatkulDetailAdapter.MatkulViewHolder>() {
    
    private var matkulList = listOf<Matkul>()
    
    fun updateData(newList: List<Matkul>) {
        matkulList = newList.sortedBy { it.kodeMatkul }
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatkulViewHolder {
        val binding = ItemMatkulDetailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MatkulViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MatkulViewHolder, position: Int) {
        holder.bind(matkulList[position])
    }
    
    override fun getItemCount(): Int = matkulList.size
    
    inner class MatkulViewHolder(
        private val binding: ItemMatkulDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(matkul: Matkul) {
            binding.apply {
                tvKodeMatkul.text = matkul.kodeMatkul
                tvNamaMatkul.text = matkul.namaMatkul
                tvSks.text = "${matkul.sks} SKS"
                tvSemester.text = "Semester ${matkul.semester}"
                tvJadwal.text = "${matkul.hari}, ${matkul.jamMulai} - ${matkul.jamSelesai}"
                tvRuang.text = "Ruang ${matkul.ruang}"
                
                // Card click for detail
                root.setOnClickListener { onDetailClick(matkul) }
            }
        }
    }
} 