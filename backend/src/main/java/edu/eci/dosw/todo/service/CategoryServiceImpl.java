package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.CategoryCreateRequest;
import edu.eci.dosw.todo.dto.CategoryResponse;
import edu.eci.dosw.todo.dto.CategoryUpdateRequest;
import edu.eci.dosw.todo.entity.CategoryEntity;
import edu.eci.dosw.todo.exception.CategoryNameAlreadyExistsException;
import edu.eci.dosw.todo.exception.CategoryNotFoundException;
import edu.eci.dosw.todo.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public CategoryResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    public CategoryResponse create(CategoryCreateRequest request) {
        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new CategoryNameAlreadyExistsException(name);
        }

        CategoryEntity entity = new CategoryEntity();
        entity.setName(name);
        entity.setColor(request.getColor());
        return toResponse(categoryRepository.save(entity));
    }

    @Override
    public CategoryResponse update(Long id, CategoryUpdateRequest request) {
        CategoryEntity entity = getOrThrow(id);

        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new CategoryNameAlreadyExistsException(name);
        }

        entity.setName(name);
        entity.setColor(request.getColor());
        return toResponse(categoryRepository.save(entity));
    }

    private CategoryEntity getOrThrow(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));
    }

    private CategoryResponse toResponse(CategoryEntity entity) {
        return new CategoryResponse(entity.getId(), entity.getName(), entity.getColor());
    }
}
