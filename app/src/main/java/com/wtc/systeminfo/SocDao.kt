package com.wtc.systeminfo

import androidx.room.Dao
import androidx.room.Query

@Dao
interface SocDao {
    // 先精确匹配 model
    // 如果没找到，模糊匹配以 prefix 开头的 model
    @Query("""
    SELECT name FROM soc
    WHERE model = :model
       OR model LIKE :prefix || '%'
    LIMIT 1
    """)
    suspend fun getProcessorName(model: String, prefix: String): String?
}
