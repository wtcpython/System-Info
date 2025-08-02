package com.wtc.systeminfo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "soc")
data class SocEntity (
    @PrimaryKey val model: String,
    val name: String
)
