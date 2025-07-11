package com.isa.minisiasat.utils

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class User(
    val id: String,
    val nama: String,
    val password: String,
    val role: String,
    val tahunMasuk: String? = null // Hanya untuk mahasiswa
)

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    
    suspend fun getUserById(userId: String, role: String): User? {
        return try {
            val collectionName = when (role) {
                "kaprogdi" -> "users_kaprogdi"
                "dosen" -> "users_dosen"
                "mahasiswa" -> "users_mahasiswa"
                else -> return null
            }
            
            val document = db.collection(collectionName)
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                val data = document.data
                User(
                    id = userId,
                    nama = data?.get("nama") as? String ?: "",
                    password = data?.get("password") as? String ?: "",
                    role = data?.get("role") as? String ?: "",
                    tahunMasuk = data?.get("tahunMasuk") as? String
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun authenticateUser(userId: String, password: String): User? {
        // Coba cek di semua collection berdasarkan format ID
        val role = when {
            userId.length == 5 && userId.startsWith("67") -> {
                // Cek apakah kaprogdi atau dosen
                val kaprogdiUser = getUserById(userId, "kaprogdi")
                if (kaprogdiUser != null && kaprogdiUser.password == password) {
                    return kaprogdiUser
                }
                "dosen"
            }
            userId.length == 9 && userId.startsWith("672") -> "mahasiswa"
            else -> return null
        }
        
        val user = getUserById(userId, role)
        return if (user?.password == password) user else null
    }
    
    suspend fun getAllUsers(): Map<String, List<User>> {
        val allUsers = mutableMapOf<String, List<User>>()
        
        try {
            // Get Kaprogdi
            val kaprogdiSnapshot = db.collection("users_kaprogdi").get().await()
            val kaprogdiUsers = kaprogdiSnapshot.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) {
                    User(
                        id = doc.id,
                        nama = data["nama"] as? String ?: "",
                        password = data["password"] as? String ?: "",
                        role = data["role"] as? String ?: "",
                        tahunMasuk = data["tahunMasuk"] as? String
                    )
                } else null
            }
            allUsers["kaprogdi"] = kaprogdiUsers
            
            // Get Dosen
            val dosenSnapshot = db.collection("users_dosen").get().await()
            val dosenUsers = dosenSnapshot.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) {
                    User(
                        id = doc.id,
                        nama = data["nama"] as? String ?: "",
                        password = data["password"] as? String ?: "",
                        role = data["role"] as? String ?: "",
                        tahunMasuk = data["tahunMasuk"] as? String
                    )
                } else null
            }
            allUsers["dosen"] = dosenUsers
            
            // Get Mahasiswa
            val mahasiswaSnapshot = db.collection("users_mahasiswa").get().await()
            val mahasiswaUsers = mahasiswaSnapshot.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) {
                    User(
                        id = doc.id,
                        nama = data["nama"] as? String ?: "",
                        password = data["password"] as? String ?: "",
                        role = data["role"] as? String ?: "",
                        tahunMasuk = data["tahunMasuk"] as? String
                    )
                } else null
            }
            allUsers["mahasiswa"] = mahasiswaUsers
            
        } catch (e: Exception) {
            // Handle error
        }
        
        return allUsers
    }
} 