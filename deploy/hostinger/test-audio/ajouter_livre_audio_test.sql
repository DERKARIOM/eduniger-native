-- Livre audio de test « Le cycle de l'eau » (2 chapitres) dans la structure 1.
-- Rejouable sans risque : rien n'est cree en double.
-- Les fichiers doivent etre dans ~/elim-api/storage/app/private/structures/1/{blankets,audios}/
SET NAMES utf8mb4;
START TRANSACTION;

-- Auteur « Equipe Elim » (cree une seule fois)
INSERT INTO Author (name, firstName, profession, biography)
SELECT 'Elim', 'Équipe', 'Équipe Elim', 'Contenus pédagogiques produits par l''équipe Elim.'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM Author WHERE name='Elim' AND firstName='Équipe');
SET @author := (SELECT idAuthor FROM Author WHERE name='Elim' AND firstName='Équipe' LIMIT 1);

-- Categorie : une categorie « sciences » si elle existe, sinon la premiere
SET @cat := COALESCE(
  (SELECT idCategory FROM Category WHERE title LIKE '%cienc%' ORDER BY idCategory LIMIT 1),
  (SELECT idCategory FROM Category WHERE title LIKE '%olog%'  ORDER BY idCategory LIMIT 1),
  (SELECT MIN(idCategory) FROM Category));

INSERT IGNORE INTO Book (idBook, title, description, idAuthor, blanket, electronic, isPhysic, isAudio, available, size, nbrPage)
VALUES ('ELIMA0001', 'Le cycle de l''eau',
        'Livre audio pédagogique en deux chapitres : l''évaporation, puis la condensation et les précipitations. Une introduction claire au voyage de l''eau, avec des exemples tirés du Niger.',
        @author, 'ELIMA0001_cover.jpg', NULL, 0, 1, 1, '1,1 Mo', NULL);

INSERT IGNORE INTO StructBook (idStruct, idBook, date) VALUES (1, 'ELIMA0001', NOW());
INSERT IGNORE INTO BookCategory (idBook, idCategory, date) VALUES ('ELIMA0001', @cat, NOW());

INSERT INTO Audio (idBook, audio, title, size, maxTime)
SELECT 'ELIMA0001', 'ELIMA0001_ch1.mp3', 'Chapitre 1 : L''évaporation', '0,5 Mo', '00:42'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM Audio WHERE audio='ELIMA0001_ch1.mp3');
INSERT INTO Audio (idBook, audio, title, size, maxTime)
SELECT 'ELIMA0001', 'ELIMA0001_ch2.mp3', 'Chapitre 2 : Condensation et précipitations', '0,6 Mo', '00:49'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM Audio WHERE audio='ELIMA0001_ch2.mp3');

COMMIT;

SELECT b.idBook, b.title, b.isAudio, b.blanket, a.name AS auteur, c.title AS categorie,
       (SELECT COUNT(*) FROM Audio WHERE idBook='ELIMA0001') AS pistes,
       (SELECT COUNT(*) FROM StructBook WHERE idBook='ELIMA0001' AND idStruct=1) AS dans_structure_1
FROM Book b JOIN Author a ON a.idAuthor=b.idAuthor
LEFT JOIN BookCategory bc ON bc.idBook=b.idBook LEFT JOIN Category c ON c.idCategory=bc.idCategory
WHERE b.idBook='ELIMA0001';
