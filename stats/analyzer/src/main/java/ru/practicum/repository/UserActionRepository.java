package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.UserAction;

import java.util.List;
import java.util.Optional;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    @Query(value = """
                    select event_id as eventId
                    , sum(coalesce(action_weight, 0.0)) as score
                    from user_actions
                    where event_id in (:eventIds)
                    group by event_id
                    """, nativeQuery = true)
    List<RecommendedEventProjection> getCountOfInteractions(List<Long> eventIds);

    Optional<UserAction> findByUserIdAndEventId(Long userId, Long eventId);
}
