package com.example.minesweeper.service;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Cell {
    private int mineNum;
    boolean isOpened;
}
