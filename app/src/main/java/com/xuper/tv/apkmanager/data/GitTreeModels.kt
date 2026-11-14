package com.xuper.tv.apkmanager.data

data class GitTreeResponse(
    val sha: String,
    val url: String,
    val tree: List<GitTreeItem>,
    val truncated: Boolean
)

data class GitTreeItem(
    val path: String,
    val mode: String,
    val type: String,
    val sha: String,
    val size: Long?,
    val url: String
)

data class ApkFile(
    val name: String,
    val path: String,
    val size: Long,
    val downloadUrl: String
)
