package com.example.thedetectivesgrid.models

sealed class PuzzleState {
    object Input: PuzzleState()
    object Playing: PuzzleState()
}