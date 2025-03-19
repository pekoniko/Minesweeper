package com.example.minesweeper.repositories;

import com.example.minesweeper.entities.Field;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FieldRepository extends JpaRepository<Field, Long> {
    public Field findByGameId(String id);
}
