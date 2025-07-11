package com.isa.minisiasat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isa.minisiasat.databinding.ItemAbsensiAktifBinding
import com.isa.minisiasat.models.AbsensiSession

class AbsensiAktifAdapter(
    private val onMonitorClick: (AbsensiSession) -> Unit,
    private val onTutupClick: (AbsensiSession) -> Unit
) : RecyclerView.Adapter<AbsensiAktifAdapter.ViewHolder>() {
    
    private var sessionList = listOf<AbsensiSession>()
    
    inner class ViewHolder(private val binding: ItemAbsensiAktifBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(session: AbsensiSession) {
            with(binding) {
                tvMatkul.text = session.matkulCode
                tvWaktu.text = "Dibuka ${session.jamBuka}"
                tvStatus.text = "${session.mahasiswaHadir.size} mahasiswa sudah hadir"
                
                btnMonitor.setOnClickListener {
                    onMonitorClick(session)
                }
                
                btnTutup.setOnClickListener {
                    onTutupClick(session)
                }
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAbsensiAktifBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(sessionList[position])
    }
    
    override fun getItemCount(): Int = sessionList.size
    
    fun updateData(newList: List<AbsensiSession>) {
        sessionList = newList
        notifyDataSetChanged()
    }
} 