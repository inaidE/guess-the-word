package com.sfedu.guesstheword

import androidx.compose.ui.graphics.Color

enum class LetterStatus(
    val backgroundColor: Color,
    val contentColor: Color
) {
    EMPTY(Color(0xFFFFFFFF), Color(0xFF000000)),       // Пустая ячейка (белая)
    TYPED(Color(0xFFFFFFFF), Color(0xFF000000)),       // Введенная, но не проверенная
    CORRECT(Color(0xFF6AAA64), Color(0xFFFFFFFF)),     // Зеленый (угадал место)
    PRESENT(Color(0xFFC9B458), Color(0xFFFFFFFF)),     // Желтый (есть в слове, но в другом месте)
    ABSENT(Color(0xFF787C7E), Color(0xFFFFFFFF))       // Серый (нет в слове)
}

data class CellState(
    val char: Char? = null,
    val status: LetterStatus = LetterStatus.EMPTY
)