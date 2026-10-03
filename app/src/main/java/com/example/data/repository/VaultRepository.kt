package com.example.data.repository

import com.example.data.crypto.CryptoService
import com.example.data.db.VaultDao
import com.example.data.db.VaultEntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class DecryptedVaultEntry(
    val id: Long = 0,
    val title: String,
    val username: String,
    val password: String,
    val notes: String,
    val category: String,
    val createdAt: Long,
    val updatedAt: Long
)

class VaultRepository(
    private val vaultDao: VaultDao,
    private val cryptoService: CryptoService
) {

    val allEntries: Flow<List<DecryptedVaultEntry>> = vaultDao.getAllEntries().map { list ->
        list.map { entityToDecrypted(it) }
    }

    val entryCount: Flow<Int> = vaultDao.getEntryCount()

    fun searchEntries(query: String): Flow<List<DecryptedVaultEntry>> {
        return vaultDao.searchEntries(query).map { list ->
            list.map { entityToDecrypted(it) }
        }
    }

    suspend fun getEntryById(id: Long): DecryptedVaultEntry? {
        val entity = vaultDao.getEntryById(id) ?: return null
        return entityToDecrypted(entity)
    }

    suspend fun saveEntry(
        id: Long = 0,
        title: String,
        username: String,
        password: String,
        notes: String,
        category: String
    ): Long {
        val (encUser, userIv) = cryptoService.encrypt(username)
        val (encPass, passIv) = cryptoService.encrypt(password)
        val (encNotes, notesIv) = cryptoService.encrypt(notes)

        val entity = VaultEntryEntity(
            id = id,
            title = title.trim(),
            encryptedUsername = encUser,
            usernameIv = userIv,
            encryptedPassword = encPass,
            passwordIv = passIv,
            encryptedNotes = encNotes,
            notesIv = notesIv,
            category = category,
            createdAt = if (id == 0L) System.currentTimeMillis() else System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        return if (id == 0L) {
            vaultDao.insertEntry(entity)
        } else {
            vaultDao.updateEntry(entity)
            id
        }
    }

    suspend fun deleteEntry(id: Long) {
        vaultDao.deleteById(id)
    }

    suspend fun clearAll() {
        vaultDao.clearAll()
    }

    private fun entityToDecrypted(entity: VaultEntryEntity): DecryptedVaultEntry {
        return DecryptedVaultEntry(
            id = entity.id,
            title = entity.title,
            username = cryptoService.decrypt(entity.encryptedUsername, entity.usernameIv),
            password = cryptoService.decrypt(entity.encryptedPassword, entity.passwordIv),
            notes = cryptoService.decrypt(entity.encryptedNotes, entity.notesIv),
            category = entity.category,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
