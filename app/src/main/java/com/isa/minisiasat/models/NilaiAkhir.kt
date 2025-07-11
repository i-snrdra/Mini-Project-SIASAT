package com.isa.minisiasat.models

data class NilaiAkhir(
    val id: String = "",
    val matkulCode: String = "",
    val dosenId: String = "",
    val mahasiswaId: String = "",
    val mahasiswaNama: String = "",
    val nilaiAkhir: String = "", // A, B, C, D, E
    val tanggal: String = "",
    val createdAt: Long = System.currentTimeMillis()
) 