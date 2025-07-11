package com.isa.minisiasat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isa.minisiasat.databinding.ItemHistoryAbsensiBinding
import com.isa.minisiasat.models.AbsensiSession

class HistoryAbsensiAdapter(
    private val onItemClick: (AbsensiSession) -> Unit
) : RecyclerView.Adapter<HistoryAbsensiAdapter.HistoryViewHolder>() {
    
    private var historyList = listOf<AbsensiSession>()
    
    fun updateData(newList: List<AbsensiSession>) {
        historyList = newList.sortedByDescending { it.tanggal + " " + it.jamBuka }
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryAbsensiBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position])
    }
    
    override fun getItemCount(): Int = historyList.size
    
    inner class HistoryViewHolder(
        private val binding: ItemHistoryAbsensiBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(session: AbsensiSession) {
            binding.apply {
                tvMatkulCode.text = session.matkulCode
                tvTanggal.text = session.tanggal
                tvJamBuka.text = "Dibuka: ${session.jamBuka}"
                
                if (session.jamTutup != null) {
                    tvStatus.text = "Selesai"
                    tvStatus.setBackgroundResource(android.R.color.holo_green_light)
                    tvJamTutup.text = "Ditutup: ${session.jamTutup}"
                } else {
                    tvStatus.text = "Aktif"
                    tvStatus.setBackgroundResource(android.R.color.holo_orange_light)
                    tvJamTutup.text = "Masih berlangsung"
                }
                
                val jumlahHadir = session.mahasiswaHadir.size
                tvJumlahHadir.text = "$jumlahHadir mahasiswa hadir"
                
                root.setOnClickListener {
                    onItemClick(session)
                }
            }
        }
    }
} 