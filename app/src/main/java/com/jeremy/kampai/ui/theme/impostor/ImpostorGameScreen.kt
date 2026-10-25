package com.jeremy.kampai.ui.theme.impostor

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeremy.kampai.R
import com.jeremy.kampai.domain.models.PlayerModel
import com.jeremy.kampai.ui.theme.*
import com.jeremy.kampai.ui.theme.partymanager.PartyManagerViewModel
import kotlinx.coroutines.delay

@Composable
fun ImpostorGameScreen(
    viewModel: ImpostorViewModel = hiltViewModel(),
    partyViewModel: PartyManagerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val players by partyViewModel.players.collectAsState()
    val currentPlayerIndex by viewModel.currentPlayerIndex.collectAsState()

    LaunchedEffect(players) { viewModel.setPlayers(players) }

    val activeAtmosphere = when (gameState) {
        is ImpostorViewModel.GameState.Setup -> KColor.Danger
        is ImpostorViewModel.GameState.ShowingWord -> KColor.Purple
        is ImpostorViewModel.GameState.GivingClues -> KColor.Amber
        is ImpostorViewModel.GameState.Voting -> KColor.Cyan
        is ImpostorViewModel.GameState.Results -> KColor.Danger
    }

    val animatedAtmosphere by animateColorAsState(
        targetValue = activeAtmosphere,
        animationSpec = tween(500),
        label = "impostorAtmosphere"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KColor.Base)
    ) {
        NeoAtmosphereBackground(activeColor = animatedAtmosphere) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                ImpostorHeader(onBack = onBack, accentColor = animatedAtmosphere)

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when (val state = gameState) {
                        is ImpostorViewModel.GameState.Setup -> {
                            SetupContent(
                                playerCount = players.size,
                                onStart = { viewModel.startGame() }
                            )
                        }
                        is ImpostorViewModel.GameState.ShowingWord -> {
                            ShowingWordContent(
                                player = players.getOrNull(currentPlayerIndex),
                                word = state.word,
                                isImpostor = state.isImpostor,
                                currentIndex = currentPlayerIndex,
                                totalPlayers = players.size,
                                onNext = { viewModel.nextPlayer() }
                            )
                        }
                        is ImpostorViewModel.GameState.GivingClues -> {
                            GivingCluesContent(
                                onStartVoting = { viewModel.startVoting() }
                            )
                        }
                        is ImpostorViewModel.GameState.Voting -> {
                            VotingContent(
                                players = players,
                                votedPlayers = viewModel.votedPlayers.collectAsState().value,
                                onVote = { viewModel.votePlayer(it) },
                                onShowResults = { viewModel.showResults() }
                            )
                        }
                        is ImpostorViewModel.GameState.Results -> {
                            ResultsContent(
                                impostorWon = state.impostorWon,
                                impostor = state.impostor,
                                realWord = state.realWord,
                                onReset = { viewModel.reset() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImpostorHeader(onBack: () -> Unit, accentColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SmallIconButton(
            icon = Icons.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back),
            onClick = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Overline(
                text = "EL IMPOSTOR / ENGAÑO",
                color = accentColor
            )
            Text(
                text = stringResource(R.string.impostor_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                color = KColor.Foreground
            )
        }

        Spacer(modifier = Modifier.size(46.dp))
    }
}

@Composable
fun SetupContent(playerCount: Int, onStart: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ChallengeReticle(
            emoji = "🕵️",
            accent = KColor.Danger
        )

        GlassPanel(
            accent = KColor.Danger,
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SectionHeading(
                    title = stringResource(R.string.impostor_setup_title),
                    aside = "REGLAS"
                )

                Text(
                    text = stringResource(R.string.impostor_rules_bullet),
                    fontSize = 13.sp,
                    color = KColor.Secondary,
                    lineHeight = 18.sp
                )

                if (playerCount < 3) {
                    Pill(
                        text = "⚠️ " + stringResource(R.string.impostor_warn_players),
                        tint = KColor.Danger
                    )
                } else {
                    Pill(
                        text = "✓ " + stringResource(R.string.impostor_ready_players, playerCount),
                        tint = KColor.Green
                    )
                }
            }
        }

        KampaiButton(
            label = stringResource(R.string.impostor_btn_start).uppercase(),
            onClick = onStart,
            accent = KColor.Danger,
            primary = playerCount >= 3,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ShowingWordContent(
    player: PlayerModel?,
    word: String,
    isImpostor: Boolean,
    currentIndex: Int,
    totalPlayers: Int,
    onNext: () -> Unit
) {
    var showWord by remember { mutableStateOf(false) }
    LaunchedEffect(player) { showWord = false }

    val scale by animateFloatAsState(
        targetValue = if (showWord) 1f else 0.90f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "wordScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Pill(
            text = stringResource(R.string.impostor_player_counter, currentIndex + 1, totalPlayers),
            tint = KColor.Purple
        )

        player?.let {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(KColor.Raised)
                    .border(1.dp, it.getAvatarColor().copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PlayerAvatarBadge(
                    emoji = it.getDisplayEmoji(),
                    color = it.getAvatarColor(),
                    size = 48.dp
                )
                Text(
                    text = it.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = KColor.Foreground
                )
            }
        }

        if (!showWord) {
            GlassPanel(
                accent = KColor.Purple,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(text = "🔒", fontSize = 64.sp)
                        Text(
                            text = stringResource(R.string.impostor_card_locked_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = KColor.Foreground,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            KampaiButton(
                label = "👁️ " + stringResource(R.string.impostor_btn_reveal).uppercase(),
                onClick = { showWord = true },
                accent = KColor.Purple,
                primary = true,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            GlassPanel(
                accent = if (isImpostor) KColor.Danger else KColor.Cyan,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .scale(scale)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(20.dp)
                    ) {
                        if (isImpostor) {
                            Text(text = "🕵️", fontSize = 64.sp)
                            Text(
                                text = stringResource(R.string.impostor_role_impostor),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = KColor.Danger,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(R.string.impostor_role_desc_impostor),
                                fontSize = 13.sp,
                                color = KColor.Secondary,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text(text = "🔑", fontSize = 42.sp)
                            Text(
                                text = word,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = KColor.Cyan,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(R.string.impostor_role_desc_crew),
                                fontSize = 13.sp,
                                color = KColor.Secondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            KampaiButton(
                label = if (currentIndex < totalPlayers - 1) {
                    stringResource(R.string.impostor_btn_next_player).uppercase() + " →"
                } else {
                    stringResource(R.string.impostor_btn_start_round).uppercase() + " 🚀"
                },
                onClick = onNext,
                accent = if (isImpostor) KColor.Danger else KColor.Cyan,
                primary = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun GivingCluesContent(onStartVoting: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ChallengeReticle(
            emoji = "💭",
            accent = KColor.Amber
        )

        GlassPanel(
            accent = KColor.Amber,
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionHeading(
                    title = stringResource(R.string.impostor_clues_title),
                    aside = "PISTAS"
                )

                Text(
                    text = stringResource(R.string.impostor_clues_desc),
                    fontSize = 13.sp,
                    color = KColor.Secondary,
                    lineHeight = 18.sp
                )
            }
        }

        KampaiButton(
            label = "🗳️ " + stringResource(R.string.impostor_btn_vote).uppercase(),
            onClick = onStartVoting,
            accent = KColor.Amber,
            primary = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun VotingContent(
    players: List<PlayerModel>,
    votedPlayers: Map<String, Int>,
    onVote: (String) -> Unit,
    onShowResults: () -> Unit
) {
    val totalVotes = votedPlayers.values.sum()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.impostor_vote_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = KColor.Foreground
            )

            Pill(
                text = stringResource(R.string.impostor_vote_count, totalVotes),
                tint = KColor.Cyan
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(players) { player ->
                PlayerVoteCard(
                    player = player,
                    votes = votedPlayers[player.id] ?: 0,
                    onVote = { onVote(player.id) }
                )
            }
        }

        KampaiButton(
            label = "🔍 " + stringResource(R.string.impostor_btn_results).uppercase(),
            onClick = onShowResults,
            accent = KColor.Danger,
            primary = totalVotes > 0,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PlayerVoteCard(
    player: PlayerModel,
    votes: Int,
    onVote: () -> Unit
) {
    GlassPanel(
        accent = if (votes > 0) KColor.Danger else KColor.Raised,
        onClick = onVote,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerAvatarBadge(
                emoji = player.getDisplayEmoji(),
                color = player.getAvatarColor(),
                size = 46.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = KColor.Foreground
                )
                Text(
                    text = stringResource(R.string.impostor_vote_plural, votes),
                    fontSize = 12.sp,
                    color = KColor.Secondary
                )
            }

            Pill(
                text = "+1 🗳️",
                tint = if (votes > 0) KColor.Danger else KColor.Secondary
            )
        }
    }
}

@Composable
fun ResultsContent(
    impostorWon: Boolean,
    impostor: PlayerModel,
    realWord: String,
    onReset: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ChallengeReticle(
            emoji = if (impostorWon) "😈" else "🎉",
            accent = if (impostorWon) KColor.Danger else KColor.Green
        )

        Text(
            text = if (impostorWon) {
                stringResource(R.string.impostor_res_win)
            } else {
                stringResource(R.string.impostor_res_caught)
            },
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = if (impostorWon) KColor.Danger else KColor.Green,
            textAlign = TextAlign.Center,
            lineHeight = 38.sp
        )

        GlassPanel(
            accent = if (impostorWon) KColor.Danger else KColor.Green,
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Overline(
                    text = stringResource(R.string.impostor_res_who_was),
                    color = KColor.Secondary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PlayerAvatarBadge(
                        emoji = impostor.getDisplayEmoji(),
                        color = impostor.getAvatarColor(),
                        size = 50.dp
                    )
                    Text(
                        text = impostor.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = KColor.Foreground
                    )
                }

                Overline(
                    text = stringResource(R.string.impostor_res_word_was),
                    color = KColor.Secondary
                )

                Text(
                    text = realWord,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = KColor.Cyan
                )

                Pill(
                    text = if (impostorWon) {
                        stringResource(R.string.impostor_res_msg_win)
                    } else {
                        stringResource(R.string.impostor_res_msg_caught)
                    },
                    tint = if (impostorWon) KColor.Danger else KColor.Green
                )
            }
        }

        KampaiButton(
            label = "🔄 " + stringResource(R.string.impostor_btn_replay).uppercase(),
            onClick = onReset,
            accent = KColor.Purple,
            primary = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}