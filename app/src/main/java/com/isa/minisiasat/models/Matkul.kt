package com.isa.minisiasat.models

data class Matkul(
    val id: String = "",
    val kodeMatkul: String = "",
    val namaMatkul: String = "",
    val hari: String = "",
    val jamMulai: String = "",
    val jamSelesai: String = "",
    val kapasitas: Int = 0,
    val sks: Int = 0,
    val semester: Int = 1,
    val ruang: String = "",
    val dosenId: String? = null,
    val dosenNama: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) 