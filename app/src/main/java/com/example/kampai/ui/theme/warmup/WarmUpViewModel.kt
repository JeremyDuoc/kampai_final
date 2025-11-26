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
        data class Phrase(val textRes: Int, val emoji: String, val color: Color) : WarmupAction()

        data class Event(
            val eventType: EventType,
            val titleRes: Int,
            val descriptionRes: Int,
            val descriptionArgs: List<String> = emptyList(),
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
        data class Finished(val stats: Map<PlayerModel, Int>) : GameState()
    }

    private val _gameState = MutableStateFlow<GameState>(GameState.Idle)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _showRulesDialog = MutableStateFlow(false)
    val showRulesDialog: StateFlow<Boolean> = _showRulesDialog.asStateFlow()

    private val _showStatsDialog = MutableStateFlow(false)
    val showStatsDialog: StateFlow<Boolean> = _showStatsDialog.asStateFlow()

    private val _selectedPlayerForEvent = MutableStateFlow<PlayerModel?>(null)
    val selectedPlayerForEvent: StateFlow<PlayerModel?> = _selectedPlayerForEvent.asStateFlow()

    private val _drinkStats = MutableStateFlow<Map<String, Int>>(emptyMap())
    val drinkStats: StateFlow<Map<String, Int>> = _drinkStats.asStateFlow()

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
        Triple(R.string.ph_hug, "🤗", Color(0xFFF472B6)),
        Triple(R.string.ph_youngest_drink, "🎂", Color(0xFF8B5CF6)),
        Triple(R.string.ph_oldest_shot_cross, "👴", Color(0xFF6366F1)),
        Triple(R.string.ph_siblings_drink, "👨‍👩‍👧‍👦", Color(0xFFEF4444)),
        Triple(R.string.ph_bday_closest_shot, "🎈", Color(0xFFF97316)),
        Triple(R.string.ph_own_vehicle_drink, "🚗", Color(0xFF06B6D4)),
        Triple(R.string.ph_last_hand_up_shot, "✋", Color(0xFFA855F7)),
        Triple(R.string.ph_oldest_phone_drink, "📱", Color(0xFF64748B)),
        Triple(R.string.ph_seated_drink, "🪑", Color(0xFF6B7280)),
        Triple(R.string.ph_last_stand_choose, "🏃", Color(0xFFF59E0B)),
        Triple(R.string.ph_age_split_shot, "🎂", Color(0xFF8B5CF6)),
        Triple(R.string.ph_age_over_22_drink, "👵", Color(0xFF4B5563)),
        Triple(R.string.ph_youngest_shot_cross, "👶", Color(0xFF3B82F6)),
        Triple(R.string.ph_tall_short_shot_cross, "📏", Color(0xFF10B981)),
        Triple(R.string.ph_dark_hair_drink, "💇", Color(0xFF78350F)),
        Triple(R.string.ph_height_over_drink, "🦒", Color(0xFF059669)),
        Triple(R.string.ph_eye_color_drink, "👀", Color(0xFF06B6D4)),
        Triple(R.string.ph_glasses_drink, "👓", Color(0xFF6366F1)),
        Triple(R.string.ph_dimples_drink, "😊", Color(0xFFF472B6)),
        Triple(R.string.ph_smallest_foot_drink, "🦶", Color(0xFFA3A3A3)),
        Triple(R.string.ph_curly_hair_drink, "🌀", Color(0xFF10B981)),
        Triple(R.string.ph_left_handed_shot, "✋", Color(0xFF8B5CF6)),
        Triple(R.string.ph_freckles_drink, "🌞", Color(0xFFF59E0B)),
        Triple(R.string.ph_zodiac_fire_earth_drink, "🦁", Color(0xFFF59E0B)),
        Triple(R.string.ph_zodiac_air_water_drink, "♈", Color(0xFFEC4899)),
        Triple(R.string.ph_longest_name_drink, "📝", Color(0xFF10B981)),
        Triple(R.string.ph_fall_in_love_drink, "❤️‍🔥", Color(0xFFE11D48)),
        Triple(R.string.ph_get_angry_drink, "😡", Color(0xFFDC2626)),
        Triple(R.string.ph_sagi_shot, "♐", Color(0xFFF59E0B)),
        Triple(R.string.ph_relationship_shot, "💔", Color(0xFFF43F5E)),
        Triple(R.string.ph_phone_brand_shot, "📱", Color(0xFF2563EB)),
        Triple(R.string.ph_back_ex_drink, "🤡", Color(0xFFDC2626)),
        Triple(R.string.ph_sneaking_out_drink, "🤫", Color(0xFF8B5CF6)),
        Triple(R.string.ph_kiss_someone_drink, "💋", Color(0xFFEC4899)),
        Triple(R.string.ph_first_time_age_drink, "🔞", Color(0xFFB91C1C)),
        Triple(R.string.ph_prefer_married_shot, "💍", Color(0xFF0EA5E9)),
        Triple(R.string.ph_pity_kiss_shot, "😬", Color(0xFFF59E0B)),
        Triple(R.string.ph_fake_ig_shot, "🕵️", Color(0xFF10B981)),
        Triple(R.string.ph_dating_app_drink, "🔥", Color(0xFFF97316)),
        Triple(R.string.ph_erotic_dream_drink, "💭", Color(0xFF818CF8)),
        Triple(R.string.ph_caught_in_act_drink, "🚪", Color(0xFFEF4444)),
        Triple(R.string.ph_read_receipts_drink, "✔️", Color(0xFF3B82F6)),
        Triple(R.string.ph_most_followers_shot, "📸", Color(0xFFD946EF)),
        Triple(R.string.ph_bite_kiss_shot, "🐺", Color(0xFF8B0000)),
        Triple(R.string.ph_fantasy_boss_shot, "📚", Color(0xFFF59E0B)),
        Triple(R.string.ph_sneaky_phone_check_shot, "👀", Color(0xFFEF4444)),
        Triple(R.string.ph_phone_checked_drink, "📱", Color(0xFFDC2626)),
        Triple(R.string.ph_vote_best_smell_distribute, "👃", Color(0xFF34D399)),
        Triple(R.string.ph_vote_spender_shot, "🤑", Color(0xFFD946EF)),
        Triple(R.string.ph_vote_quiet_shot, "🤫", Color(0xFF64748B)),
        Triple(R.string.ph_vote_party_animal_drink, "🥳", Color(0xFFF59E0B)),
        Triple(R.string.ph_vote_least_party_drink, "🥱", Color(0xFF94A3B8)),
        Triple(R.string.ph_vote_shameless_drink, "😈", Color(0xFFEF4444)),
        Triple(R.string.ph_vote_embarrassed_drink, "😳", Color(0xFFF472B6)),
        Triple(R.string.ph_vote_extrovert_drink, "🗣️", Color(0xFF34D399)),
        Triple(R.string.ph_vote_otaku_drink, "🤓", Color(0xFFFCD34D)),
        Triple(R.string.ph_vote_gamer_drink, "🎮", Color(0xFF60A5FA)),
        Triple(R.string.ph_vote_best_dressed_drink, "👗", Color(0xFFEC4899)),
        Triple(R.string.ph_vote_most_pointed_bottoms_up, "🗳️", Color(0xFF4C1D95)),
        Triple(R.string.ph_proposer_choose_drink, "👑", Color(0xFFF59E0B)),
        Triple(R.string.ph_oldest_choose_kiss, "👴", Color(0xFF6B7280)),
        Triple(R.string.ph_earliest_choose_kiss, "⏰", Color(0xFF10B981)),
        Triple(R.string.ph_latest_drink, "🐢", Color(0xFFF59E0B)),
        Triple(R.string.ph_host_choose_drink, "🏠", Color(0xFF0EA5E9)),
        Triple(R.string.ph_serious_drink, "😐", Color(0xFFDC2626)),
        Triple(R.string.ph_count_to_ten_drink, "🔟", Color(0xFF8B5CF6)),
        Triple(R.string.ph_most_battery_choose, "🔋", Color(0xFF22C55E)),
        Triple(R.string.ph_women_choose_drink, "👉", Color(0xFFF472B6)),
        Triple(R.string.ph_men_massage, "💆‍♂️", Color(0xFF3B82F6)),
        Triple(R.string.ph_women_massage, "💆‍♀️", Color(0xFFEC4899)),
        Triple(R.string.ph_last_nose_touch_drink, "👃", Color(0xFFEF4444)),
        Triple(R.string.ph_tattoos_drink, "💉", Color(0xFF1F2937)),
        Triple(R.string.ph_no_tattoos_drink, "👶", Color(0xFF9CA3AF)),
        Triple(R.string.ph_red_underwear_shot, "👙", Color(0xFFDC2626)),
        Triple(R.string.ph_swap_clothing_drink, "👕", Color(0xFF8B5CF6)),
        Triple(R.string.ph_best_cook_choose, "🍳", Color(0xFF10B981)),
        Triple(R.string.ph_biggest_foot_drink, "🦶", Color(0xFF6B7280)),
        Triple(R.string.ph_travel_continent_safe, "✈️", Color(0xFF0EA5E9)),
        Triple(R.string.ph_most_coins_drink, "💰", Color(0xFFFFD700)),
        Triple(R.string.ph_pets_drink, "🐶", Color(0xFFA855F7)),
        Triple(R.string.ph_siblings_present_drink, "👫", Color(0xFFF43F5E)),
        Triple(R.string.ph_most_languages_distribute, "🗣️", Color(0xFF34D399)),
        Triple(R.string.ph_motorcycle_drink, "🏍️", Color(0xFF1F2937)),
        Triple(R.string.ph_confession_3t1l, "🗣️", Color(0xFFE879F9)),
        Triple(R.string.ph_confession_first_time, "🗣️", Color(0xFF7C3AED)),
        Triple(R.string.ph_confession_weird_place, "🌍", Color(0xFF059669)),
        Triple(R.string.ph_confession_first_look, "👀", Color(0xFFDB2777)),
        Triple(R.string.ph_swap_places, "🔄", Color(0xFF8B5CF6)),
        Triple(R.string.ph_thumb_war, "👍", Color(0xFFF59E0B)),
        Triple(R.string.ph_biggest_foot_song, "🦶", Color(0xFF10B981)),
        Triple(R.string.ph_lock_screen_show, "📲", Color(0xFF3B82F6)),
        Triple(R.string.ph_touch_nose_tongue, "🤪", Color(0xFFF59E0B)),
        Triple(R.string.ph_vote_president, "🏛️", Color(0xFF60A5FA)),
        Triple(R.string.ph_massage_right, "💆", Color(0xFFEC4899)),
        Triple(R.string.ph_last_green_touch_lose, "🟢", Color(0xFF22C55E)),
        Triple(R.string.ph_low_battery_google_search, "🔍", Color(0xFFEF4444)),
        Triple(R.string.ph_get_sushi,"🍣", Color(0xFFF59E0B)),
    )

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
        EventDefinition(
            type = EventType.RPS_DUEL,
            titleRes = R.string.evt_title_rps,
            emoji = "✂️",
            color = Color(0xFFF59E0B),
            descriptionsRes = listOf(R.string.evt_desc_rps_random),
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
            descriptionsRes = listOf(R.string.gift_pos_1),
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
    private val totalRounds = 100
    private val eventFrequency = 5
    private var eventPlayers: List<PlayerModel> = emptyList()

    fun setPlayers(players: List<PlayerModel>) {
        eventPlayers = players
        if (_drinkStats.value.isEmpty()) {
            _drinkStats.value = players.associate { it.id to 0 }
        }
    }

    fun showRules() { _showRulesDialog.value = true }
    fun hideRules() { _showRulesDialog.value = false }
    fun toggleStats() { _showStatsDialog.value = !_showStatsDialog.value }

    fun startWarmup() {
        currentRound = 0
        _drinkStats.value = eventPlayers.associate { it.id to 0 }
        showNextAction()
    }

    fun nextAction() {
        currentRound++
        if (currentRound >= totalRounds) {
            val finalStats = _drinkStats.value.mapKeys { entry ->
                eventPlayers.find { it.id == entry.key } ?: eventPlayers.first()
            }
            _gameState.value = GameState.Finished(finalStats)
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
        val descArgs = mutableListOf<String>()
        var instructionRes = eventDef.instructionRes
        var giftPhase: GiftPhase? = null

        val groupEvents = listOf(
            EventType.MOST_LIKELY, EventType.MEDUSA, EventType.VOTING,
            EventType.SELFIE, EventType.ICE_PASS, EventType.GIFT
        )

        if (eventDef.type !in groupEvents && eventPlayers.isNotEmpty()) {
            selectedPlayer = eventPlayers.random()
        }

        if (eventDef.type == EventType.GIFT) {
            giftPhase = GiftPhase.RAISE_HAND
            finalDescRes = R.string.evt_desc_gift_prep
        }

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
        } else if (eventDef.type == EventType.STARING_CONTEST && eventPlayers.size >= 2) {
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

    // --- LÓGICA DE ESTADÍSTICAS ---

    fun acceptChallenge() {
        val state = _gameState.value
        if (state is GameState.ShowingEvent && state.event.eventType == EventType.SHOT_CHALLENGE) {
            state.event.selectedPlayer?.let { player ->
                addDrinksToPlayer(player.id, 1)
            }
        }
        nextAction()
    }

    fun rejectChallenge() {
        val state = _gameState.value
        if (state is GameState.ShowingEvent) {
            val player = state.event.selectedPlayer
            if (player != null) {
                addDrinksToPlayer(player.id, state.event.penaltyDrinks)
            }
        }
        nextAction()
    }

    private fun addDrinksToPlayer(playerId: String, amount: Int) {
        val currentStats = _drinkStats.value.toMutableMap()
        val currentCount = currentStats[playerId] ?: 0
        currentStats[playerId] = currentCount + amount
        _drinkStats.value = currentStats

    }

    fun addManualDrink(playerId: String) {
        addDrinksToPlayer(playerId, 1)
    }

    fun revealGift() {
        val currentState = _gameState.value
        if (currentState is GameState.ShowingEvent &&
            currentState.event.eventType == EventType.GIFT &&
            currentState.event.giftPhase == GiftPhase.RAISE_HAND) {

            val gifts = listOf(
                R.string.gift_pos_1, R.string.gift_pos_2,
                R.string.gift_neg_1, R.string.gift_neg_2
            )
            val selectedGift = gifts.random()

            if (selectedGift == R.string.gift_neg_1 || selectedGift == R.string.gift_neg_2) {
                currentState.event.selectedPlayer?.let { addDrinksToPlayer(it.id, 1) }
            }

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