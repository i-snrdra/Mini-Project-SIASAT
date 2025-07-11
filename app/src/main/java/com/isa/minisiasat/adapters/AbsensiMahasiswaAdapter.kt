package com.isa.minisiasat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isa.minisiasat.databinding.ItemAbsensiMahasiswaBinding
import com.isa.minisiasat.models.AbsensiSession
import com.isa.minisiasat.models.Matkul

class AbsensiMahasiswaAdapter(
    private val onJoinClick: (AbsensiSession) -> Unit
) : RecyclerView.Adapter<AbsensiMahasiswaAdapter.AbsensiViewHolder>() {
    
    private var absensiList = listOf<AbsensiSession>()
    private var matkulList = listOf<Matkul>()
    private var currentMahasiswaNama = ""
    
    fun updateData(newAbsensi: List<AbsensiSession>, newMatkul: List<Matkul>, mahasiswaNama: String) {
        absensiList = newAbsensi.sortedByDescending { it.createdAt }
        matkulList = newMatkul
        currentMahasiswaNama = mahasiswaNama
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbsensiViewHolder {
        val binding = ItemAbsensiMahasiswaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AbsensiViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: AbsensiViewHolder, position: Int) {
        holder.bind(absensiList[position])
    }
    
    override fun getItemCount(): Int = absensiList.size
    
    inner class AbsensiViewHolder(
        private val binding: ItemAbsensiMahasiswaBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(session: AbsensiSession) {
            binding.apply {
                // Find mata kuliah info
                val matkul = matkulList.find { it.kodeMatkul == session.matkulCode }
                
                tvKodeMatkul.text = session.matkulCode
                tvNamaMatkul.text = matkul?.namaMatkul ?: "Mata Kuliah"
                tvDosen.text = "Dosen: ${matkul?.dosenNama ?: "Unknown"}"
                tvRuang.text = "Ruang: ${matkul?.ruang ?: "TBA"}"
                tvJamBuka.text = "Dibuka: ${session.jamBuka}"
                tvTanggal.text = session.tanggal
                
                val jumlahHadir = session.mahasiswaHadir.size
                tvJumlahHadir.text = "$jumlahHadir mahasiswa sudah absen"
                
                // Check if already joined
                val alreadyJoined = session.mahasiswaHadir.contains(currentMahasiswaNama)
                
                if (alreadyJoined) {
                    btnJoinAbsensi.text = "Sudah Absen"
                    btnJoinAbsensi.isEnabled = false
                    btnJoinAbsensi.alpha = 0.6f
                } else {
                    btnJoinAbsensi.text = "Join Absensi"
                    btnJoinAbsensi.isEnabled = true
                    btnJoinAbsensi.alpha = 1.0f
                    btnJoinAbsensi.setOnClickListener {
                        onJoinClick(session)
                    }
                }
            }
        }
    }
} 