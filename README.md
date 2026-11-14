# Auris-Store 📦🤖

¡Bienvenido a **Auris-Store**, tu propia tienda y gestor de APKs personalizado para Android!

## 🚀 ¿Qué es Auris-Store?
Es una aplicación moderna desarrollada en **Jetpack Compose** y **Material 3** que se conecta directamente a este repositorio de GitHub para listar, descargar e instalar automáticamente todas las aplicaciones APK disponibles.

---

## 📂 APKs Disponibles en el Repositorio

- **Xuper_Tv.apk** (`Xuper/`)
- **AurisTube.apk** (`AurisTube/`)
- **XuperMobileNF-6.5.9-ModByNF.apk** (`Xuper/`)

---

## 🛠️ ¿Cómo funciona la App?
1. **Exploración Recursiva:** La app consulta la API de GitHub para escanear todo el repositorio en búsqueda de archivos `.apk`.
2. **Descarga en Segundo Plano:** Utiliza `DownloadManager` para descargar el APK seleccionado de forma segura y eficiente.
3. **Instalación con 1 Click:** Utiliza `FileProvider` para iniciar la instalación automática del paquete en tu dispositivo Android.
