package com.example.minesweeper.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "field")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "game_id", nullable = false, unique = true)
    private String gameId;

    @Column(name = "width", nullable = false)
    private Integer width;

    @Column(name = "height", nullable = false)
    private Integer height;

    @Column(name = "mines_count", nullable = false)
    private Integer mines_count;

    @Column(name = "field", nullable = false)
    private String field;

    @Column(name = "game_ended", nullable = false)
    private boolean gameEnded = false;

    public Field(Integer width, Integer height, Integer mines_count) {
        this.height = height;
        this.width = width;
        this.mines_count = mines_count;
    }
}
