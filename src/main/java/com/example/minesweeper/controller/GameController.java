package com.example.minesweeper.controller;

import com.example.minesweeper.dto.ErrorResponse;
import com.example.minesweeper.dto.GameTurnRequest;
import com.example.minesweeper.dto.NewGameRequest;
import com.example.minesweeper.exception.CustomException;
import com.example.minesweeper.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.HashSet;


@RestController
@RequestMapping(value = "/minesweeper")
@RequiredArgsConstructor
public class GameController {
    private final GameService service;

    @PostMapping(value = "/new", consumes = MediaType.APPLICATION_JSON_VALUE, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> newGame(@RequestBody @Valid NewGameRequest newGameRequest) {
        return this.service.createNewGame(newGameRequest);
    }

    @PostMapping(value = "/turn", consumes = MediaType.APPLICATION_JSON_VALUE, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> newTurn(@RequestBody GameTurnRequest gameTurnRequest) {
        return this.service.makeNewTurn(gameTurnRequest);
    }


    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
       HashSet<String> errors = new HashSet<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String errorMessage = error.getDefaultMessage();
            errors.add(errorMessage);
        });
        return new ResponseEntity<>(new ErrorResponse(errors.toArray()[0].toString()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({CustomException.class})
    public ResponseEntity<ErrorResponse> handleCustomExceptions(CustomException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
