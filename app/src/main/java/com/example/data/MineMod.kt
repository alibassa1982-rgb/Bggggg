package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mine_mods")
data class MineMod(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val identifier: String,
    val damage: Int,
    val durability: Int,
    val texturePath: String?,
    val createdAt: Long = System.currentTimeMillis()
) {
    // Utility to get identifier without prefix, e.g. "legendary_sword" from "mymod:legendary_sword"
    val shortName: String
        get() = identifier.substringAfter(":")
}
