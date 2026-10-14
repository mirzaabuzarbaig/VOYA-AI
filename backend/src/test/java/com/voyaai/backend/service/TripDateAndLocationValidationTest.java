package com.voyaai.backend.service;

import com.voyaai.backend.dto.CreateTripRequest;
import com.voyaai.backend.dto.TripResponse;
import com.voyaai.backend.entity.Trip;
import com.voyaai.backend.entity.User;
import com.voyaai.backend.repository.TripRepository;
import com.voyaai.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripDateAndLocationValidationTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TripService tripService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("traveler@voyaai.com");
        testUser.setName("Traveler");
    }

    @Test
    @DisplayName("createTrip rejects start date that is today")
    void testCreateTripRejectsTodayStartDate() {
        when(userRepository.findByEmail("traveler@voyaai.com")).thenReturn(Optional.of(testUser));

        CreateTripRequest req = new CreateTripRequest();
        req.setDestination("Ooty");
        req.setSource("Hyderabad");
        req.setStartDate(LocalDate.now()); // today
        req.setEndDate(LocalDate.now().plusDays(3));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                tripService.createTrip(req, "traveler@voyaai.com")
        );
        assertTrue(ex.getMessage().contains("Trip start date must be tomorrow or later"));
        verify(tripRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTrip rejects start date that is in the past (yesterday)")
    void testCreateTripRejectsYesterdayStartDate() {
        when(userRepository.findByEmail("traveler@voyaai.com")).thenReturn(Optional.of(testUser));

        CreateTripRequest req = new CreateTripRequest();
        req.setDestination("Paris");
        req.setSource("London");
        req.setStartDate(LocalDate.now().minusDays(1)); // yesterday
        req.setEndDate(LocalDate.now().plusDays(3));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                tripService.createTrip(req, "traveler@voyaai.com")
        );
        assertTrue(ex.getMessage().contains("Trip start date must be tomorrow or later"));
        verify(tripRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTrip rejects end date before start date")
    void testCreateTripRejectsEndDateBeforeStartDate() {
        when(userRepository.findByEmail("traveler@voyaai.com")).thenReturn(Optional.of(testUser));

        CreateTripRequest req = new CreateTripRequest();
        req.setDestination("Tokyo");
        req.setSource("Bangkok");
        req.setStartDate(LocalDate.now().plusDays(5));
        req.setEndDate(LocalDate.now().plusDays(2)); // before start date

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                tripService.createTrip(req, "traveler@voyaai.com")
        );
        assertTrue(ex.getMessage().contains("End date cannot be before start date"));
        verify(tripRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTrip succeeds with valid destination and start date starting from tomorrow")
    void testCreateTripSucceedsTomorrow() {
        when(userRepository.findByEmail("traveler@voyaai.com")).thenReturn(Optional.of(testUser));

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate returnDate = LocalDate.now().plusDays(5);

        CreateTripRequest req = new CreateTripRequest();
        req.setDestination("Ooty");
        req.setSource("Hyderabad");
        req.setStartDate(tomorrow);
        req.setEndDate(returnDate);
        req.setBudget(25000.0);
        req.setTravelers(2);

        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> {
            Trip t = invocation.getArgument(0);
            t.setId(101L);
            return t;
        });

        TripResponse res = tripService.createTrip(req, "traveler@voyaai.com");

        assertNotNull(res);
        assertEquals("Ooty", res.getDestination());
        assertEquals("Hyderabad", res.getSource());
        assertEquals(tomorrow, res.getStartDate());
        assertEquals(returnDate, res.getEndDate());
        verify(tripRepository, times(1)).save(any(Trip.class));
    }
}
