-- Utilisateurs par défaut (seul le rôle ADMIN existe)
INSERT INTO users (id, username, password, role) VALUES (1, 'admin', '$2b$12$AhSVJu8UBUBD6qTugU/ixuTG1PN9TN9wlUzRXEL7yDe.KWaeTGvNS', 'ADMIN') ON CONFLICT (id) DO NOTHING;

-- Ajout de nouveaux genres pour plus de variété
INSERT INTO genres (id, name) VALUES (1, 'RPG') ON CONFLICT (id) DO NOTHING;
INSERT INTO genres (id, name) VALUES (2, 'Action') ON CONFLICT (id) DO NOTHING;
INSERT INTO genres (id, name) VALUES (3, 'Stratégie') ON CONFLICT (id) DO NOTHING;
INSERT INTO genres (id, name) VALUES (4, 'Aventure') ON CONFLICT (id) DO NOTHING;
INSERT INTO genres (id, name) VALUES (5, 'FPS') ON CONFLICT (id) DO NOTHING; 
INSERT INTO genres (id, name) VALUES (6, 'Plateforme') ON CONFLICT (id) DO NOTHING;
INSERT INTO genres (id, name) VALUES (7, 'Course') ON CONFLICT (id) DO NOTHING;
INSERT INTO genres (id, name) VALUES (8, 'Horreur') ON CONFLICT (id) DO NOTHING;
INSERT INTO genres (id, name) VALUES (9, 'Combat') ON CONFLICT (id) DO NOTHING;
INSERT INTO genres (id, name) VALUES (10, 'Simulation') ON CONFLICT (id) DO NOTHING;

-- RPG (Genre 1)
INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('4eb25636-f5a1-5bda-8d79-d248eb0a1158', 'The Elder Scrolls V: Skyrim', 'Une aventure épique dans la province glaciale de Bordeciel.', '2011-11-11', 19.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1x7p.png', 1) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('e5248119-55ee-5296-8a81-e54eff107e5d', 'Elden Ring', 'Un action-RPG colossal créé par Hidetaka Miyazaki et George R.R. Martin.', '2022-02-25', 59.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co4jni.png', 1) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('ad37a259-1cf0-5bdb-82af-c0b456a34f33', 'Baldur''s Gate 3', 'Un RPG nouvelle génération basé sur l''univers de Donjons & Dragons.', '2023-08-03', 59.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co670h.png', 1) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('fe16ca8d-c6ab-5353-82e2-73e2fea8b683', 'Persona 5 Royal', 'Vivez la double vie d''un lycéen voleur de cœurs à Tokyo.', '2020-03-31', 59.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1nic.png', 1) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('9d5cfdda-8f73-57a8-bcf0-92a4febaab91', 'Final Fantasy VII Remake', 'La réinvention époustouflante d''un classique intemporel.', '2020-04-10', 49.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1qxr.png', 1) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('fa98f000-2203-5f19-b665-3303e87cb5d0', 'Mass Effect Legendary Edition', 'Revivez la légende du Commandant Shepard.', '2021-05-14', 59.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co2l1q.png', 1) ON CONFLICT (id) DO NOTHING;

-- Action (Genre 2)
INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('44cc5a65-c5b6-52a0-b4c7-804e7f15f318', 'Sekiro: Shadows Die Twice', 'Frayez-vous un chemin sanglant dans le Japon de l''ère Sengoku.', '2019-03-22', 59.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1wz1.png', 2) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('262342a8-d89d-55be-81ce-8c472cde39de', 'Ghost of Tsushima', 'Incarnez un samouraï prêt à tout pour protéger son île.', '2020-07-17', 49.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co2k9u.png', 2) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('3fe1c2ee-5514-5fc7-8520-fb12687fbd70', 'Marvel''s Spider-Man Remastered', 'Balancez-vous de toile en toile dans un New York plus vrai que nature.', '2022-08-12', 59.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co50kk.png', 2) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('5cac42b0-7898-5a8d-bb97-42ac47b14dbb', 'Hades', 'Défiez le dieu des morts dans ce rogue-like encensé par la critique.', '2020-09-17', 24.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co2uur.png', 2) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('b6600fbb-f2cc-52bc-84ad-e54f2eca50f0', 'Devil May Cry 5', 'L''ultime chasseur de démons fait son grand retour avec style.', '2019-03-08', 29.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1qza.png', 2) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('1018f4a5-3f3c-5bc8-84ad-8685d724248e', 'Monster Hunter: World', 'Traquez des monstres gigantesques dans des écosystèmes vivants.', '2018-01-26', 29.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1x02.png', 2) ON CONFLICT (id) DO NOTHING;

-- Stratégie (Genre 3)
INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('3b8d0c45-31f1-530c-853b-ce96a6cdab7f', 'Age of Empires II: Definitive Edition', 'Le jeu de stratégie historique par excellence, remastérisé.', '2019-11-14', 19.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1yzr.png', 3) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('22391794-22c0-5dc3-b21a-ad8c5759cdd3', 'XCOM 2', 'Menez la résistance mondiale contre une occupation extraterrestre.', '2016-02-05', 49.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1x6e.png', 3) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('d1afb92b-fc7e-537d-a90b-ac3adf7b041c', 'Crusader Kings III', 'Bâtissez une dynastie médiévale à travers les siècles.', '2020-09-01', 49.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co2p2c.png', 3) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('690a48a1-0281-5edd-9606-e9f42e920a91', 'Stellaris', 'Explorez et colonisez une vaste galaxie générée de manière procédurale.', '2016-05-09', 39.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1s0n.png', 3) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('dea13064-1078-5b5c-a6c8-80de7337d684', 'Total War: WARHAMMER III', 'Rassemblez vos forces et plongez dans le Royaume du Chaos.', '2022-02-17', 59.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co2sz5.png', 3) ON CONFLICT (id) DO NOTHING;

-- Aventure (Genre 4)
INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('319dc482-3e1a-5f64-a080-9207b2d1d95a', 'The Last of Us Part I', 'Une histoire poignante de survie dans un monde post-apocalyptique.', '2022-09-02', 69.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co4xmm.png', 4) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('a13f4060-006b-5655-bbdc-31c08e669f3d', 'Outer Wilds', 'Un mystère en monde ouvert dans un système solaire piégé dans une boucle temporelle.', '2019-05-28', 24.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1qxu.png', 4) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('9fa55d88-d7fb-5507-97a4-1b475d4b2c32', 'Uncharted 4: A Thief''s End', 'La dernière aventure explosive du célèbre chasseur de trésors Nathan Drake.', '2016-05-10', 39.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1r7h.png', 4) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('af1827e4-f172-58ec-a76a-83310c2ffbbd', 'Assassin''s Creed Valhalla', 'Incarnez un Viking légendaire en quête de gloire.', '2020-11-10', 59.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co2eog.png', 4) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('479efcb7-dba3-5948-98a1-228b86ecdce8', 'A Plague Tale: Requiem', 'Fuyez à travers des paysages magnifiques ravagés par la peste.', '2022-10-18', 49.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co4xnz.png', 4) ON CONFLICT (id) DO NOTHING;

-- FPS (Genre 5)
INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('4968738d-aaf5-5480-abda-f0fa41ec69c0', 'Half-Life 2', 'Le jeu de tir mythique de Valve qui a redéfini le genre.', '2004-11-16', 9.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1z0x.png', 5) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('a68156b7-7e35-5f28-9419-760895265323', 'Halo Infinite', 'Le Master Chief est de retour dans sa plus vaste aventure à ce jour.', '2021-12-08', 59.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co38e9.png', 5) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('66539711-8f9a-5f19-bca5-a6ca5a1773c9', 'Titanfall 2', 'Des combats frénétiques entre pilotes et titans géants.', '2016-10-28', 19.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1r7y.png', 5) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('d90a95ab-be10-5471-9904-d3502f169aca', 'BioShock Infinite', 'Envolez-vous vers la majestueuse cité flottante de Columbia.', '2013-03-26', 29.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1vce.png', 5) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('c847507f-1ec1-560f-bccb-2b536e66cc98', 'Cyberpunk 2077: Phantom Liberty', 'Extension d''espionnage et de suspense pour Cyberpunk 2077.', '2023-09-26', 29.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co5p1d.png', 5) ON CONFLICT (id) DO NOTHING;

-- Plateforme (Genre 6)
INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('d648483a-f52b-5981-b12c-5aa7bc187963', 'Hollow Knight', 'Plongez dans les profondeurs d''un royaume insectoïde en ruine.', '2017-02-24', 14.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1r7q.png', 6) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('9a5d1c2d-ed75-52ad-9df9-714065c4bfad', 'Celeste', 'Aidez Madeline à survivre à ses démons intérieurs en gravissant le mont Celeste.', '2018-01-25', 19.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1tq9.png', 6) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('ff1fdc05-7fef-5dab-b277-884c68c260fc', 'Ori and the Will of the Wisps', 'Embarquez pour une aventure poétique aux graphismes somptueux.', '2020-03-11', 29.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1r7p.png', 6) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('f6191efd-a494-50a7-afab-821efe56be91', 'Super Mario Odyssey', 'Parcourez le monde avec Mario et son nouvel allié, Cappy.', '2017-10-27', 59.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1mxf.png', 6) ON CONFLICT (id) DO NOTHING;

-- Course (Genre 7)
INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('a242b874-071b-5f69-9843-e6fe1de91967', 'Forza Horizon 5', 'Explorez les paysages vibrants du Mexique à bord de voitures d''exception.', '2021-11-09', 59.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co3d2t.png', 7) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('e07000ff-ea17-5168-b5fc-729c8d49b274', 'Mario Kart 8 Deluxe', 'Le jeu de course culte de Nintendo, plus riche que jamais.', '2017-04-28', 59.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1trg.png', 7) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('ebe39100-0506-58ea-a12c-438a8f0ab955', 'Gran Turismo 7', 'Le simulateur de conduite de référence fait peau neuve.', '2022-03-04', 69.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co4jpx.png', 7) ON CONFLICT (id) DO NOTHING;

-- Horreur (Genre 8)
INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('d3ac0d14-ae2c-585f-87e3-0b9d89754295', 'Resident Evil 4', 'Un remake époustouflant du monument du survival-horror.', '2023-03-24', 59.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co5ycd.png', 8) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('73eff5c1-1f10-598b-81cf-51916689e503', 'Dead Space', 'Survivez à un cauchemar claustrophobe à bord de l''USG Ishimura.', '2023-01-27', 59.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co5z8n.png', 8) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('f5976a67-9e8a-594a-abc2-f8ed8c64915e', 'Alan Wake 2', 'Une histoire psychologique captivante aux frontières du réel.', '2023-10-27', 49.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co6q0h.png', 8) ON CONFLICT (id) DO NOTHING;

-- Combat (Genre 9)
INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('cd0e253c-23a8-5872-a395-277e34be630d', 'Street Fighter 6', 'Le roi du versus fighting revient avec un gameplay et un hub inédits.', '2023-06-02', 59.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co6m9f.png', 9) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('64379e5a-f455-5aa1-a3cd-19e1acb3256f', 'Super Smash Bros. Ultimate', 'Le casting le plus monumental de l''histoire de la série.', '2018-12-07', 59.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1pvg.png', 9) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('d8b687ea-edc1-50ab-ae90-16029c8ede2b', 'Tekken 8', 'Le nouveau chapitre de la légendaire saga de la famille Mishima.', '2024-01-26', 69.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co7d4c.png', 9) ON CONFLICT (id) DO NOTHING;

-- Simulation (Genre 10)
INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('266e5691-47e8-5e7a-808b-0f654207cf37', 'Microsoft Flight Simulator', 'Parcourez le globe depuis les cieux dans un niveau de détail inouï.', '2020-08-18', 59.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co2f8j.png', 10) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('ec17cce0-7502-51b8-8179-89f211063fb2', 'Les Sims 4', 'Créez et contrôlez des personnages dans un monde virtuel sans règles.', '2014-09-02', 0.00, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1tqu.png', 10) ON CONFLICT (id) DO NOTHING;

INSERT INTO games (id, title, description, release_date, price, image_url, genre_id) 
VALUES ('6ba2e3f8-12b1-5f85-a511-751f780d9fe5', 'Cities: Skylines', 'Construisez et gérez la métropole de vos rêves.', '2015-03-10', 29.99, 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1x91.png', 10) ON CONFLICT (id) DO NOTHING;
