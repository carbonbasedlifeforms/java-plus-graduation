package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.enums.ActionType;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AggregatorService {
    private final Map<Long, Map<Long, Double>> userInteractionsByEvent = new HashMap<>();
    private final Map<Long, Double> totalEventWeights = new HashMap<>();
    private final Map<Long, Map<Long, Double>> minimumWeightSums = new HashMap<>();

    public List<EventSimilarityAvro> handle(UserActionAvro userActionAvro) {
        List<EventSimilarityAvro> results = new ArrayList<>();
        long userId = userActionAvro.getUserId();
        long eventId = userActionAvro.getEventId();
        double weightChange = updateUserInteraction(userActionAvro);
        if (weightChange > 0) {
            updateTotalWeight(eventId, weightChange);

            Set<Long> relatedEvents = getRelatedEvents(eventId);
            log.info("Calculating similarities for events: {}", relatedEvents);

            relatedEvents.forEach(otherEvent -> {
                double similarityScore = calculateSimilarity(eventId, otherEvent, weightChange, userId);
                if (similarityScore > 0.0) {
                    results.add(createSimilarityRecord(eventId, otherEvent, similarityScore));
                }
            });
        }
        return results;
    }

    private void updateTotalWeight(Long eventId, Double weightChange) {
        totalEventWeights.merge(eventId, weightChange, Double::sum);
        log.info("Updated total weight for event {}: {}", eventId, totalEventWeights.get(eventId));
    }

    private Set<Long> getRelatedEvents(Long currentEventId) {
        return userInteractionsByEvent.keySet().stream()
                .filter(id -> !Objects.equals(id, currentEventId))
                .collect(Collectors.toSet());
    }

    private double calculateSimilarity(Long eventA, Long eventB, Double delta, Long userId) {
        double numerator = getMinimumWeightSum(eventA, eventB, delta, userId);
        double denominator = Math.sqrt(totalEventWeights.get(eventA)) * Math.sqrt(totalEventWeights.get(eventB));

        return denominator == 0 ? 0.0 : numerator / denominator;
    }

    private Double updateUserInteraction(UserActionAvro action) {
        Long eventId = action.getEventId();
        Long userId = action.getUserId();
        Double newWeight = ActionType.getWeight(action.getActionType().name());
        Map<Long, Double> userActions = userInteractionsByEvent.computeIfAbsent(eventId, k -> new HashMap<>());
        Double oldWeight = userActions.get(userId);

        if (oldWeight == null || newWeight > oldWeight) {
            userActions.put(userId, newWeight);
            double weightDelta = oldWeight == null ? newWeight : newWeight - oldWeight;
            log.info("Updated interaction for user {} in event {}: {}", userId, eventId, weightDelta);
            return weightDelta;
        }
        log.info("No change in weight for user {} in event {}", userId, eventId);
        return 0.0;
    }

    private Double getMinimumWeightSum(Long eventA, Long eventB, Double delta, Long userId) {
        Long firstEvent = Math.min(eventA, eventB);
        Long secondEvent = Math.max(eventA, eventB);
        Double weightA = userInteractionsByEvent.get(eventA).get(userId);
        Double weightB = userInteractionsByEvent.get(eventB).get(userId);

        if (weightB == null) {
            log.info("User {} has no interaction with event {}", userId, eventB);
            return 0.0;
        }

        Map<Long, Double> innerMap = minimumWeightSums.computeIfAbsent(firstEvent, k -> new HashMap<>());
        Double cachedSum = innerMap.get(secondEvent);

        if (cachedSum == null) {
            cachedSum = calculateMinimumWeightSum(eventA, eventB);
            innerMap.put(secondEvent, cachedSum);
            log.info("Calculated new minimum weight sum between {} and {}: {}", eventA, eventB, cachedSum);
            return cachedSum;
        }

        if (weightA > weightB && (weightA - delta) < weightB) {
            double updatedSum = cachedSum + (weightB - (weightA - delta));
            log.info("Updated minimum weight sum after value change: {}", updatedSum);
            innerMap.put(secondEvent, updatedSum);
            return updatedSum;
        }

        if (weightA <= weightB) {
            double updatedSum = cachedSum + delta;
            log.info("Updated minimum weight sum by delta: {}", updatedSum);
            innerMap.put(secondEvent, updatedSum);
            return updatedSum;
        }

        log.info("Minimum weight sum remains unchanged: {}", cachedSum);
        return cachedSum;
    }

    private Double calculateMinimumWeightSum(Long eventA, Long eventB) {
        Set<Long> commonUsers = getCommonUsers(eventA, eventB);
        if (commonUsers.isEmpty()) {
            return 0.0;
        }

        return commonUsers.stream()
                .mapToDouble(userId -> Math.min(
                        userInteractionsByEvent.get(eventA).get(userId),
                        userInteractionsByEvent.get(eventB).get(userId)
                ))
                .sum();
    }

    private Set<Long> getCommonUsers(Long eventA, Long eventB) {
        Map<Long, Double> usersA = userInteractionsByEvent.get(eventA);
        Map<Long, Double> usersB = userInteractionsByEvent.get(eventB);

        Set<Long> commonUsers = new HashSet<>(usersA.keySet());
        commonUsers.retainAll(usersB.keySet());

        return commonUsers;
    }

    private EventSimilarityAvro createSimilarityRecord(Long eventA, Long eventB, double score) {
        return EventSimilarityAvro.newBuilder()
                .setEventA(Math.min(eventA, eventB))
                .setEventB(Math.max(eventA, eventB))
                .setScore(score)
                .setTimestamp(Instant.now())
                .build();
    }
}
