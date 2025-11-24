package com.example.kampai.ui.theme.warmup

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.kampai.R
import com.example.kampai.domain.models.PlayerModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class WarmupViewModel @Inject constructor() : ViewModel() {

    sealed class WarmupAction {
        // Ahora guarda el ID del recurso (R.string.xxx)
        data class Phrase(val textRes: Int, val emoji: String, val color: Color) : WarmupAction()

        data class Event(
            val eventType: EventType,
            val titleRes: Int,
            val descriptionRes: Int, // ID del recurso base
            val descriptionArgs: List<String> = emptyList(), // Argumentos para strings dinámicos (%s)
            val selectedPlayer: PlayerModel?,
            val emoji: String,
            val color: Color,
            val instructionRes: Int,
            val penaltyDrinks: Int = 2,
            val giftPhase: GiftPhase? = null
        ) : WarmupAction()
    }

    enum class GiftPhase { RAISE_HAND, REVEAL }

    enum class EventType {
        CHALLENGE, MEDUSA, TRUTH_OR_DARE, ROULETTE, SHOT_CHALLENGE, SPEED_TEST,
        DANCE_BATTLE, MIMIC_DUEL, MOST_LIKELY, RPS_DUEL, TONGUE_TWISTER,
        THE_JUDGE, GIFT, VOTING, STARING_CONTEST, SELFIE, ICE_PASS
    }

    sealed class GameState {
        object Idle : GameState()
        data class ShowingAction(val action: WarmupAction, val number: Int, val total: Int) : GameState()
        data class ShowingEvent(val event: WarmupAction.Event) : GameState()
        object Finished : GameState()
    }

    private val _gameState = MutableStateFlow<GameState>(GameState.Idle)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _showRulesDialog = MutableStateFlow(false)
    val showRulesDialog: StateFlow<Boolean> = _showRulesDialog.asStateFlow()

    private val _selectedPlayerForEvent = MutableStateFlow<PlayerModel?>(null)
    val selectedPlayerForEvent: StateFlow<PlayerModel?> = _selectedPlayerForEvent.asStateFlow()

    // LISTA DE FRASES CON IDs
    private val phrases = listOf(
        Triple(R.string.ph_men_drink, "🍺", Color(0xFF2563EB)),
        Triple(R.string.ph_women_drink, "🍷", Color(0xFFEC4899)),
        Triple(R.string.ph_last_stand, "🏃", Color(0xFFF59E0B)),
        Triple(R.string.ph_floor_touch, "👇", Color(0xFF10B981)),
        Triple(R.string.ph_youngest_distribute, "🎂", Color(0xFF8B5CF6)),
        Triple(R.string.ph_everyone_drinks, "🎉", Color(0xFFDC2626)),
        Triple(R.string.ph_tallest, "📏", Color(0xFF14B8A6)),
        Triple(R.string.ph_singles, "💔", Color(0xFFDB2777)),
        Triple(R.string.ph_couples, "💍", Color(0xFFF43F5E)),
        Triple(R.string.ph_phone_addict, "📱", Color(0xFFEF4444)),
        Triple(R.string.ph_colors_black, "⚫", Color(0xFF1F2937)),
        Triple(R.string.ph_colors_white, "⚪", Color(0xFF9CA3AF)),
        Triple(R.string.ph_never_have_u, "🔤", Color(0xFF8B5CF6)),
        Triple(R.string.ph_smoke, "🚬", Color(0xFF374151)),
        Triple(R.string.ph_gym, "💪", Color(0xFFEF4444)),
        Triple(R.string.ph_vote_laugh, "😂", Color(0xFFF59E0B)),
        Triple(R.string.ph_vote_shy, "🥺", Color(0xFF818CF8)),
        Triple(R.string.ph_action_battery, "🪫", Color(0xFFEF4444)),
        Triple(R.string.ph_no_explain, "📱", Color(0xFFEC4899)),
        Triple(R.string.ph_hug, "🤗", Color(0xFFF472B6))
    )

    // LISTA DE EVENTOS CON IDs
    private val events = listOf(
        EventDefinition(
            type = EventType.SHOT_CHALLENGE,
            titleRes = R.string.evt_title_shots,
            emoji = "🥃",
            color = Color(0xFFEF4444),
            descriptionsRes = listOf(R.string.evt_desc_shot_1, R.string.evt_desc_shot_2),
            instructionRes = R.string.evt_inst_shots
        ),
        EventDefinition(
            type = EventType.SPEED_TEST,
            titleRes = R.string.evt_title_speed,
            emoji = "⚡",
            color = Color(0xFFF59E0B),
            descriptionsRes = listOf(R.string.evt_desc_speed_1, R.string.evt_desc_speed_2),
            instructionRes = R.string.evt_inst_speed
        ),
        EventDefinition(
            type = EventType.CHALLENGE,
            titleRes = R.string.evt_title_challenge,
            emoji = "😨",
            color = Color(0xFF8B5CF6),
            descriptionsRes = listOf(R.string.evt_desc_challenge_1, R.string.evt_desc_challenge_2),
            instructionRes = R.string.evt_inst_challenge
        ),
        // Evento especial (la descripción se decide dinámicamente)
        EventDefinition(
            type = EventType.RPS_DUEL,
            titleRes = R.string.evt_title_rps,
            emoji = "✂️",
            color = Color(0xFFF59E0B),
            descriptionsRes = listOf(R.string.evt_desc_rps_random), // Placeholder
            instructionRes = R.string.evt_inst_rps
        ),
        EventDefinition(
            type = EventType.STARING_CONTEST,
            titleRes = R.string.evt_title_staring,
            emoji = "👁️",
            color = Color(0xFF06B6D4),
            descriptionsRes = listOf(R.string.evt_desc_staring),
            instructionRes = R.string.evt_inst_staring
        ),
        EventDefinition(
            type = EventType.THE_JUDGE,
            titleRes = R.string.evt_title_judge,
            emoji = "⚖️",
            color = Color(0xFF1F2937),
            descriptionsRes = listOf(R.string.evt_desc_judge),
            instructionRes = R.string.evt_inst_judge
        ),
        EventDefinition(
            type = EventType.GIFT,
            titleRes = R.string.evt_title_gift,
            emoji = "🎁",
            color = Color(0xFFEC4899),
            descriptionsRes = listOf(R.string.gift_pos_1), // Placeholder
            instructionRes = R.string.evt_inst_gift_prep
        ),
        EventDefinition(
            type = EventType.ICE_PASS,
            titleRes = R.string.evt_title_ice,
            emoji = "🧊",
            color = Color(0xFF3B82F6),
            descriptionsRes = listOf(R.string.evt_desc_ice),
            instructionRes = R.string.evt_inst_ice
        ),
        EventDefinition(
            type = EventType.SELFIE,
            titleRes = R.string.evt_title_selfie,
            emoji = "📸",
            color = Color(0xFF8B5CF6),
            descriptionsRes = listOf(R.string.evt_desc_selfie),
            instructionRes = R.string.evt_inst_selfie
        ),
        EventDefinition(
            type = EventType.MEDUSA,
            titleRes = R.string.evt_title_medusa,
            emoji = "🐍",
            color = Color(0xFF10B981),
            descriptionsRes = listOf(R.string.evt_desc_medusa),
            instructionRes = R.string.evt_inst_medusa
        )
    )

    private var currentRound = 0
    private val totalRounds = 50
    private val eventFrequency = 4
    private var eventPlayers: List<PlayerModel> = emptyList()

    fun setPlayers(players: List<PlayerModel>) { eventPlayers = players }
    fun showRules() { _showRulesDialog.value = true }
    fun hideRules() { _showRulesDialog.value = false }

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
        val shouldShowEvent = currentRound > 0 && currentRound % eventFrequency == 0 && Random.nextBoolean()

        val action = if (shouldShowEvent && eventPlayers.isNotEmpty()) {
            generateRandomEvent()
        } else {
            val (textRes, emoji, color) = phrases.random()
            WarmupAction.Phrase(textRes, emoji, color)
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
        var selectedPlayer: PlayerModel? = null
        var finalDescRes = eventDef.descriptionsRes.random()
        val descArgs = mutableListOf<String>() // Argumentos dinámicos
        var instructionRes = eventDef.instructionRes
        var giftPhase: GiftPhase? = null

        val groupEvents = listOf(
            EventType.MOST_LIKELY, EventType.MEDUSA, EventType.VOTING,
            EventType.SELFIE, EventType.ICE_PASS, EventType.GIFT
        )

        if (eventDef.type !in groupEvents && eventPlayers.isNotEmpty()) {
            selectedPlayer = eventPlayers.random()
        }

        // LÓGICA DE REGALOS
        if (eventDef.type == EventType.GIFT) {
            giftPhase = GiftPhase.RAISE_HAND
            finalDescRes = R.string.evt_desc_gift_prep
        }

        // LÓGICA PIEDRA PAPEL TIJERA
        if (eventDef.type == EventType.RPS_DUEL && eventPlayers.size >= 2) {
            val male = eventPlayers.find { it.gender == com.example.kampai.domain.models.Gender.MALE }
            val female = eventPlayers.find { it.gender == com.example.kampai.domain.models.Gender.FEMALE }

            if (male != null && female != null) {
                selectedPlayer = male
                finalDescRes = R.string.evt_desc_rps_mix
                descArgs.add(female.name)
            } else {
                val p1 = eventPlayers.random()
                val p2 = (eventPlayers - p1).random()
                selectedPlayer = p1
                finalDescRes = R.string.evt_desc_rps_random
                descArgs.add(p2.name)
            }
        }

        // LÓGICA DUELO DE MIRADAS
        else if (eventDef.type == EventType.STARING_CONTEST && eventPlayers.size >= 2) {
            val p1 = eventPlayers.random()
            val p2 = (eventPlayers - p1).random()
            selectedPlayer = p1
            finalDescRes = R.string.evt_desc_staring
            descArgs.add(p2.name)
        }

        return WarmupAction.Event(
            eventType = eventDef.type,
            titleRes = eventDef.titleRes,
            descriptionRes = finalDescRes,
            descriptionArgs = descArgs,
            selectedPlayer = selectedPlayer,
            emoji = eventDef.emoji,
            color = eventDef.color,
            instructionRes = instructionRes,
            giftPhase = giftPhase
        )
    }

    fun acceptChallenge() { nextAction() }
    fun rejectChallenge() { nextAction() }

    fun revealGift() {
        val currentState = _gameState.value
        if (currentState is GameState.ShowingEvent &&
            currentState.event.eventType == EventType.GIFT &&
            currentState.event.giftPhase == GiftPhase.RAISE_HAND) {

            // Premios/Castigos (IDs)
            val gifts = listOf(
                R.string.gift_pos_1, R.string.gift_pos_2,
                R.string.gift_neg_1, R.string.gift_neg_2
            )
            val selectedGift = gifts.random()

            val updatedEvent = currentState.event.copy(
                giftPhase = GiftPhase.REVEAL,
                descriptionRes = selectedGift,
                instructionRes = R.string.evt_inst_gift_reveal
            )
            _gameState.value = GameState.ShowingEvent(updatedEvent)
        }
    }

    fun reset() {
        _gameState.value = GameState.Idle
        _selectedPlayerForEvent.value = null
        currentRound = 0
    }

    private data class EventDefinition(
        val type: EventType,
        val titleRes: Int,
        val emoji: String,
        val color: Color,
        val descriptionsRes: List<Int>,
        val instructionRes: Int
    )
}