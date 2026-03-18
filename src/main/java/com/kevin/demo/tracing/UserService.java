package com.kevin.demo.tracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final Map<Long, User> userStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public UserService() {
        userStore.put(1L, new User(1L, "Alice", "alice@example.com"));
        userStore.put(2L, new User(2L, "Bob", "bob@example.com"));
        userStore.put(3L, new User(3L, "Charlie", "charlie@example.com"));
        idGenerator.set(4);
    }

    public User getUserById(Long id) {
        log.info("Getting user by id: {}", id);
        return userStore.get(id);
    }

    public List<User> getAllUsers() {
        log.info("Getting all users, count: {}", userStore.size());
        return List.copyOf(userStore.values());
    }

    public User createUser(String name, String email) {
        Long id = idGenerator.getAndIncrement();
        User user = new User(id, name, email);
        userStore.put(id, user);
        log.info("Created user: {}", user);
        return user;
    }
}
