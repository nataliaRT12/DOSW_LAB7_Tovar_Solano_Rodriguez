package edu.eci.dosw.todo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Entidad que representa una categoria de tareas (ej: Estudio, Trabajo, Personal).
// Se mapea a la tabla "categories" creada en database/002_add_categories.sql
@Entity
@Table(name = "categories")
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // nombre de la categoria, entre 3 y 40 caracteres, no se puede repetir
    @Column(nullable = false, length = 40, unique = true)
    private String name;

    // color de la categoria, uno de los 4 definidos en CategoryColor
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CategoryColor color;

    public CategoryEntity() {
        // constructor vacio que pide JPA
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryColor getColor() {
        return this.color;
    }

    public void setColor(CategoryColor color) {
        this.color = color;
    }
}
