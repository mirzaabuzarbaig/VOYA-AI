package com.voyaai.backend.repository;

import com.voyaai.backend.entity.Itinerary;
import com.voyaai.backend.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {

    Optional<Itinerary> findByTrip(Trip trip);

    Optional<Itinerary> findByTripId(Long tripId);

    void deleteByTrip(Trip trip);
}
