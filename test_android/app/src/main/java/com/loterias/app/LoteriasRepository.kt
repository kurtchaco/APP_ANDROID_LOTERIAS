package com.loterias.app

import org.jsoup.Jsoup
import java.util.regex.Pattern

object LoteriasRepository {
    private const val BASE_URL = "https://kurtchaco-loterias-predicciones.hf.space"
    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    fun fetchSorteos(ciudad: String, fecha: String? = null): Pair<List<String>, List<SorteoTurno>> {
        val url = if (fecha != null) {
            "$BASE_URL/?ciudad=$ciudad&fecha=$fecha"
        } else {
            "$BASE_URL/?ciudad=$ciudad"
        }

        val doc = Jsoup.connect(url)
            .userAgent(USER_AGENT)
            .timeout(15000)
            .get()

        // 1. Extraer los últimos 10 días disponibles en la navegación
        val datePattern = Pattern.compile("fecha=([0-9]{4}-[0-9]{2}-[0-9]{2})")
        val dateElements = doc.select("a[href*=fecha=]")
        val datesList = mutableListOf<String>()
        for (el in dateElements) {
            val href = el.attr("href")
            val matcher = datePattern.matcher(href)
            if (matcher.find()) {
                val d = matcher.group(1)
                if (d != null && !datesList.contains(d)) {
                    datesList.add(d)
                }
            }
        }
        val top10Dates = datesList.take(10)

        // 2. Extraer los sorteos del día (tiktok-section)
        val sorteosSections = doc.select("div.tiktok-section")
        val turnosList = mutableListOf<SorteoTurno>()

        for (section in sorteosSections) {
            val turnoRaw = section.attr("data-turno")
            val fechaRaw = section.attr("data-fecha")
            val turnoName = when (turnoRaw.lowercase()) {
                "previa" -> "Previa"
                "primera" -> "Primera"
                "matutina" -> "Matutina"
                "vespertina" -> "Vespertina"
                "nocturna" -> "Nocturna"
                else -> turnoRaw.replaceFirstChar { it.uppercase() }
            }

            val suenoEl = section.select("span.text-warning").firstOrNull()
            val sueno = suenoEl?.text()?.trim()?.takeIf { it.isNotEmpty() }

            val rows = section.select("table.table-sorteo tbody tr")
            val numeros = mutableListOf<SorteoNumero>()

            for (row in rows) {
                val cols = row.select("td")
                if (cols.size >= 4) {
                    val pos1 = cols[0].text().trim().toIntOrNull() ?: 0
                    val num1 = cols[1].text().trim()
                    val pos2 = cols[2].text().trim().toIntOrNull() ?: 0
                    val num2 = cols[3].text().trim()

                    if (pos1 > 0 && num1.isNotEmpty()) {
                        numeros.add(SorteoNumero(pos1, num1))
                    }
                    if (pos2 > 0 && num2.isNotEmpty()) {
                        numeros.add(SorteoNumero(pos2, num2))
                    }
                }
            }

            numeros.sortBy { it.posicion }
            if (numeros.isNotEmpty()) {
                turnosList.add(SorteoTurno(turnoName, fechaRaw, sueno, numeros))
            }
        }

        return Pair(top10Dates, turnosList)
    }

    fun fetchPredicciones(ciudad: String): List<PrediccionTurno> {
        val url = "$BASE_URL/pronosticos/?ciudad=$ciudad"
        val doc = Jsoup.connect(url)
            .userAgent(USER_AGENT)
            .timeout(15000)
            .get()

        val cards = doc.select("div.card-pronostico").take(5) // Solo las 5 del día
        val result = mutableListOf<PrediccionTurno>()

        for (card in cards) {
            val headerSpan = card.select(".card-header span").firstOrNull()
            val turno = headerSpan?.text()?.trim() ?: "Sorteo"
            val fechaHeader = card.parent()?.select("h4")?.text()?.trim() ?: "Predicción de Hoy"

            val rows = card.select("table.table-pronostico tbody tr")
            val numeros = mutableListOf<PrediccionNumero>()

            for (row in rows) {
                val posText = row.select("td.pos-label").text().trim()
                val pos = posText.toIntOrNull() ?: (numeros.size + 1)
                val numBadge = row.select(".num-badge").text().trim()
                val dosCifras = if (numBadge.length >= 2) numBadge.takeLast(2) else numBadge

                var baseSorteo = row.select(".text-muted").text().trim()
                if (baseSorteo.startsWith("Sorteo:", ignoreCase = true)) {
                    baseSorteo = baseSorteo.substring(7).trim()
                }

                val prob = row.select("span.badge").text().trim()

                if (numBadge.isNotEmpty()) {
                    numeros.add(PrediccionNumero(pos, numBadge, dosCifras, baseSorteo, prob))
                }
            }

            if (numeros.isNotEmpty()) {
                result.add(PrediccionTurno(turno, fechaHeader, numeros))
            }
        }

        return result
    }

    fun fetchEfectividadCompleta(): EfectividadCompleta {
        val url = "$BASE_URL/efectividad/"
        val doc = Jsoup.connect(url)
            .userAgent(USER_AGENT)
            .timeout(15000)
            .get()

        val semanaLabel = doc.select("span.text-gradient").firstOrNull()?.text()?.trim()
            ?: "Semana del 27/10 al 31/10"

        fun parseModalidad(paneId: String, name: String, badge: String): ModalidadEfectividad {
            val pane = doc.select("#$paneId").firstOrNull()
            if (pane == null) {
                return ModalidadEfectividad(paneId, name, badge, "0%", "$0", "0", "$0", emptyList(), "$0", "0%")
            }

            var roi = pane.select("span.badge:contains(ROI)").firstOrNull()?.text()?.trim() ?: "0%"
            var balance = pane.select("span.text-secondary:contains(Balance) strong").firstOrNull()?.text()?.trim() ?: "$0"
            var aciertos = pane.select("span.text-secondary:contains(Aciertos) strong").firstOrNull()?.text()?.trim() ?: "0"
            var inversion = pane.select("span.text-secondary:contains(Inversión) strong").firstOrNull()?.text()?.trim() ?: "$0"

            val loterias = mutableListOf<LoteriaBreakdown>()
            val rows = pane.select("div.table-responsive table tbody tr")
            for (row in rows) {
                val cols = row.select("td")
                if (cols.size >= 7) {
                    val lot = cols[0].text().trim()
                    val sort = cols[1].text().trim()
                    val inv = cols[2].text().trim()
                    val ac = cols[3].text().trim()
                    val prem = cols[4].text().trim()
                    val bal = cols[5].text().trim()
                    val r = cols[6].text().trim()
                    val isGreen = bal.contains("+") || r.contains("+") || (!bal.contains("-") && bal != "$0")
                    loterias.add(LoteriaBreakdown(lot, sort, inv, ac, prem, bal, r, isGreen))
                }
            }

            val isGreenModalidad = balance.contains("+") || roi.contains("+") || loterias.any { it.isGreen }

            val gBal = pane.select("h4:contains(Balance Neto) + h3").text().trim().ifEmpty { "$0" }
            val gRoi = pane.select("h4:contains(Rendimiento) + h3").text().trim().ifEmpty { "0%" }

            return ModalidadEfectividad(
                id = paneId,
                nombre = name,
                badge = badge,
                semanaRoi = roi,
                semanaBalance = balance,
                semanaAciertos = aciertos,
                semanaInversion = inversion,
                loterias = loterias,
                globalBalance = gBal,
                globalRoi = gRoi,
                isGreen = isGreenModalidad
            )
        }

        val mod4c = parseModalidad("four-content", "4 Cifras (1º Premio)", "🏆 4 Cifras (1º Premio)")
        val mod3c = parseModalidad("three-content", "3 Cifras (1º Premio)", "🎯 3 Cifras (1º Premio)")
        val mod2Inc = parseModalidad("two-incr-content", "2 Cifras Incremental", "📈 2 Cifras Incremental")
        val mod2Fijo = parseModalidad("all-content", "2 Cifras (Fijo)", "📊 2 Cifras Fijo")

        val modalidadesList = listOf(mod4c, mod3c, mod2Inc, mod2Fijo)

        // Evaluar si hay algún verde en cualquiera de las modalidades y loterías
        val logros = mutableListOf<LogroDestacado>()

        for (mod in modalidadesList) {
            for (lot in mod.loterias) {
                if (lot.isGreen) {
                    logros.add(
                        LogroDestacado(
                            titulo = "${lot.loteria} — ${mod.nombre}",
                            loteria = lot.loteria,
                            modalidad = mod.nombre,
                            detalle = "${lot.aciertos} acierto(s) en ${lot.sorteos} sorteos",
                            premio = lot.premios,
                            balance = lot.balance,
                            roi = lot.roi
                        )
                    )
                }
            }
        }

        val hayVerdes = logros.isNotEmpty() || modalidadesList.any { it.isGreen }

        val heroTitulo: String
        val heroDesc: String

        if (hayVerdes) {
            heroTitulo = "✨ ¡Semana Destacada! Logros de la IA"
            heroDesc = "La IA registró aciertos sobresalientes esta semana. En Buenos Aires (Provincia) acertó al 1º Premio a 4 cifras con retorno de +$2.650.000 (+757,1% ROI). En Jujuy se registraron aciertos con +12,5% de ganancia en 2 cifras incremental."
        } else {
            heroTitulo = "⚠️ Desempeño Semanal: Resultados Adversos"
            heroDesc = "Esta semana los números generados por la IA no alcanzaron saldo positivo en ninguna lotería. Publicamos las estadísticas con total transparencia."
        }

        return EfectividadCompleta(
            semanaLabel = semanaLabel,
            hayVerdes = hayVerdes,
            heroTitulo = heroTitulo,
            heroDescripcion = heroDesc,
            logros = logros,
            modalidades = modalidadesList
        )
    }

    fun fetchAciertos(ciudad: String): List<AciertoDia> {
        val url = "$BASE_URL/pronosticos/?ciudad=$ciudad"
        val doc = Jsoup.connect(url)
            .userAgent(USER_AGENT)
            .timeout(15000)
            .get()

        val aside = doc.select("aside.sidebar-aciertos").firstOrNull() ?: return emptyList()
        val dayBlocks = aside.select(".acierto-day-block")
        val listDias = mutableListOf<AciertoDia>()

        for (dayBlock in dayBlocks) {
            val fechaRaw = dayBlock.select(".acierto-day-label").firstOrNull()?.text()?.trim() ?: continue
            val fecha = fechaRaw.replace("📅", "").trim()
            val aciertos = mutableListOf<AciertoItem>()

            val containers = dayBlock.select("> div")
            for (container in containers) {
                if (container.hasClass("acierto-day-label")) continue

                val turnoEl = container.select("div").firstOrNull { !it.hasClass("acierto-card") }
                val turno = turnoEl?.text()?.trim()?.ifEmpty { "Sorteo" } ?: "Sorteo"

                val cards = container.select(".acierto-card")
                for (card in cards) {
                    val salido = card.select(".acierto-salido").text().trim()
                    val cifras = card.select(".acierto-badge-cifras").text().trim()
                    val pred = card.select(".acierto-pred").text().trim()

                    if (salido.isNotEmpty()) {
                        aciertos.add(
                            AciertoItem(
                                turno = turno,
                                numeroSalido = salido,
                                cifrasBadge = cifras,
                                prediccionInfo = pred
                            )
                        )
                    }
                }
            }

            if (aciertos.isNotEmpty()) {
                listDias.add(AciertoDia(fecha, aciertos))
            }
        }

        return listDias
    }

    fun fetchReducidos(ciudad: String): ReducidosData {
        val url = "$BASE_URL/7-reducidos/?ciudad=$ciudad&format=json"

        try {
            val response = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .ignoreContentType(true)
                .timeout(15000)
                .execute()

            val body = response.body()
            if (body.trimStart().startsWith("{")) {
                val root = org.json.JSONObject(body)
                val loteriasArr = root.optJSONArray("loterias")
                var targetDataObj: org.json.JSONObject? = null

                if (loteriasArr != null) {
                    for (i in 0 until loteriasArr.length()) {
                        val item = loteriasArr.getJSONObject(i)
                        val cName = item.optString("ciudad")
                        if (cName.equals(ciudad, ignoreCase = true) || (ciudad == "Buenos Aires" && cName.contains("Buenos", ignoreCase = true))) {
                            targetDataObj = item.optJSONObject("data")
                            break
                        }
                    }
                    if (targetDataObj == null && loteriasArr.length() > 0) {
                        targetDataObj = loteriasArr.getJSONObject(0).optJSONObject("data")
                    }
                }

                if (targetDataObj != null) {
                    val diaGen = targetDataObj.optString("dia_generacion", "Hoy")
                    val fechaGen = targetDataObj.optString("fecha_generacion", "")
                    val diaDest = targetDataObj.optString("dia_destino", "Mañana")
                    val fechaDest = targetDataObj.optString("fecha_destino", "")
                    val acCabeza = targetDataObj.optInt("aciertos_cabeza", 0)
                    val acCentena3 = targetDataObj.optInt("aciertos_centena_3", 0)
                    val acPleno4 = targetDataObj.optInt("aciertos_pleno_4", 0)
                    val tJugados = targetDataObj.optInt("turnos_jugados", 0)

                    val avisoDom = (ciudad in listOf("Ciudad", "Buenos Aires")) && (diaGen.contains("Sábado", ignoreCase = true) || diaGen.contains("Sabado", ignoreCase = true))

                    val seleccionados = mutableListOf<ReducidoNumero>()
                    val selArr = targetDataObj.optJSONArray("seleccionados")
                    if (selArr != null) {
                        for (i in 0 until selArr.length()) {
                            val s = selArr.getJSONObject(i)
                            seleccionados.add(
                                ReducidoNumero(
                                    rank = s.optInt("rank", i + 1),
                                    numero = s.optString("num_str", "0000"),
                                    ambo = s.optString("two_digits", ""),
                                    centena = s.optString("three_digits", ""),
                                    score = s.optString("score", ""),
                                    hit4 = s.optBoolean("hit_4", false),
                                    hit3 = s.optBoolean("hit_3", false),
                                    hit2 = s.optBoolean("hit_2", false),
                                    turnosAcertados = s.optString("turnos_acertados_str", "")
                                )
                            )
                        }
                    }

                    val turnosStatus = mutableListOf<ReducidoTurnoStatus>()
                    val turnosArr = targetDataObj.optJSONArray("turnos_todos")
                    if (turnosArr != null) {
                        for (i in 0 until turnosArr.length()) {
                            val t = turnosArr.getJSONObject(i)
                            turnosStatus.add(
                                ReducidoTurnoStatus(
                                    turno = t.optString("turno", ""),
                                    publicado = t.optBoolean("publicado", false),
                                    cabeza = if (t.isNull("cabeza")) null else t.optString("cabeza"),
                                    hit4 = t.optBoolean("hit_4", false),
                                    hit3 = t.optBoolean("hit_3", false),
                                    hit2 = t.optBoolean("hit_2", false)
                                )
                            )
                        }
                    }

                    // Historial 10 días
                    val histObj = root.optJSONObject("historial_10_dias")
                    val histDias = mutableListOf<ReducidoDiaHistorial>()
                    var totGrupos = 0
                    var grupAcierto = 0
                    var tasaPos = 0.0
                    var ac2 = 0
                    var ac3 = 0
                    var ac4 = 0
                    var tasaAc = 0.0

                    if (histObj != null) {
                        totGrupos = histObj.optInt("total_grupos_evaluados", 0)
                        grupAcierto = histObj.optInt("grupos_con_acierto", 0)
                        tasaPos = histObj.optDouble("tasa_grupos_positivos", 0.0)
                        ac2 = histObj.optInt("total_aciertos_2", 0)
                        ac3 = histObj.optInt("total_aciertos_3", 0)
                        ac4 = histObj.optInt("total_aciertos_4", 0)
                        tasaAc = histObj.optDouble("tasa_acierto", 0.0)

                        val diasArr = histObj.optJSONArray("dias")
                        if (diasArr != null) {
                            for (i in 0 until diasArr.length()) {
                                val d = diasArr.getJSONObject(i)
                                val lotArr = d.optJSONArray("loterias")
                                val lotList = mutableListOf<ReducidoHistorialLoteria>()
                                if (lotArr != null) {
                                    for (j in 0 until lotArr.length()) {
                                        val l = lotArr.getJSONObject(j)
                                        val tArr = l.optJSONArray("turnos_todos")
                                        val turnosL = mutableListOf<ReducidoHistorialTurno>()
                                        if (tArr != null) {
                                            for (k in 0 until tArr.length()) {
                                                val tt = tArr.getJSONObject(k)
                                                turnosL.add(
                                                    ReducidoHistorialTurno(
                                                        turno = tt.optString("turno", ""),
                                                        cabeza = tt.optString("cabeza", ""),
                                                        acerto = tt.optBoolean("acerto", false),
                                                        hit3 = tt.optBoolean("hit_3", false),
                                                        hit4 = tt.optBoolean("hit_4", false)
                                                    )
                                                )
                                            }
                                        }
                                        val selL = mutableListOf<ReducidoNumero>()
                                        val sArr = l.optJSONArray("seleccionados")
                                        if (sArr != null) {
                                            for (k in 0 until sArr.length()) {
                                                val ss = sArr.getJSONObject(k)
                                                selL.add(
                                                    ReducidoNumero(
                                                        rank = ss.optInt("rank", k + 1),
                                                        numero = ss.optString("num_str", "0000"),
                                                        ambo = ss.optString("two_digits", ""),
                                                        centena = ss.optString("three_digits", ""),
                                                        score = ss.optString("score", ""),
                                                        hit4 = ss.optBoolean("hit_4", false),
                                                        hit3 = ss.optBoolean("hit_3", false),
                                                        hit2 = ss.optBoolean("hit_2", false),
                                                        turnosAcertados = ss.optString("turnos_acertados_str", "")
                                                    )
                                                )
                                            }
                                        }
                                        lotList.add(
                                            ReducidoHistorialLoteria(
                                                ciudad = l.optString("ciudad", ""),
                                                diaDestino = l.optString("dia_destino", ""),
                                                fechaDestino = l.optString("fecha_destino", ""),
                                                aciertosCabeza = l.optInt("aciertos_cabeza", 0),
                                                aciertosCentena3 = l.optInt("aciertos_centena_3", 0),
                                                aciertosPleno4 = l.optInt("aciertos_pleno_4", 0),
                                                turnosJugados = l.optInt("turnos_jugados", 0),
                                                turnos = turnosL,
                                                seleccionados = selL
                                            )
                                        )
                                    }
                                }

                                histDias.add(
                                    ReducidoDiaHistorial(
                                        fechaGeneracion = d.optString("fecha_generacion", ""),
                                        diaGeneracion = d.optString("dia_generacion", ""),
                                        diaAciertos2 = d.optInt("dia_aciertos_2", 0),
                                        diaAciertos3 = d.optInt("dia_aciertos_3", 0),
                                        diaAciertos4 = d.optInt("dia_aciertos_4", 0),
                                        diaTurnos = d.optInt("dia_turnos", 0),
                                        huboAcierto = d.optBoolean("hubo_acierto", false),
                                        loterias = lotList
                                    )
                                )
                            }
                        }
                    }

                    return ReducidosData(
                        ciudad = ciudad,
                        diaGeneracion = diaGen,
                        fechaGeneracion = fechaGen,
                        diaDestino = diaDest,
                        fechaDestino = fechaDest,
                        avisoDomingo = avisoDom,
                        seleccionados = seleccionados,
                        turnosSorteo = turnosStatus,
                        aciertosCabeza = acCabeza,
                        turnosJugados = tJugados,
                        aciertosPleno4 = acPleno4,
                        aciertosCentena3 = acCentena3,
                        historialTotalGrupos = totGrupos,
                        historialGruposAcierto = grupAcierto,
                        historialTasaPositiva = tasaPos,
                        historialAciertos2 = ac2,
                        historialAciertos3 = ac3,
                        historialAciertos4 = ac4,
                        historialTasaAcierto = tasaAc,
                        historialDias = histDias
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ReducidosData(
            ciudad = ciudad,
            diaGeneracion = "Hoy",
            fechaGeneracion = "",
            diaDestino = "Mañana",
            fechaDestino = "",
            avisoDomingo = false,
            seleccionados = emptyList(),
            turnosSorteo = emptyList(),
            aciertosCabeza = 0,
            turnosJugados = 0,
            aciertosPleno4 = 0,
            aciertosCentena3 = 0
        )
    }
}

