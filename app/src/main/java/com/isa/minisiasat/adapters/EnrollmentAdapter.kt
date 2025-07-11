package com.isa.minisiasat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isa.minisiasat.databinding.ItemEnrollmentMatkulBinding
import com.isa.minisiasat.models.Matkul

class EnrollmentAdapter(
    private val onEnrollClick: (Matkul) -> Unit,
    private val onDropClick: (Matkul) -> Unit,
    private val onDetailClick: (Matkul) -> Unit
) : RecyclerView.Adapter<EnrollmentAdapter.EnrollmentViewHolder>() {
    
    private var matkulList = listOf<Matkul>()
    private var enrollmentStatus = mapOf<String, Boolean>()
    
    fun updateData(newMatkulList: List<Matkul>, newEnrollmentStatus: Map<String, Boolean>) {
        matkulList = newMatkulList.sortedBy { it.kodeMatkul }
        enrollmentStatus = newEnrollmentStatus
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnrollmentViewHolder {
        val binding = ItemEnrollmentMatkulBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EnrollmentViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: EnrollmentViewHolder, position: Int) {
        holder.bind(matkulList[position])
    }
    
    override fun getItemCount(): Int = matkulList.size
    
    inner class EnrollmentViewHolder(
        private val binding: ItemEnrollmentMatkulBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(matkul: Matkul) {
            binding.apply {
                tvKodeMatkul.text = matkul.kodeMatkul
                tvNamaMatkul.text = matkul.namaMatkul
                tvSks.text = "${matkul.sks} SKS"
                tvSemester.text = "Semester ${matkul.semester}"
                tvDosen.text = "Dosen: ${matkul.dosenNama ?: "TBA"}"
                tvJadwal.text = "${matkul.hari}, ${matkul.jamMulai} - ${matkul.jamSelesai}"
                tvRuang.text = "Ruang: ${matkul.ruang}"
                
                val isEnrolled = enrollmentStatus[matkul.kodeMatkul] == true
                
                if (isEnrolled) {
                    // Already enrolled
                    btnEnroll.text = "Batalkan"
                    btnEnroll.setBackgroundColor(android.graphics.Color.parseColor("#F44336"))
                    btnEnroll.setOnClickListener {
                        onDropClick(matkul)
                    }
                    
                    // Status enrolled
                    tvStatus.text = "✓ Sudah Diambil"
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                } else {
                    // Not enrolled
                    btnEnroll.text = "Ambil"
                    btnEnroll.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                    btnEnroll.setOnClickListener {
                        onEnrollClick(matkul)
                    }
                    
                    // Status not enrolled
                    tvStatus.text = "Belum Diambil"
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#757575"))
                }
                
                // Detail button
                btnDetail.setOnClickListener {
                    onDetailClick(matkul)
                }
            }
        }
    }
} 