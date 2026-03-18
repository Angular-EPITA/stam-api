INSERT INTO genres (id, name) VALUES (1, 'RPG');
INSERT INTO genres (id, name) VALUES (2, 'Action');
INSERT INTO genres (id, name) VALUES (3, 'Stratégie');
INSERT INTO genres (id, name) VALUES (4, 'Aventure');
INSERT INTO genres (id, name) VALUES (5, 'FPS');

-- Ajout du champ image_url et des liens
INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('11111111-1111-1111-1111-111111111111', 'The Witcher 3: Wild Hunt', 'Un jeu de rôle en monde ouvert primé.', '2015-05-19', 29.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1wyy.png', 1);

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('22222222-2222-2222-2222-222222222222', 'Cyberpunk 2077', 'RPG d''action dans la mégalopole de Night City.', '2020-12-10', 39.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co2mvt.png', 1);

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('33333333-3333-3333-3333-333333333333', 'God of War', 'Kratos affronte les dieux nordiques.', '2018-04-20', 49.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1tmu.png', 2);

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('44444444-4444-4444-4444-444444444444', 'Civilization VI', 'Bâtissez un empire à l''épreuve du temps.', '2016-10-21', 59.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co2k03.png', 3);

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('55555555-5555-5555-5555-555555555555', 'Helldivers 2', 'Un jeu de tir coopératif intense.', '2024-02-08', 39.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co7u2z.png', 5);