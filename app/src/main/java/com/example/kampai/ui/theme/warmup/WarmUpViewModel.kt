package com.example.kampai.ui.theme.warmup

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kampai.domain.models.PlayerModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class WarmupViewModel @Inject constructor() : ViewModel() {

    sealed class WarmupAction {
        data class Phrase(val text: String, val emoji: String, val color: Color) : WarmupAction()
        data class Event(
            val eventType: EventType,
            val title: String,
            val description: String,
            val selectedPlayer: PlayerModel?,
            val emoji: String,
            val color: Color,
            val instruction: String,
            val penaltyDrinks: Int = 2
        ) : WarmupAction()
    }

    enum class EventType {
        CHALLENGE,      // Reto específico
        MEDUSA,         // La Medusa - todos deben participar
        TRUTH_OR_DARE,  // Verdad o Reto - jugador seleccionado elige
        ROULETTE,       // Ruleta Rusa - jugador seleccionado
        SHOT_CHALLENGE, // Reto de shots
        SPEED_TEST,     // Prueba de velocidad
        DANCE_BATTLE,   // Batalla de baile
        MIMIC_DUEL,      // Duelo de mímica
        MOST_LIKELY, //QUIEN ES MÁS PROBABLE QUE
        RPS_DUEL,       // Piedra Papel Tijera
        TONGUE_TWISTER, // Trabalenguas
        THE_JUDGE,      // El Juez
        GIFT,           // Regalos
        VOTING,         // Votación genérica
        STARING_CONTEST,// Duelo de miradas
        SELFIE,         // Selfie grupal
        ICE_PASS
    }

    sealed class GameState {
        object Idle : GameState()
        data class ShowingAction(val action: WarmupAction, val number: Int, val total: Int) : GameState()
        data class ShowingEvent(val event: WarmupAction.Event) : GameState()
        object Finished : GameState()
    }

    private val _gameState = MutableStateFlow<GameState>(GameState.Idle)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _selectedPlayerForEvent = MutableStateFlow<PlayerModel?>(null)
    val selectedPlayerForEvent: StateFlow<PlayerModel?> = _selectedPlayerForEvent.asStateFlow()

    private val phrases = listOf(
        Triple("¡TODOS LOS HOMBRES BEBEN!", "🍺", Color(0xFF2563EB)),
        Triple("¡TODAS LAS MUJERES BEBEN!", "🍷", Color(0xFFEC4899)),
        Triple("El último en ponerse de pie: 2 SHOTS", "🏃", Color(0xFFF59E0B)),
        Triple("El último en tocar el suelo: BEBE", "👇", Color(0xFF10B981)),
        Triple("El más joven: DISTRIBUYE 3 TRAGOS", "🎂", Color(0xFF8B5CF6)),
        Triple("El más joven: BEBE", "🎂", Color(0xFF8B5CF6)),
        Triple("El más mayor: ELIGE A DOS PARA HACER SHOT CRUZADO", "👴", Color(0xFF6366F1)),
        Triple("Quien tenga más hermanos: BEBE", "👨‍👩‍👧‍👦", Color(0xFFEF4444)),
        Triple("¡TODOS BEBEN!", "🎉", Color(0xFFDC2626)),
        Triple("El más alto: ELIGE QUIEN BEBE", "📏", Color(0xFF14B8A6)),
        Triple("El de cumpleaños más cercano: 1 SHOTS", "🎈", Color(0xFFF97316)),
        Triple("Quien vino en vehículo propio: BEBE", "🚗", Color(0xFF06B6D4)),
        Triple("Los solteros: BEBEN", "💔", Color(0xFFDB2777)),
        Triple("Los comprometidos: BEBEN", "💍", Color(0xFFF43F5E)),
        Triple("Último en levantar la mano: 2 SHOTS", "✋", Color(0xFFA855F7)),
        Triple("El que tenga el móvil más viejo: BEBE", "📱", Color(0xFF64748B)),
        // --- CLÁSICOS Y SHOTS ---
        Triple("Un trago todos los que estén sentados", "🪑", Color(0xFF6B7280)),
        Triple("El último en ponerse de pie: ELIGE QUIEN BEBE", "🏃", Color(0xFFF59E0B)),
        Triple("El último en tocar el suelo: BEBE", "👇", Color(0xFF10B981)),
        Triple("Los que tienen el celular en la mano: BEBEN", "📱", Color(0xFFEF4444)),
        Triple("Quien juega Clash Royale: BEBE", "🃏", Color(0xFF6366F1)),


        // --- EDADES Y FÍSICO ---
        Triple("-20 años: 1 shot. +20 años: 2 shots", "🎂", Color(0xFF8B5CF6)),
        Triple("Toman los mayores de 22 años", "👵", Color(0xFF4B5563)),
        Triple("El/la más joven elige a 2 para shot cruzado", "👶", Color(0xFF3B82F6)),
        Triple("La persona más alta y la más baja: SHOT CRUZADO", "📏", Color(0xFF10B981)),
        Triple("Los de pelo oscuro: 1 trago. Teñidos: 2 tragos", "💇", Color(0xFF78350F)),
        Triple("Los que miden más de 1.70m: BEBEN", "🦒", Color(0xFF059669)),
        Triple("Si hay alguien de ojos verdes o azules: BEBE", "👀", Color(0xFF06B6D4)),
        Triple("Si usas lentes: BEBE", "👓", Color(0xFF6366F1)),
        Triple("Los que están de negro: BEBEN", "⚫", Color(0xFF1F2937)),
        Triple("Los que están de blanco: BEBEN", "⚪", Color(0xFF9CA3AF)),
        Triple("Los que tienen hoyuelos: BEBEN", "😊", Color(0xFFF472B6)),
        Triple("Quien tenga el pie más pequeño: BEBE", "🦶", Color(0xFFA3A3A3)),
        Triple("Los de pelo rizado: BEBEN", "🌀", Color(0xFF10B981)),
        Triple("Si eres zurdo: SHOT", "✋", Color(0xFF8B5CF6)),
        Triple("Si tienes pecas: BEBE", "🌞", Color(0xFFF59E0B)),

        // --- PERSONALIDAD Y SIGNOS ---
        Triple("Toman: Leo, Capricornio, Cáncer y Géminis", "🦁", Color(0xFFF59E0B)),
        Triple("Toman: Acuario, Tauro, Aries y Libra", "♈", Color(0xFFEC4899)),
        Triple("Si tu nombre lleva una U: BEBES", "🔤", Color(0xFF8B5CF6)),
        Triple("El nombre más largo: BEBE", "📝", Color(0xFF10B981)),
        Triple("Los que se enamoran rápido: BEBEN", "❤️‍🔥", Color(0xFFE11D48)),
        Triple("Si te enojas rápido: TRAGO", "😡", Color(0xFFDC2626)),
        Triple("Sagitario: SHOT por andar hablando demás", "♐", Color(0xFFF59E0B)),

        // --- SITUACIONES Y CONFESIONES (PICANTE) ---
        Triple("Si fumas: BEBE UN TRAGO LARGO", "🚬", Color(0xFF374151)),
        Triple("Si vas al gym: BEBES. Si no vas: TAMBIÉN", "💪", Color(0xFFEF4444)),
        Triple("Solteros: 1 shot. En algo: 2 shots", "💔", Color(0xFFF43F5E)),
        Triple("iPhone: 2 shots. Samsung: 1. Otros: Salvados", "📱", Color(0xFF2563EB)),
        Triple("Si volviste con tu ex: BEBE", "🤡", Color(0xFFDC2626)),
        Triple("Si saliste a escondidas alguna vez: BEBE", "🤫", Color(0xFF8B5CF6)),
        Triple("Si besarías a alguien de aquí: BEBE", "💋", Color(0xFFEC4899)),
        Triple("Primera vez antes de los 16: BEBE", "🔞", Color(0xFFB91C1C)),
        Triple("Si prefieres estar casado que soltero: SHOT", "💍", Color(0xFF0EA5E9)),
        Triple("Si besaste a alguien por lástima: SHOT", "😬", Color(0xFFF59E0B)),
        Triple("Si tienes un Instagram falso para stalkear: SHOT", "🕵️", Color(0xFF10B981)),
        Triple("Si usaste Apps de Citas: TRAGO LARGO", "🔥", Color(0xFFF97316)),
        Triple("Si tuviste un sueño erótico con alguien presente: BEBE", "💭", Color(0xFF818CF8)),
        Triple("Si te pillaron en el acto: BEBE", "🚪", Color(0xFFEF4444)),
        Triple("Si tienes el visto desactivado: BEBE", "✔️", Color(0xFF3B82F6)),
        Triple("El que tenga más seguidores en IG: SHOT", "📸", Color(0xFFD946EF)),
        Triple("Si te gustan los besos con mordida: SHOT", "🐺", Color(0xFF8B0000)),
        Triple("Si has fantaseado con un profe/jefe: SHOT", "📚", Color(0xFFF59E0B)),
        Triple("Si revisaste el celular de alguien a escondidas: SHOT", "👀", Color(0xFFEF4444)),
        Triple("Si te han revisado el celular: TRAGO", "📱", Color(0xFFDC2626)),

        // --- VOTACIONES GRUPALES ---
        Triple("Voten al que más ríe: BEBE", "😂", Color(0xFFF59E0B)),
        Triple("Voten al que mejor huele: REPARTE 2", "👃", Color(0xFF34D399)),
        Triple("Voten al más gastador: SHOT", "🤑", Color(0xFFD946EF)),
        Triple("Señalen al más callado: TOMA SHOT", "🤫", Color(0xFF64748B)),
        Triple("Voten al más fiestero: BEBE", "🥳", Color(0xFFF59E0B)),
        Triple("Voten al menos fiestero: BEBE", "🥱", Color(0xFF94A3B8)),
        Triple("Voten al más sinvergüenza: BEBE", "😈", Color(0xFFEF4444)),
        Triple("Voten al más vergonzoso: BEBE", "😳", Color(0xFFF472B6)),
        Triple("Voten al más tímido: BEBE", "🥺", Color(0xFF818CF8)),
        Triple("Voten al más extrovertido: BEBE", "🗣️", Color(0xFF34D399)),
        Triple("Voten al Otaku del grupo: BEBE", "🤓", Color(0xFFFCD34D)),
        Triple("Voten al más Gamer: BEBE", "🎮", Color(0xFF60A5FA)),
        Triple("Voten a la persona mejor vestida", "👗", Color(0xFFEC4899)),
        Triple("¡VOTACIÓN! El más señalado hace fondo", "🗳️", Color(0xFF4C1D95)),

        // --- ACCIONES RÁPIDAS ---
        Triple("El que propuso jugar elige a 3 para beber", "👑", Color(0xFFF59E0B)),
        Triple("La persona mayor elige 2 para que se den un pico", "👴", Color(0xFF6B7280)),
        Triple("El que llegó más temprano elige 2 para un pico", "⏰", Color(0xFF10B981)),
        Triple("El que llegó más tarde: BEBE", "🐢", Color(0xFFF59E0B)),
        Triple("El que puso la casa elige 2 para beber", "🏠", Color(0xFF0EA5E9)),
        Triple("Todos serios: El primero en reírse BEBE", "😐", Color(0xFFDC2626)),
        Triple("Cuenten hasta 10: Al que le toca el 10 BEBE", "🔟", Color(0xFF8B5CF6)),
        Triple("El que tenga menos batería: BEBE", "🪫", Color(0xFFEF4444)),
        Triple("El que tenga más batería elige 2", "🔋", Color(0xFF22C55E)),
        Triple("Cada mujer elige a 1 persona para beber", "👉", Color(0xFFF472B6)),
        Triple("Hombres masajean a mujeres 1 min (o shot)", "💆‍♂️", Color(0xFF3B82F6)),
        Triple("Mujeres masajean a hombres 1 min (o shot)", "💆‍♀️", Color(0xFFEC4899)),
        Triple("El último que toque su nariz: BEBE", "👃", Color(0xFFEF4444)),
        Triple("Los que tengan tatuajes: BEBEN", "💉", Color(0xFF1F2937)),
        Triple("Los que no tengan tatuajes: BEBEN", "👶", Color(0xFF9CA3AF)),
        Triple("Si llevas ropa interior roja: SHOT", "👙", Color(0xFFDC2626)),
        Triple("Intercambia una prenda con la persona de la derecha o BEBE", "👕", Color(0xFF8B5CF6)),
        Triple("Quien sepa cocinar mejor: Elige quién bebe", "🍳", Color(0xFF10B981)),
        Triple("Quien tenga el pie más grande: BEBE", "🦶", Color(0xFF6B7280)),
        Triple("Si has viajado a otro continente: SALVADO. Los demás beben.", "✈️", Color(0xFF0EA5E9)),
        Triple("El que tenga más monedas en el bolsillo: BEBE", "💰", Color(0xFFFFD700)),
        Triple("Si tienes mascota: BEBE", "🐶", Color(0xFFA855F7)),
        Triple("Todos los que tengan hermanos: BEBEN", "👫", Color(0xFFF43F5E)),
        Triple("El que hable más idiomas: Reparte 2 tragos", "🗣️", Color(0xFF34D399)),
        Triple("Si conduces moto: BEBE", "🏍️", Color(0xFF1F2937)),
        // --- (NO BEBER) ---
        Triple("Digan 3 verdades y 1 mentira de ustedes mismos", "🗣️", Color(0xFFE879F9)),
        Triple("CONFESIÓN: ¿A qué edad fue su primera vez?", "🗣️", Color(0xFF7C3AED)),
        Triple("CONFESIÓN: ¿Lugar más extraño donde lo han hecho?", "🌍", Color(0xFF059669)),
        Triple("CONFESIÓN: ¿Qué es lo primero que miran en alguien?", "👀", Color(0xFFDB2777)),
        Triple("¡CAMBIO DE LUGAR! Todos cambien de lugar con quien sea", "🔄", Color(0xFF8B5CF6)),
        Triple("Muestra la última foto de tu galería (Sin explicaciones)", "📱", Color(0xFFEC4899)),
        Triple("Guerra de Pulgares con la persona de tu derecha", "👍", Color(0xFFF59E0B)),
        Triple("El que tenga el pie más grande elige la próxima canción", "🦶", Color(0xFF10B981)),
        Triple("Ronda de Abrazos: Abraza a la persona de tu izquierda", "🤗", Color(0xFFF472B6)),
        Triple("Muestren su fondo de pantalla de bloqueo", "📲", Color(0xFF3B82F6)),
        Triple("Intenten tocar su nariz con la lengua.", "🤪", Color(0xFFF59E0B)),
        Triple("Señalen a quien creen que sería el mejor presidente del país", "🏛️", Color(0xFF60A5FA)),
        Triple("Hazle un masaje de hombros de 1 min a quien tengas a la derecha", "💆", Color(0xFFEC4899)),
        Triple("El último en tocar algo de color VERDE pierde", "🟢", Color(0xFF22C55E)),
        Triple("El que tenga menos batería debe mostrar su última búsqueda en Google", "🔍", Color(0xFFEF4444)),

    )

    private val events = listOf(
        // Retos de shots
        EventDefinition(
            type = EventType.SHOT_CHALLENGE,
            title = "RETO DE SHOTS",
            emoji = "🥃",
            color = Color(0xFFEF4444),
            descriptions = listOf(
                "debe beber 2 shots agachado",
                "debe beber 1 shot sin usar las manos",
                "debe beber 2 shots y girar 3 veces",
                "debe beber 3 shots en 10 segundos"
            ),
            instruction = "Completa el reto o bebe 2 tragos extra de penalización"
        ),
        // Retos de velocidad
        EventDefinition(
            type = EventType.SPEED_TEST,
            title = "PRUEBA DE VELOCIDAD",
            emoji = "⚡",
            color = Color(0xFFF59E0B),
            descriptions = listOf(
                "debe nombrar 5 países en 10 segundos",
                "debe decir 10 palabras que rimen con 'ON' en 15 segundos",
                "debe nombrar 7 marcas de cerveza en 12 segundos"
            ),
            instruction = "¡Rápido! Si no lo logras, bebes 2 tragos"
        ),
        // Retos de valentía
        EventDefinition(
            type = EventType.CHALLENGE,
            title = "RETO DE VALENTÍA",
            emoji = "😨",
            color = Color(0xFF8B5CF6),
            descriptions = listOf(
                "debe hacer 15 sentadillas mientras bebe",
                "debe cantar una canción en voz alta sin parar",
                "debe lamer el cuello de la persona a su derecha",
                "debe decir algo vergonzoso que haya hecho"
            ),
            instruction = "¿Te atreves? Si no, bebe 2 tragos"
        ),

            // --- EVENTO: PIEDRA PAPEL TIJERA ---
            EventDefinition(
                type = EventType.RPS_DUEL,
                title = "PIEDRA, PAPEL O TIJERA",
                emoji = "✂️",
                color = Color(0xFFF59E0B),
                descriptions = listOf("Desafía a tu oponente."), // Se rellena en generateRandomEvent
                instruction = "Si el HOMBRE gana, las mujeres beben. Si la MUJER gana, los hombres beben."
            ),

        EventDefinition(
            type = EventType.TONGUE_TWISTER,
            title = "TRABALENGUAS",
            emoji = "👅",
            color = Color(0xFF10B981),
            descriptions = listOf(
                "Camarero desencamaronamelo.",
                "Tres tristes tigres tragaban trigo en un trigal.",
                "Pablito clavó un clavito, ¿qué clavito clavó Pablito?",
                "El hipopótamo Hipo está con hipo, ¿quién le quita el hipo?",
                "Parra tenía una perra. La perra de Parra subió a la parra de Guerra."
            ),
            instruction = "Dilo rápido. Si fallas: 2 shots. Si lo logras: repartes 3."
        ),

        EventDefinition(
            type = EventType.STARING_CONTEST,
            title = "DUELO DE MIRADAS",
            emoji = "👁️",
            color = Color(0xFF06B6D4),
            descriptions = listOf("¡No pestañees!"), // Se rellena automáticamente con el rival
            instruction = "Mírense fijamente. El primero en pestañear o reírse: BEBE."
        ),

        EventDefinition(
            type = EventType.THE_JUDGE,
            title = "EL JUEZ",
            emoji = "⚖️",
            color = Color(0xFF1F2937),
            descriptions = listOf(
                "Crea una regla Ej: Nadie puede decir 'SI' o 'NO'.",
            ),
            instruction = "Quien rompa tu regla, bebe. (Dura hasta que salga otro Juez)"
        ),

        EventDefinition(
            type = EventType.GIFT,
            title = "CAJA MISTERIOSA",
            emoji = "🎁",
            color = Color(0xFFEC4899),
            descriptions = listOf(
                // POSITIVAS
                "¡PREMIO! Puedes regalar 3 tragos a quien quieras.",
                "¡SALVACIÓN! Comodín para no cumplir un reto futuro.",
                "¡DJ! Eliges la música por los próximos 10 minutos.",
                "¡INMUNE! Nadie te puede mandar a beber por 2 rondas.",
                "¡VENGANZA! Elige a alguien para que se acabe su bebida.",
                "¡REY! Todos deben tratarte de 'Usted' hasta tu próximo turno.",
                "¡MAESTRO! Puedes cambiar una regla del juego ahora mismo.",
                "¡SUERTE! No bebes nada en esta ronda.",
                "¡DEDO MÁGICO! A quien señales debe beber (un uso).",
                "¡INTERCAMBIO! Cambia de lugar con quien quieras.",

                // NEGATIVAS
                "¡CASTIGO! Bebes el doble en tu próximo turno.",
                "¡MALA SUERTE! Shot de tequila (o lo más fuerte que haya).",
                "¡FITNESS! Haz 10 flexiones ahora mismo.",
                "¡KARAOKE! Canta el estribillo de una canción a capela.",
                "¡MAYORDOMO! Debes servirle el trago a los demás por 2 rondas.",
                "¡ESTATUA! Quédate congelado hasta tu próximo turno.",
                "¡SIN MANOS! Debes beber tu próximo trago sin manos.",
                "¡EXILIADO! Ve al rincón por 1 minuto.",
                "¡BAILARÍN! Baila sin música por 30 segundos.",
                "¡FONDO! Termina tu vaso ahora mismo."
            ),
            instruction = "Si levantaste la mano primero... ¡ESTO ES PARA TI!"
        ),

        // --- EVENTO: HIELO ---
        EventDefinition(
            type = EventType.ICE_PASS,
            title = "EL HIELO",
            emoji = "🧊",
            color = Color(0xFF3B82F6),
            descriptions = listOf(
                "Pasen un hielo boca a boca por toda la ronda.",
                "Hagan una fila H-M-H-M y pasen el hielo."
            ),
            instruction = "Si se te cae o no te animas: SHOT."
        ),

        // --- EVENTO: SELFIE ---
        EventDefinition(
            type = EventType.SELFIE,
            title = "MOMENTO SELFIE",
            emoji = "📸",
            color = Color(0xFF8B5CF6),
            descriptions = listOf("¡Foto grupal ahora mismo!"),
            instruction = "Tómense la foto y súbanla. El que tomó la foto elige quién bebe."
        ),


        // Batallas de baile
        EventDefinition(
            type = EventType.DANCE_BATTLE,
            title = "BATALLA DE BAILE",
            emoji = "💃",
            color = Color(0xFFEC4899),
            descriptions = listOf(
                "debe bailar 30 segundos sin parar",
                "debe bailar imitando a alguien del grupo",
                "debe hacer un baile viral de TikTok"
            ),
            instruction = "¡Muéstrate! Si no bailas, bebes 2 tragos"
        ),

        // --- NUEVO: QUIÉN ES MÁS PROBABLE ---
        EventDefinition(
            type = EventType.MOST_LIKELY,
            title = "QUIÉN ES MÁS PROBABLE",
            emoji = "👉",
            color = Color(0xFF0EA5E9),
            descriptions = listOf(
                "que hoy termine vomitando",
                "que se case primero",
                "que acabe en la cárcel algún día",
                "que se vuelva millonario",
                "que llame a su ex esta noche",
                "que se una a una secta"
            ),
            instruction = "A la cuenta de 3, todos señalan a alguien. El más señalado BEBE."
        ),

        // --- NUEVO: LA MEDUSA ---
        EventDefinition(
            type = EventType.MEDUSA,
            title = "LA MEDUSA",
            emoji = "🐍",
            color = Color(0xFF10B981),
            descriptions = listOf("Todos agachan la cabeza..."),
            instruction = "Cuenten hasta 3 y miren a alguien. Si cruzas miradas con esa persona, deben gritar ¡MEDUSA! y AMBOS BEBEN"
        ),

        // --- NUEVO: DUELO DE MÍMICA ---
        EventDefinition(
            type = EventType.MIMIC_DUEL,
            title = "DUELO DE MÍMICA",
            emoji = "🎭",
            color = Color(0xFFFFD700),
            descriptions = listOf(
                "debe imitar a otro jugador sin hablar",
                "debe imitar un animal haciendo el amor",
                "debe imitar su posición sexual favorita"
            ),
            instruction = "Los demás adivinan. Si nadie adivina en 30s, BEBES."
        ),

    )


    private var currentRound = 0
    private val totalRounds = 100 //Cantidad de rondas
    private val eventFrequency = 4 //Frecuencia de eventos en rondas (cada 4 rondas)
    private var eventPlayers: List<PlayerModel> = emptyList()

    fun setPlayers(players: List<PlayerModel>) {
        eventPlayers = players
    }

    fun startWarmup() {
        currentRound = 0
        showNextAction()
    }

    fun nextAction() {
        currentRound++
        if (currentRound >= totalRounds) {
            _gameState.value = GameState.Finished
        } else {
            showNextAction()
        }
    }

    private fun showNextAction() {
        // Probabilidad de evento basada en rondas
        val shouldShowEvent = currentRound > 0 && currentRound % eventFrequency == 0 && Random.nextBoolean()

        val action = if (shouldShowEvent && eventPlayers.isNotEmpty()) {
            generateRandomEvent()
        } else {
            val (text, emoji, color) = phrases.random()
            WarmupAction.Phrase(text, emoji, color)
        }

        when (action) {
            is WarmupAction.Event -> {
                _selectedPlayerForEvent.value = action.selectedPlayer
                _gameState.value = GameState.ShowingEvent(action)
            }
            else -> {
                _gameState.value = GameState.ShowingAction(action, currentRound + 1, totalRounds)
            }
        }
    }

    private fun generateRandomEvent(): WarmupAction {
        val eventDef = events.random()

        // 1. Inicializamos variables
        var selectedPlayer: PlayerModel? = null
        var finalDescription = eventDef.descriptions.random()

        // 2. Eventos Grupales (Sin jugador seleccionado en tarjeta principal)
        val groupEvents = listOf(
            EventType.MOST_LIKELY,
            EventType.MEDUSA,
            EventType.VOTING,
            EventType.SELFIE,
            EventType.ICE_PASS,
            EventType.GIFT // El regalo es para quien levantó la mano, no seleccionado por app
        )

        // Si NO es grupal, seleccionamos un jugador base
        if (eventDef.type !in groupEvents) {
            selectedPlayer = eventPlayers.random()
        }

        // 3. Lógica Especial: PIEDRA PAPEL O TIJERA (Hombre vs Mujer preferentemente)
        if (eventDef.type == EventType.RPS_DUEL && eventPlayers.size >= 2) {
            val male = eventPlayers.find { it.gender == com.example.kampai.domain.models.Gender.MALE }
            val female = eventPlayers.find { it.gender == com.example.kampai.domain.models.Gender.FEMALE }

            if (male != null && female != null) {
                selectedPlayer = male
                finalDescription = "VS ${female.name}\n¡Duelo de sexos!"
            } else {
                // Si no hay mix, 2 aleatorios
                val p1 = eventPlayers.random()
                val p2 = (eventPlayers - p1).random()
                selectedPlayer = p1
                finalDescription = "VS ${p2.name}\n¡A muerte!"
            }
        }

        // 4. Lógica Especial: DUELO DE MIRADAS (2 Aleatorios)
        else if (eventDef.type == EventType.STARING_CONTEST && eventPlayers.size >= 2) {
            val p1 = eventPlayers.random()
            val p2 = (eventPlayers - p1).random()
            selectedPlayer = p1
            finalDescription = "Te enfrentas a...\n👉 ${p2.name} 👈\n\n¡No pestañees!"
        }

        return WarmupAction.Event(
            eventType = eventDef.type,
            title = eventDef.title,
            description = finalDescription,
            selectedPlayer = selectedPlayer,
            emoji = eventDef.emoji,
            color = eventDef.color,
            instruction = eventDef.instruction,
            penaltyDrinks = 2
        )
    }

    fun acceptChallenge() {
        // El jugador aceptó el reto - se espera que lo complete
        nextAction()
    }

    fun rejectChallenge() {
        // El jugador rechazó - penalización de 2 tragos
        nextAction()
    }

    fun reset() {
        _gameState.value = GameState.Idle
        _selectedPlayerForEvent.value = null
        currentRound = 0
    }

    private data class EventDefinition(
        val type: EventType,
        val title: String,
        val emoji: String,
        val color: Color,
        val descriptions: List<String>,
        val instruction: String
    )
}