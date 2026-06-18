package com.sfedu.guesstheword

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.io.BufferedReader
import java.io.InputStreamReader

class GameViewModel : ViewModel() {

    val maxGuesses = 6
    val wordLength = 5

    private var allWords = listOf<String>()
    private var allowedWords = setOf<String>()
    private var currentTargetWord = "СЛОВО"
    private var currentWordIndex = 0

    var currentLevel by mutableIntStateOf(1)
        private set
    var errorMessage by mutableStateOf("")
        private set
    var gridState by mutableStateOf(List(maxGuesses) { List(wordLength) { CellState() } })
        private set
    var currentRow by mutableStateOf(0)
        private set
    var currentCol by mutableStateOf(0)
        private set
    var isGameOver by mutableStateOf(false)
        private set
    var isGameWon by mutableStateOf(false)
        private set
    var gameResultStatus by mutableStateOf("")
        private set
    var keyboardKeysStatus = mutableStateMapOf<Char, LetterStatus>()
        private set

    fun loadDictionary(context: Context) {
        val inputStream = context.resources.openRawResource(R.raw.words)
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

        allWords = reader.readLines()
            .map { it.trim().uppercase().replace("[^А-ЯЁ]".toRegex(), "") }
            .filter { it.length == 5 }
            .shuffled()

        reader.close()

        allowedWords = allWords.toSet()

        currentWordIndex = 0
        currentTargetWord = allWords[currentWordIndex]
    }

    fun onKeyPress(char: Char) {
        errorMessage = ""
        if (isGameOver || currentCol >= wordLength) return

        val newGrid = gridState.map { it.toMutableList() }.toMutableList()
        newGrid[currentRow][currentCol] = CellState(char, LetterStatus.TYPED)
        gridState = newGrid
        currentCol++
    }

    fun onBackspace() {
        errorMessage = ""
        if (isGameOver || currentCol == 0) return

        currentCol--
        val newGrid = gridState.map { it.toMutableList() }.toMutableList()
        newGrid[currentRow][currentCol] = CellState()
        gridState = newGrid
    }

    fun onEnter() {
        if (isGameOver || currentCol < wordLength) return

        val currentWord = gridState[currentRow].joinToString("") { it.char.toString() }

        if (!allowedWords.contains(currentWord)) {
            errorMessage = "Такого слова нет"
            return
        }

        val rowStatuses = MutableList(wordLength) { LetterStatus.ABSENT }
        val targetLettersPool = currentTargetWord.toMutableList()

        for (i in 0 until wordLength) {
            if (currentWord[i] == currentTargetWord[i]) {
                rowStatuses[i] = LetterStatus.CORRECT
                targetLettersPool[i] = '_'
            }
        }

        for (i in 0 until wordLength) {
            if (rowStatuses[i] != LetterStatus.CORRECT) {
                val char = currentWord[i]
                val index = targetLettersPool.indexOf(char)
                if (index != -1) {
                    rowStatuses[i] = LetterStatus.PRESENT
                    targetLettersPool[index] = '_'
                }
            }
        }

        val newGrid = gridState.map { it.toMutableList() }.toMutableList()
        for (i in 0 until wordLength) {
            val char = currentWord[i]
            val status = rowStatuses[i]
            newGrid[currentRow][i] = newGrid[currentRow][i].copy(status = status)

            val currentKeyStatus = keyboardKeysStatus[char]
            if (currentKeyStatus != LetterStatus.CORRECT) {
                if (status == LetterStatus.CORRECT || currentKeyStatus != LetterStatus.PRESENT) {
                    keyboardKeysStatus[char] = status
                }
            }
        }
        gridState = newGrid

        if (currentWord == currentTargetWord) {
            isGameOver = true
            isGameWon = true
            gameResultStatus = "Отлично!"
        } else if (currentRow == maxGuesses - 1) {
            isGameOver = true
            isGameWon = false
            gameResultStatus = "Было загадано: $currentTargetWord"
        } else {
            currentRow++
            currentCol = 0
        }
    }

    fun nextLevel() {
        currentLevel++
        currentWordIndex++
        if (allWords.isNotEmpty()) {
            currentTargetWord = allWords[currentWordIndex % allWords.size]
        }
        resetGrid()
    }

    fun restartLevel() {
        currentWordIndex++
        if (allWords.isNotEmpty()) {
            currentTargetWord = allWords[currentWordIndex % allWords.size]
        }
        resetGrid()
    }

    private fun resetGrid() {
        gridState = List(maxGuesses) { List(wordLength) { CellState() } }
        currentRow = 0
        currentCol = 0
        isGameOver = false
        isGameWon = false
        gameResultStatus = ""
        errorMessage = ""
        keyboardKeysStatus.clear()
    }
}