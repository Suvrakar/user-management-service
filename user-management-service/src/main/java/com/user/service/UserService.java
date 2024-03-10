package com.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.entity.UserEntity;
import com.user.exception.InvalidUserDataException;
import com.user.exception.UserNotFoundException;
import com.user.model.User;
import com.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public User saveUser(User user) {
        log.info("Saving user: {}", user);
        validateUser(user);

        UserEntity userEntity = new UserEntity();
        userEntity.setName(user.getName());

        if (!isValidEmail(user.getEmail())) {
            log.warn("Invalid email address provided: {}", user.getEmail());
            throw new InvalidUserDataException("Invalid email address");
        }

        userEntity.setEmail(user.getEmail());

        UserEntity savedUserEntity = userRepository.save(userEntity);

        User savedUser = convertToUser(savedUserEntity);

        saveUserDataToRedis(savedUser);

        log.info("User saved successfully: {}", savedUser);
        return savedUser;
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public Optional<User> getUserById(Long id) {
        log.info("Retrieving user by ID: {}", id);

        User userDataFromRedis = getUserDataFromRedis(id);

        if (Objects.nonNull(userDataFromRedis)) {
            return Optional.of(userDataFromRedis);
        }

        Optional<User> user = userRepository.findById(id).map(this::convertToUser);

        if (user.isPresent()) {
            saveUserDataToRedis(user.get());
            log.info("User retrieved by ID {}: {}", id, user.get());
        } else {
            log.info("No user found with ID: {}", id);
        }

        return user;
    }

    public User updateUser(Long id, User user) {
        log.info("Updating user with ID {}: {}", id, user);

        if (user == null || user.getName() == null || user.getEmail() == null) {
            log.warn("Invalid user data for update: {}", user);
            throw new InvalidUserDataException("User name and email cannot be null");
        }

        if (!isValidEmail(user.getEmail())) {
            log.warn("Invalid email address provided for update: {}", user.getEmail());
            throw new InvalidUserDataException("Invalid email address");
        }

        Optional<UserEntity> existingUserOptional = userRepository.findById(id);
        if (existingUserOptional.isPresent()) {
            UserEntity existingUser = existingUserOptional.get();
            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());

            UserEntity updatedUserEntity = userRepository.save(existingUser);

            User updatedUser = convertToUser(updatedUserEntity);

            log.info("User updated successfully: {}", updatedUser);
            return updatedUser;
        } else {
            log.info("No user found with ID: {}", id);
            throw new UserNotFoundException("No user found with ID: " + id);
        }
    }


    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            log.info("User deleted successfully with ID: {}", id);
        } else {
            log.info("User not found with ID: {}", id);
            throw new UserNotFoundException("User not found with id: " + id);

        }
    }


    private void validateUser(User user) {
        if (user == null || user.getName() == null || user.getEmail() == null) {
            log.warn("Invalid user data: {}", user);
            throw new InvalidUserDataException("User name and email cannot be null");
        }
        UserEntity entity = userRepository.findByEmail(user.getEmail());

        if (Objects.nonNull(entity)) {
            log.warn("Account already exists with this email");
            throw new InvalidUserDataException("Account already exists with this email");
        }
    }

    private User convertToUser(UserEntity userEntity) {
        return new User(userEntity.getId(), userEntity.getName(), userEntity.getEmail());
    }

    private void saveUserDataToRedis(User user) {
        try {
            redisTemplate.opsForValue().set(String.valueOf(user.getId()), user);
        } catch (Exception e) {
            log.info("Failed to save user data into redis");
        }
    }

    private User getUserDataFromRedis(Long id) {
        try {
            Object result = redisTemplate.opsForValue().get(String.valueOf(id));
            if (result != null) {
                return objectMapper.convertValue(result, User.class);
            } else {
                return null;
            }
        } catch (Exception e) {
            log.info("No data found in redis");
        }
        return null;
    }


    private void updateUserDataIntoRedis(User user) {
        try {
            if (redisTemplate.hasKey(String.valueOf(user.getId()))) {

                redisTemplate.opsForValue().set(String.valueOf(user.getId()), user);
            } else {
                saveUserDataToRedis(user);
            }
        } catch (Exception e) {
            log.info("Failed to save user data into redis");
        }
    }
}
