package xyz.haloai.haloai_android_productivity.data.local.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.haloai.haloai_android_productivity.data.local.dao.MiscInfoDao
import xyz.haloai.haloai_android_productivity.data.local.entities.EnumValType
import xyz.haloai.haloai_android_productivity.data.local.entities.KVPair

class MiscInfoDbRepository(private val miscInfoDao: MiscInfoDao) {

    suspend fun updateOrCreate(key: String, value: Any, type: EnumValType = EnumValType.STRING) = withContext(Dispatchers.IO)
    {
        val existingEntry = miscInfoDao.getByKey(key)
        var valToInsert = ""
        if (type == EnumValType.LIST_STRING) {
            valToInsert = (value as List<*>).joinToString("<DELIM>")
        }
        else if (type == EnumValType.STRING) {
            valToInsert = value.toString()
        }
        if (existingEntry != null)
        {
            if (existingEntry.type != type)
            {
                throw IllegalArgumentException("Type mismatch")
            }
            miscInfoDao.updateByKey(key, valToInsert)
        }
        else
        {
            miscInfoDao.insert(
                KVPair(
                    id = 0,
                    k = key,
                    v = valToInsert,
                    type = type
                )
            )
        }
    }

    suspend fun get(key: String): Any? = withContext(Dispatchers.IO)
    {
        val entry = miscInfoDao.getByKey(key)
        if (entry != null)
        {
            if (entry.type == EnumValType.STRING)
            {
                return@withContext entry.v
            }
            else if (entry.type == EnumValType.LIST_STRING)
            {
                return@withContext entry.v.split("<DELIM>")
            }
        }
        return@withContext null
    }

    suspend fun delete(key: String) = withContext(Dispatchers.IO)
    {
        miscInfoDao.deleteByKey(key)
    }
}