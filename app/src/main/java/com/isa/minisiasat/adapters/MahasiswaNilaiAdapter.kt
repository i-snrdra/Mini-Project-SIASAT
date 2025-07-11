package com.isa.minisiasat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.isa.minisiasat.databinding.ItemMahasiswaNilaiBinding
import com.isa.minisiasat.models.NilaiAkhir
import com.isa.minisiasat.utils.User

class MahasiswaNilaiAdapter : RecyclerView.Adapter<MahasiswaNilaiAdapter.ViewHolder>() {
    
    private var mahasiswaList = listOf<User>()
    private var nilaiMap = mutableMapOf<String, String>() // mahasiswa_id -> nilai
    private var existingNilaiMap = mutableMapOf<String, String>() // existing nilai from database
    
    private val gradeOptions = listOf("", "A", "B", "C", "D", "E") // Empty untuk belum diisi
    
    inner class ViewHolder(private val binding: ItemMahasiswaNilaiBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(mahasiswa: User) {
            with(binding) {
                tvNamaMahasiswa.text = mahasiswa.nama
                tvIdMahasiswa.text = mahasiswa.id
                
                // Setup spinner
                val adapter = ArrayAdapter(binding.root.context, android.R.layout.simple_spinner_item, gradeOptions)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerNilai.adapter = adapter
                
                // Set existing nilai if available
                val existingNilai = existingNilaiMap[mahasiswa.id] ?: ""
                val currentNilai = nilaiMap[mahasiswa.id] ?: existingNilai
                
                val position = gradeOptions.indexOf(currentNilai)
                if (position >= 0) {
                    spinnerNilai.setSelection(position)
                }
                
                // Set listener untuk perubahan nilai
                spinnerNilai.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                        val selectedGrade = gradeOptions[position]
                        nilaiMap[mahasiswa.id] = selectedGrade
                    }
                    
                    override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                })
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMahasiswaNilaiBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mahasiswaList[position])
    }
    
    override fun getItemCount(): Int = mahasiswaList.size
    
    fun updateData(newList: List<User>) {
        val diffCallback = MahasiswaDiffCallback(mahasiswaList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        mahasiswaList = newList
        diffResult.dispatchUpdatesTo(this)
    }
    
    fun setExistingNilai(nilaiList: List<NilaiAkhir>) {
        existingNilaiMap.clear()
        for (nilai in nilaiList) {
            existingNilaiMap[nilai.mahasiswaId] = nilai.nilaiAkhir
        }
        notifyDataSetChanged()
    }
    
    fun getAllNilai(): List<MahasiswaNilaiItem> {
        return mahasiswaList.map { mahasiswa ->
            val nilai = nilaiMap[mahasiswa.id] ?: existingNilaiMap[mahasiswa.id] ?: ""
            MahasiswaNilaiItem(mahasiswa, nilai)
        }
    }
    
    private class MahasiswaDiffCallback(
        private val oldList: List<User>,
        private val newList: List<User>
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

data class MahasiswaNilaiItem(
    val mahasiswa: User,
    val nilai: String
) 