package me.mmtr.xwarehouse.controller;

import me.mmtr.xwarehouse.configuration.TestSecurityConfiguration;
import me.mmtr.xwarehouse.model.User;
import me.mmtr.xwarehouse.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfiguration.class)
@WebMvcTest(AuthenticationController.class)
public class AuthenticationControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private User currentUser;

    @BeforeEach
    public void setup() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("user");
        currentUser.setPassword("password");
    }

    @Test
    void verifyLoginGet() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void verifyRegisterGet() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void verifyRedirectingToRegisterWhenPasswordsDoNotMatch() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "test")
                        .param("password", "password")
                        .param("confirmPassword", "differentPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("error",
                        "Passwords do not match.")
                );
    }

    @Test
    void verifyRedirectingToRegisterWhenUsernameAlreadyExists() throws Exception {
        when(userService.createUser(anyString(), anyString())).thenReturn(null);

        mockMvc.perform(post("/register")
                        .param("username", "user")
                        .param("password", "password")
                        .param("confirmPassword", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute(
                        "error",
                        "Username is already taken."
                ));
    }

    @Test
    void verifyRedirectingToLoginWhenRegistrationIsSuccessful() throws Exception {
        when(userService.createUser(currentUser.getUsername(), currentUser.getPassword()))
                .thenReturn(currentUser);

        Assertions.assertNotNull(currentUser.getPassword());

        mockMvc.perform(post("/register")
                        .param("username", currentUser.getUsername())
                        .param("password", currentUser.getPassword())
                        .param("confirmPassword", currentUser.getPassword()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

}
