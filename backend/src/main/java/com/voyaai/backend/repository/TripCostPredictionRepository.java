package com.voyaai.backend.repository;

import com.voyaai.backend.entity.TripCostPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TripCostPredictionRepository extends JpaRepository<TripCostPrediction, Long> {

    Optional<TripCostPrediction> findTopByTripIdOrderByCreatedAtDesc(Long tripId);

    void deleteAllByTripId(Long tripId);
}
