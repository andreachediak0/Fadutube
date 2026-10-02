# FaduTube

Aplicación Android liviana con interfaz azul y nombre **fadutube**. Incluye:

- Biblioteca de audio local para ver y reproducir canciones del dispositivo.
- Controles básicos de reproducción y pausa.
- Descarga de enlaces HTTPS directos a archivos de audio (`.mp3`, `.m4a`, `.aac`, `.ogg`, `.wav`, `.flac`, `.opus`) a la carpeta **Música**.

Por diseño, no extrae ni descarga pistas desde YouTube. Usala solo con archivos que sean tuyos o que tengas autorización para descargar. La primera vez que abras la biblioteca, Android pedirá permiso para acceder a los archivos de audio.

## Compilar en GitHub

1. Creá un repositorio nuevo en GitHub y subí el contenido de esta carpeta.
2. En **Actions**, ejecutá **Compilar FaduTube** (también se ejecuta en cada push a `main`).
3. Al terminar, descargá el artefacto `fadutube-debug-apk` de la ejecución.
4. Instalá `app-debug.apk` en un dispositivo Android. Para distribuir públicamente, generá una versión release firmada.

## Compilar localmente

Requiere JDK 17, Android SDK y Gradle 8.9:

```sh
gradle assembleDebug
```

El APK debug queda en `app/build/outputs/apk/debug/app-debug.apk`.
