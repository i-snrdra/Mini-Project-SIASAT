package com.isa.minisiasat.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.isa.minisiasat.models.AbsensiSession
import com.isa.minisiasat.models.Matkul
import com.isa.minisiasat.models.MatkulMahasiswa
import com.isa.minisiasat.models.NilaiAkhir
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MahasiswaRepository {
    
    private val db = FirebaseFirestore.getInstance()
    
    // ========== MATA KULIAH ==========
    suspend fun getMatkulByMahasiswa(mahasiswaId: String): List<Matkul> {
        return try {
            Log.d("MahasiswaRepository", "Loading matkul for mahasiswa: $mahasiswaId")
            
            // Get enrolled mata kuliah
            val enrollmentSnapshot = db.collection("matkul_mahasiswa")
                .whereEqualTo("mahasiswaId", mahasiswaId)
                .get()
                .await()
            
            Log.d("MahasiswaRepository", "Found ${enrollmentSnapshot.documents.size} enrollments")
            
            val matkulCodes = enrollmentSnapshot.documents.mapNotNull { doc ->
                doc.toObject(MatkulMahasiswa::class.java)?.matkulCode
            }
            
            Log.d("MahasiswaRepository", "Matkul codes: $matkulCodes")
            
            // Get mata kuliah details
            val matkulList = mutableListOf<Matkul>()
            for (code in matkulCodes) {
                val matkulSnapshot = db.collection("matkul")
                    .whereEqualTo("kodeMatkul", code)
                    .get()
                    .await()
                
                matkulSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(Matkul::class.java)
                }?.let { matkulList.addAll(it) }
            }
            
            val sortedList = matkulList.sortedBy { it.kodeMatkul }
            Log.d("MahasiswaRepository", "Final matkul list: ${sortedList.size} items")
            
            sortedList
        } catch (e: Exception) {
            Log.e("MahasiswaRepository", "Error loading matkul", e)
            emptyList()
        }
    }
    
    suspend fun getMatkulHariIni(mahasiswaId: String): List<Matkul> {
        return try {
            val hariIni = getHariIni()
            val allMatkul = getMatkulByMahasiswa(mahasiswaId)
            
            allMatkul.filter { it.hari == hariIni }
        } catch (e: Exception) {
            Log.e("MahasiswaRepository", "Error loading matkul hari ini", e)
            emptyList()
        }
    }
    
    // ========== ABSENSI ==========
    suspend fun getAbsensiAktif(mahasiswaId: String): List<AbsensiSession> {
        return try {
            Log.d("MahasiswaRepository", "Loading absensi aktif for mahasiswa: $mahasiswaId")
            
            // Get mata kuliah yang diikuti mahasiswa
            val matkulCodes = getMatkulByMahasiswa(mahasiswaId).map { it.kodeMatkul }
            Log.d("MahasiswaRepository", "Mahasiswa enrolled in: $matkulCodes")
            
            if (matkulCodes.isEmpty()) {
                Log.d("MahasiswaRepository", "No enrolled mata kuliah found")
                return emptyList()
            }
            
            // Get active sessions untuk mata kuliah tersebut
            // FIXED: Menggunakan status="terbuka" bukan isActive=true
            val snapshot = db.collection("absensi_sessions")
                .whereIn("matkulCode", matkulCodes)
                .whereEqualTo("status", "terbuka")
                .get()
                .await()
            
            Log.d("MahasiswaRepository", "Found ${snapshot.documents.size} sessions with status=terbuka")
            
            val sessionList = snapshot.documents.mapNotNull { doc ->
                Log.d("MahasiswaRepository", "Session doc: ${doc.id} -> ${doc.data}")
                doc.toObject(AbsensiSession::class.java)
            }.sortedByDescending { it.createdAt }
            
            Log.d("MahasiswaRepository", "Final active sessions: ${sessionList.map { "${it.matkulCode}: ${it.status}" }}")
            
            sessionList
        } catch (e: Exception) {
            Log.e("MahasiswaRepository", "Error loading absensi aktif", e)
            emptyList()
        }
    }
    
    suspend fun joinAbsensi(sessionId: String, mahasiswaId: String, mahasiswaNama: String): Boolean {
        return try {
            Log.d("MahasiswaRepository", "Joining absensi: $sessionId for $mahasiswaId")
            
            val sessionRef = db.collection("absensi_sessions").document(sessionId)
            val sessionDoc = sessionRef.get().await()
            
            if (!sessionDoc.exists()) {
                Log.e("MahasiswaRepository", "Session not found")
                return false
            }
            
            val session = sessionDoc.toObject(AbsensiSession::class.java)
            // FIXED: Menggunakan status="terbuka" bukan isActive=true
            if (session?.status != "terbuka") {
                Log.e("MahasiswaRepository", "Session not active, status: ${session?.status}")
                return false
            }
            
            // Check if already joined
            if (session.mahasiswaHadir.contains(mahasiswaNama)) {
                Log.d("MahasiswaRepository", "Already joined")
                return true
            }
            
            // Add to attendance list
            val updatedList = session.mahasiswaHadir.toMutableList()
            updatedList.add(mahasiswaNama)
            
            sessionRef.update("mahasiswaHadir", updatedList).await()
            
            Log.d("MahasiswaRepository", "Successfully joined absensi")
            true
        } catch (e: Exception) {
            Log.e("MahasiswaRepository", "Error joining absensi", e)
            false
        }
    }
    
    // ========== NILAI ==========
    suspend fun getNilaiByMahasiswa(mahasiswaId: String): List<NilaiAkhir> {
        return try {
            Log.d("MahasiswaRepository", "Loading nilai for mahasiswa: $mahasiswaId")
            
            val snapshot = db.collection("nilai_akhir")
                .whereEqualTo("mahasiswaId", mahasiswaId)
                .get()
                .await()
            
            val nilaiList = snapshot.documents.mapNotNull { doc ->
                doc.toObject(NilaiAkhir::class.java)
            }.sortedBy { it.matkulCode }
            
            Log.d("MahasiswaRepository", "Found ${nilaiList.size} nilai")
            
            nilaiList
        } catch (e: Exception) {
            Log.e("MahasiswaRepository", "Error loading nilai", e)
            emptyList()
        }
    }
    
    // ========== JADWAL ==========
    suspend fun getJadwalKuliah(mahasiswaId: String): List<Matkul> {
        return try {
            val matkulList = getMatkulByMahasiswa(mahasiswaId)
            
            // Sort by hari dan jam
            val hariUrutan = mapOf(
                "Senin" to 1, "Selasa" to 2, "Rabu" to 3, 
                "Kamis" to 4, "Jumat" to 5, "Sabtu" to 6, "Minggu" to 7
            )
            
            matkulList.sortedWith(compareBy(
                { hariUrutan[it.hari] ?: 8 },
                { it.jamMulai }
            ))
        } catch (e: Exception) {
            Log.e("MahasiswaRepository", "Error loading jadwal", e)
            emptyList()
        }
    }
    
    // ========== PROFILE ==========
    suspend fun updatePassword(mahasiswaId: String, newPassword: String): Boolean {
        return try {
            Log.d("MahasiswaRepository", "Updating password for mahasiswa: $mahasiswaId")
            
            db.collection("users_mahasiswa")
                .document(mahasiswaId)
                .update("password", newPassword)
                .await()
            
            Log.d("MahasiswaRepository", "Password updated successfully")
            true
        } catch (e: Exception) {
            Log.e("MahasiswaRepository", "Error updating password", e)
            false
        }
    }
    
    // ========== ENROLLMENT ==========
    suspend fun getAvailableMatkul(): List<Matkul> {
        return try {
            Log.d("MahasiswaRepository", "Loading available mata kuliah")
            
            val snapshot = db.collection("matkul")
                .get()
                .await()
            
            val matkulList = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Matkul::class.java)
            }.sortedBy { it.kodeMatkul }
            
            Log.d("MahasiswaRepository", "Found ${matkulList.size} available mata kuliah")
            
            matkulList
        } catch (e: Exception) {
            Log.e("MahasiswaRepository", "Error loading available matkul", e)
            emptyList()
        }
    }
    
    suspend fun enrollMatkul(mahasiswaId: String, matkulCode: String): Boolean {
        return try {
            Log.d("MahasiswaRepository", "Enrolling mahasiswa $mahasiswaId to $matkulCode")
            
            // Check if already enrolled
            val existingEnrollment = db.collection("matkul_mahasiswa")
                .whereEqualTo("mahasiswaId", mahasiswaId)
                .whereEqualTo("matkulCode", matkulCode)
                .get()
                .await()
            
            if (!existingEnrollment.isEmpty) {
                Log.d("MahasiswaRepository", "Already enrolled")
                return false // Already enrolled
            }
            
            // Create new enrollment
            val enrollment = MatkulMahasiswa(
                id = "${mahasiswaId}_${matkulCode}",
                mahasiswaId = mahasiswaId,
                matkulCode = matkulCode,
                tanggalDaftar = getTanggalHariIni(),
                status = "aktif"
            )
            
            db.collection("matkul_mahasiswa")
                .document(enrollment.id)
                .set(enrollment)
                .await()
            
            Log.d("MahasiswaRepository", "Successfully enrolled")
            true
        } catch (e: Exception) {
            Log.e("MahasiswaRepository", "Error enrolling matkul", e)
            false
        }
    }
    
    suspend fun dropMatkul(mahasiswaId: String, matkulCode: String): Boolean {
        return try {
            Log.d("MahasiswaRepository", "Dropping matkul $matkulCode for $mahasiswaId")
            
            val enrollmentSnapshot = db.collection("matkul_mahasiswa")
                .whereEqualTo("mahasiswaId", mahasiswaId)
                .whereEqualTo("matkulCode", matkulCode)
                .get()
                .await()
            
            if (enrollmentSnapshot.isEmpty) {
                Log.d("MahasiswaRepository", "Enrollment not found")
                return false
            }
            
            // Delete enrollment
            for (document in enrollmentSnapshot.documents) {
                document.reference.delete().await()
            }
            
            Log.d("MahasiswaRepository", "Successfully dropped matkul")
            true
        } catch (e: Exception) {
            Log.e("MahasiswaRepository", "Error dropping matkul", e)
            false
        }
    }
    
    suspend fun getEnrollmentStatus(mahasiswaId: String): Map<String, Boolean> {
        return try {
            Log.d("MahasiswaRepository", "Getting enrollment status for $mahasiswaId")
            
            val enrollmentSnapshot = db.collection("matkul_mahasiswa")
                .whereEqualTo("mahasiswaId", mahasiswaId)
                .get()
                .await()
            
            val enrolledMatkul = enrollmentSnapshot.documents.mapNotNull { doc ->
                doc.toObject(MatkulMahasiswa::class.java)?.matkulCode
            }
            
            // Get all available matkul
            val allMatkul = getAvailableMatkul()
            
            // Create status map
            val statusMap = mutableMapOf<String, Boolean>()
            for (matkul in allMatkul) {
                statusMap[matkul.kodeMatkul] = enrolledMatkul.contains(matkul.kodeMatkul)
            }
            
            Log.d("MahasiswaRepository", "Enrollment status: ${statusMap.size} matkul checked")
            
            statusMap
        } catch (e: Exception) {
            Log.e("MahasiswaRepository", "Error getting enrollment status", e)
            emptyMap()
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
} 