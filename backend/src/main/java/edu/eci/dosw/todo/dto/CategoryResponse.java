package edu.eci.dosw.todo.dto;

import edu.eci.dosw.todo.entity.CategoryColor;

public class CategoryResponse {

    private Long id;

    private String name;

    private CategoryColor color;

    public CategoryResponse() {
    }

    public CategoryResponse(Long id, String name, CategoryColor color) {
        this.id = id;
        this.name = name;
        this.color = color;
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
