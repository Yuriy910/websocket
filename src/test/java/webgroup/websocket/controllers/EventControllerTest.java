package webgroup.websocket.controllers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import webgroup.websocket.dto.EventDTO;
import webgroup.websocket.entities.Event;
import webgroup.websocket.mappers.EventMapper;
import webgroup.websocket.services.EventService;
import webgroup.websocket.services.NotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

class EventControllerTest {

    @Mock
    private EventService eventService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventController eventController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
    }

    @Test
    void createEvent_shouldReturnOkAndProcessNotification_whenValid() throws Exception {
        EventDTO inputDto = new EventDTO();
        inputDto.setMessage("Test event");
        inputDto.setOccurredAt(LocalDateTime.now()); // Добавлено обязательное поле

        EventDTO savedDto = new EventDTO();
        savedDto.setId(1L);
        savedDto.setMessage("Test event");
        savedDto.setOccurredAt(inputDto.getOccurredAt()); // добавь сюда тоже

        Event savedEntity = new Event();

        when(eventService.save(any(EventDTO.class))).thenReturn(savedDto);
        when(eventMapper.toEntity(savedDto)).thenReturn(savedEntity);

        mockMvc.perform(MockMvcRequestBuilders.post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.message").value("Test event"));

        verify(eventService).save(any(EventDTO.class));
        verify(eventMapper).toEntity(savedDto);
        verify(notificationService).processEvent(savedEntity);
    }


    @Test
    void createEvent_shouldReturnInternalServerError_whenSavedDtoHasNullId() throws Exception {
        EventDTO inputDto = new EventDTO();
        inputDto.setMessage("Test event");
        inputDto.setOccurredAt(LocalDateTime.now());  // Заполняем обязательное поле!

        EventDTO savedDto = new EventDTO();
        savedDto.setId(null); // ID null triggers error path
        savedDto.setMessage("Test event");
        savedDto.setOccurredAt(inputDto.getOccurredAt());

        when(eventService.save(any(EventDTO.class))).thenReturn(savedDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isInternalServerError());

        verify(eventService).save(any(EventDTO.class));
        verifyNoMoreInteractions(eventMapper, notificationService);
    }

    @Test
    void createEvent_shouldReturnInternalServerError_whenNotificationServiceThrows() throws Exception {
        EventDTO inputDto = new EventDTO();
        inputDto.setMessage("Test event");
        inputDto.setOccurredAt(LocalDateTime.now()); // обязательно!

        EventDTO savedDto = new EventDTO();
        savedDto.setId(1L);
        savedDto.setMessage("Test event");
        savedDto.setOccurredAt(inputDto.getOccurredAt());

        Event savedEntity = new Event();

        when(eventService.save(any(EventDTO.class))).thenReturn(savedDto);
        when(eventMapper.toEntity(savedDto)).thenReturn(savedEntity);
        doThrow(new RuntimeException("Notification failure")).when(notificationService).processEvent(savedEntity);

        mockMvc.perform(MockMvcRequestBuilders.post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.message").value("Test event"));

        verify(eventService).save(any(EventDTO.class));
        verify(eventMapper).toEntity(savedDto);
        verify(notificationService).processEvent(savedEntity);
    }

    @Test
    void getAllEvents_shouldReturnList() throws Exception {
        EventDTO dto1 = new EventDTO();
        dto1.setId(1L);
        dto1.setMessage("Event 1");
        EventDTO dto2 = new EventDTO();
        dto2.setId(2L);
        dto2.setMessage("Event 2");

        when(eventService.findAll()).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(MockMvcRequestBuilders.get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].message").value("Event 1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].message").value("Event 2"));

        verify(eventService).findAll();
    }

    @Test
    void getEvent_shouldReturnEvent_whenFound() throws Exception {
        Long eventId = 1L;
        EventDTO dto = new EventDTO();
        dto.setId(eventId);
        dto.setMessage("Found event");

        when(eventService.findById(eventId)).thenReturn(Optional.of(dto));

        mockMvc.perform(MockMvcRequestBuilders.get("/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.message").value("Found event"));

        verify(eventService).findById(eventId);
    }

    @Test
    void getEvent_shouldReturnNotFound_whenMissing() throws Exception {
        Long eventId = 99L;

        when(eventService.findById(eventId)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/events/{id}", eventId))
                .andExpect(status().isNotFound());

        verify(eventService).findById(eventId);
    }

    @Test
    void getEventsByUser_shouldReturnList() throws Exception {
        Long userId = 10L;
        EventDTO dto = new EventDTO();
        dto.setId(5L);
        dto.setMessage("User's event");

        when(eventService.findByUserId(userId)).thenReturn(List.of(dto));

        mockMvc.perform(MockMvcRequestBuilders.get("/events/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(5L))
                .andExpect(jsonPath("$[0].message").value("User's event"));

        verify(eventService).findByUserId(userId);
    }

    @Test
    void deleteEvent_shouldReturnNoContent() throws Exception {
        Long eventId = 1L;
        doNothing().when(eventService).deleteById(eventId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/events/{id}", eventId))
                .andExpect(status().isNoContent());

        verify(eventService).deleteById(eventId);
    }
}
