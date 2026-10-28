# NASKULL AI Photo — Android 0.2

Aplicación nativa Android en Kotlin + Jetpack Compose.

## Obtener el APK sin instalar nada (GitHub Actions)
1. Crea un repositorio en GitHub y sube **el contenido de la carpeta `NASKULL_AI_Photo_CloudBuild`** (debe quedar `.github/` y `NASKULL_AI_Photo/` en la raíz del repo).
2. Cada vez que subas cambios a `main` se compila solo (pestaña **Actions**, ~5 min).
3. Descarga `NASKULL-AI-Photo.apk` desde **Releases** en el móvil e instálalo (permite "instalar apps desconocidas" cuando lo pida).

## Funciones que sí incluye
- Hacer una foto con la cámara del móvil o importar una imagen de la galería.
- Filtros: Auto IA, Comida, Retrato, Noche, Paisaje, B/N, Vintage, Cálido y Frío.
- Intensidad ajustable y mejora local básica de contraste/color.
- Guardar el resultado en `Pictures/NASKULL AI Photo`.
- Consejos de captura orientados al Xiaomi 17 Ultra según el tipo de escena.

## Límites importantes
Esta es una primera versión funcional de interfaz y procesamiento básico, no una IA generativa: no analiza semánticamente el contenido ni reconstruye detalles. El asistente no controla directamente la aplicación Cámara de Xiaomi; da recomendaciones. El proyecto no se ha compilado ni probado en un dispositivo en este entorno.

## Ejecutar
1. Instala Android Studio en un ordenador.
2. Descomprime el ZIP y abre la carpeta `NASKULL_AI_Photo`.
3. Acepta la sincronización de Gradle y descarga de dependencias.
4. Conecta el Xiaomi por USB con depuración USB activada o usa un emulador.
5. Pulsa **Run** para instalarla.
6. Para generar un APK: **Build > Build APK(s)**.

Paquete: `com.naskull.aiphoto` · Versión: `0.2.0` · Android mínimo: 10 (API 29).
