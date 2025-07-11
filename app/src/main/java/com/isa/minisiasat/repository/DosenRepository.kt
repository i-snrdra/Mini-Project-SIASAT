package com.isa.minisiasat.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.isa.minisiasat.models.AbsensiSession
import com.isa.minisiasat.models.Matkul
import com.isa.minisiasat.models.MatkulMahasiswa
import com.isa.minisiasat.models.NilaiAkhir
import com.isa.minisiasat.utils.User
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class DosenRepository {
    private val db = FirebaseFirestore.getInstance()
    
    // ========== MATA KULIAH ==========
    suspend fun getMatkulByDosen(dosenId: String): List<Matkul> {
        return try {
            Log.d("DosenRepository", "Loading matkul for dosenId: $dosenId")
            
            val snapshot = db.collection("matkul")
                .whereEqualTo("dosenId", dosenId)
                .get()
                .await()
            
            Log.d("DosenRepository", "Found ${snapshot.documents.size} documents")
            
            val matkulList = snapshot.documents.mapNotNull { doc ->
                Log.d("DosenRepository", "Document: ${doc.id} -> ${doc.data}")
                doc.toObject(Matkul::class.java)
            }.sortedBy { it.kodeMatkul }
            
            Log.d("DosenRepository", "Parsed ${matkulList.size} matkul: ${matkulList.map { it.kodeMatkul }}")
            
            matkulList
        } catch (e: Exception) {
            Log.e("DosenRepository", "Error loading matkul", e)
            emptyList()
        }
    }
    
    suspend fun getMatkulHariIni(dosenId: String): List<Matkul> {
        return try {
            val today = getHariIni()
            Log.d("DosenRepository", "Today is: $today")
            
            val allMatkul = getMatkulByDosen(dosenId)
            Log.d("DosenRepository", "All matkul: ${allMatkul.map { "${it.kodeMatkul} (${it.hari})" }}")
            
            val matkulHariIni = allMatkul.filter { it.hari.equals(today, ignoreCase = true) }
            Log.d("DosenRepository", "Matkul hari ini: ${matkulHariIni.map { it.kodeMatkul }}")
            
            matkulHariIni
        } catch (e: Exception) {
            Log.e("DosenRepository", "Error in getMatkulHariIni", e)
            emptyList()
        }
    }
    
    // ========== ABSENSI ==========
    suspend fun bukaAbsensi(matkulCode: String, dosenId: String): String? {
        return try {
            val tanggalHariIni = getTanggalHariIni()
            val jamSekarang = getJamSekarang()
            
            val docRef = db.collection("absensi_sessions").document()
            val session = AbsensiSession(
                id = docRef.id,
                matkulCode = matkulCode,
                dosenId = dosenId,
                tanggal = tanggalHariIni,
                jamBuka = jamSekarang,
                status = "terbuka"
            )
            
            docRef.set(session).await()
            docRef.id
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun tutupAbsensi(sessionId: String): Boolean {
        return try {
            val jamSekarang = getJamSekarang()
            
            db.collection("absensi_sessions")
                .document(sessionId)
                .update(
                    mapOf(
                        "jamTutup" to jamSekarang,
                        "status" to "tertutup"
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getAbsensiAktif(dosenId: String): List<AbsensiSession> {
        return try {
            Log.d("DosenRepository", "Loading absensi aktif for dosenId: $dosenId")
            
            val snapshot = db.collection("absensi_sessions")
                .whereEqualTo("dosenId", dosenId)
                .whereEqualTo("status", "terbuka")
                .get()
                .await()
            
            Log.d("DosenRepository", "Found ${snapshot.documents.size} active sessions")
            
            val sessionList = snapshot.documents.mapNotNull { doc ->
                Log.d("DosenRepository", "Active session: ${doc.id} -> ${doc.data}")
                doc.toObject(AbsensiSession::class.java)
            }.sortedBy { it.createdAt }
            
            Log.d("DosenRepository", "Active sessions: ${sessionList.map { "${it.matkulCode}: ${it.status}" }}")
            
            sessionList
        } catch (e: Exception) {
            Log.e("DosenRepository", "Error loading active sessions", e)
            emptyList()
        }
    }
    
    suspend fun getHistoryAbsensi(dosenId: String, matkulCode: String? = null): List<AbsensiSession> {
        return try {
            var query = db.collection("absensi_sessions")
                .whereEqualTo("dosenId", dosenId)
                .whereEqualTo("status", "tertutup")
            
            if (matkulCode != null) {
                query = query.whereEqualTo("matkulCode", matkulCode)
            }
            
            val snapshot = query.get().await()
            
            val sessionList = snapshot.documents.mapNotNull { doc ->
                doc.toObject(AbsensiSession::class.java)
            }.sortedByDescending { it.createdAt }
            
            sessionList
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getAbsensiSession(sessionId: String): AbsensiSession? {
        return try {
            val doc = db.collection("absensi_sessions")
                .document(sessionId)
                .get()
                .await()
            
            doc.toObject(AbsensiSession::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    // ========== NILAI ==========
    suspend fun getMahasiswaByMatkul(matkulCode: String): List<User> {
        return try {
            Log.d("DosenRepository", "Loading mahasiswa for matkulCode: $matkulCode")
            
            val snapshot = db.collection("matkul_mahasiswa")
                .whereEqualTo("matkulCode", matkulCode)
                .get()
                .await()
            
            Log.d("DosenRepository", "Found ${snapshot.documents.size} enrollment documents")
            
            val mahasiswaIds = snapshot.documents.mapNotNull { doc ->
                Log.d("DosenRepository", "Enrollment doc: ${doc.id} -> ${doc.data}")
                doc.toObject(MatkulMahasiswa::class.java)?.mahasiswaId
            }
            
            Log.d("DosenRepository", "Mahasiswa IDs: $mahasiswaIds")
            
            // Get mahasiswa details
            val mahasiswaList = mutableListOf<User>()
            for (id in mahasiswaIds) {
                val mahasiswaDoc = db.collection("users_mahasiswa")
                    .document(id)
                    .get()
                    .await()
                
                val data = mahasiswaDoc.data
                Log.d("DosenRepository", "Mahasiswa $id -> $data")
                if (data != null) {
                    mahasiswaList.add(
                        User(
                            id = id,
                            nama = data["nama"] as? String ?: "",
                            password = data["password"] as? String ?: "",
                            role = "mahasiswa"
                        )
                    )
                }
            }
            
            val sortedList = mahasiswaList.sortedBy { it.nama }
            Log.d("DosenRepository", "Final mahasiswa list: ${sortedList.map { "${it.id}: ${it.nama}" }}")
            
            sortedList
        } catch (e: Exception) {
            Log.e("DosenRepository", "Error loading mahasiswa", e)
            emptyList()
        }
    }
    
    suspend fun simpanNilai(nilaiList: List<NilaiAkhir>): Boolean {
        return try {
            val batch = db.batch()
            
            for (nilai in nilaiList) {
                val docRef = db.collection("nilai_akhir").document("${nilai.matkulCode}_${nilai.mahasiswaId}")
                batch.set(docRef, nilai)
            }
            
            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getNilaiByMatkul(matkulCode: String): List<NilaiAkhir> {
        return try {
            Log.d("DosenRepository", "Loading nilai for matkulCode: $matkulCode")
            
            val snapshot = db.collection("nilai_akhir")
                .whereEqualTo("matkulCode", matkulCode)
                .get()
                .await()
            
            Log.d("DosenRepository", "Found ${snapshot.documents.size} nilai documents")
            
            val nilaiList = snapshot.documents.mapNotNull { doc ->
                Log.d("DosenRepository", "Nilai doc: ${doc.id} -> ${doc.data}")
                doc.toObject(NilaiAkhir::class.java)
            }.sortedBy { it.mahasiswaNama }
            
            Log.d("DosenRepository", "Final nilai list: ${nilaiList.map { "${it.mahasiswaId}: ${it.nilaiAkhir}" }}")
            
            nilaiList
        } catch (e: Exception) {
            Log.e("DosenRepository", "Error loading nilai", e)
            emptyList()
        }
    }
    
    // ========== ENROLLMENT (DUMMY DATA) ==========
    suspend fun createDummyEnrollment() {
        try {
            // Dummy enrollment data - nanti bisa dihapus kalau ada sistem enrollment yang proper
            val enrollments = listOf(
                MatkulMahasiswa("", "TIF123", "672022708", "Isa Noorendra", "2022"),
                MatkulMahasiswa("", "TIF123", "672022134", "Aghus Fajar M", "2022"),
                MatkulMahasiswa("", "TIF123", "672022076", "Ardiva Nugraheni", "2022")
            )
            
            for (enrollment in enrollments) {
                val docRef = db.collection("matkul_mahasiswa").document()
                val enrollmentWithId = enrollment.copy(id = docRef.id)
                docRef.set(enrollmentWithId).await()
            }
        } catch (e: Exception) {
            // Ignore if already exists
        }
    }
    
    // ========== UTILITY FUNCTIONS ==========
    private fun getHariIni(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Senin"
            Calendar.TUESDAY -> "Selasa"
            Calendar.WEDNESDAY -> "Rabu"
            Calendar.THURSDAY -> "Kamis"
            Calendar.FRIDAY -> "Jumat"
            Calendar.SATURDAY -> "Sabtu"
            Calendar.SUNDAY -> "Minggu"
            else -> "Senin"
        }
    }
    
    private fun getTanggalHariIni(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }
    
    private fun getJamSekarang(): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(Date())
    }

    // ========== HISTORY ABSENSI ==========
    suspend fun getHistoryAbsensi(dosenId: String): List<AbsensiSession> {
        return try {
            Log.d("DosenRepository", "Loading history absensi for dosen: $dosenId")
            
            val snapshot = db.collection("absensi_sessions")
                .whereEqualTo("dosenId", dosenId)
                .get()
                .await()
            
            Log.d("DosenRepository", "Found ${snapshot.documents.size} absensi sessions")
            
            val sessionList = snapshot.documents.mapNotNull { doc ->
                Log.d("DosenRepository", "Session doc: ${doc.id} -> ${doc.data}")
                doc.toObject(AbsensiSession::class.java)
            }.sortedByDescending { it.tanggal + " " + it.jamBuka }
            
            Log.d("DosenRepository", "Final history list: ${sessionList.size} items")
            
            sessionList
        } catch (e: Exception) {
            Log.e("DosenRepository", "Error loading history absensi", e)
            emptyList()
        }
    }

    // ========== DEBUG FUNCTIONS ==========
    suspend fun debugFirestoreConnection(): String {
        return try {
            Log.d("DosenRepository", "Testing Firestore connection...")
            
            // Test 1: Get all matkul documents
            val matkulSnapshot = db.collection("matkul").get().await()
            val matkulCount = matkulSnapshot.documents.size
            Log.d("DosenRepository", "Total matkul documents: $matkulCount")
            
            // Test 2: Get all users_dosen documents  
            val dosenSnapshot = db.collection("users_dosen").get().await()
            val dosenCount = dosenSnapshot.documents.size
            Log.d("DosenRepository", "Total dosen documents: $dosenCount")
            
            // Log all dosen IDs
            dosenSnapshot.documents.forEach { doc ->
                Log.d("DosenRepository", "Dosen doc: ${doc.id} -> ${doc.data}")
            }
            
            // Log all matkul with dosenId
            matkulSnapshot.documents.forEach { doc ->
                val data = doc.data
                Log.d("DosenRepository", "Matkul doc: ${doc.id} -> dosenId: ${data?.get("dosenId")} -> ${data}")
            }
            
            "Connection OK. Found $matkulCount matkul, $dosenCount dosen"
        } catch (e: Exception) {
            Log.e("DosenRepository", "Debug failed", e)
            "Error: ${e.message}"
        }
    }
} 