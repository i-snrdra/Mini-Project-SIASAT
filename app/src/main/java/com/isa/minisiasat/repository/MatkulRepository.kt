package com.isa.minisiasat.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.isa.minisiasat.models.Matkul
import com.isa.minisiasat.utils.User
import kotlinx.coroutines.tasks.await

class MatkulRepository {
    private val db = FirebaseFirestore.getInstance()
    
    enum class TambahMatkulResult {
        SUCCESS,
        DUPLICATE_CODE,
        ERROR
    }
    
    suspend fun tambahMatkul(matkul: Matkul): TambahMatkulResult {
        return try {
            // Cek apakah kode mata kuliah sudah ada
            val existingDoc = db.collection("matkul")
                .document(matkul.kodeMatkul)
                .get()
                .await()
            
            if (existingDoc.exists()) {
                return TambahMatkulResult.DUPLICATE_CODE
            }
            
            // Gunakan kodeMatkul sebagai document ID
            val matkulWithId = matkul.copy(id = matkul.kodeMatkul)
            db.collection("matkul")
                .document(matkul.kodeMatkul)
                .set(matkulWithId)
                .await()
            TambahMatkulResult.SUCCESS
        } catch (e: Exception) {
            TambahMatkulResult.ERROR
        }
    }
    
    suspend fun getAllMatkul(): List<Matkul> {
        return try {
            val snapshot = db.collection("matkul")
                .orderBy("kodeMatkul")
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Matkul::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getMatkulByDosen(dosenId: String): List<Matkul> {
        return try {
            val snapshot = db.collection("matkul")
                .whereEqualTo("dosenId", dosenId)
                .orderBy("kodeMatkul")
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Matkul::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun assignDosenToMatkul(kodeMatkul: String, dosenId: String, dosenNama: String): Boolean {
        return try {
            db.collection("matkul")
                .document(kodeMatkul)
                .update(
                    mapOf(
                        "dosenId" to dosenId,
                        "dosenNama" to dosenNama
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun unassignDosenFromMatkul(kodeMatkul: String): Boolean {
        return try {
            db.collection("matkul")
                .document(kodeMatkul)
                .update(
                    mapOf(
                        "dosenId" to null,
                        "dosenNama" to null
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun updateMatkul(matkul: Matkul): Boolean {
        return try {
            db.collection("matkul")
                .document(matkul.kodeMatkul)
                .set(matkul)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun deleteMatkul(kodeMatkul: String): Boolean {
        return try {
            db.collection("matkul")
                .document(kodeMatkul)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getMatkulByKode(kodeMatkul: String): Matkul? {
        return try {
            val doc = db.collection("matkul")
                .document(kodeMatkul)
                .get()
                .await()
            
            doc.toObject(Matkul::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getAllDosen(): List<User> {
        return try {
            val snapshot = db.collection("users_dosen")
                .orderBy("nama")
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) {
                    User(
                        id = doc.id,
                        nama = data["nama"] as? String ?: "",
                        password = data["password"] as? String ?: "",
                        role = data["role"] as? String ?: ""
                    )
                } else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getDashboardStats(): DashboardStats {
        return try {
            val allMatkul = getAllMatkul()
            val allDosen = getAllDosen()
            
            val matkulWithDosen = allMatkul.count { it.dosenId != null }
            val matkulWithoutDosen = allMatkul.count { it.dosenId == null }
            
            DashboardStats(
                totalMatkul = allMatkul.size,
                totalDosen = allDosen.size,
                matkulWithDosen = matkulWithDosen,
                matkulWithoutDosen = matkulWithoutDosen
            )
        } catch (e: Exception) {
            DashboardStats()
        }
    }
}

data class DashboardStats(
    val totalMatkul: Int = 0,
    val totalDosen: Int = 0,
    val matkulWithDosen: Int = 0,
    val matkulWithoutDosen: Int = 0
) 