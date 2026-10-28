package edu.eci.dosw.todo.dto;

import edu.eci.dosw.todo.entity.CategoryColor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CategoryCreateRequest {

    @NotBlank(message = "El nombre de la categoria es obligatorio")
    @Size(min = 3, max = 40, message = "El nombre debe tener entre 3 y 40 caracteres")
    private String name;

    @NotNull(message = "El color de la categoria es obligatorio")
    private CategoryColor color;

    public CategoryCreateRequest() {
    }

    public CategoryCreateRequest(String name, CategoryColor color) {
        this.name = name;
        this.color = color;
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
