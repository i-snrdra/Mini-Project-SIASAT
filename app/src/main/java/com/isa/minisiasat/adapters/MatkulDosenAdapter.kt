package com.isa.minisiasat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.isa.minisiasat.databinding.ItemMatkulDosenBinding
import com.isa.minisiasat.models.Matkul

class MatkulDosenAdapter(
    private val onBukaAbsensiClick: (Matkul) -> Unit
) : RecyclerView.Adapter<MatkulDosenAdapter.ViewHolder>() {
    
    private var matkulList = listOf<Matkul>()
    
    inner class ViewHolder(private val binding: ItemMatkulDosenBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(matkul: Matkul) {
            with(binding) {
                tvKodeMatkul.text = matkul.kodeMatkul
                tvNamaMatkul.text = matkul.namaMatkul
                tvJadwal.text = "${matkul.jamMulai} - ${matkul.jamSelesai}"
                tvSksKapasitas.text = "${matkul.sks} SKS • ${matkul.kapasitas} mhs"
                
                btnBukaAbsensi.setOnClickListener {
                    onBukaAbsensiClick(matkul)
                }
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMatkulDosenBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(matkulList[position])
    }
    
    override fun getItemCount(): Int = matkulList.size
    
    fun updateData(newList: List<Matkul>) {
        val diffCallback = MatkulDiffCallback(matkulList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        matkulList = newList
        diffResult.dispatchUpdatesTo(this)
    }
    
    private class MatkulDiffCallback(
        private val oldList: List<Matkul>,
        private val newList: List<Matkul>
    ) : DiffUtil.Callback() {
        
        override fun getOldListSize(): Int = oldList.size
        
        override fun getNewListSize(): Int = newList.size
        
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].kodeMatkul == newList[newItemPosition].kodeMatkul
        }
        
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
} 