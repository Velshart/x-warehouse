package me.mmtr.xwarehouse.service;

import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void verifyIfFalseIsReturnedWhenUserAlreadyExists() {
        String username = "user";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User()));

        boolean result = userService.createUser(username, "password");

        assertFalse(result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyIfUserIsCreatedWhenUsernameIsAvailable() {
        String username = "user";
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        boolean result = userService.createUser(username, rawPassword);

        assertTrue(result);
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals(username) &&
                        user.getPassword().equals(encodedPassword)
        ));
    }
}
