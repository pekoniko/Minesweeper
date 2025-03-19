package com.example.minesweeper.dto;

public record GameTurnRequest(
        String game_id,
        Integer col,
        Integer row
) {
}
