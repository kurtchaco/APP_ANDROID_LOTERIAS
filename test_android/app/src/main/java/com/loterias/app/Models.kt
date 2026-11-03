package com.loterias.app

data class SorteoNumero(
    val posicion: Int,
    val numero: String
)

data class SorteoTurno(
    val turno: String,
    val fecha: String,
    val sueno: String?,
    val numeros: List<SorteoNumero>
)

data class PrediccionNumero(
    val pos: Int,
    val numero: String,
    val dosCifras: String,
    val baseSorteo: String,
    val probabilidad: String
)

data class PrediccionTurno(
    val turno: String,
    val fechaText: String,
    val numeros: List<PrediccionNumero>
)

data class LoteriaBreakdown(
    val loteria: String,
    val sorteos: String,
    val inversion: String,
    val aciertos: String,
    val premios: String,
    val balance: String,
    val roi: String,
    val isGreen: Boolean = false
)

data class LogroDestacado(
    val titulo: String,
    val loteria: String,
    val modalidad: String,
    val detalle: String,
    val premio: String,
    val balance: String,
    val roi: String
)

data class ModalidadEfectividad(
    val id: String,
    val nombre: String,
    val badge: String,
    val semanaRoi: String,
    val semanaBalance: String,
    val semanaAciertos: String,
    val semanaInversion: String,
    val loterias: List<LoteriaBreakdown>,
    val globalBalance: String,
    val globalRoi: String,
    val isGreen: Boolean = false
)

data class EfectividadCompleta(
    val semanaLabel: String,
    val hayVerdes: Boolean,
    val heroTitulo: String,
    val heroDescripcion: String,
    val logros: List<LogroDestacado>,
    val modalidades: List<ModalidadEfectividad>
)

data class AciertoItem(
    val turno: String,
    val numeroSalido: String,
    val cifrasBadge: String,
    val prediccionInfo: String
)

data class AciertoDia(
    val fechaLabel: String,
    val aciertos: List<AciertoItem>
)

data class ReducidoNumero(
    val rank: Int,
    val numero: String,
    val ambo: String,
    val centena: String,
    val score: String,
    val hit4: Boolean = false,
    val hit3: Boolean = false,
    val hit2: Boolean = false,
    val turnosAcertados: String = ""
)

data class ReducidoTurnoStatus(
    val turno: String,
    val publicado: Boolean,
    val cabeza: String?,
    val hit4: Boolean = false,
    val hit3: Boolean = false,
    val hit2: Boolean = false
)

data class ReducidoHistorialTurno(
    val turno: String,
    val cabeza: String,
    val acerto: Boolean,
    val hit3: Boolean = false,
    val hit4: Boolean = false
)

data class ReducidoHistorialLoteria(
    val ciudad: String,
    val diaDestino: String,
    val fechaDestino: String,
    val aciertosCabeza: Int,
    val aciertosCentena3: Int,
    val aciertosPleno4: Int,
    val turnosJugados: Int,
    val turnos: List<ReducidoHistorialTurno>
)

data class ReducidoDiaHistorial(
    val fechaGeneracion: String,
    val diaGeneracion: String,
    val diaAciertos2: Int,
    val diaAciertos3: Int,
    val diaAciertos4: Int,
    val diaTurnos: Int,
    val huboAcierto: Boolean,
    val loterias: List<ReducidoHistorialLoteria>
)

data class ReducidosData(
    val ciudad: String,
    val diaGeneracion: String,
    val fechaGeneracion: String,
    val diaDestino: String,
    val fechaDestino: String,
    val avisoDomingo: Boolean,
    val seleccionados: List<ReducidoNumero>,
    val turnosSorteo: List<ReducidoTurnoStatus>,
    val aciertosCabeza: Int,
    val turnosJugados: Int,
    val aciertosPleno4: Int,
    val aciertosCentena3: Int,
    // Historial 10 días
    val historialTotalGrupos: Int = 0,
    val historialGruposAcierto: Int = 0,
    val historialTasaPositiva: Double = 0.0,
    val historialAciertos2: Int = 0,
    val historialAciertos3: Int = 0,
    val historialAciertos4: Int = 0,
    val historialTasaAcierto: Double = 0.0,
    val historialDias: List<ReducidoDiaHistorial> = emptyList()
)

