package com.auris.store.oficial.data

class ApkRepository(
    private val apiService: GitHubApiService = GitHubApiService.create()
) {
    suspend fun getApkFiles(): List<ApkFile> {
        return try {
            val treeResponse = apiService.getRepositoryTree("Botsdiscordv1", "Auris-Store", "main")
            treeResponse.tree
                .filter { it.type == "blob" && it.path.endsWith(".apk", ignoreCase = true) }
                .map { item ->
                    val fileName = item.path.substringAfterLast('/')
                    val downloadUrl = "https://raw.githubusercontent.com/Botsdiscordv1/Auris-Store/main/${item.path}"
                    ApkFile(
                        name = fileName,
                        path = item.path,
                        size = item.size ?: 0L,
                        downloadUrl = downloadUrl
                    )
                }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
