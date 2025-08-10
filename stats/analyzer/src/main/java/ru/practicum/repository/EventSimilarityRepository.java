package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.EventSimilarity;

import java.util.List;
import java.util.Optional;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {
    @Query(value = """
            with similar_events as (
                select
                    es.event_b as event_id,
                    es.score
                from events_similarity es
                where es.event_a = :eventId
                union all
                select
                    es.event_a as event_id,
                    es.score
                from events_similarity es
                where es.event_b = :eventId
            ),
            filtered_events as (
                select se.event_id, se.score
                from similar_events se
                left join user_actions ua
                    on ua.user_id = :userId and ua.event_id = se.event_id
                where ua.id is null
            )
            select fe.event_id as eventId, fe.score as score
            from filtered_events fe
            order by fe.score desc
            limit :maxResults""", nativeQuery = true)
    List<RecommendedEventProjection> getSimilarEvents(long eventId, long userId, int maxResults);


    @Query(value = """
            with user_events as (
                select event_id
                from user_actions
                where user_id = :userId
                order by timestamp desc
                limit :maxResults
            ),
            similar_events_a as (
                select es.event_b as event_id, es.score
                from events_similarity es
                where es.event_a in (select event_id from user_events)
            ),
            similar_events_b as (
                select es.event_a as event_id, es.score
                from events_similarity es
                where es.event_b in (select event_id from user_events)
            ),
            all_similar_events as (
                select * from similar_events_a
                union all
                select * from similar_events_b
            ),
            filtered_events as (
                select ase.event_id, ase.score
                from all_similar_events ase
                left join user_actions ua
                    on ua.user_id = :userId and ua.event_id = ase.event_id
                where ua.id is null
            ),
            viewed_event_scores as (
                select
                    se.event_id,
                    sum(ua.mark * se.score) as weighted_marks,
                    sum(se.score) as total_score
                from filtered_events fe
                join events_similarity se
                    on (fe.event_id = se.event_a or fe.event_id = se.event_b)
                join user_actions ua
                    on (se.event_a = ua.event_id or se.event_b = ua.event_id)
                    and ua.user_id = :userId
                group by se.event_id
            ),
            final_scores as (
                select
                    fe.event_id,
                    coalesce(wes.weighted_marks / nullif(wes.total_score, 0), 0) as score
                from filtered_events fe
                left join viewed_event_scores wes on fe.event_id = wes.event_id
            )
            select fs.event_id as eventId, fs.score as score
            from final_scores fs
            order by fs.score desc
            limit :maxResults""", nativeQuery = true)
    List<RecommendedEventProjection> getRecommendationsForUser(long userId, int maxResults);

    Optional<EventSimilarity> findByEventAAndEventB(Long eventA, Long eventB);
}
