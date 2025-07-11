package com.isa.minisiasat

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.isa.minisiasat.databinding.ActivityDataSeederBinding

class DataSeederActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataSeederBinding
    private val db = FirebaseFirestore.getInstance()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataSeederBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.btnSeedData.setOnClickListener {
            showLoading(true)
            seedAllData()
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSeedData.isEnabled = false
            binding.btnSeedData.text = "Seeding Data..."
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnSeedData.isEnabled = true
            binding.btnSeedData.text = "Seed Data ke Firestore"
        }
    }
    
    private fun seedAllData() {
        seedKaprogdiData()
        seedDosenData()
        seedMahasiswaData()
    }
    
    private fun seedKaprogdiData() {
        val kaprogdiData = mapOf(
            "nama" to "Prof. Dr. Ir. Eko Sediyono, M.Kom.",
            "password" to "67003",
            "role" to "kaprogdi"
        )
        
        db.collection("users_kaprogdi")
            .document("67003")
            .set(kaprogdiData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data Kaprogdi berhasil di-seed", Toast.LENGTH_SHORT).show()
                checkIfAllDataSeeded()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error seeding Kaprogdi: ${e.message}", Toast.LENGTH_SHORT).show()
                showLoading(false)
            }
    }
    
    private fun seedDosenData() {
        val dosenList = listOf(
            mapOf(
                "id" to "67506",
                "nama" to "Dr. Sri Yulianto Joko Prasetyo",
                "password" to "67506",
                "role" to "dosen"
            ),
            mapOf(
                "id" to "67002",
                "nama" to "Magdalena A. Ineke Pakereng",
                "password" to "67002",
                "role" to "dosen"
            ),
            mapOf(
                "id" to "67515",
                "nama" to "Dr. Adi Nugroho",
                "password" to "67515",
                "role" to "dosen"
            )
        )
        
        dosenList.forEach { dosen ->
            val dosenData = mapOf(
                "nama" to dosen["nama"],
                "password" to dosen["password"],
                "role" to dosen["role"]
            )
            
            db.collection("users_dosen")
                .document(dosen["id"] as String)
                .set(dosenData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data Dosen ${dosen["nama"]} berhasil di-seed", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error seeding Dosen ${dosen["nama"]}: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    
    private fun seedMahasiswaData() {
        val mahasiswaList = listOf(
            mapOf(
                "id" to "672022708",
                "nama" to "Isa Noorendra",
                "tahunMasuk" to "2022",
                "password" to "672022708",
                "role" to "mahasiswa"
            ),
            mapOf(
                "id" to "672022134",
                "nama" to "Aghus Fajar M",
                "tahunMasuk" to "2022",
                "password" to "672022134",
                "role" to "mahasiswa"
            ),
            mapOf(
                "id" to "672022076",
                "nama" to "Ardiva Nugraheni",
                "tahunMasuk" to "2022",
                "password" to "672022076",
                "role" to "mahasiswa"
            )
        )
        
        mahasiswaList.forEach { mahasiswa ->
            val mahasiswaData = mapOf(
                "nama" to mahasiswa["nama"],
                "tahunMasuk" to mahasiswa["tahunMasuk"],
                "password" to mahasiswa["password"],
                "role" to mahasiswa["role"]
            )
            
            db.collection("users_mahasiswa")
                .document(mahasiswa["id"] as String)
                .set(mahasiswaData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data Mahasiswa ${mahasiswa["nama"]} berhasil di-seed", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error seeding Mahasiswa ${mahasiswa["nama"]}: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    
    private fun checkIfAllDataSeeded() {
        // Simulate a delay to allow all operations to complete
        binding.root.postDelayed({
            showLoading(false)
            Toast.makeText(this, "Semua data berhasil di-seed ke Firestore!", Toast.LENGTH_LONG).show()
        }, 2000)
    }
} 