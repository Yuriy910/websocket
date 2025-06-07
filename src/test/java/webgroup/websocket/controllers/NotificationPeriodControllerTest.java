package webgroup.websocket.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import webgroup.websocket.dto.NotificationPeriodDTO;
import webgroup.websocket.services.NotificationPeriodService;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NotificationPeriodControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationPeriodService notificationPeriodService;

    @InjectMocks
    private NotificationPeriodController notificationPeriodController;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(notificationPeriodController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

    }

    @Test
    void getByUser_shouldReturnList() throws Exception {
        Long userId = 1L;
        NotificationPeriodDTO dto = new NotificationPeriodDTO();
        List<NotificationPeriodDTO> list = List.of(dto);

        when(notificationPeriodService.getByUser(userId)).thenReturn(list);

        mockMvc.perform(get("/not/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(list)));

        verify(notificationPeriodService).getByUser(userId);
    }

    @Test
    void create_shouldReturnCreatedDto() throws Exception {
        Long userId = 1L;

        NotificationPeriodDTO inputDto = new NotificationPeriodDTO();
        inputDto.setDay(DayOfWeek.MONDAY);
        inputDto.setStartTime(LocalTime.of(9, 0));  // 09:00
        inputDto.setEndTime(LocalTime.of(17, 0));   // 17:00

        NotificationPeriodDTO savedDto = new NotificationPeriodDTO();
        savedDto.setDay(inputDto.getDay());
        savedDto.setStartTime(inputDto.getStartTime());
        savedDto.setEndTime(inputDto.getEndTime());

        when(notificationPeriodService.create(eq(userId), any(NotificationPeriodDTO.class))).thenReturn(savedDto);

        mockMvc.perform(post("/not/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(savedDto)));

        verify(notificationPeriodService).create(eq(userId), any(NotificationPeriodDTO.class));
    }


    @Test
    void update_shouldReturnUpdatedDto() throws Exception {
        Long id = 1L;

        NotificationPeriodDTO inputDto = new NotificationPeriodDTO();
        inputDto.setDay(DayOfWeek.TUESDAY);
        inputDto.setStartTime(LocalTime.of(10, 0));
        inputDto.setEndTime(LocalTime.of(18, 0));

        NotificationPeriodDTO updatedDto = new NotificationPeriodDTO();
        updatedDto.setDay(inputDto.getDay());
        updatedDto.setStartTime(inputDto.getStartTime());
        updatedDto.setEndTime(inputDto.getEndTime());

        when(notificationPeriodService.update(eq(id), any(NotificationPeriodDTO.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/not/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(updatedDto)));

        verify(notificationPeriodService).update(eq(id), any(NotificationPeriodDTO.class));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        Long id = 1L;

        doNothing().when(notificationPeriodService).delete(id);

        mockMvc.perform(delete("/not/{id}", id))
                .andExpect(status().isNoContent());

        verify(notificationPeriodService).delete(id);
    }
}
