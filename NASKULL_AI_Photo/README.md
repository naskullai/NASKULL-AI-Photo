# NASKULL AI Photo â€” Android 0.3

AplicaciÃ³n nativa Android en Kotlin + Jetpack Compose.

## Obtener el APK sin instalar nada (GitHub Actions)
1. Crea un repositorio en GitHub y sube **el contenido de la carpeta `NASKULL_AI_Photo_CloudBuild`** (debe quedar `.github/` y `NASKULL_AI_Photo/` en la raÃ­z del repo).
2. Cada vez que subas cambios a `main` se compila solo (pestaÃ±a **Actions**, ~5 min).
3. Descarga `NASKULL-AI-Photo.apk` desde **Releases** en el mÃ³vil e instÃ¡lalo (permite "instalar apps desconocidas" cuando lo pida).

## Funciones que sÃ­ incluye
- Hacer una foto con la cÃ¡mara del mÃ³vil o importar una imagen de la galerÃ­a.
- Filtros: Original, Auto IA (activa Noche HDR si la escena es oscura), Noche HDR, Comida, Retrato, Noche, Paisaje, B/N, Vintage, CÃ¡lido y FrÃ­o.
- Intensidad ajustable y mejora local bÃ¡sica de contraste/color.
- Guardar el resultado en `Pictures/NASKULL AI Photo`.
- Consejos de captura orientados al Xiaomi 17 Ultra segÃºn el tipo de escena.

## LÃ­mites importantes
Esta es una primera versiÃ³n funcional de interfaz y procesamiento bÃ¡sico, no una IA generativa: no analiza semÃ¡nticamente el contenido ni reconstruye detalles. El asistente no controla directamente la aplicaciÃ³n CÃ¡mara de Xiaomi; da recomendaciones. El proyecto no se ha compilado ni probado en un dispositivo en este entorno.

## Ejecutar
1. Instala Android Studio en un ordenador.
2. Descomprime el ZIP y abre la carpeta `NASKULL_AI_Photo`.
3. Acepta la sincronizaciÃ³n de Gradle y descarga de dependencias.
4. Conecta el Xiaomi por USB con depuraciÃ³n USB activada o usa un emulador.
5. Pulsa **Run** para instalarla.
6. Para generar un APK: **Build > Build APK(s)**.

Paquete: `com.naskull.aiphoto` Â· VersiÃ³n: `0.3.0` Â· Android mÃ­nimo: 10 (API 29).

