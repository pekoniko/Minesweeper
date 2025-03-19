package com.example.minesweeper.service;

import com.example.minesweeper.dto.GameInfoResponse;
import com.example.minesweeper.dto.GameTurnRequest;
import com.example.minesweeper.dto.NewGameRequest;
import com.example.minesweeper.entities.Field;
import com.example.minesweeper.exception.CustomException;
import com.example.minesweeper.repositories.FieldRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GameService {
    private final FieldRepository fieldRepository;

    public ResponseEntity<?> createNewGame(NewGameRequest newGameRequest) {
        int maxSize = newGameRequest.height() * newGameRequest.width() - 1;
        if (newGameRequest.mines_count() > maxSize || newGameRequest.mines_count() < 1)
            throw new CustomException("количество мин должно быть не менее 1 и не более " + maxSize);

        Field field = new Field(newGameRequest.width(), newGameRequest.height(), newGameRequest.mines_count());
        Cell[][] fieldData = generateField(field);
        String[][] resultData = readMasked(fieldData);
        String result = getStringFromFieldData(fieldData);
        field.setField(result);
        fieldRepository.save(field);
        return new ResponseEntity<>(new GameInfoResponse(field, resultData), HttpStatus.OK);
    }

    private Cell[][] generateField(Field field) {
        Cell[][] result = generateMines(field);
        markEmptyPoints(result);
        return result;
    }

    private void markEmptyPoints(Cell[][] result) {
        for (int colIndex = 0; colIndex < result.length; colIndex++) {
            for (int rowIndex = 0; rowIndex < result[0].length; rowIndex++) {
                if (result[colIndex][rowIndex] == null) {
                    result[colIndex][rowIndex] = new Cell(calcNearMines(result, colIndex, rowIndex), false);
                }
            }
        }
    }

    private int calcNearMines(Cell[][] result, int colIndex, int rowIndex) {
        int minesNum = 0;
        minesNum += checkMine(result, colIndex + 1, rowIndex - 1);
        minesNum += checkMine(result, colIndex + 1, rowIndex);
        minesNum += checkMine(result, colIndex + 1, rowIndex + 1);
        minesNum += checkMine(result, colIndex, rowIndex + 1);
        minesNum += checkMine(result, colIndex, rowIndex - 1);
        minesNum += checkMine(result, colIndex - 1, rowIndex - 1);
        minesNum += checkMine(result, colIndex - 1, rowIndex);
        minesNum += checkMine(result, colIndex - 1, rowIndex + 1);
        return minesNum;
    }

    private int checkMine(Cell[][] result, int colIndex, int rowIndex) {
        if (colIndex < 0 || colIndex > result.length - 1 || rowIndex < 0 || rowIndex > result[0].length - 1) {
            return 0;
        }
        if (result[colIndex][rowIndex] != null && result[colIndex][rowIndex].getMineNum() == 9)
            return 1;
        return 0;
    }

    private Cell[][] generateMines(Field field) {
        Cell[][] result = new Cell[field.getWidth()][field.getHeight()];
        int mineNumber = 0;
        while (mineNumber < field.getMines_count()) {
            int col = (int) Math.round(Math.random() * (field.getWidth() - 1));
            int row = (int) Math.round(Math.random() * (field.getHeight() - 1));
            if (result[col][row] != null)
                continue;
            result[col][row] = new Cell(9, false);
            mineNumber++;
        }
        return result;
    }

    public ResponseEntity<?> makeNewTurn(GameTurnRequest gameTurnRequest) {
        Field field = fieldRepository.findByGameId(gameTurnRequest.game_id());
        if (field.isGameEnded())
            throw new CustomException("игра завершена");
        Cell[][] fieldData = readData(field);
        int col = gameTurnRequest.row();
        int row = gameTurnRequest.col();
        Cell chosenPoint = fieldData[col][row];
        if (chosenPoint.isOpened())
            throw new CustomException("уже открытая ячейка");

        String[][] resultField;
        if (chosenPoint.getMineNum() == 9) {
            resultField = unmaskAll(fieldData, "X");
            field.setGameEnded(isLeftNoUnmasked(fieldData, field));
        } else {
            if (chosenPoint.getMineNum() == 0)
                openEmpty(fieldData, col, row);
            resultField = readMasked(fieldData);
            resultField[col][row] = String.valueOf(chosenPoint.getMineNum());
            fieldData[col][row].setOpened(true);

            field.setGameEnded(isLeftNoUnmasked(fieldData, field));
            if (field.isGameEnded()) {
                resultField = unmaskAll(fieldData, "M");
            }
        }

        field.setField(getStringFromFieldData(fieldData));
        fieldRepository.save(field);
        return new ResponseEntity<>(new GameInfoResponse(field, resultField), HttpStatus.OK);
    }

    private String getStringFromFieldData(Cell[][] resultField) {
        StringBuilder builder = new StringBuilder();
        for (int colIndex = 0; colIndex < resultField.length; colIndex++) {
            for (int rowIndex = 0; rowIndex < resultField[0].length; rowIndex++) {
                if (resultField[colIndex][rowIndex].isOpened)
                    builder.append('o');
                else
                    builder.append('c');
                builder.append(resultField[colIndex][rowIndex].getMineNum());
            }
        }
        return builder.toString();
    }

    private void openEmpty(Cell[][] fieldData, Integer col, Integer row) {
        if (col < 0 || col >= fieldData.length || row < 0 || row >= fieldData[0].length) {
            return;
        }
        if (fieldData[col][row].getMineNum() == 0 && !fieldData[col][row].isOpened()) {
            fieldData[col][row].setOpened(true);
            openEmpty(fieldData, col + 1, row);
            openEmpty(fieldData, col - 1, row);
            openEmpty(fieldData, col, row + 1);
            openEmpty(fieldData, col, row - 1);
            openEmpty(fieldData, col + 1, row + 1);
            openEmpty(fieldData, col - 1, row - 1);
            openEmpty(fieldData, col - 1, row + 1);
            openEmpty(fieldData, col + 1, row - 1);
        }
        if (fieldData[col][row].getMineNum() != 9)
            fieldData[col][row].setOpened(true);
    }

    private Cell[][] readData(Field field) {
        Cell[][] result = new Cell[field.getWidth()][field.getHeight()];
        for (int colIndex = 0; colIndex < field.getWidth(); colIndex++) {
            for (int rowIndex = 0; rowIndex < field.getHeight(); rowIndex++) {
                result[colIndex][rowIndex] = getPointAt(field, colIndex, rowIndex);
            }
        }
        return result;
    }

    private boolean isLeftNoUnmasked(Cell[][] fieldData, Field field) {
        int minesLeft = 0;
        for (int colIndex = 0; colIndex < fieldData.length; colIndex++) {
            for (int rowIndex = 0; rowIndex < fieldData[0].length; rowIndex++) {
                if (!fieldData[colIndex][rowIndex].isOpened())
                    minesLeft++;
            }
        }
        return minesLeft == field.getMines_count();
    }

    private String[][] unmaskAll(Cell[][] field, String changeOn) {
        String[][] result = new String[field[0].length][field.length];
        for (int colIndex = 0; colIndex < result.length; colIndex++) {
            for (int rowIndex = 0; rowIndex < result[0].length; rowIndex++) {
                if (field[rowIndex][colIndex].getMineNum() == 9) {
                    result[rowIndex][colIndex] = changeOn;
                } else {
                    result[rowIndex][colIndex] = String.valueOf(field[rowIndex][colIndex].getMineNum());
                }
            }
        }
        return result;
    }

    private Cell getPointAt(Field field, Integer col, Integer row) {
        boolean opened = field.getField().charAt((col * 2) * field.getWidth() + row * 2) == 'o';
        String data = String.valueOf(field.getField().charAt((col * 2) * field.getWidth() + row * 2 + 1));
        return new Cell(Integer.parseInt(data), opened);
    }

    private String[][] readMasked(Cell[][] field) {
        String[][] result = new String[field[0].length][field.length];
        for (int colIndex = 0; colIndex < result.length; colIndex++) {
            for (int rowIndex = 0; rowIndex < result[0].length; rowIndex++) {
                if (!field[rowIndex][colIndex].isOpened() || field[rowIndex][colIndex].getMineNum() == 9) {
                    result[rowIndex][colIndex] = " ";
                } else {
                    result[rowIndex][colIndex] = String.valueOf(field[rowIndex][colIndex].getMineNum());
                }
            }
        }
        return result;
    }

}
