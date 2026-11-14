package com.xuper.tv.apkmanager.data

class ApkRepository(
    private val apiService: GitHubApiService = GitHubApiService.create()
) {
    suspend fun getApkFiles(): List<GitHubFile> {
        return try {
            val contents = apiService.getRepositoryContents("Botsdiscordv1", "Auris-Store", "Xuper")
            contents.filter { it.name.endsWith(".apk", ignoreCase = true) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
