# Guía para Probar la App Nativa de Loterías en Android

Esta guía te explicará cómo ejecutar el proyecto que acabamos de crear (ubicado en `test_android`) para que puedas verlo funcionando en tu propio teléfono móvil utilizando **Android Studio**.

## Paso 1: Preparar tu Computadora

1. **Descargar Android Studio:** Si aún no lo tienes, descárgalo gratis desde la [página oficial de Google](https://developer.android.com/studio) e instálalo.
2. **Abrir el Proyecto:** 
   - Inicia Android Studio.
   - Selecciona **"Open"** (Abrir).
   - Navega hasta la carpeta de tu proyecto y selecciona `/home/kurt/projects/APP_ANDROID_LOTERIAS/test_android`.
3. **Esperar la Sincronización:** Al abrir el proyecto, Android Studio comenzará a descargar herramientas y librerías (verás una barra de progreso que dice "Gradle Sync" en la parte inferior). Espera hasta que termine completamente.

---

## Paso 2: Preparar tu Celular Android (Activar Modo Desarrollador)

Para poder instalar aplicaciones directamente desde tu PC, debes habilitar la Depuración USB:

1. Ve a los **Ajustes** (Configuración) de tu celular.
2. Desplázate hasta **Acerca del teléfono** (o Información del sistema).
3. Busca donde dice **"Número de compilación"** (Build number).
4. Toca ese número rápidamente **7 veces seguidas**. Te aparecerá un mensaje diciendo "¡Ya eres un desarrollador!".
5. Vuelve al menú anterior de Ajustes, y busca la nueva opción **"Opciones de desarrollador"** (a veces está dentro de Sistema o Ajustes adicionales).
6. Entra allí y activa la opción **Depuración por USB** (USB Debugging).

---

## Paso 3: Pasar la App al Celular

1. **Conecta tu celular** a la computadora usando un cable USB.
2. En la pantalla de tu celular aparecerá un aviso de seguridad ("¿Permitir depuración USB?"). Marca la casilla "Permitir siempre desde esta computadora" y dale a **Aceptar**.
3. **Selecciona tu celular en Android Studio:** En la barra superior de Android Studio (cerca de los controles de Play/Stop), deberías ver que el modelo de tu celular aparece seleccionado en un menú desplegable.
4. **Ejecuta la App:** Haz clic en el botón **verde de "Play" (▶️ Run)** en la barra superior.

### ¿Qué sucederá ahora?
Android Studio compilará el código de la carpeta `test_android`, empaquetará la app y la instalará automáticamente en tu celular. Segundos después, ¡la aplicación nativa se abrirá en tu pantalla, descargando los resultados de hoy desde tu servidor web de Hugging Face!
