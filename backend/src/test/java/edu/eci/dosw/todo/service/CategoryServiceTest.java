package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.CategoryCreateRequest;
import edu.eci.dosw.todo.dto.CategoryResponse;
import edu.eci.dosw.todo.dto.CategoryUpdateRequest;
import edu.eci.dosw.todo.entity.CategoryColor;
import edu.eci.dosw.todo.entity.CategoryEntity;
import edu.eci.dosw.todo.exception.CategoryNameAlreadyExistsException;
import edu.eci.dosw.todo.exception.CategoryNotFoundException;
import edu.eci.dosw.todo.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository repository;

    @InjectMocks
    private CategoryServiceImpl service;

    private CategoryEntity buildEntity(Long id, String name, CategoryColor color) {
        CategoryEntity entity = new CategoryEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setColor(color);
        return entity;
    }

    @Test
    void findAll_shouldReturnCategories() {
        when(repository.findAll()).thenReturn(List.of(
                buildEntity(1L, "Estudio", CategoryColor.BLUE),
                buildEntity(2L, "Trabajo", CategoryColor.GREEN)));

        List<CategoryResponse> result = service.findAll();

        assertThat(result).extracting(CategoryResponse::getName).containsExactly("Estudio", "Trabajo");
    }

    @Test
    void findById_shouldReturnCategoryWhenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(buildEntity(1L, "Estudio", CategoryColor.BLUE)));

        CategoryResponse result = service.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getColor()).isEqualTo(CategoryColor.BLUE);
    }

    @Test
    void findById_shouldThrowExceptionWhenCategoryDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_shouldTrimNameAndSaveCategory() {
        when(repository.existsByNameIgnoreCase("Estudio")).thenReturn(false);
        when(repository.save(any(CategoryEntity.class))).thenAnswer(inv -> {
            CategoryEntity e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        CategoryResponse result = service.create(new CategoryCreateRequest("  Estudio ", CategoryColor.PURPLE));

        ArgumentCaptor<CategoryEntity> captor = ArgumentCaptor.forClass(CategoryEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Estudio");
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getColor()).isEqualTo(CategoryColor.PURPLE);
    }

    @Test
    void create_shouldThrowExceptionWhenNameAlreadyExists() {
        when(repository.existsByNameIgnoreCase("Estudio")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CategoryCreateRequest("Estudio", CategoryColor.BLUE)))
                .isInstanceOf(CategoryNameAlreadyExistsException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void update_shouldUpdateExistingCategory() {
        when(repository.findById(1L)).thenReturn(Optional.of(buildEntity(1L, "Estudio", CategoryColor.BLUE)));
        when(repository.existsByNameIgnoreCaseAndIdNot("Universidad", 1L)).thenReturn(false);
        when(repository.save(any(CategoryEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryResponse result = service.update(1L, new CategoryUpdateRequest("Universidad", CategoryColor.ORANGE));

        assertThat(result.getName()).isEqualTo("Universidad");
        assertThat(result.getColor()).isEqualTo(CategoryColor.ORANGE);
    }

    @Test
    void update_shouldThrowExceptionWhenCategoryDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new CategoryUpdateRequest("Otra", CategoryColor.BLUE)))
                .isInstanceOf(CategoryNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void update_shouldThrowExceptionWhenNameBelongsToAnotherCategory() {
        when(repository.findById(1L)).thenReturn(Optional.of(buildEntity(1L, "Estudio", CategoryColor.BLUE)));
        when(repository.existsByNameIgnoreCaseAndIdNot("Trabajo", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new CategoryUpdateRequest("Trabajo", CategoryColor.BLUE)))
                .isInstanceOf(CategoryNameAlreadyExistsException.class);
        verify(repository, never()).save(any());
    }
}
