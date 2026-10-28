# NASKULL AI Photo — Android 0.3

Aplicación nativa Android en Kotlin + Jetpack Compose.

## Obtener el APK sin instalar nada (GitHub Actions)
1. Crea un repositorio en GitHub y sube **el contenido de la carpeta `NASKULL_AI_Photo_CloudBuild`** (debe quedar `.github/` y `NASKULL_AI_Photo/` en la raíz del repo).
2. Cada vez que subas cambios a `main` se compila solo (pestaña **Actions**, ~5 min).
3. Descarga `NASKULL-AI-Photo.apk` desde **Releases** en el móvil e instálalo (permite "instalar apps desconocidas" cuando lo pida).

## Funciones que sí incluye
- Cámara propia con vista previa del filtro en directo; el filtro se aplica al disparar y la foto se guarda sola.
- Usa el modo Noche del fabricante (CameraX Extensions) si el móvil lo ofrece a otras apps.
- Editar fotos de la galería con los mismos filtros.
- Filtros: Original, Auto IA (activa Noche HDR si la escena es oscura), Noche HDR, Comida, Retrato, Noche, Paisaje, B/N, Vintage, Cálido y Frío.
- Guardar el resultado en `Pictures/NASKULL AI Photo` (opcionalmente también el original).
- Consejos de captura orientados al Xiaomi 17 Ultra según el tipo de escena.

## Límites importantes
Los filtros son procesamiento de imagen en el móvil, no IA generativa: no inventan detalles. Noche HDR aclara las sombras por zonas, por lo que en zonas muy oscuras puede aparecer algo de ruido. Una app de terceros no tiene acceso a todo el procesado de la cámara de Xiaomi.

## Compilar en local (opcional)
1. Instala Android Studio y abre la carpeta `NASKULL_AI_Photo`.
2. Acepta la sincronización de Gradle.
3. Conecta el móvil con depuración USB y pulsa **Run**, o usa **Build > Build APK(s)**.

Paquete: `com.naskull.aiphoto` · Versión: `0.3.0` · Android mínimo: 10 (API 29).
