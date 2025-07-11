package com.isa.minisiasat.models

data class MatkulMahasiswa(
    val id: String = "",
    val matkulCode: String = "",
    val mahasiswaId: String = "",
    val mahasiswaNama: String = "",
    val tahunMasuk: String = "",
    val tanggalDaftar: String = "", // Tanggal pendaftaran
    val status: String = "aktif", // aktif, dropout, lulus
    val enrolledAt: Long = System.currentTimeMillis()
) 