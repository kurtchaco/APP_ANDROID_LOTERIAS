# 📋 TODO / Hoja de Ruta - App Android Loterías y Quinielas

Este documento reúne ideas, mejoras y funcionalidades planificadas para las próximas versiones de la aplicación móvil nativa.

---

## 🚀 1. Funcionalidades y Experiencia de Usuario (UX)

- [ ] **Modo Offline / Caché Local (Room Database o SharedPreferences):**
  - Guardar localmente los últimos sorteos y las predicciones descargadas para que el usuario pueda consultarlos incluso si no tiene señal o internet en el momento.
  
- [ ] **Compartir Resultados en Redes Sociales y WhatsApp:**
  - Botón rápido para compartir el extracto de un sorteo o las predicciones del día en formato texto limpio o captura de imagen con marca de agua elegante.

- [ ] **Buscador de Números / Historial:**
  - Permitir al usuario ingresar un número (de 2, 3 o 4 cifras) y verificar en cuántos sorteos ha salido en los últimos días y en qué posiciones.

- [ ] **Filtro de Turno Favorito:**
  - Opción para recordar la última lotería y turno consultados (por ejemplo, iniciar siempre en *Jujuy - Nocturna* si es la preferencia del usuario).

- [ ] **Soporte de Tema Dinámico (Claro / Oscuro):**
  - Permitir alternar entre el tema oscuro actual y un tema claro, o sincronizar automáticamente con el tema del sistema Android.

---

## 🔔 2. Notificaciones y Alertas

- [ ] **Notificaciones Push (Firebase Cloud Messaging - FCM):**
  - Notificar al celular apenas se publiquen los resultados oficiales de un nuevo turno (Previa, Primera, Matutina, Vespertina, Nocturna).
  - Alerta diaria matutina cuando estén listas las 5 nuevas predicciones de la IA para la jornada.

- [ ] **Configuración de Alertas Personalizadas:**
  - Elegir de qué loterías recibir avisos (solo Jujuy, solo Ciudad o todas).

---

## 📱 3. Integración con el Sistema Android

- [ ] **Widget para Pantalla de Inicio (Android App Widget):**
  - Widget compacto para el escritorio del teléfono que muestre en tiempo real el último 1º premio del día y las predicciones destacadas sin necesidad de abrir la app.

- [ ] **Acceso Rápido / Atajos (App Shortcuts):**
  - Al mantener presionado el ícono de la app en el celular, mostrar accesos directos: *"Ver Último Sorteo"* y *"Ver Predicciones de Hoy"*.

---

## 🛡️ 4. Infraestructura y Rendimiento

- [ ] **API Nativa JSON Dedicada:**
  - Crear un endpoint centralizado en el backend Django (`/api/mobile-data/`) que consolide sorteos, fechas, 5 predicciones y estadísticas en una única llamada optimizada en formato JSON (reduciendo el consumo de datos móviles y acelerando la carga).

- [ ] **Generación de APK Release y Bundle (AAB):**
  - Configurar firma digital (*Keystore*) y reglas de optimización ProGuard/R8 para generar el archivo final listo para publicar en Google Play Store o distribuir directamente.

---

## 🎯 5. Rediseño Prioritario: Pestaña Efectividad (Lógica de Logros vs. Negativos)

> **Motivo del cambio:** La versión actual tomó una simulación global fija de 2 cifras que mostraba números negativos generales, pasando por alto los grandes logros reales de la IA (por ejemplo: en Buenos Aires se acertó **2 veces a las 4 cifras al 1º premio** en la misma semana, y Jujuy también registró aciertos significativos).

- [x] **Nueva regla de negocio: "Destacar primero los logros"**
  - Evaluar el rendimiento considerando los siguientes 4 indicadores clave:
    1. **4 Cifras al 1º Premio** (Acierto máximo: multiplicador 3.000x).
    2. **3 Cifras al 1º Premio** (Acierto mayor: multiplicador 500x).
    3. **2 Cifras Incremental** (Estrategia progresiva con saldo positivo).
    4. **Balance General por Lotería** (Jujuy, Ciudad y Buenos Aires evaluadas individualmente).

- [x] **Lógica de visualización condicional:**
  - **SI AL MENOS UNO** de estos ítems o loterías está en **VERDE** (positivo o con aciertos de 3/4 cifras):
    - **Resaltar con protagonismo los logros:** Mostrar tarjetas destacadas en verde/dorado con la lotería ganadora, el turno, el número exacto acertado y las ganancias estimadas.
  - **ÚNICAMENTE SI NINGUNO** de estos ítems en ninguna de las loterías tiene valores en verde:
    - Recién en ese caso extremo se debe mostrar la tarjeta de rendimiento negativo / semana adversa.


- [ ] **Enfoque prioritario: La Semana en Curso**
  - La pantalla principal de Efectividad se enfocará exclusivamente en el rendimiento de la **semana activa**:
    - **Si hubo aciertos:** Celebrar y destacar los logros con tarjetas verdes y doradas (como los aciertos de 4 cifras en Provincia o los de Jujuy).
    - **Si no hubo aciertos:** Mostrar la realidad con absoluta transparencia ("aquí mostramos nuestras debilidades"), sin ocultar las pérdidas.

- [ ] **Nueva sub-pestaña / selector: "Histórico de Semanas Anteriores"**
  - Separar el histórico en una pestaña o vista dedicada para auditar el rendimiento semana por semana (semana previa, hace 2 semanas, hace 3 semanas), evitando mezclar semanas pasadas con la semana actual.

---

## 🔬 6. Plan de Corrección para Lotería de la Ciudad (Sin aciertos desde 24/09/2026)

> **Diagnóstico del problema:** La Lotería de la Ciudad (LOTBA) llevaba más de un mes sin registrar aciertos en 2 cifras (último acierto registrado: **24-09-2026**). Al combinarse con la estrategia *2 Cifras Incremental* (multiplicadores de 1x, 2x, 4x, 8x y 16x), generaba pérdidas simuladas de más de **$2.170.000 por semana**, arruinando el balance general de la IA.

### Acciones Técnicas Implementadas y Verificadas:

- [x] **1. Reentrenamiento y Fine-Tuning de `model_ciudad.keras`:**
  - Actualizado el dataset `ciudad_csv.csv` con los sorteos hasta el 28 de octubre de 2026.
  - Corregido el bug en [predict.py](file:///home/kurt/loterias/src/predict.py) donde se sobreescribía el scaler y se evaluaba un lag de 25 días atrás.
  - Aplicado `sample_weights` ponderado hasta 3.5x para los sorteos más recientes (combatiendo el *Concept Drift*).
  - Reentrenado y guardado `model_ciudad.keras` (40 épocas completadas con éxito).

- [x] **2. Inyección Híbrida de Números Atrasados (Anti-Racha Fría):**
  - Implementada la función `inyectar_atrasados_hibrido` en [logica_loterias.py](file:///home/kurt/loterias/src/loterias/logica_loterias.py).
  - Detecta rachas frías (>5 a 7 días sin acierto en 2 cifras al 1º premio) y fuerza la inclusión de los números más atrasados de la tabla `atrazados_ciudad` (ej. 30, 11) asignándoles prefijos aleatorios únicos de 4 cifras.
  - Test unitario validado en [test_fix_ciudad.py](file:///home/kurt/loterias/src/tests/test_fix_ciudad.py).

- [x] **3. Implementar "Stop-Loss" en la Simulación Incremental:**
  - En `calculate_multi_digit_simulation_stats` ([views.py](file:///home/kurt/coding/website/loterias/views.py)), implementada la regla de gestión de riesgo:
    - Tras 2 turnos consecutivos sin acierto (Previa y Primera), la apuesta se congela en **2x** en lugar de escalar ciegamente a 4x, 8x y 16x.
    - Reduce las pérdidas diarias máximas de **31x ($1.085.000)** a solo **9x ($315.000)**, representando un ahorro de capital de más del **70.9%** por semana en loterías frías.
  - Aplicado tanto en el website local como en el entorno de despliegue Hugging Face.


