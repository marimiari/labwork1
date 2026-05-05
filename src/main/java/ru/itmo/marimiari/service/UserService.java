package ru.itmo.marimiari.service;

import ru.itmo.marimiari.repository.UserRepository;
import ru.itmo.marimiari.user.User;
import java.util.Optional;

public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public Optional<User> login(String login, String password) {
        Optional<User> userOpt = repository.findByLogin(login);
        if (userOpt.isPresent() && userOpt.get().checkPassword(password)) {
            return userOpt;
        }
        return Optional.empty();
    }

    public boolean register(String login, String password) {
        String hash = User.hashPassword(password);
        return repository.register(login, hash);
    }
}
