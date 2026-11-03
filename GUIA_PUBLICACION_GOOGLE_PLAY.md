# 📱 Guía Definitiva de Publicación en Google Play Store
### App: *Loterías y Estadísticas (Simulador de Azar y Resultados)*

Esta guía contiene la estrategia legal, técnica y paso a paso para publicar tu aplicación en **Google Play Store**, cumpliendo al 100% las normativas contra fraude y apuestas, garantizando que sea aceptada como una herramienta de **entretenimiento y estadística experimental**.

---

## 📑 Índice de Contenidos
1. [Blindaje Legal y Prevención de Rechazos](#1-blindaje-legal-y-prevención-de-rechazos)
2. [Estrategia de Textos (Copywriting Seguro para Google)](#2-estrategia-de-textos-copywriting-seguro-para-google)
3. [Requisitos Previos de la Cuenta](#3-requisitos-previos-de-la-cuenta)
4. [Generación de Llaves y Creación del `.AAB` (Android App Bundle)](#4-generación-de-llaves-y-creación-del-aab)
5. [Cuestionarios Críticos en Google Play Console](#5-cuestionarios-críticos-en-google-play-console)
6. [Diseño Gráfico Requerido para la Ficha](#6-diseño-gráfico-requerido-para-la-ficha)
7. [Proceso de Lanzamiento y Pruebas Cerradas](#7-proceso-de-lanzamiento-y-pruebas-cerradas)

---

## 1. Blindaje Legal y Prevención de Rechazos

Google Play audita estrictamente dos áreas en apps vinculadas al azar:
1. **Apuestas con Dinero Real (*Gambling Policy*):** Exige licencias de juego emitidas por el Estado.
2. **Declaraciones Engañosas (*Misleading Claims Policy*):** Sanciona apps que prometen "ganancias garantizadas" o "números infalibles".

### ¿Por qué tu app es 100% legal?
* **Cero Transacciones:** La app no recibe dinero, no vende boletas, no gestiona apuestas ni cobra comisiones.
* **Carácter Estadístico/Lúdico:** Los pronósticos son cálculos probabilísticos y simulaciones algorítmicas de frecuencias pasadas.
* **Marco Legal Argentino Incorporado:** Tu app ya incluye en su pestaña de Legales la referencia al **Art. 301 bis del Código Penal** y a la **Ley 24.240 de Defensa del Consumidor**.

### Categoría obligatoria al crear la app:
* **Categoría:** `Entretenimiento` (*Entertainment*) o `Herramientas` (*Tools*).
* ⚠️ **NUNCA** selecciones *Casino*, *Juegos de Azar* o *Finanzas*.

---

## 2. Estrategia de Textos (Copywriting Seguro para Google)

Google evalúa mediante bots de IA el contenido de la ficha de la tienda. Utiliza los siguientes textos aprobables:

### Título de la Aplicación (Máx. 30 caracteres)
> `Loterías y Estadísticas: Azar`

### Descripción Corta (Máx. 80 caracteres)
> `Simulador estadístico experimental de probabilidades y visor de sorteos públicos.`

### Descripción Larga (Completa)
```text
Loterías y Estadísticas es una herramienta independiente de consulta informativa y simulación matemática para aficionados a los juegos de azar y quinielas de la República Argentina.

CARACTERÍSTICAS PRINCIPALES:
• Resultados al instante: Consulta los extractos públicos y números sorteados de las principales loterías (Jujuy, Ciudad de Buenos Aires y Provincia de Buenos Aires) en todos sus turnos diarios (Previa, Primera, Matutina, Vespertina y Nocturna).
• Simulador Estadístico Experimental: Análisis basado en algoritmos de recurrencia y probabilidades numéricas para fines lúdicos y de entretenimiento.
• Historial y Efectividad: Registro transparente del desempeño y frecuencias históricas de los últimos sorteos.
• Interfaz Moderna: Navegación ágil, soporte de modo oscuro y actualización rápida.

AVISO LEGAL Y DESCARGO DE RESPONSABILIDAD (IMPORTANTE):
1. Independencia Institucional: Esta aplicación es de desarrollo privado e independiente. NO posee afiliación, convenio, vinculación comercial ni patrocinio con ningún organismo estatal regulador de juegos de azar (como INPROJUY, LOTBA, IPLyC ni loterías provinciales).
2. Carácter Lúdico y Sin Garantía: Los cálculos y pronósticos generados son simulaciones estadísticas de carácter experimental y educativo. NO se garantiza ningún acierto ni resultado favorable en apuestas reales. Los juegos de azar son eventos matemáticamente aleatorios e independientes.
3. No es una Plataforma de Apuestas: Esta aplicación no recauda apuestas, no procesa pagos, no vende billetes ni premia con dinero real. Cumple estrictamente con la legislación argentina (Código Penal de la Nación, Art. 301 bis).
4. Juego Responsable: Si decide participar en juegos de azar oficiales, hágalo con moderación y exclusivamente en agencias oficiales autorizadas por el Estado. El juego compulsivo es perjudicial para la salud.
```

---

## 3. Requisitos Previos de la Cuenta

1. **Crear Cuenta de Google Play Console:**
   - Ingresa a [play.google.com/console](https://play.google.com/console).
   - Abona el pago único de registro (**$25 USD**).
2. **URL de Política de Privacidad (Exigido por Google):**
   - Puedes usar directamente el enlace público de tu backend:
     `https://kurtchaco-loterias-predicciones.hf.space/legales/`

---

## 4. Generación de Llaves y Creación del `.AAB`

Google Play **no admite archivos `.apk`** para nuevas publicaciones; exige el formato **`.aab` (Android App Bundle)** firmado con una llave privada (*Keystore*).

### Paso 4.1: Generar tu archivo Keystore de producción
Ejecuta en tu terminal (desde la carpeta de tu proyecto):

```bash
cd /home/kurt/projects/APP_ANDROID_LOTERIAS/test_android
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias loterias-release
```
*(El sistema te pedirá una contraseña para el keystore y unos datos personales. **Guarda muy bien esa contraseña**)*.

### Paso 4.2: Configurar la firma en `app/build.gradle`
Agrega la configuración de firma dentro del bloque `android { ... }` en `app/build.gradle`:

```groovy
android {
    ...
    signingConfigs {
        release {
            storeFile file("release-key.jks")
            storePassword "TU_CONTRASEÑA"
            keyAlias "loterias-release"
            keyPassword "TU_CONTRASEÑA"
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

### Paso 4.3: Compilar el App Bundle (`.aab`)
Ejecuta desde la terminal:

```bash
./gradlew bundleRelease
```

El archivo generado estará listo en:
📁 **`app/build/outputs/bundle/release/app-release.aab`**

*(Este es el archivo que se sube directamente a Google Play Console).*

---

## 5. Cuestionarios Críticos en Google Play Console

Al crear la ficha en Play Console, deberás completar varias declaraciones obligatorias. Respóndelas exactamente así:

### A. Acceso a la App (*App Access*)
* Seleccionar: **"Todas las funciones están disponibles sin restricciones"** *(no hay inicio de sesión ni muros de pago)*.

### B. Anuncios (*Ads*)
* Si por ahora no tienes AdMob u otra red: Seleccionar **"No, mi app no contiene anuncios"**.

### C. Clasificación de Contenido (Cuestionario IARC)
* **Categoría:** *Utilidades / Entretenimiento / Noticias*.
* **¿La app permite apostar dinero real?** $\rightarrow$ **NO**.
* **¿La app incluye referencias o simula juegos de azar?** $\rightarrow$ **SÍ** *(Al responder Sí, Google te asignará una clasificación para mayores de 16/18 años, lo cual es ideal porque evita problemas de normativas infantiles)*.
* **¿La app permite a los usuarios interactuar o compartir ubicaciones?** $\rightarrow$ **NO**.

### D. Seguridad de los Datos (*Data Safety*)
* **¿Tu app recopila o comparte datos de los usuarios?** $\rightarrow$ **NO**.
*(Al no solicitar nombres, emails, tarjetas ni ubicación GPS, tu app califica como "No recopila datos", acelerando drásticamente la aprobación).*

### E. Público Objetivo
* Seleccionar: **Mayores de 18 años**.

### F. Países y Regiones de Distribución (CRÍTICO: Solo Argentina)
Google Play permite seleccionar explícitamente en qué países estará visible y descargable tu aplicación:
1. En la consola de Google Play, ve al menú lateral $\rightarrow$ **Producción** (o Pruebas) $\rightarrow$ pestaña **Países y regiones** (*Countries / regions*).
2. Haz clic en **Añadir países / regiones**.
3. Selecciona **únicamente Argentina 🇦🇷** y desmarca el resto del mundo.

#### 🛡️ Ventajas legales de restringir a Argentina:
* **Blindaje Regulatorio Total:** Evitas tener que cumplir con leyes de juego, protección de datos y apuestas de otros países (como EE.UU., Reino Unido o la Unión Europea con RGPD).
* **Jurisdicción Unificada:** Tu marco legal (Art. 301 bis del Código Penal Argentino y Ley de Defensa del Consumidor 24.240) rige al 100% para todos los usuarios que descarguen la app.
* **Usuarios Fuera de Argentina:** Usuarios con cuentas de Google Play de otros países no podrán encontrar la app en la tienda ni descargarla.

---

## 6. Diseño Gráfico Requerido para la Ficha

Google Play exige obligatoriamente los siguientes recursos visuales:

| Recurso | Dimensiones | Formato | Función |
| :--- | :--- | :--- | :--- |
| **Ícono de la App** | 512 x 512 px | PNG (32 bits con canal alfa) | Ícono principal en la tienda. |
| **Gráfico de Funciones (Banner)** | 1024 x 500 px | JPG o PNG | Portada promocional superior. |
| **Capturas de Pantalla (Teléfono)** | Mínimo 2 capturas (ej. 1080 x 1920 px) | JPG o PNG | Muestras de la interfaz (Sorteos, Predicciones, Aciertos). |

> **Tip:** Con `scrcpy` en tu PC, puedes tomar capturas nítidas de tu celular directamente con el atajo de captura de tu sistema o usando el comando:
> ```bash
> /home/kurt/Android/Sdk/platform-tools/adb exec-out screencap -p captura_sorteos.png
> ```

---

## 7. Proceso de Lanzamiento y Pruebas Cerradas

Google implementó una política para todas las cuentas personales nuevas:
1. **Prueba Cerrada Obligatoria (*Closed Testing*):**
   - Debes subir tu `.aab` a la sección de **Pruebas Cerradas**.
   - Configura la disponibilidad de la prueba únicamente para **Argentina**.
   - Invitas a **12 a 20 evaluadores** (amigos o familiares con sus correos de Google en Argentina).
   - Los evaluadores deben descargar la app desde el enlace de prueba y mantenerla instalada durante **14 días**.
2. **Solicitud de Pase a Producción:**
   - Cumplidos los 14 días, se habilitará un botón en Google Play Console: *"Solicitar acceso a producción"*.
   - Respondes unas breves preguntas sobre las pruebas realizadas.
3. **Revisión Final y Publicación:**
   - El equipo de revisión de Google valida la app (suele demorar de 24 a 72 horas).
   - ¡Tu app pasa a estar **Pública y disponible exclusivamente en Argentina** en la Play Store!

---

*Documento guardado para consulta en tu proyecto: `APP_ANDROID_LOTERIAS/GUIA_PUBLICACION_GOOGLE_PLAY.md`*

