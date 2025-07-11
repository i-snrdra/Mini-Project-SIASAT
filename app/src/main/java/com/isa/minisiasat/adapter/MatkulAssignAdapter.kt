package com.isa.minisiasat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isa.minisiasat.databinding.ItemMatkulAssignBinding
import com.isa.minisiasat.models.Matkul

class MatkulAssignAdapter(
    private val onAssignClick: (Matkul) -> Unit,
    private val onUnassignClick: (Matkul) -> Unit
) : RecyclerView.Adapter<MatkulAssignAdapter.MatkulViewHolder>() {
    
    private var matkulList = listOf<Matkul>()
    
    fun updateList(newList: List<Matkul>) {
        matkulList = newList
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatkulViewHolder {
        val binding = ItemMatkulAssignBinding.inflate(
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
        private val binding: ItemMatkulAssignBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(matkul: Matkul) {
            binding.apply {
                // Basic info
                tvKodeMatkul.text = matkul.kodeMatkul
                tvNamaMatkul.text = matkul.namaMatkul
                tvJadwal.text = "${matkul.hari}, ${matkul.jamMulai}-${matkul.jamSelesai}"
                tvSksKapasitas.text = "${matkul.sks} SKS • ${matkul.kapasitas} mhs"
                
                // Dosen status
                if (matkul.dosenId != null && matkul.dosenNama != null) {
                    tvDosenStatus.text = matkul.dosenNama
                    tvDosenStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                    
                    // Show reassign and unassign buttons, hide assign button
                    btnAssign.visibility = View.GONE
                    btnReassign.visibility = View.VISIBLE
                    btnUnassign.visibility = View.VISIBLE
                    
                    btnReassign.setOnClickListener {
                        onAssignClick(matkul) // Reassign menggunakan fungsi yang sama
                    }
                    
                    btnUnassign.setOnClickListener {
                        onUnassignClick(matkul)
                    }
                } else {
                    tvDosenStatus.text = "Belum ada dosen"
                    tvDosenStatus.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
                    
                    // Show assign button, hide reassign and unassign buttons
                    btnAssign.visibility = View.VISIBLE
                    btnReassign.visibility = View.GONE
                    btnUnassign.visibility = View.GONE
                    
                    btnAssign.setOnClickListener {
                        onAssignClick(matkul)
                    }
                }
            }
        }
    }
} 