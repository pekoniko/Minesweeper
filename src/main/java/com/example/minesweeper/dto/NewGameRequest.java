package com.example.minesweeper.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record NewGameRequest(
        @Min(value = 2, message = "ширина поля должна быть не менее 2 и не более 30")
        @Max(value = 30, message = "ширина поля должна быть не менее 2 и не более 30")
        Integer width,
        @Min(value = 2, message = "высота поля должна быть не менее 2 и не более 30")
        @Max(value = 30, message = "высота поля должна быть не менее 2 и не более 30")
        Integer height,
        Integer mines_count
) {

}
