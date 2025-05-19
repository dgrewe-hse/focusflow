/*
 * Copyright (c) 2025 FocusFlow Project Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
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
        private final UUID TEST_USER_ID = UUID.randomUUID();

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
                testUser.setId(TEST_USER_ID);
                testUser.setEmail("test@example.com");
                testUser.setFirstName("Test");
                testUser.setLastName("User");
                testUser.setPassword("encodedPassword");

                authentication = new UsernamePasswordAuthenticationToken(
                                "test@example.com", "password");

                // Use doReturn...when instead of when...thenReturn to avoid actual method calls
                doReturn(authentication)
                                .when(authenticationManager).authenticate(any(Authentication.class));
                doReturn("test.token.value")
                                .when(tokenProvider).generateToken(any(Authentication.class));
                doReturn(testUser)
                                .when(userService).createUser(anyString(), anyString(), anyString(), anyString());
                doReturn(testUser)
                                .when(userService).getUserByEmail(anyString());
        }

        @Test
        void loginSuccess() throws Exception {
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validLoginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("test.token.value"))
                                .andExpect(jsonPath("$.email").value(validLoginRequest.getEmail()))
                                .andExpect(jsonPath("$.userId").value(TEST_USER_ID.toString()));
        }

        @Test
        void registerSuccess() throws Exception {
                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("test.token.value"))
                                .andExpect(jsonPath("$.email").value(validRegisterRequest.getEmail()))
                                .andExpect(jsonPath("$.userId").value(TEST_USER_ID.toString()));
        }
}
