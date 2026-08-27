-- The 12 tapes the web-portal renders today (web-portal/src/data/tapes.ts), so a
-- fresh database matches what the frontend already shows. Dev-only: application-dev.yaml
-- is the only profile that puts classpath:db/seed on Flyway's path.
--
-- Original catalogue only carried a year and a flat hex palette; genre and
-- colors are now their own reference/child tables (see V1), so release_date
-- is pinned to Jan 1 of that year and duration is converted to milliseconds.

INSERT INTO genre (id, name, description)
VALUES ('8e17b20c-0e19-4c68-9eba-f5d5e9e9688d', 'Action', 'High-stakes chases, fights, and stunts.'),
       ('bcbba66a-c4d1-431a-bf61-47f767996b32', 'Sci-Fi', 'Speculative technology and futures.'),
       ('d422fdbd-acf5-406f-b3ba-2000720726b5', 'Thriller', 'Suspense-driven, high-tension plots.'),
       ('12003675-9e72-40ed-baee-9b7c644181fd', 'Horror', 'Built to frighten and unsettle.'),
       ('b9854801-2ff2-446b-acbe-5ded2392b20a', 'Adventure', 'Journeys into the unknown.'),
       ('6887bb1a-e412-4fb2-96c7-7b5952fdf37c', 'War', 'Combat and its human cost.'),
       ('05bb67a0-3f22-4e74-a2fb-fef39872e7ae', 'Fantasy', 'Magic and imagined worlds.'),
       ('ace874bc-fee5-41e5-853c-6c769efef3ee', 'Crime', 'Criminals, cops, and the line between.'),
       ('b849e7be-8ff8-4b59-9bf6-442d6d077f0d', 'Western', 'The frontier and its outlaws.');

INSERT INTO tape_colors (id, "primary", secondary, accent, label)
VALUES ('2e70ec2c-1cea-4126-bb9f-3e6f45364f19', '#ff006e', '#8338ec', '#ffbe0b', '#1a1a2e'),
       ('702cb964-e6af-4360-b833-721d6860f23b', '#00b4d8', '#0077b6', '#90e0ef', '#023e8a'),
       ('67397d13-3193-492c-9008-55e0b2defd20', '#9b5de5', '#f15bb5', '#fee440', '#240046'),
       ('12b2ff84-7452-4b01-8bae-fcaac55dbce7', '#ff7b00', '#ff0000', '#ffdd00', '#3d0000'),
       ('5d958b29-bf7c-4b6e-9b75-616ce1c5c893', '#2d00f7', '#6a00f4', '#e500a4', '#0a0a23'),
       ('d584c007-8636-43a7-9f2c-a66eb1b4b270', '#00f5d4', '#00bbf9', '#f15bb5', '#0b132b'),
       ('28ec7725-3a7d-4f3e-a044-c9e18566684e', '#606c38', '#283618', '#dda15e', '#1b1b1b'),
       ('265b8f30-3b45-4bcc-9b93-8431c97f631b', '#d4a5ff', '#a855f7', '#67e8f9', '#1e1b4b'),
       ('ae77eb01-7949-4ea7-b8e8-82f6d0aa5c2b', '#dc2626', '#991b1b', '#fbbf24', '#1c1917'),
       ('e725537b-d3c1-44a9-88bc-9e9501665f80', '#06d6a0', '#118ab2', '#ef476f', '#073b4c'),
       ('b4f1db3c-cdd2-4cae-b82c-cff031ae5bc6', '#e9c46a', '#f4a261', '#e76f51', '#264653'),
       ('2e07f1b5-3c25-40aa-8c4f-d3cd166e197a', '#94a3b8', '#475569', '#38bdf8', '#0f172a');

INSERT INTO tape (id, title, subtitle, release_date, genre_id, duration, colors_id, pattern)
VALUES (gen_random_uuid(), 'NEON NIGHTS', 'The City Never Sleeps', DATE '1987-01-01',
        '8e17b20c-0e19-4c68-9eba-f5d5e9e9688d', 6840000,
        '2e70ec2c-1cea-4126-bb9f-3e6f45364f19', 'stripes'),
       (gen_random_uuid(), 'CHROME HORIZON', 'Beyond the Last Frontier', DATE '1984-01-01',
        'bcbba66a-c4d1-431a-bf61-47f767996b32', 7920000,
        '702cb964-e6af-4360-b833-721d6860f23b', 'gradient'),
       (gen_random_uuid(), 'VELVET THUNDER', NULL, DATE '1989-01-01',
        'd422fdbd-acf5-406f-b3ba-2000720726b5', 5880000,
        '67397d13-3193-492c-9008-55e0b2defd20', 'geometric'),
       (gen_random_uuid(), 'SOLAR BURN', 'No Escape from the Heat', DATE '1986-01-01',
        '8e17b20c-0e19-4c68-9eba-f5d5e9e9688d', 6420000,
        '12b2ff84-7452-4b01-8bae-fcaac55dbce7', 'retro-blocks'),
       (gen_random_uuid(), 'MIDNIGHT FREQUENCY', NULL, DATE '1991-01-01',
        '12003675-9e72-40ed-baee-9b7c644181fd', 5520000,
        '5d958b29-bf7c-4b6e-9b75-616ce1c5c893', 'waves'),
       (gen_random_uuid(), 'TURBO KID', 'Full Speed Ahead', DATE '1988-01-01',
        'b9854801-2ff2-446b-acbe-5ded2392b20a', 6120000,
        'd584c007-8636-43a7-9f2c-a66eb1b4b270', 'stripes'),
       (gen_random_uuid(), 'STEEL RAIN', NULL, DATE '1985-01-01',
        '6887bb1a-e412-4fb2-96c7-7b5952fdf37c', 7500000,
        '28ec7725-3a7d-4f3e-a044-c9e18566684e', 'diamonds'),
       (gen_random_uuid(), 'CRYSTAL PALACE', 'A World of Wonder', DATE '1990-01-01',
        '05bb67a0-3f22-4e74-a2fb-fef39872e7ae', 6960000,
        '265b8f30-3b45-4bcc-9b93-8431c97f631b', 'geometric'),
       (gen_random_uuid(), 'RED LINE', NULL, DATE '1983-01-01',
        'ace874bc-fee5-41e5-853c-6c769efef3ee', 6540000,
        'ae77eb01-7949-4ea7-b8e8-82f6d0aa5c2b', 'retro-blocks'),
       (gen_random_uuid(), 'PHANTOM SIGNAL', 'They Are Listening', DATE '1992-01-01',
        'bcbba66a-c4d1-431a-bf61-47f767996b32', 6240000,
        'e725537b-d3c1-44a9-88bc-9e9501665f80', 'waves'),
       (gen_random_uuid(), 'DESERT MIRAGE', NULL, DATE '1986-01-01',
        'b849e7be-8ff8-4b59-9bf6-442d6d077f0d', 7260000,
        'b4f1db3c-cdd2-4cae-b82c-cff031ae5bc6', 'gradient'),
       (gen_random_uuid(), 'BLACK ICE', 'Cold-Blooded Justice', DATE '1993-01-01',
        'd422fdbd-acf5-406f-b3ba-2000720726b5', 6660000,
        '2e07f1b5-3c25-40aa-8c4f-d3cd166e197a', 'diamonds');
