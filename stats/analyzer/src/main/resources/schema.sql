create table if not exists user_actions (
    id bigint generated always as identity primary key,
    user_id bigint not null,
    event_id bigint not null,
    action_weight decimal not null,
    action_timestamp timestamp not null,
    unique(user_id, event_id)
);

create table if not exists events_similarity (
    id bigint generated always as identity primary key,
    event_a bigint not null,
    event_b bigint not null,
    score decimal not null,
    event_timestamp timestamp not null,
    unique(event_a, event_b)
);
