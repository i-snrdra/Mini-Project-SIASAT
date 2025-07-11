package com.isa.minisiasat.models

data class MatkulMahasiswa(
    val id: String = "",
    val matkulCode: String = "",
    val mahasiswaId: String = "",
    val mahasiswaNama: String = "",
    val tahunMasuk: String = "",
    val enrolledAt: Long = System.currentTimeMillis()
) 