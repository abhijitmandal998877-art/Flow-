package com.example.data

import kotlinx.coroutines.flow.Flow

class DocumentRepository(private val documentDao: DocumentDao) {
    val allDocuments: Flow<List<DocumentEntity>> = documentDao.getAllDocuments()

    fun getDocumentById(id: Int): Flow<DocumentEntity?> = documentDao.getDocumentById(id)

    suspend fun getDocumentByIdSuspend(id: Int): DocumentEntity? = documentDao.getDocumentByIdSuspend(id)

    suspend fun insert(document: DocumentEntity): Long = documentDao.insertDocument(document)

    suspend fun update(document: DocumentEntity) = documentDao.updateDocument(document)

    suspend fun delete(document: DocumentEntity) = documentDao.deleteDocument(document)

    suspend fun deleteById(id: Int) = documentDao.deleteDocumentById(id)
}
