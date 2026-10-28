package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.CategoryCreateRequest;
import edu.eci.dosw.todo.dto.CategoryResponse;
import edu.eci.dosw.todo.dto.CategoryUpdateRequest;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> findAll();
    CategoryResponse findById(Long id);
    CategoryResponse create(CategoryCreateRequest request);
    CategoryResponse update(Long id, CategoryUpdateRequest request);
}
