package com.isa.minisiasat.models

data class AbsensiSession(
    val id: String = "",
    val matkulCode: String = "",
    val dosenId: String = "",
    val tanggal: String = "",
    val jamBuka: String = "",
    val jamTutup: String? = null, // null jika masih terbuka
    val status: String = "terbuka", // "terbuka" atau "tertutup"
    val mahasiswaHadir: List<String> = emptyList(), // list ID mahasiswa yang sudah absen
    val createdAt: Long = System.currentTimeMillis()
) 