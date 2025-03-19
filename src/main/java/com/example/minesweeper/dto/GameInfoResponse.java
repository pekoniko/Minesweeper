package com.example.minesweeper.dto;

import com.example.minesweeper.entities.Field;
import lombok.Data;

@Data
public class GameInfoResponse {
    String game_id;
    Integer width;
    Integer height;
    Integer mines_count;
    Boolean completed;
    String[][] field;

    public GameInfoResponse(Field field, String[][] fieldData) {
        this.game_id = field.getGameId();
        this.width = field.getWidth();
        this.height = field.getHeight();
        this.mines_count = field.getMines_count();
        this.completed = field.isGameEnded();
        this.field = fieldData;
    }


}
