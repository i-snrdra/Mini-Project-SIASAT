package com.isa.minisiasat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.isa.minisiasat.databinding.ItemJadwalHariBinding
import com.isa.minisiasat.models.Matkul

class JadwalMahasiswaAdapter : RecyclerView.Adapter<JadwalMahasiswaAdapter.JadwalHariViewHolder>() {
    
    private var jadwalGrouped = mapOf<String, List<Matkul>>()
    private var hariList = listOf<String>()
    
    private val hariUrutan = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    
    fun updateData(newGroupedJadwal: Map<String, List<Matkul>>) {
        jadwalGrouped = newGroupedJadwal
        
        // Sort hari berdasarkan urutan
        hariList = newGroupedJadwal.keys.sortedBy { hari ->
            hariUrutan.indexOf(hari).takeIf { it >= 0 } ?: 999
        }
        
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JadwalHariViewHolder {
        val binding = ItemJadwalHariBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return JadwalHariViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: JadwalHariViewHolder, position: Int) {
        val hari = hariList[position]
        val matkulList = jadwalGrouped[hari] ?: emptyList()
        holder.bind(hari, matkulList)
    }
    
    override fun getItemCount(): Int = hariList.size
    
    inner class JadwalHariViewHolder(
        private val binding: ItemJadwalHariBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private val matkulAdapter = JadwalMatkulAdapter()
        
        init {
            binding.rvMatkul.apply {
                layoutManager = LinearLayoutManager(binding.root.context)
                adapter = matkulAdapter
            }
        }
        
        fun bind(hari: String, matkulList: List<Matkul>) {
            binding.apply {
                tvHari.text = hari
                tvJumlahMatkul.text = "${matkulList.size} mata kuliah"
                
                // Sort by jam mulai
                val sortedMatkul = matkulList.sortedBy { it.jamMulai }
                matkulAdapter.updateData(sortedMatkul)
            }
        }
    }
}

// Adapter untuk mata kuliah dalam setiap hari
class JadwalMatkulAdapter : RecyclerView.Adapter<JadwalMatkulAdapter.MatkulViewHolder>() {
    
    private var matkulList = listOf<Matkul>()
    
    fun updateData(newList: List<Matkul>) {
        matkulList = newList
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatkulViewHolder {
        val binding = com.isa.minisiasat.databinding.ItemJadwalMatkulBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MatkulViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MatkulViewHolder, position: Int) {
        holder.bind(matkulList[position])
    }
    
    override fun getItemCount(): Int = matkulList.size
    
    inner class MatkulViewHolder(
        private val binding: com.isa.minisiasat.databinding.ItemJadwalMatkulBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(matkul: Matkul) {
            binding.apply {
                tvJamMulai.text = matkul.jamMulai
                tvJamSelesai.text = matkul.jamSelesai
                tvKodeMatkul.text = matkul.kodeMatkul
                tvNamaMatkul.text = matkul.namaMatkul
                tvDosen.text = matkul.dosenNama ?: "TBA"
                tvRuang.text = matkul.ruang
                tvSks.text = "${matkul.sks} SKS"
            }
        }
    }
} 