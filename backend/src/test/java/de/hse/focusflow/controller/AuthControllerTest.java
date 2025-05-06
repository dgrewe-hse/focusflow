package de.hse.focusflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.hse.focusflow.config.TestConfig;
import de.hse.focusflow.config.TestSecurityConfig;
import de.hse.focusflow.dto.AuthDTO;
import de.hse.focusflow.model.User;
import de.hse.focusflow.security.JwtTokenProvider;
import de.hse.focusflow.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({ TestConfig.class, TestSecurityConfig.class })
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthDTO.LoginRequest validLoginRequest;
    private AuthDTO.RegisterRequest validRegisterRequest;
    private Authentication authentication;
    private User testUser;

    @BeforeEach
    void setUp() {
        validLoginRequest = new AuthDTO.LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("password");

        validRegisterRequest = new AuthDTO.RegisterRequest();
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setPassword("password");
        validRegisterRequest.setFirstName("Test");
        validRegisterRequest.setLastName("User");

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        authentication = new UsernamePasswordAuthenticationToken(
                "test@example.com", "password");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(any(Authentication.class)))
                .thenReturn("test.token.value");
        when(userService.createUser(any(), any(), any(), any()))
                .thenReturn(testUser);
    }

    @Test
    void loginSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(validLoginRequest.getEmail()));
    }

    @Test
    void registerSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(validRegisterRequest.getEmail()));
    }
}
