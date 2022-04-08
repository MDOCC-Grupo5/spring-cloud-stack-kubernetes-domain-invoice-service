CREATE TABLE IF NOT EXISTS public.invoice (
    id integer primary key,
    user_id integer null,
    created_at timestamp not null,
    total_amount double precision not null
);