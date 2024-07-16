package xyz.haloai.haloai_android_productivity.data.local.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import xyz.haloai.haloai_android_productivity.data.local.dao.MiscInfoDao
import xyz.haloai.haloai_android_productivity.data.local.entities.EnumValType
import xyz.haloai.haloai_android_productivity.data.local.entities.KVPair
import java.util.concurrent.ConcurrentHashMap

class MiscInfoDbRepository(private val miscInfoDao: MiscInfoDao) {

    private data class MutexWithCount(val mutex: Mutex, var count: Int)

    private val mutexes = ConcurrentHashMap<String, MutexWithCount>()

    private fun getMutexForKey(key: String): Mutex {
        val mutexWithCount = mutexes.compute(key) { _, existing ->
            existing?.apply { count++ } ?: MutexWithCount(Mutex(), 1)
        }
        return mutexWithCount!!.mutex
    }

    private fun releaseMutexForKey(key: String) {
        mutexes.computeIfPresent(key) { _, mutexWithCount ->
            mutexWithCount.count--
            if (mutexWithCount.count == 0) null else mutexWithCount
        }
    }

    suspend fun updateOrCreate(key: String, value: Any, type: EnumValType = EnumValType.STRING) = withContext(Dispatchers.IO)
    {
        val mutex = getMutexForKey(key)
        try {
            mutex.withLock {
                val existingEntry = miscInfoDao.getByKey(key)
                var valToInsert = ""
                if (type == EnumValType.LIST_STRING) {
                    valToInsert = (value as List<*>).joinToString("<DELIM>")
                } else if (type == EnumValType.STRING) {
                    valToInsert = value.toString()
                }
                if (existingEntry != null) {
                    if (existingEntry.type != type) {
                        throw IllegalArgumentException("Type mismatch")
                    }
                    miscInfoDao.updateByKey(key, valToInsert)
                } else {
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
        }
        finally {
            releaseMutexForKey(key)
        }
    }

    suspend fun get(key: String): Any? = withContext(Dispatchers.IO)
    {
        val mutex = getMutexForKey(key)
        try {
            return@withContext mutex.withLock {
                val entry = miscInfoDao.getByKey(key)
                if (entry != null) {
                    if (entry.type == EnumValType.STRING) {
                        return@withLock entry.v
                    } else if (entry.type == EnumValType.LIST_STRING) {
                        return@withLock entry.v.split("<DELIM>")
                    }
                }
                return@withLock null
            }
        }
        finally {
            releaseMutexForKey(key)
        }
    }

    suspend fun delete(key: String) = withContext(Dispatchers.IO)
    {
        val mutex = getMutexForKey(key)
        try {
            mutex.withLock {
                miscInfoDao.deleteByKey(key)
            }
        }
        finally {
            releaseMutexForKey(key)
        }
    }
}