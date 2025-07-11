package com.isa.minisiasat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.isa.minisiasat.MahasiswaHadirItem
import com.isa.minisiasat.databinding.ItemMahasiswaHadirBinding

class MahasiswaHadirAdapter : RecyclerView.Adapter<MahasiswaHadirAdapter.ViewHolder>() {
    
    private var mahasiswaList = listOf<MahasiswaHadirItem>()
    
    inner class ViewHolder(private val binding: ItemMahasiswaHadirBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(mahasiswa: MahasiswaHadirItem) {
            with(binding) {
                tvNamaMahasiswa.text = mahasiswa.nama
                tvIdMahasiswa.text = mahasiswa.id
                tvWaktuAbsen.text = mahasiswa.waktuAbsen
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMahasiswaHadirBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mahasiswaList[position])
    }
    
    override fun getItemCount(): Int = mahasiswaList.size
    
    fun updateData(newList: List<MahasiswaHadirItem>) {
        val diffCallback = MahasiswaDiffCallback(mahasiswaList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        mahasiswaList = newList
        diffResult.dispatchUpdatesTo(this)
    }
    
    private class MahasiswaDiffCallback(
        private val oldList: List<MahasiswaHadirItem>,
        private val newList: List<MahasiswaHadirItem>
    ) : DiffUtil.Callback() {
        
        override fun getOldListSize(): Int = oldList.size
        
        override fun getNewListSize(): Int = newList.size
        
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }
        
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
} 