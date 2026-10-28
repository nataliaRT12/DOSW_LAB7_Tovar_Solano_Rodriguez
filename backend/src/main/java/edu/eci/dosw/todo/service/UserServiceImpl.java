package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.UserResponse;
import edu.eci.dosw.todo.exception.UserNotFoundException;
import edu.eci.dosw.todo.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserResponse findById(Long id) {
        return repository.findById(id)
                .map(user -> new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole()))
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
