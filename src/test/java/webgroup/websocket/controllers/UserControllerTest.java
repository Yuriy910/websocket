package webgroup.websocket.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import webgroup.websocket.dto.UserDTO;
import webgroup.websocket.entities.User;
import webgroup.websocket.mappers.UserMapper;
import webgroup.websocket.services.UserService;


import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        User user1 = new User();
        user1.setId(1L);
        user1.setFullName("User One");
        User user2 = new User();
        user2.setId(2L);
        user2.setFullName("User Two");

        UserDTO dto1 = new UserDTO();
        dto1.setId(1L);
        dto1.setFullName("User One");
        UserDTO dto2 = new UserDTO();
        dto2.setId(2L);
        dto2.setFullName("User Two");

        when(userService.getAllUsers()).thenReturn(List.of(user1, user2));
        when(userMapper.toDto(user1)).thenReturn(dto1);
        when(userMapper.toDto(user2)).thenReturn(dto2);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].fullName", is("User One")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].fullName", is("User Two")));

        verify(userService).getAllUsers();
        verify(userMapper, times(2)).toDto(any(User.class));
    }

    @Test
    void getUser_shouldReturnUser_whenExists() throws Exception {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setFullName("User One");
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId);
        userDTO.setFullName("User One");

        when(userService.getUserWithNotifications(userId)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDTO);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.fullName", is("User One")));

        verify(userService).getUserWithNotifications(userId);
        verify(userMapper).toDto(user);
    }

    @Test
    void getUser_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        Long userId = 1L;

        when(userService.getUserWithNotifications(userId)).thenThrow(EntityNotFoundException.class);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound());

        verify(userService).getUserWithNotifications(userId);
        verifyNoInteractions(userMapper);
    }

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        UserDTO inputDto = new UserDTO();
        inputDto.setFullName("New User");

        User createdUser = new User();
        createdUser.setId(1L);
        createdUser.setFullName("New User");

        UserDTO createdDto = new UserDTO();
        createdDto.setId(1L);
        createdDto.setFullName("New User");

        when(userService.createUser("New User")).thenReturn(createdUser);
        when(userMapper.toDto(createdUser)).thenReturn(createdDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.fullName", is("New User")));

        verify(userService).createUser("New User");
        verify(userMapper).toDto(createdUser);
    }

    @Test
    void deleteUser_shouldReturnNoContent_whenDeleted() throws Exception {
        Long userId = 1L;

        when(userService.deleteUser(userId)).thenReturn(true);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(userId);
    }

    @Test
    void deleteUser_shouldReturnNotFound_whenUserNotFound() throws Exception {
        Long userId = 1L;

        when(userService.deleteUser(userId)).thenReturn(false);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNotFound());

        verify(userService).deleteUser(userId);
    }
}
