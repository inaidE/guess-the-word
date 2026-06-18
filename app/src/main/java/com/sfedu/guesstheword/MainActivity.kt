package com.sfedu.guesstheword

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sfedu.guesstheword.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = GameBackground
                ) {
                    GameScreen()
                }
            }
        }
    }
}

@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadDictionary(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ИГРА В СЛОВА",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = GameTitle
            )
            Text(
                text = "Уровень ${viewModel.currentLevel}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GameGreen
            )
        }

        Box(modifier = Modifier.height(24.dp), contentAlignment = Alignment.Center) {
            if (viewModel.errorMessage.isNotEmpty()) {
                Text(
                    text = viewModel.errorMessage,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        WordGrid(gridState = viewModel.gridState)

        if (viewModel.isGameOver) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = viewModel.gameResultStatus,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GameTextMain,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (viewModel.isGameWon) {
                    Button(
                        onClick = { viewModel.nextLevel() },
                        colors = ButtonDefaults.buttonColors(containerColor = GameGreen)
                    ) {
                        Text("Следующий уровень", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = { viewModel.restartLevel() },
                        colors = ButtonDefaults.buttonColors(containerColor = GameGrey)
                    ) {
                        Text("Попробовать снова", color = Color.White)
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(40.dp))
        }

        Keyboard(
            keysStatus = viewModel.keyboardKeysStatus,
            onKeyPress = { viewModel.onKeyPress(it) },
            onBackspace = { viewModel.onBackspace() },
            onEnter = { viewModel.onEnter() }
        )
    }
}

@Composable
fun WordGrid(gridState: List<List<CellState>>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        gridState.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { cell ->
                    val borderColor = if (cell.char != null && cell.status == LetterStatus.TYPED) {
                        GameBorderTyped
                    } else {
                        GameBorderEmpty
                    }

                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .border(
                                width = 2.dp,
                                color = if (cell.status == LetterStatus.EMPTY || cell.status == LetterStatus.TYPED) borderColor else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .background(
                                color = cell.status.backgroundColor,
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cell.char?.toString() ?: "",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = cell.status.contentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Keyboard(
    keysStatus: Map<Char, LetterStatus>,
    onKeyPress: (Char) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit
) {
    val rows = listOf(
        "ЙЦУКЕНГШЩЗХЪ",
        "ФЫВАПРОЛДЖЭ",
        "ЯЧСМИТЬБЮ"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        rows.forEachIndexed { index, row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.fillMaxWidth(0.98f)
            ) {
                if (index == 2) {
                    KeyboardButton(
                        text = "ВВОД",
                        modifier = Modifier.weight(1.5f),
                        backgroundColor = GameKeyDefault
                    ) { onEnter() }
                }

                row.forEach { char ->
                    val status = keysStatus[char]
                    val bgButtonColor = status?.backgroundColor ?: GameKeyDefault
                    val contentButtonColor = status?.contentColor ?: Color.Black

                    KeyboardButton(
                        text = char.toString(),
                        modifier = Modifier.weight(1f),
                        backgroundColor = bgButtonColor,
                        contentColor = contentButtonColor
                    ) { onKeyPress(char) }
                }

                if (index == 2) {
                    KeyboardButton(
                        text = "⌫",
                        modifier = Modifier.weight(1.5f),
                        backgroundColor = GameKeyDefault
                    ) { onBackspace() }
                }
            }
        }
    }
}

@Composable
fun KeyboardButton(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    contentColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = if (text.length > 1) 14.sp else 18.sp,
            color = contentColor
        )
    }
}