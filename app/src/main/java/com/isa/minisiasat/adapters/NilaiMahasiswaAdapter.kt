package com.isa.minisiasat.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isa.minisiasat.databinding.ItemNilaiMahasiswaBinding
import com.isa.minisiasat.models.NilaiAkhir

class NilaiMahasiswaAdapter : RecyclerView.Adapter<NilaiMahasiswaAdapter.NilaiViewHolder>() {
    
    private var nilaiList = listOf<NilaiAkhir>()
    
    fun updateData(newList: List<NilaiAkhir>) {
        nilaiList = newList.sortedBy { it.matkulCode }
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NilaiViewHolder {
        val binding = ItemNilaiMahasiswaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NilaiViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: NilaiViewHolder, position: Int) {
        holder.bind(nilaiList[position])
    }
    
    override fun getItemCount(): Int = nilaiList.size
    
    inner class NilaiViewHolder(
        private val binding: ItemNilaiMahasiswaBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(nilai: NilaiAkhir) {
            binding.apply {
                tvKodeMatkul.text = nilai.matkulCode
                tvNamaMatkul.text = nilai.matkulName ?: "Mata Kuliah"
                
                // Set nilai dengan color coding
                tvNilaiAkhir.text = nilai.nilaiAkhir
                tvNilaiAkhir.setTextColor(getNilaiColor(nilai.nilaiAkhir))
                
                // Show bobot nilai
                val bobot = getNilaiBobot(nilai.nilaiAkhir)
                tvBobot.text = "Bobot: $bobot"
                
                // Status lulus/tidak lulus
                val status = if (bobot >= 2.0) "LULUS" else "TIDAK LULUS"
                val statusColor = if (bobot >= 2.0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
                
                tvStatus.text = status
                tvStatus.setTextColor(statusColor)
                
                // Tanggal input
                tvTanggalInput.text = "Diinput: ${nilai.tanggalInput}"
            }
        }
        
        private fun getNilaiColor(nilai: String): Int {
            return when (nilai.uppercase()) {
                "A" -> Color.parseColor("#4CAF50") // Green
                "B" -> Color.parseColor("#8BC34A") // Light Green
                "C" -> Color.parseColor("#FF9800") // Orange
                "D" -> Color.parseColor("#FF5722") // Deep Orange
                "E" -> Color.parseColor("#F44336") // Red
                else -> Color.parseColor("#9E9E9E") // Gray
            }
        }
        
        private fun getNilaiBobot(nilai: String): Double {
            return when (nilai.uppercase()) {
                "A" -> 4.0
                "B" -> 3.0
                "C" -> 2.0
                "D" -> 1.0
                "E" -> 0.0
                else -> 0.0
            }
        }
    }
} 