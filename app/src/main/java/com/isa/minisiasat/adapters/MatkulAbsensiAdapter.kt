package com.isa.minisiasat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isa.minisiasat.databinding.ItemMatkulAbsensiBinding
import com.isa.minisiasat.models.Matkul

class MatkulAbsensiAdapter(
    private val onBukaAbsensiClick: (Matkul) -> Unit
) : RecyclerView.Adapter<MatkulAbsensiAdapter.ViewHolder>() {
    
    private var matkulList = listOf<Matkul>()
    
    inner class ViewHolder(private val binding: ItemMatkulAbsensiBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(matkul: Matkul) {
            with(binding) {
                tvKodeMatkul.text = matkul.kodeMatkul
                tvNamaMatkul.text = matkul.namaMatkul
                tvJadwal.text = "${matkul.hari} • ${matkul.jamMulai} - ${matkul.jamSelesai}"
                tvDetail.text = "${matkul.sks} SKS • ${matkul.kapasitas} mahasiswa"
                
                btnBukaAbsensi.setOnClickListener {
                    onBukaAbsensiClick(matkul)
                }
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMatkulAbsensiBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(matkulList[position])
    }
    
    override fun getItemCount(): Int = matkulList.size
    
    fun updateData(newList: List<Matkul>) {
        matkulList = newList
        notifyDataSetChanged()
    }
} 