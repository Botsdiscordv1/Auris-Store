package com.xuper.tv.apkmanager.data

data class GitHubFile(
    val name: String,
    val path: String,
    val sha: String,
    val size: Long,
    val url: String,
    val html_url: String,
    val git_url: String,
    val download_url: String?,
    val type: String
)
