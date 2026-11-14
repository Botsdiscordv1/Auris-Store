package com.xuper.tv.apkmanager

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xuper.tv.apkmanager.data.ApkFile
import com.xuper.tv.apkmanager.ui.ApkListViewModel
import com.xuper.tv.apkmanager.ui.UiState
import java.io.File

class MainActivity : ComponentActivity() {

    private var activeDownloadId: Long = -1L

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == activeDownloadId) {
                val query = DownloadManager.Query().setFilterById(activeDownloadId)
                val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val cursor = manager.query(query)
                if (cursor != null && cursor.moveToFirst()) {
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (statusIndex != -1) {
                        val status = cursor.getInt(statusIndex)
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                            if (uriIndex != -1) {
                                val uriString = cursor.getString(uriIndex)
                                if (uriString != null) {
                                    val fileUri = Uri.parse(uriString)
                                    fileUri.path?.let { path ->
                                        installApk(context, File(path))
                                    }
                                }
                            }
                        } else if (status == DownloadManager.STATUS_FAILED) {
                            Toast.makeText(context, "Error al descargar el APK", Toast.LENGTH_SHORT).show()
                        }
                    }
                    cursor.close()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                downloadReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                RECEIVER_EXPORTED
            )
        } else {
            registerReceiver(
                downloadReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        onDownloadClick = { apk ->
                            downloadAndInstallApk(apk)
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(downloadReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun downloadAndInstallApk(apk: ApkFile) {
        val url = apk.downloadUrl
        val fileName = apk.name
        
        Toast.makeText(this, "Iniciando descarga de $fileName...", Toast.LENGTH_SHORT).show()

        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle(fileName)
            setDescription("Descargando Auris-Store APK...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalFilesDir(this@MainActivity, Environment.DIRECTORY_DOWNLOADS, fileName)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }

        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        activeDownloadId = manager.enqueue(request)
    }

    private fun installApk(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al instalar APK: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ApkListViewModel = viewModel(),
    onDownloadClick: (ApkFile) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auris-Store APK Manager") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.fetchApks() }) {
                            Text("Reintentar")
                        }
                    }
                }
                is UiState.Success -> {
                    if (state.apks.isEmpty()) {
                        Text(
                            text = "No se encontraron APKs en el repositorio.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.apks) { apk ->
                                ApkItemCard(apk = apk, onDownloadClick = { onDownloadClick(apk) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApkItemCard(
    apk: ApkFile,
    onDownloadClick: () -> Unit
) {
    val sizeMb = if (apk.size > 0) String.format("%.2f MB", apk.size / (1024.0 * 1024.0)) else "Tamaño desconocido"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = apk.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ruta: ${apk.path} | $sizeMb",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onDownloadClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Descargar e Instalar")
            }
        }
    }
}
