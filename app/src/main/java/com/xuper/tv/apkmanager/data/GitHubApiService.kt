package com.xuper.tv.apkmanager.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubApiService {
    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getRepositoryContents(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String = "Xuper"
    ): List<GitHubFile>

    companion object {
        private const val BASE_URL = "https://api.github.com/"

        fun create(): GitHubApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GitHubApiService::class.java)
        }
    }
}
