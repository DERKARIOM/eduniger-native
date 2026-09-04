-- phpMyAdmin SQL Dump
-- version 5.1.1deb5ubuntu1
-- https://www.phpmyadmin.net/
--
-- Hôte : localhost:3306
-- Généré le : ven. 04 sep. 2026 à 11:34
-- Version du serveur : 8.0.43-0ubuntu0.22.04.2
-- Version de PHP : 8.1.2-1ubuntu2.22

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `eduniger`
--

-- --------------------------------------------------------

--
-- Structure de la table `Agent`
--

CREATE TABLE `Agent` (
  `idAgent` varchar(256) NOT NULL,
  `name` varchar(256) NOT NULL,
  `firstName` varchar(256) NOT NULL,
  `isAdmin` tinyint DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- Déchargement des données de la table `Agent`
--

INSERT INTO `Agent` (`idAgent`, `name`, `firstName`, `isAdmin`) VALUES
('100', 'Koffi', 'Ali', 1);

-- --------------------------------------------------------

--
-- Structure de la table `AgentAccount`
--

CREATE TABLE `AgentAccount` (
  `idAgentAccount` int NOT NULL,
  `idAgent` varchar(256) NOT NULL,
  `email` varchar(256) NOT NULL,
  `password` varchar(1000) NOT NULL,
  `profile` varchar(256) DEFAULT NULL,
  `isAdmin` tinyint DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- --------------------------------------------------------

--
-- Structure de la table `Audio`
--

CREATE TABLE `Audio` (
  `idAudio` int NOT NULL,
  `idBook` varchar(256) DEFAULT NULL,
  `audio` varchar(256) NOT NULL,
  `title` varchar(256) NOT NULL,
  `size` varchar(1000) DEFAULT NULL,
  `maxTime` varchar(1000) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- Déchargement des données de la table `Audio`
--

INSERT INTO `Audio` (`idAudio`, `idBook`, `audio`, `title`, `size`, `maxTime`, `created_at`, `updated_at`) VALUES
(23, 'OPEN0002', 'OPEN0002.mp3', 'OPEN0002', '4,6', '19:31', '2025-06-17 09:20:52', '2025-06-17 09:20:52'),
(24, 'HHA0001', 'HHA0001.mp3', 'HHA0001.mp3', '4,6 Mo', '19:31', '2025-06-17 09:20:52', '2025-06-17 09:20:52'),
(25, 'HHA0002', 'HHA0002.mp3', 'HHA0002.mp3', '19,9 Mo', '30,21', '2025-06-17 09:20:52', '2025-06-17 09:20:52'),
(26, 'HHA0003', 'HHA0003.mp3', 'HHA0003.mp3', '4,6 Mo', '19:31', '2025-06-17 09:20:52', '2025-06-17 09:20:52'),
(27, 'HHA0004', 'HHA0004.mp3', 'HHA0004.mp3', '4,6 Mo', '19:31', '2025-06-17 09:20:52', '2025-06-17 09:20:52'),
(28, 'HHA0004', 'HHA0004.mp3', 'HHA0004.mp3', '4,6 Mo', '19:30', '2025-06-17 09:20:52', '2025-06-17 09:20:52'),
(29, 'HHA0006', 'HHA0006.mp3', 'HHA0006', '6,7 Mo', '26:31', '2025-06-17 09:20:52', '2025-06-17 09:20:52'),
(30, 'HHA0005', 'HHA0005.mp3', 'HHA0005', '4,4', '14:30', '2025-06-17 09:20:52', '2025-06-17 09:20:52'),
(31, 'OPEN0003', 'OPEN0003.mp3', 'OPEN0003', '22 Mo', '24:02', '2025-06-17 09:20:52', '2025-06-17 09:20:52'),
(32, 'OPEN0004', 'OPEN0004.mp3', 'OPEN0004', '13 Mo', '14:41', '2025-06-17 09:20:52', '2025-06-17 09:20:52'),
(33, 'OPEN0005', 'OPEN0005.mp3', 'OPEN0005', '13 Mo', '14:41', '2025-06-17 09:20:52', '2025-06-17 09:20:52'),
(34, 'OPEN0007', 'OPEN0007.mp3', 'OPEN0007', '21,6 Mo', '22:38', '2025-06-17 09:20:52', '2025-06-17 09:20:52'),
(35, 'OPEN0008', 'OPEN0008.mp3', 'OPEN0008', '8,6 Mo', '09:14', '2025-06-17 09:20:52', '2025-06-17 09:20:52'),
(36, 'OPEN0010', 'OPEN0010.mp3', 'OPEN0010', '7,6 Mo', '08:19', '2025-06-17 09:20:52', '2025-06-17 09:20:52'),
(37, 'OPEN0011', 'OPEN0011.mp3', 'OPEN0011', '5,8 Mo', '06:20', '2025-06-17 09:20:52', '2025-06-17 09:20:52'),
(38, 'OPEN0015', 'OPEN0015.mp3', 'OPEN0015', '8,6 Mo', '09:25', '2025-06-17 09:20:52', '2025-06-17 09:20:52'),
(39, 'OPEN0016', 'OPEN0016.mp3', 'OPEN0016', '12 Mo', '12:39', '2025-06-17 09:20:52', '2025-06-17 09:20:52'),
(40, 'af7c2712-a258-4f82-88a4-7efda52c0e42', 'af7c2712-a258-4f82-88a4-7efda52c0e42_271c21aa-8582-41fc-b027-63041f962fce.mp3', 'Chapitre 1 - Introduction', '2.85MB', NULL, '2025-06-17 09:21:00', '2025-06-17 09:21:00'),
(41, '2cedd6ca-8b7e-4916-a335-6ff2b454fa0c', '2cedd6ca-8b7e-4916-a335-6ff2b454fa0c_a8408d39-b5aa-42d8-baec-f87d440b583d.mp3', 'Chapitre 1 - Introduction', '2.85MB', NULL, '2025-06-17 09:24:13', '2025-06-17 09:24:13'),
(42, 'CATI2202', 'CATI2202_4a48723d-a0ad-4736-8e53-a1ac2c81efc1.mp3', 'Chapitre 1 - Introduction', '2.85MB', NULL, '2025-06-17 09:24:42', '2025-06-17 09:24:42'),
(43, 'CATI2X221', 'CATI2X221_6208b65b-652d-4aee-917c-ce9330eb326a.mp3', '6-Shooter.mp3', '2584 KB', NULL, '2025-06-17 13:56:50', '2025-06-17 13:56:50'),
(44, 'AUDIOB', 'AUDIOB_735aff09-a23e-4ae8-89c4-bb2e25cb76b8.mp3', 'Judith_Gautier_-_Par_la_beauté.mp3', NULL, NULL, '2025-06-25 23:17:15', '2025-06-25 23:17:15'),
(45, '8176c824-176d-4fe7-afc1-bdad011a9880', '8176c824-176d-4fe7-afc1-bdad011a9880_3ca4a389-0e23-4c1b-a057-2b6ef1fb8565.mp3', 'Judith_Gautier_-_Par_la_beauté.mp3', NULL, NULL, '2025-10-13 09:20:01', '2025-10-13 09:20:01'),
(46, 'LIV2026TEST', 'LIV2026TEST_9c516773-7572-4425-92b0-7c41839d5bb5.mp3', 'Judith_Gautier_-_Par_la_beauté.mp3', NULL, NULL, '2026-03-14 16:37:05', '2026-03-14 16:37:05'),
(47, 'AUD1212', 'AUD1212_d2ae5103-6ddd-4176-b54f-f486262f8d25.mp3', 'Judith_Gautier_-_Par_la_beauté.mp3', NULL, NULL, '2026-03-24 12:56:01', '2026-03-24 12:56:01'),
(54, 'OPEN0001', 'OPEN0001_95d739ba-c5a8-43b2-bb13-b8be6822c360.mp3', 'OPEN0001.mp3', '17.5 Mo', '19', '2026-05-12 07:04:26', '2026-05-12 09:10:25'),
(56, 'HA0021', 'HA0021_e749d44e-05ad-4a73-8389-62d1daa72800.mp3', '4_Choses_Essentielles_à_Ne_Jamais_Forcer_dans_la_Vie_!_[_motivation_](256k).mp3', NULL, NULL, '2026-05-21 07:24:32', '2026-05-21 07:24:32');

-- --------------------------------------------------------

--
-- Structure de la table `Author`
--

CREATE TABLE `Author` (
  `idAuthor` int NOT NULL,
  `name` varchar(256) NOT NULL,
  `firstName` varchar(256) NOT NULL,
  `profile` varchar(256) DEFAULT NULL,
  `level` tinyint DEFAULT '0',
  `profession` varchar(1000) DEFAULT 'AUTEUR EDUNIGER',
  `biography` text,
  `call` varchar(1000) DEFAULT NULL,
  `email` varchar(1000) DEFAULT NULL,
  `whatsapp` varchar(1000) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- Déchargement des données de la table `Author`
--

INSERT INTO `Author` (`idAuthor`, `name`, `firstName`, `profile`, `level`, `profession`, `biography`, `call`, `email`, `whatsapp`) VALUES
(100, 'AUTEUR', 'AUTEUR', 'user.png', 0, 'AUTEUR EDUNIGER', NULL, NULL, NULL, NULL),
(108, 'Abdoul Kader', 'Bachir', 'derkariom.png', 2, 'CO-FONDATEUR NINOTECH', NULL, NULL, NULL, NULL),
(109, 'Ridouane', 'Aboubacar Hamidou', '109.png', 1, 'CO-FONDATEUR NINOTECH', NULL, NULL, NULL, NULL),
(110, 'Illa Yacouba', 'Moubarak', 'mouba.png', 1, 'CO-FONDATEUR NINOTECH', NULL, NULL, NULL, NULL),
(112, 'Alazi', 'Issoufou Dodo', '112.png', 1, 'CO-FONDATEUR NINOTECH', NULL, NULL, NULL, NULL),
(113, 'Oumarou', 'Younoussa Dillé', 'dille.png', 1, 'CO-FONDATEUR NINOTECH', NULL, NULL, NULL, NULL),
(114, 'Abdoul Rachid', 'Hachimou Lassan', 'rachid.png', 1, 'CO-FONDATEUR NINOTECH', NULL, NULL, NULL, NULL),
(115, 'Abdou Moumouni', 'Dioffo', 'user.png', 0, 'AUTEUR EDUNIGER', NULL, NULL, NULL, NULL),
(116, 'Aboubacar Arafat', 'Chaibou', '116.png', 2, 'AUTEUR EDUNIGER', NULL, NULL, NULL, NULL),
(119, 'Idi Aboubakar', 'Abrahams', 'a19.png', 3, 'AUTEUR NIGÉRIEN', NULL, NULL, NULL, NULL),
(120, 'Abdoul Madjid', 'Illa', 'author_ee6d33a6-47b9-4aea-b703-e77d0c56c6e6.jpg', 0, 'Auteur', NULL, NULL, NULL, NULL),
(121, 'NINOTECH', 'xCode', 'author_8bb89466-9abb-4e9a-ac98-e2bec5b70264.png', 0, 'Auteur', NULL, NULL, NULL, NULL),
(122, 'Abdelkader', 'ALIO SANDA', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(123, 'Abdelkader', 'ALIO SANDA', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(124, 'Laouali', 'ADAMOU IBRAHIM Maman', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(125, 'ISSOUFOU', 'Djibrillou MOUSSA', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(126, 'Oumarou', 'Halilou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(127, 'Kolafane', 'ABOUBACAR ADAMOU', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(128, 'Abdelkader', 'ALIO SANDA', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(129, 'Abdoul kader', 'Maman Yarodji', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(130, 'YANOUSSA MAMANE', 'Bassirou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(131, 'SOUMANA SIDDO', 'ABDOUL KADRI', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(132, 'ALI', 'ARZIKA', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(133, 'OUSMANE', 'Habsatou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(134, 'ADAMOU MAHAMAN', 'Samaila', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(135, 'Djibo', 'Moustapha', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(136, 'Dodo', 'Natatou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(137, 'HAMADOU', 'Adamou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(138, 'Hassane', 'Abba Mallam', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(139, 'YOUNSA  KOTONDI', 'Halimatou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(140, 'BARAOU IDI', 'Souley', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(141, 'Jagaya', 'Yaou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(142, 'Boubacar', 'MOUNDIO', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(143, 'Hassane Ganda', 'Rabi', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(144, 'Baraou Idi', 'Souley', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(145, 'Abdelkader', 'ALIO SANDA', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(146, 'ALI', 'Mahamane Saminou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(147, 'SEYDOU', 'Moussa', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(148, 'OUSMANE', 'TOUDOU ISSA', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(149, 'Yahaya Alassane', 'Maman Nouri', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(150, 'Zakari', 'Yaou MOUSSA', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(151, 'Oumarou', 'Abba Mahaman', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(152, 'Ibrahim', 'Ganaou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(153, 'Dan-Djari', 'Finale', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(154, 'Ibrahim', 'Ganaou Noura', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(155, 'Yahaya Alassane', 'M. Nouri', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(156, 'Boudah Maidjakaye', 'Mahamane Moustapha', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(157, 'Abdou Ali', 'Ibrahim', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(158, 'Maman Laouali', 'ADAMOU IBRAHIM', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(159, 'Djibrillou', 'MOUSSA ISSOUFOU', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(160, 'Oumarou', 'Halilou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(161, 'Sani', 'Abdoulwahid', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(162, 'ALIOU MAHAMIDOU', 'Mach’houdou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(163, 'Kolafane', 'ABOUBACAR ADAMOU', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(164, 'Issifou Fatiou', 'Adiss Kamal', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(165, 'ABASS SALEY', 'Abdoulatif', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(166, 'Ibrahim Maharou', 'Hassan', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(167, 'Laouali Idi', 'Karimou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(168, 'ALZOUMA AMADOU', 'Diafarou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(169, 'IBRAHIM SARKI', 'LAOUALI', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(170, 'Lawrence Nyarko', 'Fletcher', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(171, 'SOUMANA SIDDO', 'ABDOUL KADRI', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(172, 'Laminou', 'Saïdou Amani', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(173, 'Abdou Boko', 'Boubacar', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(174, 'IBRAHIM MAHAROU', 'Hassan', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(175, 'Par YACOUBA', 'Abouhourairata', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(176, 'Ibrahim', 'HAMIDOU HAROUNA', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(177, 'Abdou Ali', 'Ibrahim', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(178, 'Ibrahim', 'ABDOU ZAKARY YAOU', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(179, 'ADAMOU MAHAMAN', 'Samaila', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(180, 'BOUBACAR Abdou', 'Fati', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(181, 'Saley', 'MOUSSA DIAGARA', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(182, 'Hamadou Younoussa', 'Bachirou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(183, 'SANI', 'Abdoulwahid', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(184, 'SAHABI', 'Issoufou Abdoulkarim', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(185, 'Abdoul Matsalabi', 'ISSA SAADOU', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(186, 'Bachir', 'MIJITABA SAHIROU', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(187, 'SANI GIGO', 'Omar Farouk', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(188, 'Idrissa', 'Moussa', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(189, 'LAOUALI IDI', 'Karimou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(190, 'Maman Nafiou', 'AMINOU ILLIA', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(191, 'Dan', 'Djari', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(192, 'Aida Sylviane', 'FRANCOIS COMLAN', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(193, 'MOUSSA  ALI', 'Ismaël', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(194, 'Habibou', 'MOUMOUNI ADAMOU', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(195, 'Sala Harouna', 'Yanoussa', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(196, 'ABDOURAHAMANE', 'Abdoulmananou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(197, 'Idrissa GARBA HAMIDOU', 'HAMIDOU', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(198, 'Habiboulla', 'ALMOU', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(199, 'Ango Namata', 'Habsatou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(200, 'MADJIRI OUSSEINI', 'SOULEYMANE', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(201, 'ISSOUFOU MOUSSA Moustapha', 'ISSOUFOU MOUSSA', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(202, 'AMADOU KIOUSSO', 'OUMAROU', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(203, 'Illias', 'Alhassane', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(204, 'MOUSSA Ousmane Nayaya  Abdoul Moumouni', 'Abdoul Moumouni', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(205, 'Nassirou', 'SABIOU SANI', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(206, 'Saidou Garba', 'Inaytoulaye', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(207, 'GAMBO AMADOU', 'ABDOUL', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(208, 'Zakari Yaou', 'SOULEYMANE DIDIGUE', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(209, 'Ibrahim Kamayé', 'Yassin', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(210, 'Issoufou', 'Maigary', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(211, 'ALI TIMBO', 'MAMADOU ALMAMY', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(212, 'Roufaï', 'HAROUNA Liman Salifou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(213, 'Boukari Ousmane', 'Mahamadou Lamine', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(214, 'Issa Malam', 'Salmanou Souleymane', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(215, 'HOUSSEINI MATHIO', 'MAHAMAN LAWALI', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(216, 'Mahaman Manzo', 'Nana Saratou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(217, 'YACOUBA ISSOUFOU', 'Zabeirou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(218, 'Abdou Dodo', 'Bohari', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(219, 'HABOU ABDOU', 'IDRISSA', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(220, 'Laouali Idi', 'Karimou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(221, 'Boukari Gori Ousmane', 'Mahaman Siradji', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(222, 'Rachide', 'IBRAHIM NAFATCHE', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(223, 'ISSAKA ASSANE', 'Aminatou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(224, 'Zeinabou', 'HASSANE HAMIDOU', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(225, 'BADAMASSI KADRI', 'Mahaman Mansour', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(226, 'Mariama', 'ABDOULAYE MALAM BOUKAR', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(227, 'ISSA ASSOUMANE', 'Abdou', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(228, 'Abdoul Aziz', 'Moussa', 'user.png', 0, NULL, NULL, NULL, NULL, NULL),
(229, 'Hakim', 'Abdoul', 'user.png', 0, 'Écrivain', NULL, NULL, NULL, NULL),
(230, 'Hakim', 'Abdoul', 'user.png', 0, 'Écrivain', NULL, NULL, NULL, NULL),
(231, 'Hakim', 'Abdoul', 'user.png', 0, 'Écrivain', NULL, NULL, NULL, NULL),
(232, 'Hakim', 'Abdoul', 'user.png', 0, 'Écrivain', NULL, NULL, NULL, NULL),
(233, 'test', 'auteur', 'user.png', 0, 'Écrivain', NULL, NULL, NULL, NULL),
(234, '', 'autre', 'user.png', 0, 'Écrivain', NULL, NULL, NULL, NULL),
(235, 'Gautier', 'Judith', 'user.png', 0, 'Écrivain', NULL, NULL, NULL, NULL),
(236, '', 'issu', 'user.png', 0, 'Écrivain', NULL, NULL, NULL, NULL),
(237, '', 'QBD', 'user.png', 0, 'Écrivain', NULL, NULL, NULL, NULL),
(238, '', 'Moustaa', 'user.png', 0, 'Écrivain', NULL, NULL, NULL, NULL),
(239, '', 'SANIII', 'user.png', 0, 'Écrivain', NULL, NULL, NULL, NULL),
(240, '', 'tests', 'author_e5a127a8-6367-4fa8-9973-6c85b408217b.png', 0, NULL, NULL, NULL, NULL, NULL),
(241, 'Mas', 'Idi', 'author_d28457ed-01ad-4cd3-bdc8-a084ecc30338.jpg', 0, NULL, NULL, NULL, NULL, NULL),
(242, 'stock', 'Test', 'author_d9476e37-1633-4cb7-8e77-f9708fe1e121.png', 1, 'Developer', NULL, NULL, NULL, NULL),
(243, 'Point (I) Pvt. Ltd.', 'Tutorials', 'user.png', 0, 'Écrivain', NULL, NULL, NULL, NULL),
(244, 'Foka', 'Alain', 'user.png', 0, 'Écrivain', NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `Book`
--

CREATE TABLE `Book` (
  `idBook` varchar(256) NOT NULL,
  `title` varchar(256) NOT NULL,
  `description` text,
  `idAuthor` int NOT NULL,
  `blanket` varchar(1000) NOT NULL,
  `electronic` varchar(1000) DEFAULT NULL,
  `isPhysic` tinyint DEFAULT '0',
  `isAudio` tinyint DEFAULT '0',
  `available` int DEFAULT '0',
  `numberLike` int DEFAULT '0',
  `numberNoLike` int DEFAULT '0',
  `numberView` int DEFAULT '0',
  `numberComment` int DEFAULT '0',
  `numberSubscribe` int DEFAULT '0',
  `size` varchar(1000) DEFAULT NULL,
  `nbrPage` varchar(1000) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- Déchargement des données de la table `Book`
--

INSERT INTO `Book` (`idBook`, `title`, `description`, `idAuthor`, `blanket`, `electronic`, `isPhysic`, `isAudio`, `available`, `numberLike`, `numberNoLike`, `numberView`, `numberComment`, `numberSubscribe`, `size`, `nbrPage`, `created_at`, `updated_at`) VALUES
('HA0021', 'Aimé Césaire', 'Aimé_Césaire', 244, 'HA0021_1779355472.png', NULL, 0, 1, 1, 0, 0, 3, 0, 0, NULL, '1', '2026-05-21 07:24:32', '2026-07-01 10:55:11'),
('OPEN0001', 'Histoire des Haoussa', 'Histoire des Haoussa est un ouvrage retraçant l\'origine, l\'évolution et linfluence du peuple haoussa en Afrique de l\'Ouest. il explore leur organisation sociale, politique et économique, ainsi que leur rôle dans le commerce transsaharien et la diffusion de l\'islam. Ce livre est une ressource précieuse pour comprendre l\'identité et l\'héritage culturel des Haoussa.', 108, 'OPEN0001_1778576666.png', NULL, 0, 1, 1, 1, 0, 3, 0, 0, NULL, '1', '2026-05-12 07:04:26', '2026-05-17 15:42:36'),
('OPEN0006', 'Résumé des grandes œuvres littéraires', 'Résumé des grandes œuvres littéraires\r\nDécouvrez l\'essentiel des plus grandes œuvres à travers des résumés clairs et rapides, pour enrichir votre culture littéraire en toute simplicité.', 109, 'OPEN0006_1774435093.png', 'OPEN0006_1774435093.pdf', 1, 0, 4, 0, 0, 2, 0, 1, '0.79', '34', '2026-03-25 09:38:13', '2026-05-05 11:43:33'),
('OPEN0012', 'Épreuves Eamac Technicien Mathématiques 2011-2024', 'Ce recueil regroupe l’ensemble des sujets d’épreuves de mathématiques du concours EAMAC pour les techniciens de 2011 à 2024, une ressource incontournable pour s’entraîner, comprendre les attentes du jury et maximiser ses chances de réussite.', 109, 'OPEN0012_1774435550.jpg', 'OPEN0012_1774435550.pdf', 0, 0, 1, 0, 0, 1, 0, 0, '5.3', '21', '2026-03-25 09:45:50', '2026-04-16 08:44:05'),
('OPEN0013', 'Maths et vie réelle', 'Ce livre établit un lien concret entre les mathématiques et la vie quotidienne. À travers des exemples pratiques et des situations réelles, il montre comment les notions mathématiques s’appliquent à notre environnement, facilitant ainsi la compréhension et l’intérêt pour cette discipline.', 110, 'OPEN0013_1774438746.jpg', 'OPEN0013_1774438746.pdf', 1, 0, 10, 0, 0, 2, 0, 0, '0.89', '8', '2026-03-25 10:39:06', '2026-05-04 09:35:46'),
('OPEN0014', 'Maths et Vie Réelle - Volume II', 'À travers ce volume, découvrez comment la fonction  intervient dans différents domaines de la vie réelle. L’ouvrage explique de manière simple et accessible des situations concrètes où cette fonction est utilisée, sans exercices, pour faciliter la compréhension théorique.', 110, 'OPEN0014_1774871923.jpg', 'OPEN0014_1774871923.pdf', 1, 0, 1, 0, 0, 3, 0, 0, '3.26', '11', '2026-03-30 09:58:43', '2026-05-14 07:49:03'),
('OPEN0017', 'Argent sous contrôle', 'Ce livre est un guide essentiel pour toute personne souhaitant reprendre le contrôle de ses finances. À travers une approche simple et accessible, il vous apprend à établir un budget clair, à suivre vos dépenses, à économiser intelligemment et à atteindre vos objectifs financiers. Que vous soyez étudiant, salarié ou entrepreneur, ce livre vous donne les clés pour mieux gérer votre argent au quotidien, éviter le surendettement et bâtir une stabilité financière durable. Grâce à des exemples concrets, des conseils pratiques et des outils faciles à utiliser, la budgétisation deviendra une habitude qui transformera votre vie.', 108, 'OPEN0017_1774887188.jpg', 'OPEN0017_1774887188.pdf', 0, 0, 1, 0, 0, 1, 0, 0, '2.62', '25', '2026-03-30 14:13:08', '2026-05-07 10:13:22'),
('OPEN0021', 'OPEN0021', 'Un guide pratique pour apprendre à poser ses limites avec assurance et bienveillance. Ce livre vous aide à dire non sans culpabiliser, afin de préserver votre temps, votre énergie et votre bien-être.', 108, 'OPEN0021_1774887425.jpg', 'OPEN0021_1774887425.pdf', 0, 0, 1, 0, 0, 2, 0, 0, '1.15', '18', '2026-03-30 14:17:05', '2026-05-04 17:03:12'),
('OPEN0022', 'Le temps sous contrôle', 'Ce guide essentiel qui vous aide à dompter votre emploi du temps, à fixer vos priorités et à transformer vos journées en opportunités productives, que vous soyez étudiant, professionnel ou entrepreneur.', 108, 'OPEN0022_1774887601.jpg', 'OPEN0022_1774887601.pdf', 0, 0, 1, 0, 1, 2, 0, 1, '2.22', '32', '2026-03-30 14:20:01', '2026-05-04 08:56:19'),
('OPEN0023', 'Test', 'Ce livre.....', 100, 'OPEN0023_1775044710.jpg', 'OPEN0023_1775044710.pdf', 1, 0, 0, 2, 0, 3, 0, 1, '2.05', '38', '2026-04-01 09:58:30', '2026-06-04 10:48:15'),
('OPEN0024', 'Les Gardiens de l’Équilibre', 'Et si chaque page que vous lisez pouvait changer votre regard sur le monde ?\r\nDans cette œuvre captivante, Mahamadou Saminou nous embarque dans un voyage unique, à la croisée de la fiction, de l’engagement et de l’amour pour la Terre. À travers une aventure palpitante, des personnages profondément humains et une plume poétique, ce roman donne vie à une mission universelle : sauver notre planète.', 146, 'OPEN0024_1774893384.png', 'OPEN0024_1774893384.pdf', 1, 0, 0, 1, 0, 2, 0, 2, '2.72', '53', '2026-03-30 15:56:24', '2026-06-04 11:09:52'),
('OPEN0025', 'Veilleurs contre la misère du sahel', 'Dans les dunes du Sahel, l\'allika, gardienne du désert, affronte la misère en révélant des secrets enfouis. Un roman poignant où traditions et modernité s\'affrontent, porté par une héroïne déchirée entre son devoir et ses rêves. Une ode vibrante à la résilience sahélienne.', 146, 'OPEN0025_1774893517.png', 'OPEN0025_1774893517.pdf', 0, 0, 1, 1, 1, 2, 0, 1, '2.27', '53', '2026-03-30 15:58:37', '2026-05-07 13:56:17'),
('OPEN0027', 'L\'informatique : La voie express vers l\'indépendance', 'Ce livre s’adresse particulièrement aux jeunes bacheliers nigériens qui, à la sortie du bac, se trouvent confrontés à des choix d’orientation souvent difficiles et à une méconnaissance des opportunités offertes par l’informatique. Il vise à clarifier que l’informatique dépasse largement le cadre de la bureautique et qu’elle constitue un levier essentiel pour l’indépendance professionnelle et personnelle.\r\n\r\nÀ travers ce témoignage et ces réflexions, ce livre souhaite offrir un regard concret et motivant sur le chemin possible à emprunter pour développer des compétences numériques solides, s’ouvrir à un univers d’innovations et s’inscrire dans la dynamique d’une économie numérique croissante.', 109, 'OPEN0027_1774893804.jpg', 'OPEN0027_1774893804.pdf', 1, 0, 0, 1, 0, 3, 0, 0, '1.42', '22', '2026-03-30 16:03:24', '2026-05-14 07:48:42'),
('TD0001', 'BAC D Maths 2010-2024 Niger', 'Ce livre est un recueil des épreuves de mathématiques du Baccalauréat série D au Niger, de 2010 à 2024. Ce une ressource précieuse pour les candidats souhaitant s\'entraîner efficacement et se familiariser avec les types d\'exercices proposés aux examens.', 108, 'TD0001_1777391201.jpg', 'TD0001_1777391201.pdf', 0, 0, 1, 0, 0, 1, 0, 0, '6', '33', '2026-04-28 13:46:41', '2026-04-28 15:59:38'),
('TD0002', 'BAC D PC Niger 2010-2024', 'BAC D PC Niger 2010-2024 est un recueil des sujets et corrigés des épreuves de Physique-Chime du Baccalauréat série D au Niger, de 2010 à 2024. Ce livre est une ressource précieuse pour les candidats souhaitant s\'entraîner efficacement et se familiariser avec les types d\'exercices proposés aux examens.', 108, 'TD0002_1777391726.jpg', 'TD0002_1777391726.pdf', 0, 0, 1, 0, 0, 1, 0, 0, '2.66', '18', '2026-04-28 13:55:26', '2026-05-15 16:05:17'),
('TD0003', 'BAC D SVT Niger 2010-2024', 'BAC D Maths Niger 2010-2024 est un recueil des sujets et corrigés des épreuves de SVT du Baccalauréat série D au Niger, de 2010 à 2024. Ce livre est une ressource précieuse pour les candidats souhaitant s\'entraîner efficacement et se familiariser avec les types d\'exercices proposés aux examens.', 112, 'TD0003_1777392047.jpg', 'TD0003_1777392047.pdf', 0, 0, 1, 0, 0, 2, 0, 0, '18.27', '37', '2026-04-28 14:00:47', '2026-05-16 09:45:10'),
('TD0004', 'BAC D,C et E Anglais Niger 2010-2024', 'Ce recueil regroupe l\'ensemble des épreuves d\'anglais du Baccalauréat des séries D,C ET E au Niger, de 2010 à 2024. Il constitue une ressource précieuse pour les Candidats et enseignants souhaitant se familiariser avec les types d\'exercices proposés, renforcer leur préparation et maximiser leurs chances de réussite.', 109, 'TD0004_1777392369.jpg', 'TD0004_1777392369.pdf', 0, 0, 1, 0, 0, 1, 0, 0, '1.66', '25', '2026-04-28 14:06:09', '2026-04-28 16:07:05'),
('TEST0001', 'SVT Troisiéme', NULL, 109, 'TEST0001_1774787725.png', NULL, 1, 0, 3, 0, 0, 1, 0, 0, NULL, '1', '2026-03-29 10:35:25', '2026-04-19 09:45:51');

-- --------------------------------------------------------

--
-- Structure de la table `BookCategory`
--

CREATE TABLE `BookCategory` (
  `idBook` varchar(256) NOT NULL,
  `idCategory` int NOT NULL,
  `date` datetime NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- Déchargement des données de la table `BookCategory`
--

INSERT INTO `BookCategory` (`idBook`, `idCategory`, `date`, `created_at`, `updated_at`) VALUES
('HA0021', 8, '2026-05-21 09:24:32', '2026-05-21 07:24:32', '2026-05-21 07:24:32'),
('OPEN0001', 9, '2026-05-12 09:04:26', '2026-05-12 07:04:26', '2026-05-12 07:04:26'),
('OPEN0006', 9, '2026-04-19 11:43:17', '2026-04-19 09:43:17', '2026-04-19 09:43:17'),
('OPEN0012', 1, '2026-03-25 10:45:50', '2026-03-25 09:45:50', '2026-03-25 09:45:50'),
('OPEN0013', 1, '2026-04-29 09:50:33', '2026-04-29 07:50:33', '2026-04-29 07:50:33'),
('OPEN0014', 1, '2026-04-13 18:03:23', '2026-04-13 16:03:23', '2026-04-13 16:03:23'),
('OPEN0017', 8, '2026-03-30 16:13:08', '2026-03-30 14:13:08', '2026-03-30 14:13:08'),
('OPEN0021', 8, '2026-03-30 16:17:05', '2026-03-30 14:17:05', '2026-03-30 14:17:05'),
('OPEN0022', 8, '2026-03-30 16:20:01', '2026-03-30 14:20:01', '2026-03-30 14:20:01'),
('OPEN0023', 8, '2026-04-06 17:13:20', '2026-04-06 15:13:20', '2026-04-06 15:13:20'),
('OPEN0024', 8, '2026-03-31 09:34:36', '2026-03-31 07:34:36', '2026-03-31 07:34:36'),
('OPEN0024', 9, '2026-03-31 09:34:36', '2026-03-31 07:34:36', '2026-03-31 07:34:36'),
('OPEN0025', 9, '2026-03-31 08:01:11', '2026-03-31 06:01:11', '2026-03-31 06:01:11'),
('OPEN0027', 2, '2026-04-06 09:47:37', '2026-04-06 07:47:37', '2026-04-06 07:47:37'),
('TD0001', 1, '2026-04-28 15:46:41', '2026-04-28 13:46:41', '2026-04-28 13:46:41'),
('TD0002', 3, '2026-04-28 15:55:26', '2026-04-28 13:55:26', '2026-04-28 13:55:26'),
('TD0002', 6, '2026-04-28 15:55:26', '2026-04-28 13:55:26', '2026-04-28 13:55:26'),
('TD0003', 5, '2026-04-28 16:00:47', '2026-04-28 14:00:47', '2026-04-28 14:00:47'),
('TD0004', 10, '2026-04-28 16:06:09', '2026-04-28 14:06:09', '2026-04-28 14:06:09'),
('TEST0001', 5, '2026-03-29 12:35:25', '2026-03-29 10:35:25', '2026-03-29 10:35:25');

-- --------------------------------------------------------

--
-- Structure de la table `cache`
--

CREATE TABLE `cache` (
  `key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `value` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `expiration` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `cache_locks`
--

CREATE TABLE `cache_locks` (
  `key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `owner` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `expiration` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `Category`
--

CREATE TABLE `Category` (
  `idCategory` int NOT NULL,
  `title` varchar(256) DEFAULT NULL,
  `blanket` text NOT NULL,
  `date` datetime NOT NULL,
  `numberSubscribe` int DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- Déchargement des données de la table `Category`
--

INSERT INTO `Category` (`idCategory`, `title`, `blanket`, `date`, `numberSubscribe`) VALUES
(1, 'Mathématique ', 'mathematique.png', '2024-05-13 14:01:23', 0),
(2, 'Informatique', 'informatique.png', '2024-05-13 18:57:04', 0),
(3, 'Physique', 'physique.png', '2024-05-22 18:01:28', 0),
(4, 'Biochimie', 'biochimie.png', '2024-05-23 19:12:48', 0),
(5, 'Biologie', 'biologie.png', '2024-05-23 19:13:26', 0),
(6, 'Chimie', 'chimie.png', '2024-05-24 08:35:19', 0),
(7, 'Geologie', 'geologie.png', '2024-05-24 08:38:17', 0),
(8, 'Développement personnel', 'entrepreneuriat.png', '2024-06-02 14:41:47', 0),
(9, 'Histoire et Culture', 'culture.png', '2025-01-29 23:21:09', 0),
(10, 'Littérature', 'litta.png', '2025-04-13 13:08:07', 0);

--
-- Déclencheurs `Category`
--
DELIMITER $$
CREATE TRIGGER `AFTER_INSERT_CATEGORY` AFTER INSERT ON `Category` FOR EACH ROW BEGIN
    DECLARE tmp VARCHAR(256);
    DECLARE student VARCHAR(256);
    DECLARE category INT;
    SET category = NEW.idCategory;

    SELECT idNumber INTO student FROM SubscribeCategory WHERE idCategory=category ORDER BY idNumber DESC LIMIT 1;
    REPEAT
        INSERT INTO `Notification` VALUES (NULL,student,NOW(),"Un nouveau livre de cette catgorie est mainteant disponiple.",0,4,category,0);
        SET tmp=student;
        SET student=NULL;
        SELECT idNumber INTO student FROM SubscribeCategory WHERE idCategory=category AND idNumber<tmp ORDER BY idNumber DESC LIMIT 1;
        UNTIL student IS NULL
    END REPEAT;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `EdunaConversation`
--

CREATE TABLE `EdunaConversation` (
  `idConversation` int NOT NULL,
  `idUser` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_message` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `assistant_response` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `book_context` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `EdunaConversation`
--

INSERT INTO `EdunaConversation` (`idConversation`, `idUser`, `session_id`, `user_message`, `assistant_response`, `book_context`, `created_at`) VALUES
(1, '94961793', '94961793_6a01b7be95c439.06530194', 'bonjour', 'Bonjour ! Je suis Eduna, l\'assistant intelligent d\'EduNiger. Comment puis-je vous aider aujourd\'hui ? 😊', NULL, '2026-05-12 13:50:13'),
(2, '94961793', '94961793_6a01b7be95c439.06530194', 'de quoi parle ce livre?', 'Pour que je puisse vous dire de quoi parle le livre, **vous devez me donner le titre du livre ou toute autre information à son sujet.** 😊\n\nDites-moi de quel livre il s\'agit !', NULL, '2026-05-12 13:53:05'),
(3, '94961793', '94961793_6a01b7be95c439.06530194', 'résume moi ce livre', 'Pour que je puisse résumer ce livre, **vous devez me donner le titre du livre ou toute autre information à son sujet.** 😊\n\nDites-moi de quel livre il s\'agit !', NULL, '2026-05-12 15:41:32'),
(4, '94961793', '94961793_6a01b7be95c439.06530194', 'résume ce livre', 'Pour que je puisse résumer ce livre, **vous devez me donner le titre du livre ou toute autre information à son sujet.** 😊\n\nDites-moi de quel livre il s\'agit !', NULL, '2026-05-12 15:45:27'),
(5, '94961793', '94961793_6a01b7be95c439.06530194', 'résume ce livre', 'Pour que je puisse résumer ce livre, **vous devez me donner le titre du livre ou toute autre information à son sujet.** 😊\n\nDites-moi de quel livre il s\'agit !', NULL, '2026-05-12 15:51:10'),
(6, '94961793', '94961793_6a01b7be95c439.06530194', 'résume moi ce livre', 'Pour que je puisse résumer ce livre, **vous devez me donner le titre du livre ou toute autre information à son sujet.** 😊\n\nDites-moi de quel livre il s\'agit !', NULL, '2026-05-12 16:02:54'),
(7, '94961793', '94961793_6a01b7be95c439.06530194', 'bonjour', 'Bonjour ! Je suis Eduna, l\'assistant intelligent d\'EduNiger. Comment puis-je vous aider aujourd\'hui ? 😊', NULL, '2026-05-12 18:15:06'),
(8, '94961793', '94961793_6a01b7be95c439.06530194', 'résume moi ce livre', 'Pour que je puisse résumer ce livre, **vous devez me donner le titre du livre ou toute autre information à son sujet.** 😊\n\nDites-moi de quel livre il s\'agit !', 'Livre: Maths et vie réelle', '2026-05-12 18:15:35'),
(9, '94961793', '94961793_6a01b7be95c439.06530194', 'résume moi ce livre', 'Pour que je puisse résumer ce livre, **vous devez me donner le titre du livre ou toute autre information à son sujet.** 😊\n\nDites-moi de quel livre il s\'agit !', 'Livre: Maths et Vie Réelle - Volume II', '2026-05-12 18:18:38'),
(10, '94961793', '94961793_6a01b7be95c439.06530194', 'résume moi ce livre', 'Pour que je puisse résumer ce livre, **vous devez me donner le titre du livre ou toute autre information à son sujet.** 😊\n\nDites-moi de quel livre il s\'agit !', 'Livre: Maths et Vie Réelle - Volume II', '2026-05-12 18:25:05'),
(11, '94961793', '94961793_6a01b7be95c439.06530194', 'explique moi ce livre', 'Bonjour ! Je suis Eduna, l\'assistant d\'EduNiger. 😊\n\nPour que je puisse vous expliquer le livre **\"Les Gardiens de l’Équilibre\"**, pourriez-vous me confirmer que c\'est bien ce livre que vous souhaitez que je résume ou que j\'explique ?\n\nSi oui, je peux commencer, mais si vous avez un autre livre en tête, n\'hésitez pas à me le préciser ! 😊', 'Livre: Les Gardiens de l’Équilibre', '2026-05-12 18:28:26'),
(12, '94961793', '94961793_6a01b7be95c439.06530194', 'résume moi ce livre', 'Bonjour ! Je suis Eduna, l\'assistant d\'EduNiger. 😊\n\nPour que je puisse résumer ce livre, **vous devez me donner le titre du livre ou toute autre information à son sujet.** 😊\n\nDites-moi de quel livre il s\'agit !', 'Livre: Maths et Vie Réelle - Volume II', '2026-05-12 18:36:19'),
(13, '94961793', '94961793_6a01b7be95c439.06530194', 'résume moi ce livre', 'Pour que je puisse résumer ce livre, **vous devez me donner le titre du livre ou toute autre information à son sujet.** 😊\n\nDites-moi de quel livre il s\'agit !', 'Livre: Maths et Vie Réelle - Volume II', '2026-05-12 19:02:51'),
(14, '94961793', '94961793_6a01b7be95c439.06530194', 'un résumé', 'Pour que je puisse résumer ce livre, **vous devez me donner le titre du livre ou toute autre information à son sujet.** 😊\n\nDites-moi de quel livre il s\'agit !', 'Livre: Maths et Vie Réelle - Volume II', '2026-05-12 19:08:14'),
(15, '94961793', '94961793_6a01b7be95c439.06530194', 'résume moi ce livre', 'Erreur réseau: Operation timed out after 120001 milliseconds with 0 bytes received', 'Livre: Maths et Vie Réelle - Volume II', '2026-05-12 19:30:23'),
(16, '94961793', '94961793_6a01b7be95c439.06530194', 'bonjour Eduna', 'Bonjour ! Je suis Eduna, l\'assistant intelligent d\'EduNiger. 😊\n\nComment puis-je vous aider aujourd\'hui ? Avez-vous une question, besoin d\'une explication, ou souhaitez-vous que je résume un livre ? 😊', NULL, '2026-05-13 18:48:23'),
(17, '94961793', '94961793_6a01b7be95c439.06530194', 'résume moi ce livre', 'Voici un résumé du contenu extrait du document :\n\nCe document est un extrait d\'un livre intitulé **\"Volume 2 : La Fonction Carrée\"** faisant partie de la collection \"Les Fondamentaux Mathématiques\", conçu pour rendre les mathématiques utiles et applicables aux réalités du quotidien, notamment en Afrique.\n\n**Idée Centrale :**\nLa fonction carrée, définie par $f(x) = x^2$, est présentée non pas comme un simple calcul, mais comme une **idée puissante** qui permet de modéliser et d\'anticiper des réalités concrètes (agriculture, construction, économie, planification).\n\n**Points Clés du Livre :**\n\n1. **Origine (Chapitre 1) :** Le concept du \"carré\" est historique, car les civilisations anciennes (Égyptiens, Nubiens, Haoussa, Mandingue) l\'utilisaient pour mesurer et répartir équitablement les terres.\n2. **Définition Mathématique (Chapitre 2) :** La fonction $f(x) = x^2$ signifie élever un nombre au carré. Il est souligné que le résultat est toujours positif ou nul (symétrie par rapport à l\'axe vertical).\n3. **Exemples Africains Concrets (Chapitre 3) :** Le livre illustre l\'application de $f(x) = x^2$ dans des contextes africains :\n    * **Togo :** Calcul de la surface d\'un enclos carré ($12 \\times 12 = 144 \\text{ m}^2$).\n    * **Bénin :** Calcul de la surface cultivable (ex: $30^2 = 900 \\text{ m}^2$).\n    * **Burkina Faso :** Planification de stands de marché ($4^2 = 16 \\text{ m}^2$).\n    * **Niger :** Calcul de la quantité de carreaux nécessaires pour recouvrir un sol (utilisant la fonction deux fois).\n    * **Économie :** Illustration d\'une croissance quadratique (bénéfices quadruplant quand la production double).\n4. **Importance (Chapitre 4) :** La fonction carrée est essentielle pour calculer des surfaces, modéliser des phénomènes croissants (coût, production), et est une base pour des concepts', 'Livre: Maths et Vie Réelle - Volume II', '2026-05-13 18:49:52'),
(18, '94961793', '94961793_6a01b7be95c439.06530194', 'merci beaucoup', 'Je vous en prie. Je suis là pour vous aider. 😊', 'Livre: Maths et Vie Réelle - Volume II', '2026-05-13 18:51:25'),
(19, '94961793', '94961793_6a01b7be95c439.06530194', 'explique moi ce livre', 'Ce document est un extrait d\'un livre intitulé **\"Volume 2 : La Fonction Carrée\"**, qui fait partie de la collection \"Les Fondamentaux Mathématiques\".\n\n**Idée Centrale :**\nLe livre présente la fonction carrée ($f(x) = x^2$) non pas comme un simple calcul, mais comme un **outil puissant pour modéliser et anticiper des réalités concrètes** (agriculture, construction, économie, planification), en les rendant applicables aux réalités africaines.\n\n**Points Clés :**\n\n1. **Origine :** Le concept du \"carré\" est historique, car les civilisations anciennes (Égyptiens, Haoussa, Mandingue) l\'utilisaient pour mesurer et répartir équitablement les terres.\n2. **Définition Mathématique :** La fonction $f(x) = x^2$ signifie élever un nombre au carré, avec la propriété que le résultat est toujours positif ou nul.\n3. **Exemples Africains Concrets :** Le livre utilise cette fonction pour illustrer des applications pratiques dans des contextes africains, comme le calcul de surfaces (enclos, terres cultivables) et la modélisation de la croissance économique.\n4. **Importance :** La fonction carrée est essentielle pour calculer des surfaces, modéliser des phénomènes croissants, et sert de base à des concepts mathématiques appliqués.', 'Livre: L\'informatique : La voie express vers l\'indépendance', '2026-05-14 10:07:10'),
(20, '94961793', '94961793_6a01b7be95c439.06530194', 'explique moi ce livre', 'Ce document est un extrait d\'un livre intitulé **\"Volume 2 : La Fonction Carrée\"**, qui fait partie de la collection \"Les Fondamentaux Mathématiques\".\n\n**Idée Centrale :**\nLe livre présente la fonction carrée ($f(x) = x^2$) non pas comme un simple calcul, mais comme un **outil puissant pour modéliser et anticiper des réalités concrètes** (agriculture, construction, économie, planification), en les rendant applicables aux réalités africaines.\n\n**Points Clés :**\n\n1. **Origine :** Le concept du \"carré\" est historique, car les civilisations anciennes (Égyptiens, Haoussa, Mandingue) l\'utilisaient pour mesurer et répartir équitablement les terres.\n2. **Définition Mathématique :** La fonction $f(x) = x^2$ signifie élever un nombre au carré, avec la propriété que le résultat est toujours positif ou nul.\n3. **Exemples Africains Concrets :** Le livre utilise cette fonction pour illustrer des applications pratiques dans des contextes africains, comme le calcul de surfaces (enclos, terres cultivables) et la modélisation de la croissance économique.\n4. **Importance :** La fonction carrée est essentielle pour calculer des surfaces, modéliser des phénomènes croissants, et sert de base à des concepts mathématiques appliqués.', 'Livre: Le temps sous contrôle', '2026-05-14 10:15:29'),
(21, '94961793', '94961793_6a0826c2477ba4.73020775', 'bonjour', 'Bonjour ! Je suis Eduna, l\'assistant intelligent d\'EduNiger. Comment puis-je vous aider aujourd\'hui ? 😊', NULL, '2026-05-16 10:11:50'),
(22, '94961793', '94961793_6a0826c2477ba4.73020775', 'résume moi ce livre', 'Voici un résumé du document \"Le temps sous contrôle\" :\n\nCe guide pratique vise à aider les dirigeants très occupés à mieux gérer leur temps et à améliorer leur productivité quotidienne.\n\n**Points clés du livre :**\n\n1. **La gestion de l\'énergie plutôt que du temps :** Le livre critique l\'idée de limiter le temps disponible et propose de se concentrer sur l\'énergie. Il suggère qu\'il n\'y a pas un nombre limité d\'heures, mais plutôt un manque d\'énergie pour accomplir les tâches.\n2. **L\'importance de la gestion de l\'énergie :** Il fait référence à une expérience où renforcer l\'énergie (corps, émotion, esprit) a augmenté la productivité de 13 % chez les participants.\n3. **Identifier les faiblesses personnelles :** Avant d\'appliquer des techniques, il est crucial d\'identifier ses propres points faibles.\n4. **Étape 1 : Effectuer un audit du temps :** Il est conseillé de suivre précisément comment le temps est réellement passé pendant une semaine (en utilisant un journal de suivi ou un outil) pour prendre conscience de l\'utilisation réelle du temps (par exemple, pour identifier le temps passé sur les réseaux sociaux).\n5. **Étape 2 : Identifier les tendances et les informations :** Après l\'audit, il faut interpréter les données pour repérer les tendances et les points faibles (par exemple, le perfectionnisme, la difficulté à dire non, la surestimation de ses capacités, la peur de déléguer, ou la noyade dans les distractions).\n6. **Clarté et action :** La reconnaissance de ces problèmes est la moitié de la solution. L\'avantage de l\'audit est la clarté, car vous savez exactement sur quoi travailler (déléguer, améliorer les tactiques, etc.).\n\nEn conclusion, le livre propose un processus structuré (audit $\\rightarrow$ analyse $\\rightarrow$ application ciblée) pour que l\'utilisateur choisisse les conseils de gestion du temps qui correspondent spécifiquement à ses problèmes.', 'Livre: Le temps sous contrôle', '2026-05-16 10:13:21'),
(23, '94961793', '94961793_6a0826c2477ba4.73020775', 'résume moi ce livre', 'Ce document est le **Volume 2 de la collection \"Les Fondamentaux Mathématiques\"** et se concentre sur la **fonction carrée, $f(x) = x^2$**.\n\n**Idées principales :**\n\n*   **Concept fondamental :** La fonction carrée ($f(x) = x^2$) est présentée comme une idée simple mais puissante, qui relie les mathématiques aux besoins du quotidien (semer un champ, construire un marché, planifier une entreprise).\n*   **Signification :** Élever un nombre au carré signifie multiplier ce nombre par lui-même.\n*   **Importance :** Elle est essentielle pour voir le monde, anticiper les besoins, répartir l\'espace et évaluer la croissance.\n*   **Origine :** Le terme \"carré\" vient de la mesure des surfaces en forme de carré, utilisée historiquement par les civilisations anciennes (Égyptiens, Nubiens, royaumes haoussa et mandingue) pour organiser et mesurer les terres.\n*   **Exemples concrets :** Le livre illustre l\'application de $f(x) = x^2$ dans des contextes africains réels :\n    *   **Construction :** Calculer la surface d\'un enclos (Togo).\n    *   **Agriculture :** Estimer la surface cultivable (Bénin).\n    *   **Planification :** Organiser un marché (Burkina Faso).\n    *   **Décoration :** Calculer le nombre de carreaux nécessaires (Niger).\n    *   **Économie :** Modéliser une croissance quadratique (bénéfices qui quadruplent).\n*   **Utilisations :** La fonction est utilisée pour calculer des **surfaces**, pour la **modélisation** de phénomènes croissants (coût, production, énergie), et elle sert de base pour des concepts mathématiques plus avancés (parabole, dérivées).\n\n**En résumé, ce livre vise à démontrer que la fonction carrée n\'est pas seulement un outil de calcul abstrait, mais un modèle mathématique fondamental qui structure la manière dont les réalités physiques et économiques (notamment en Afrique) peuvent être comprises, planifiées et gérées.**', 'Livre: Maths et Vie Réelle - Volume II', '2026-05-16 10:15:58'),
(24, '94961793', '94961793_6a0826c2477ba4.73020775', 'bonjour', 'Bonjour ! Je suis Eduna, l\'assistant intelligent d\'EduNiger. Comment puis-je vous aider aujourd\'hui ? 😊', NULL, '2026-05-16 10:22:22'),
(25, '94961793', '94961793_6a0826c2477ba4.73020775', 'de quoi parle ce livre ?', 'Ce livre est le **Volume 2 de la collection \"Les Fondamentaux Mathématiques\"** et se concentre sur la **fonction carrée, $f(x) = x^2$**.\n\n**Idées principales :**\n\n*   **Concept fondamental :** La fonction carrée ($f(x) = x^2$) est présentée comme une idée simple mais puissante, qui relie les mathématiques aux besoins du quotidien (semer un champ, construire un marché, planifier une entreprise).\n*   **Signification :** Élever un nombre au carré signifie multiplier ce nombre par lui-même.\n*   **Importance :** Elle est essentielle pour voir le monde, anticiper les besoins, répartir l\'espace et évaluer la croissance.\n*   **Origine :** Le terme \"carré\" vient de la mesure des surfaces en forme de carré, utilisée historiquement par les civilisations anciennes (Égyptiens, Nubiens, royaumes haoussa et mandingue) pour organiser et mesurer les terres.\n*   **Exemples concrets :** Le livre illustre l\'application de $f(x) = x^2$ dans des contextes africains réels :\n    *   **Construction :** Calculer la surface d\'un enclos (Togo).\n    *   **Agriculture :** Estimer la surface cultivable (Bénin).\n    *   **Planification :** Organiser un marché (Burkina Faso).\n    *   **Décoration :** Calculer le nombre de carreaux nécessaires (Niger).\n    *   **Économie :** Modéliser une croissance quadratique (bénéfices qui quadruplent).\n*   **Utilisations :** La fonction est utilisée pour calculer des **surfaces**, pour la **modélisation** de phénomènes croissants (coût, production, énergie), et elle sert de base pour des concepts mathématiques plus avancés (parabole, dérivées).\n\n**En résumé, ce livre vise à démontrer que la fonction carrée n\'est pas seulement un outil de calcul abstrait, mais un modèle mathématique fondamental qui structure la manière dont les réalités physiques et économiques (notamment en Afrique) peuvent être comprises, planifiées et gérées.**', 'Livre: Le temps sous contrôle', '2026-05-16 10:23:45'),
(26, '94961793', '94961793_6a0826c2477ba4.73020775', 'salut', 'Salut ! Je suis Eduna, l\'assistant d\'EduNiger. Comment puis-je vous aider aujourd\'hui ? 😊', 'Livre: L\'informatique : La voie express vers l\'indépendance', '2026-05-16 10:27:54'),
(27, '94961793', '94961793_6a0826c2477ba4.73020775', 'résume ce livre', 'Ce livre est le **Volume 2 de la collection \"Les Fondamentaux Mathématiques\"** et se concentre sur la **fonction carrée, $f(x) = x^2$**.\n\n**Idées principales :**\n\n*   **Concept fondamental :** La fonction carrée ($f(x) = x^2$) est présentée comme une idée simple mais puissante, qui relie les mathématiques aux besoins du quotidien (semer un champ, construire un marché, planifier une entreprise).\n*   **Signification :** Élever un nombre au carré signifie multiplier ce nombre par lui-même.\n*   **Importance :** Elle est essentielle pour voir le monde, anticiper les besoins, répartir l\'espace et évaluer la croissance.\n*   **Origine :** Le terme \"carré\" vient de la mesure des surfaces en forme de carré, utilisée historiquement par les civilisations anciennes (Égyptiens, Nubiens, royaumes haoussa et mandingue) pour organiser et mesurer les terres.\n*   **Exemples concrets :** Le livre illustre l\'application de $f(x) = x^2$ dans des contextes africains réels :\n    *   **Construction :** Calculer la surface d\'un enclos (Togo).\n    *   **Agriculture :** Estimer la surface cultivable (Bénin).\n    *   **Planification :** Organiser un marché (Burkina Faso).\n    *   **Décoration :** Calculer le nombre de carreaux nécessaires (Niger).\n    *   **Économie :** Modéliser une croissance quadratique (bénéfices qui quadruplent).\n*   **Utilisations :** La fonction est utilisée pour calculer des **surfaces**, pour la **modélisation** de phénomènes croissants (coût, production, énergie), et elle sert de base pour des concepts mathématiques plus avancés (parabole, dérivées).\n\n**En résumé, ce livre vise à démontrer que la fonction carrée n\'est pas seulement un outil de calcul abstrait, mais un modèle mathématique fondamental qui structure la manière dont les réalités physiques et économiques (notamment en Afrique) peuvent être comprises, planifiées et gérées.**', 'Livre: Maths et Vie Réelle - Volume II', '2026-05-16 10:29:03'),
(28, '94961793', '94961793_6a0826c2477ba4.73020775', 'bonjour', 'Bonjour ! Je suis Eduna, l\'assistant intelligent d\'EduNiger. Comment puis-je vous aider aujourd\'hui ? 😊', NULL, '2026-05-16 10:55:01'),
(29, '94961793', '94961793_6a0826c2477ba4.73020775', 'Explique ce livre en détail : thèmes, résumé, auteur.', 'Ce document est le **Volume 1 de la collection \"Les Fondamentaux Mathématiques\"**, intitulé **\"Les Opérations de Base – La Racine de Toutes les Fonctions\"**.\n\n**Thèmes principaux :**\n\n1.  **Les Opérations de Base :** Le livre se concentre sur l\'importance des opérations fondamentales (additionner, soustraire, multiplier, diviser) comme étant la racine de toutes les fonctions mathématiques.\n2.  **Lien avec la Vie Réelle :** Il établit un pont entre les mathématiques et la vie quotidienne (agriculture, commerce, famille, cuisine) pour montrer que ces opérations sont indispensables.\n3.  **Origines Historiques :** Il explore d\'où viennent ces opérations, en montrant qu\'elles ont commencé avec des outils simples (cailloux, doigts) et sont devenues des langages universels.\n4.  **Applications Concrètes Africaines :** Il utilise des exemples spécifiques au contexte africain pour illustrer l\'utilité des opérations :\n    *   **Exemples d\'actions :** Dénombrer du mil (addition), enlever des chèvres (soustraction), calculer le coût total (multiplication), partager équitablement (division).\n    *   **Contextes spécifiques :** Marché (calcul du prix), chantier (répartition), cuisine, et tontine.\n5.  **Fonctions Complexes :** Il explique que les fonctions plus complexes (comme les fonctions affines, les intérêts bancaires, les pourcentages) reposent entièrement sur ces bases simples.\n\n**Résumé :**\n\nLe livre vise à prouver que les opérations de base (addition, soustraction, multiplication, division) sont la **fondation** de toute la pensée mathématique. Il démontre que ces gestes simples sont essentiels pour comprendre et gérer les réalités du monde (économiques, agricoles) et sont des outils pratiques pour la vie quotidienne. La maîtrise de ces bases est présentée comme une compétence essentielle pour prendre des décisions et gérer un budget.\n\n**Auteur :** L\'ouvrage est conçu par **l\'équipe Eduniger**.\n\n**En conclusion, l\'objectif du livre est de donner aux lecteurs la confiance et la maîtrise des opérations de base pour qu\'ils puissent comprendre et agir dans le monde réel.**', 'Livre: Maths et vie réelle', '2026-05-16 10:56:26'),
(30, '94961793', '94961793_6a0826c2477ba4.73020775', 'résumé moi ce livre', 'Ce document est le **Volume 1 de la collection \"Les Fondamentaux Mathématiques\"**, intitulé **\"Les Opérations de Base – La Racine de Toutes les Fonctions\"**.\n\n**Thèmes principaux :**\n\n1.  **Les Opérations de Base :** Le livre se concentre sur l\'importance des opérations fondamentales (additionner, soustraire, multiplier, diviser) comme étant la racine de toutes les fonctions mathématiques.\n2.  **Lien avec la Vie Réelle :** Il établit un pont entre les mathématiques et la vie quotidienne (agriculture, commerce, famille, cuisine) pour montrer que ces opérations sont indispensables.\n3.  **Origines Historiques :** Il explore d\'où viennent ces opérations, en montrant qu\'elles ont commencé avec des outils simples (cailloux, doigts) et sont devenues des langages universels.\n4.  **Applications Concrètes Africaines :** Il utilise des exemples spécifiques au contexte africain pour illustrer l\'utilité des opérations :\n    *   **Exemples d\'actions :** Dénombrer du mil (addition), enlever des chèvres (soustraction), calculer le coût total (multiplication), partager équitablement (division).\n    *   **Contextes spécifiques :** Marché (calcul du prix), chantier (répartition), cuisine, et tontine.\n5.  **Fonctions Complexes :** Il explique que les fonctions plus complexes (comme les fonctions affines, les intérêts bancaires, les pourcentages) reposent entièrement sur ces bases simples.\n\n**Résumé :**\n\nLe livre vise à prouver que les opérations de base (addition, soustraction, multiplication, division) sont la **fondation** de toute la pensée mathématique. Il démontre que ces gestes simples sont essentiels pour comprendre et gérer les réalités du monde (économiques, agricoles) et sont des outils pratiques pour la vie quotidienne. La maîtrise de ces bases est présentée comme une compétence essentielle pour prendre des décisions et gérer un budget.\n\n**Auteur :** L\'ouvrage est conçu par **l\'équipe Eduniger**.\n\n**En conclusion, l\'objectif du livre est de donner aux lecteurs la confiance et la maîtrise des opérations de base pour qu\'ils puissent comprendre et agir dans le monde réel.**', 'Livre: Maths et vie réelle', '2026-05-16 11:18:13'),
(31, '94961793', '94961793_6a0826c2477ba4.73020775', 'Explique ce livre en détail : thèmes, résumé, auteur.', 'Ce document est le **Volume 1 de la collection \"Les Fondamentaux Mathématiques\"**, intitulé **\"Les Opérations de Base – La Racine de Toutes les Fonctions\"**.\n\n**Thèmes principaux :**\n\n1.  **Les Opérations de Base :** Le livre se concentre sur l\'importance des opérations fondamentales (additionner, soustraire, multiplier, diviser) comme étant la racine de toutes les fonctions mathématiques.\n2.  **Lien avec la Vie Réelle :** Il établit un pont entre les mathématiques et la vie quotidienne (agriculture, commerce, famille, cuisine) pour montrer que ces opérations sont indispensables.\n3.  **Origines Historiques :** Il explore d\'où viennent ces opérations, en montrant qu\'elles ont commencé avec des outils simples (cailloux, doigts) et sont devenues des langages universels.\n4.  **Applications Concrètes Africaines :** Il utilise des exemples spécifiques au contexte africain pour illustrer l\'utilité des opérations :\n    *   **Exemples d\'actions :** Dénombrer du mil (addition), enlever des chèvres (soustraction), calculer le coût total (multiplication), partager équitablement (division).\n    *   **Contextes spécifiques :** Marché (calcul du prix), chantier (répartition), cuisine, et tontine.\n5.  **Fonctions Complexes :** Il explique que les fonctions plus complexes (comme les fonctions affines, les intérêts bancaires, les pourcentages) reposent entièrement sur ces bases simples.\n\n**Résumé :**\n\nLe livre vise à prouver que les opérations de base (addition, soustraction, multiplication, division) sont la **fondation** de toute la pensée mathématique. Il démontre que ces gestes simples sont essentiels pour comprendre et gérer les réalités du monde (économiques, agricoles) et sont des outils pratiques pour la vie quotidienne. La maîtrise de ces bases est présentée comme une compétence essentielle pour prendre des décisions et gérer un budget.\n\n**Auteur :** L\'ouvrage est conçu par **l\'équipe Eduniger**.\n\n**En conclusion, l\'objectif du livre est de donner aux lecteurs la confiance et la maîtrise des opérations de base pour qu\'ils puissent comprendre et agir dans le monde réel.**', 'Livre: L\'informatique : La voie express vers l\'indépendance', '2026-05-16 11:22:06'),
(32, '94961793', '94961793_6a0826c2477ba4.73020775', 'bonjour raconte moi une petite histoire sur l\'informatique', 'Bonjour ! En tant qu\'Eduna, je vais vous raconter une petite histoire inspirée par le document que j\'ai analysé, en me basant sur le texte fourni.\n\nVoici une histoire sur l\'informatique, inspirée par la manière dont l\'informatique ouvre des portes :\n\n**L\'histoire du Déclic Numérique**\n\nIl y a un temps, il y avait beaucoup de doutes, surtout chez les jeunes qui découvraient le monde de l\'informatique. Beaucoup pensaient que l\'informatique, c\'était juste savoir utiliser un logiciel de bureautique, comme écrire un texte ou faire un tableau. C\'était une vision très limitée, qui limitait leurs rêves.\n\nUn jour, un jeune homme, qui était confronté à ce vide et à ces interrogations, a eu une rencontre décisive. Il avait besoin de trouver une direction, car il ne voyait pas comment l\'informatique pouvait vraiment l\'aider.\n\nCe qui a changé sa perspective, ce n\'était pas une longue leçon académique, mais une **démonstration concrète**. Un grand frère, qui était déjà dans le domaine de l\'informatique, lui a montré comment un simple programme de calculatrice scientifique, développé sur son smartphone, pouvait devenir un outil d\'autonomie et de création.\n\nCe moment a été une véritable révélation. Il a compris que l\'informatique, ce n\'est pas seulement des boutons et des formules, c\'est un domaine vaste et en constante évolution. Elle englobe le développement d\'applications, la création d\'interfaces, l\'analyse de données, et même la cybersécurité ! Elle est la colonne vertébrale de notre société moderne.\n\nCe déclic lui a donné la force de persévérer. Il a appris que chaque erreur de code, chaque bug rencontré, n\'était pas un échec, mais une leçon précieuse. Chaque solution trouvée était une victoire, car en informatique, chaque élément compte.\n\nPour continuer son voyage, il a compris qu\'il ne devait pas se limiter à ce qu\'on lui apprenait à l\'école. Il a décidé d\'apprendre à apprendre : en utilisant des tutoriels vidéo, des livres numériques, et en se lançant dans de petits projets concrets – créer une calculatrice simple, automatiser une tâche, ou développer un site web.\n\nL\'informatique est devenue pour lui un chemin vers l\'ind', 'Livre: L\'informatique : La voie express vers l\'indépendance', '2026-05-16 11:40:18'),
(33, '94961793', '94961793_6a0826c2477ba4.73020775', 'quelle est la capitale du Niger ?', 'La capitale du Niger est **Niamey**.', NULL, '2026-05-16 12:12:51'),
(34, '94961793', '94961793_6a0826c2477ba4.73020775', 'Explique ce livre en détail : thèmes, résumé, auteur.', 'Ce document est le **Volume 1 de la collection \"Les Fondamentaux Mathématiques\"**, intitulé **\"Les Opérations de Base – La Racine de Toutes les Fonctions\"**.\n\n**Thèmes principaux :**\n\n1.  **Les Opérations de Base :** Le livre se concentre sur l\'importance des opérations fondamentales (additionner, soustraire, multiplier, diviser) comme étant la racine de toutes les fonctions mathématiques.\n2.  **Lien avec la Vie Réelle :** Il établit un pont entre les mathématiques et la vie quotidienne (agriculture, commerce, famille, cuisine) pour montrer que ces opérations sont indispensables.\n3.  **Origines Historiques :** Il explore d\'où viennent ces opérations, en montrant qu\'elles ont commencé avec des outils simples (cailloux, doigts) et sont devenues des langages universels.\n4.  **Applications Concrètes Africaines :** Il utilise des exemples spécifiques au contexte africain pour illustrer l\'utilité des opérations :\n    *   **Exemples d\'actions :** Dénombrer du mil (addition), enlever des chèvres (soustraction), calculer le coût total (multiplication), partager équitablement (division).\n    *   **Contextes spécifiques :** Marché (calcul du prix), chantier (répartition), cuisine, et tontine.\n5.  **Fonctions Complexes :** Il explique que les fonctions plus complexes (comme les fonctions affines, les intérêts bancaires, les pourcentages) reposent entièrement sur ces bases simples.\n\n**Résumé :**\n\nLe livre vise à prouver que les opérations de base (addition, soustraction, multiplication, division) sont la **fondation** de toute la pensée mathématique. Il démontre que ces gestes simples sont essentiels pour comprendre et gérer les réalités du monde (économiques, agricoles) et sont des outils pratiques pour la vie quotidienne. La maîtrise de ces bases est présentée comme une compétence essentielle pour prendre des décisions et gérer un budget.\n\n**Auteur :** L\'ouvrage est conçu par **l\'équipe Eduniger**.\n\n**En conclusion, l\'objectif du livre est de donner aux lecteurs la confiance et la maîtrise des opérations de base pour qu\'ils puissent comprendre et agir dans le monde réel.**', 'Livre: L\'informatique : La voie express vers l\'indépendance', '2026-05-16 12:15:04'),
(35, '94961793', '94961793_6a0826c2477ba4.73020775', 'Propose moi un livre intéressant de ta bibliothèque et explique pourquoi je devrais le lire.', 'HTTP 404 — <!DOCTYPE html>\n<html class=\"h-full\" lang=\"en-US\" dir=\"ltr\">\n  <head>\n    <meta charset=\"utf-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n    <link rel=\"preload\" href=\"', NULL, '2026-05-16 22:14:54'),
(36, '94961793', '94961793_6a0826c2477ba4.73020775', 'bonjour', 'HTTP 404 — <!DOCTYPE html>\n<html class=\"h-full\" lang=\"en-US\" dir=\"ltr\">\n  <head>\n    <meta charset=\"utf-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n    <link rel=\"preload\" href=\"', NULL, '2026-05-17 09:04:30'),
(37, '94961793', '94961793_6a0826c2477ba4.73020775', 'salut', 'HTTP 404 — <!DOCTYPE html>\n<html class=\"h-full\" lang=\"en-US\" dir=\"ltr\">\n  <head>\n    <meta charset=\"utf-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n    <link rel=\"preload\" href=\"', NULL, '2026-05-17 09:04:41'),
(38, '94961793', '94961793_6a0826c2477ba4.73020775', 'bonjour, quelle est la capitale du Niger ?', 'La capitale du Niger est **Niamey**.', NULL, '2026-05-17 11:58:45'),
(39, '94961793', '94961793_6a0826c2477ba4.73020775', 'Explique en détail le livre \"L\'informatique : La voie express vers l\'indépendance\". Présente les thèmes principaux, l\'auteur, le résumé et ce qu\'on peut retenir.', 'En tant qu\'Eduna, l\'IA éducative d\'EduNiger, voici l\'analyse détaillée du document concernant le livre \"L\'informatique : La voie express vers l\'indépendance\" :\n\n**Présentation de l\'Auteur :**\n\nL\'auteur est **Aboubacar Hamidou Ridouane**, âgé de 24 ans. Il est actuellement en préparation de son mémoire de Master 2 en Informatique Fondamentale et Appliquée à l\'Université Abdou Moumouni de Niamey. Il est développeur mobile full stack, ce qui signifie qu\'il maîtrise à la fois le développement côté client (applications mobiles, interface utilisateur) et côté serveur (backend, gestion des données, API). Parallèlement, il est co-fondateur de la start-up Ninotech, une entreprise technologique nigérienne visant à apporter des solutions innovantes adaptées aux réalités locales.\n\n**Résumé et Thèmes Principaux du Livre :**\n\nCe livre s\'adresse spécifiquement aux **jeunes bacheliers nigériens** qui se trouvent face à des choix d\'orientation difficiles et à une méconnaissance des opportunités offertes par l\'informatique.\n\n**Thèmes Principaux abordés :**\n\n1. **Démystifier l\'Informatique :**\n    * **Clarification du champ :** Le livre combat l\'idée reçue selon laquelle l\'informatique se limiterait à la bureautique (traitement de texte, tableurs). Il souligne que l\'informatique est un domaine beaucoup plus vaste et porteur d\'avenir.\n    * **Élargissement des spécialités :** Il présente l\'informatique comme un domaine qui inclut le développement logiciel (applications, sites web), le design numérique (UX/UI), la data science, l\'intelligence artificielle et la cybersécurité.\n    * **Lien avec l\'avenir :** Il insiste sur le fait que la maîtrise de ces domaines est essentielle pour l\'indépendance professionnelle et personnelle dans une économie numérique croissante.\n\n2. **Le Chemin vers la Découverte (Le Déclic) :**\n    * **Surmonter les doutes post-bac :** Il reconnaît la pression sociale et le vide que ressentent les jeunes après le baccalauréat.\n    * **Révélation par l\'expérience :** L\'auteur raconte comment une rencontre', 'Livre: L\'informatique : La voie express vers l\'indépendance', '2026-05-17 12:00:33'),
(40, '94961793', '94961793_6a0826c2477ba4.73020775', 'Fais un résumé complet du livre \"L\'informatique : La voie express vers l\'indépendance\". Présente l\'histoire, les personnages clés, les thèmes et les points essentiels.', 'En tant qu\'Eduna, l\'IA éducative d\'EduNiger, voici un résumé complet du livre \"L\'informatique : La voie express vers l\'indépendance\" basé sur le document fourni :\n\n**Résumé du Livre : \"L\'informatique : La voie express vers l\'indépendance\"**\n\nCe livre est un guide destiné principalement aux **jeunes bacheliers nigériens** qui hésitent face à leurs choix d\'orientation, en particulier face à une vision réductrice de l\'informatique. Son objectif principal est de **clarifier que l\'informatique est un levier essentiel pour l\'indépendance professionnelle et personnelle**, en montrant qu\'elle dépasse largement le cadre de la simple bureautique.\n\n**L\'Auteur et son Expérience :**\nL\'auteur, **Aboubacar Hamidou Ridouane** (24 ans), est un développeur mobile full stack. Il maîtrise le développement côté client (applications mobiles, interface utilisateur) et côté serveur (backend, gestion des données, API). Il est également co-fondateur de la start-up **Ninotech**, une entreprise technologique nigérienne qui cherche à proposer des solutions numériques innovantes adaptées au contexte local. Son expérience personnelle sert de témoignage concret pour valider la pertinence de ce domaine.\n\n**Thèmes Principaux Abordés :**\n\n1. **Démystification de l\'Informatique :**\n    * **Réfutation de la vision limitative :** Le livre combat la confusion fréquente qui réduit l\'informatique à la maîtrise des logiciels de bureautique. Il expose que l\'informatique est un champ vaste et évolutif.\n    * **Panorama des spécialités :** Il présente l\'informatique comme un domaine pluriel, incluant le développement logiciel, le design numérique (UX/UI), la data science, l\'intelligence artificielle et la cybersécurité.\n    * **Importance Sociétale :** Il souligne que l\'informatique est la colonne vertébrale de la société moderne, structurant l\'économie, les communications, la santé et l\'éducation.\n\n2. **Le Parcours et la Motivation :**\n    * **Surmonter les doutes post-bac :** Le livre reconnaît la pression sociale et le sentiment de vide que ressentent les jeunes après le baccalauréat.\n    * **Le Déclic :', 'Livre: L\'informatique : La voie express vers l\'indépendance', '2026-05-17 12:02:30'),
(41, '94961793', '94961793_6a0826c2477ba4.73020775', 'qui est l\'auteur de ce livre ?', 'L\'auteur de ce livre est **Aboubacar Hamidou Ridouane**, âgé de 24 ans. Il est actuellement en préparation de son mémoire de Master 2 en Informatique Fondamentale et Appliquée à l\'Université Abdou Moumouni de Niamey. Il est développeur mobile full stack et co-fondateur de la start-up Ninotech.', 'Livre: L\'informatique : La voie express vers l\'indépendance', '2026-05-17 12:04:08'),
(42, '94961793', '94961793_6a0826c2477ba4.73020775', 'Décris et analyse cette image en détail.', 'Cette image présente une scène formelle et officielle, probablement une cérémonie ou une parade militaire, impliquant des figures politiques de haut rang.\n\n**Description détaillée de l\'image :**\n\n**Composition et Sujets Principaux :**\nL\'image est centrée sur deux hommes en costume qui marchent au premier plan, tandis qu\'une longue ligne de soldats en uniforme militaire se tient en formation en arrière-plan. La composition est très symétrique et ordonnée, soulignant le caractère solennel de l\'événement.\n\n1. **Les Personnages au Premier Plan :**\n    * **À gauche :** Un homme d\'apparence asiatique (probablement un dirigeant politique) marche. Il porte un costume bleu marine, une chemise blanche et une cravate rouge. Son expression semble sérieuse et digne.\n    * **À droite :** Un homme plus âgé, reconnaissable comme étant Donald Trump, marche à côté de l\'autre homme. Il porte également un costume bleu foncé, une chemise blanche et une cravate rouge. Il semble engagé dans la marche.\n    * Les deux hommes marchent sur un tapis rouge, indiquant qu\'ils sont les figures centrales de l\'événement.\n\n2. **L\'Arrière-plan (La Garde Militaire) :**\n    * Une longue rangée de soldats est alignée en formation. Ils sont vêtus d\'uniformes militaires distinctifs, principalement verts et dorés, avec des casquettes ou des coiffes traditionnelles.\n    * Les soldats tiennent des fusils ou des bâtons militaires dressés, ce qui renforce l\'aspect militaire et cérémoniel de la scène.\n    * L\'alignement est très serré et parfait, suggérant une discipline militaire stricte.\n\n3. **L\'Environnement et l\'Ambiance :**\n    * **Le Sol :** Les deux hommes marchent sur un long tapis rouge vif, qui sert de voie de parade et de mise en valeur de la procession.\n    * **L\'Arrière-plan Lointain :** Derrière la ligne de soldats, on aperçoit une zone dégagée, peut-être une place ou un champ, avec de la verdure (arbres feuillus), indiquant que l\'événement se déroule en plein air ou dans un espace large.\n    * **Lumière :** La lumière semble naturelle et vive, sugg', 'Image analysée', '2026-05-17 12:05:51');
INSERT INTO `EdunaConversation` (`idConversation`, `idUser`, `session_id`, `user_message`, `assistant_response`, `book_context`, `created_at`) VALUES
(43, '94961793', '94961793_6a0826c2477ba4.73020775', 'Transcris et résume : Histoire des Haoussa', 'Nous sommes à 1815. Mohamed Bello, succede à son père le grand Usmantan Foujo, Autrond Lampeer de Sokoto. Desormais, il rend sur une population à majorité hausa. Un grand défi latan, évite à tout prix une rebellion décidée. Les mythiques cités hausa. Créé vers le 10ème siècle par les descendants du legendary Bayadita, elles vont donner naissance au peuple hausa. Un peuple dont le commerce et l\'artisanat vont dessiner le territoire et les propres frontières. Un peuple avec une forte capacité à similatrice qui va au fil des siècles, exercier une forte attraction chez les ethnivoises. Majorité dans le nord du Nigeria et dans le sud du Niger jusqu\'au Lac-Chap, il constitue l\'un des groupes ethniques les plus importantes d\'Afrique. Le Lang classé dans les langues afroasiatiques, elle a trois emplois avec plus de 63 millions de victoires. Les migrations, les brassages, la culture, la diversité et la sociabilité sont des termes qui définissent les hausseurs. Aujourd\'hui nous allons raconter le histoire et comme c\'est de plusieurs peuples, elles commencent par une legendne. Le Sahara, le plus fast-desert au monde. L\'étambérature peut viatendre 55°. Mais une appartueuse était ainsi à réalité, dans un passé lointain, le Sahara était bubblé humide et verdoien. La progrèsive désertification a poussé les populations à s\'installer de par et d\'autres de cette monocente bande aride qui nécessitent d\'avancer au fil des siècles. C\'est ce qui arrive à, selon la legendne, à un certain biajita, originère de Baddad et s\'insuite, installé vers le 17e siècle dans les massifs de la hier. Suit à la décication accrue des cet région, il décidaire de migrer vers le sud. Il arriveur au Roi-Mbécanorie, connu sur le nord de Canemme et qui s\'était formé autour du Lacchade. Au Canemme, biajita épousa une princesse et a eu un fils du nord de Birham. Après un prêtre affrontement avec le Roi du Canemme, il quitte à la terre des Canories et s\'installer à Daura. La legendne veut que biajita étuie un serpent qui vivait dans un puits à Kusugu, suit à Daura. En effet, le serpent terrorisé la population et la privédo ne l\'autorise à ses vies une mal et vannredis. Malgré les avertissements, il s\'arrandit au puits un jédit pour aller chercher de l\'eau. C\'est de là que proviendrez son nom, biajita, qui signifie celui qui ne comprenait pas. Une fois qu\'il commence à appuyer de l\'eau, il comprit pourquoi la population l\'a vertisé car le serpent l\'attaque, mais il lui coupe à la tête avec son épée et c\'est ainsi que le serpent futit. Daura mal a rende d\'aura, avait promis la moitié de son royaume à qui conclurent le serpent du puits. Mais très intelligemment, biajita refuse à lui demander à sa mai un mariage à la place. Du jamais vu, étant donné que l\'héline restait ses debatterts durant toute leur vie et que la société était profondément matriarchale, n\'aient à moi d\'auramment qui se sentait rédevable acceptant sa demande et les deux finir par se marier. De cette ignonne Nakibau, qui a son tour, à laquelle un compagné de leur uncle Biram des mefer de Bau et fils de la princesse Canuri, dirigeait les 7 états haussas, dit haussas bakwai, qui sont connus sous la nôde d\'aura, canot, carcinat, zaria, gobire, ranno et Biram. Cependant, biajita eut un autre fils illégitim avec une conquibine appelée Karbogari, s\'adernit une 7 fils, et c\'est ainsi dirigeait les 7 autres états haussas illégitim surfacie, dit banza bakwai, et son connus sous la nôde gouari, kibi, gorropha, illorine, nupé, illoie, esamphara. Donc l\'histoire des haussas s\'est l\'arrancant entre un des prières originaires de Paltat et une rentre qui rendient sur un peuple de la gildure. Mais l\'histoire des haussas s\'est aussi les cité et d\'à. Au fil des siècles, il développer une langue commune et mentaire des meurs semblables. Leur cité devenait un à un des villes fortifiées qui contrôlait leur campagne environnante respective. Elle s\'organisait selon le système de la Sarauta, un modèle politique tristructurais avec l\'annomination d\'un roi au dirigeant, le Serkin Kassa, qui était conseillé par des dignitaires composés d\'un conseil à chef-concentre, et d\'un bonheur de la gildure. La personne du roi n\'avait aucun caractère sacré et lui parlait dignitaire, il pouvait être déposé par eux. Chaque état était indépendant, mais les incitations du gouvernement et les rapports entre le ville était semblable. La vie était très mouvementée d\'insensuciété en pleine expansion, un système fiscal de la ville qui était un système de la ville qui était un système de la ville qui était un système de la ville qui était un système de la ville qui était un système de la ville qui était un système de l\'expansion, un système fiscal de la ville qui est un système de l\'expansion, un système de l\'expansion, un système fiscal de la ville qui est un système fiscal de la ville qui est un système fiscal de la ville qui contribue à la naissance d\'une économie complexe dans laquelle l\'agriculture l\'accomérce mais aussi l\'artisanat se commune pour assurer le développement des villes. Il y avait une telle production de chefs que les cités étaient protégés par des rampards qui pouvaient atteindre 15 mètres. Ce système économique qui était très profitable au mensou Saranta c\'était un oble, elle est profité aussi à un autre social qui l\'a lui-même donné de naissance. Elle s\'agit des hommes riches ou mensou arziquis. Il dominait le marché et disposé de capitaux importants et de clientèle extrêmement ramifié la plupart étaient des mensou Saranta. À côté de la aristocratie et de la bourgeoisie, nous avions les hommes pauvres appelés mensou talauti qui, en paraphrasingaro max, étaient les proletters de l\'époque exploités sous pain. Après la chronique de canon, il semble que ce soit des lettres et commerçants malinqués, venus du mali au 14 siècle, qui ont réintroduits l\'islam à canon et dans l\'ensemble du pays hausse. C\'est comme un dingue, fur d\'ailleurs à l\'origine de l\'intégration des cités hausse et au commerce transahariens. La ville de canon et de Cartina était de loin les plus riches. Ils en même que du laurante mensou Saranta c\'est ville était de rattaché à l\'Ampire. Des l\'or, la culture hausse se biselle l\'infinance directe de la civilisation méditerranée en automat grâce au caravan qui nécessitait de régler les cités et ta hausse aux pours arabes et aux colonies de marchands tripolitaires ou fonds de la chale algérie établisseur ses marchés. Elles ont pour laquelle la langue hausse est riche d\'emprunt à la langue arabe. Cette culture fortement assimilationniste va exercer aussi une attraction croissant sur les populations voisines dont elles entègrent progressivement des fraquements de plus en plus nombreuses. Le 12 avril 1993 Mohammed Tourette Ravers Sonny Barrow fils de Sonny Alibair lors de la bataille d\'Anfaru et devient amperer du sangay créant ainsi la dinastité Asquia. Entre 1500 et 1500 un, il occuple d\'Indy sans part de l\'Empire du Mali et même ensuite la guerre singe tout contre le roi à les mystes nasserais du voyom mocie. En 1912, il fait alliance avec Kantaro à l\'équibil, un des bans-abacuay et march vers l\'Est, où il annexe les Etats-Hausa de carcinat de Gobi-Ré de Cano. Cette intégration des cités Hausa de l\'ouest des dinors dans l\'Empire de Gao va mettre à marche la grande machine de la France des Etats-Hausa. Beaucoup de sangay, surtout de l\'étniserman, vont être aussaisés avec le temps. Mais le processus de aussaisisation a touché aussi d\'autres étnis comme les Beriberis ou Canories à l\'Est, les Targi, au nord, et les Guari, Nupé et Europa au Sud, ainsi que la fraction arabe de la région Sud-Aneze. La simulation de ces peuples va contribuer davantage à amplifier la distribution géographique des Hausa et à créer une grande diversité ethnique au sein du groupe. Le 16e siècle est marqué par la bravrenne Aminat de Zazau, dont son règne est natée vers 1666. Mohamed Belaud, soutien de ce couteau, dit la plus tard, en 1836, dans son livre intitulé Ifac Almei-Sour, que la rène Aminat, a forcé qu\'Axina, Cano et d\'autres régions Hausa, allait rendre hommage et qu\'il a été la premier à être à lire un gouvernement réunissant tous les Hausa. Mais le terme Hausa n\'était utilisé qu\'à partir du 16e siècle de notre air car les gens se nommaient même en fonction de la ville que du Roi M.-Splacifique-Douille-Provenais, vu que le peuple Hausa était un mouvement constant. Ces individus aient à le même corregine, s\'organisait un groupe et consulié un autre critère de division sociale. Ces groupes d\'origine étaient désignées selon les Dalettes sous les termes d\'Asili, Zoria, Iridandhi ou Kabila. Les individus de chaque groupe étaient liés par le même gâteau, c\'est-à-dire héritage patrilinaire. La part de l\'ence à ces groupes est considérée comme de natuges proches de celle de la parenté. Leur mâme, ce qualifide, un ou à automat dit, frère de mes mères. Dans certaines situations ces groupes de régions pouvaient représenter des cartes si ils viennent littéralement métier héritaires. A cette époque, on ne pouvait être malame, ou m\'affaraure-ci, ou faire d\'autres méchés que si l\'on avait hérité de l\'accès à ces professions. Une situation qui persiste jusqu\'à nos jours dans une siltat disparaître. Après ce de l\'origine, un autre facteur très important pour cette population semi-nomade était la nationalité, la partenace à un casse automatique à un état. La suite a été une individu et une termes de référence fondamentale entre Hausa. Cette identité étatique se matérialisée à l\'aide de scarification faciale imposée à chaque enfant au septième jour de son existence. L\'origine pouvait être liée aussi à la nationalité. Par exemple, on pouvait trouver des gobirao, katinao, ou des katinao, kanawa. Ce qui signifie que des habitants du gobire pouvaient être originaire de katinao et que des habitants de katina pouvaient être originaire de kano. Il existe par ailleurs une forte loyerté des immigrés assimilés qui se soient hausse à un an vers leur collectivité d\'accueil. D\'ailleurs, plusieurs classes aristocratiques Hausa ont d\'autres origines ethniques. C\'est le cas des Babarberi, d\'origine kanori, et des bafoulani d\'origine pule. Un premier nous allons parler du cas Babarberi. Pour mieux expliquer ces métissages, nous devons retourner à l\'arrière jusqu\'aux septième siècle bien avant l\'époque de Bayadjida. Un effet c\'est à cette époque qu\'elle est le canemme. Pire va par la suite atteindre son appogée avec Donnema, d\'Ibalami, qui régnant de 1220 à 1259 et est indi l\'empire vers le faisant et le nil. Après la moire de Donnema, le roi me connu des crées du succession et sa faibli au fur et à mesure que les années passées. Au 14e siècle, les sausses et les boladavnes venus de l\'Est pouce les kanori à se réfugiés à l\'ouest du Lac Chagd. Il déplaçait ainsi le centre à l\'ouest, fondaire le roi me de Bournu, en 1925, et à étendre leur domination sur toutes les espaces sur ceci du Lac Chagd, à l\'actuel région de Tilabéry et de la région de Laïr jusqu\'au plateau central du Nigeria. Certains de ces kanori imposèrent leur égemonie un pays à Hausa, adoptés leur culture et devèrent une classe aristocratique au sein du groupe Hausa. Même plus tard, quand les cités Hausa vont se libérer de l\'actuel canori, dont le centre était à Bournu, qui a conservé une certaine prestige dans la aristocratie. Il crèrent par la suite d\'autres états Hausa entre le 17e et le 19e siècle, au nord de l\'espace formé par les Hausa Baguay, parmi ces états, nous pouvons citer le sultanat de Damagaram, dont la capitale se trouvait dans l\'actuel région des Ineres. Il fure par la suite des Ampharales dans des Banzabaguay, dont puissant état qui furent confi vers la fin du 18e siècle par les envahisseurs Gobi Raouh, qui est aussi allorgine de l\'état d\'Araewa dans le Dallol Maury, aux invités du 12e siècle. L\'état d\'Araewa fut surtout connu pour sa résistance au reforme Mistopole. Ensuite, nous allons parler des Bafoulani, mais avant d\'entrer sous le vif du sujet, introduisons un homme qui va être allorgine de ses métiers à Hausa, entre Pelle et Hausa. Au semant d\'Anne Foto, dit le Toronto, et né le 15 décembre 1754 à Marata dans l\'actuel Niger. Son père était un pôle originaire du Foto Toron. Auxmane est ainsi issu d\'une famille de lettre Pelle Hausa Isé est celleuse au Gobi depuis le 17e siècle. A 1774, Auxmane finit par reprocher au musulman du Gobi de ne pas observer strictement les règles du courant et d\'ici de mener la prédication au Gobi et dans les états hausseaux voisins. A 1795, avec ses partisans à Majoreux des Hausa, il décide de renverser les cellules du Gobi désormais considéré comme des noms musulmans. A 1802, après une tentative d\'assassinement qu\'il a mangé, le Toronto fuit à la French Air Northwest du Gobi afin de s\'arraller les numaines bulles que les Hausa appellent les Foulani-indagi. Après une ultime tentative de considération, une fois le Serkin Gobi s\'allait de son côté les Serk et des autres états hausseaux et déclarent la guerre au Toronto. Du côté de l\'autre camp, dans les Fouciaux, il a été érevé à la Serkin Musulmis. Il a été élevé à la politique et à la religion. Il peut maintenant en ablée au G-Head et ressemble à une armée et la commande. Il a aimé le pays hausa en ne pas grâce à la pluie du Sultan Daga Dess, Mohammed Bakri, et de Giri Kaelgress. Dans Fouciaux et Paris, se tenue par la paysanerie Hausa qui souffre des taxes et des cités. Le 21 juin 1804, il ramporte une victoire sur l\'armée de Yunfa à Tapkin Quato. Il se proclame comme under des guayants et rend sur le Gobi ir. De 1804 à 1808, il s\'emparque de Cano et annexe par la suite d\'autres états hausseaux, d\'abord qu\'Accinat, à Sud-Saria, Nupé et Kibbi ainsi que le nord de l\'actuel Cameroun. Il désigne ensuite des émires pour administrer les territoires conqués. A 1808, il gagne la bataille décisive d\'Alkalawa lors de la Quelle Yunfa futue. Il fonde ensuite la ville de Sukoto à 1809 dont il fait sa capitale. Dans Fouciaux, se retrouve désormais à la tête de l\'empere de Sukoto dans lequel il applique les principes coloniques. Déjà âgé au début de la guerre, il transmet à 1815 le titre de sultan de Sukoto a son fils Mohamed Belou. Cette nouvelle aristocratie modifie à très sensiblement la structure du monde hausseaux. Il est lui en partage entre conquérant minoritaire, dressant les tandards de l\'Islam orthodoxe et hausse à musulman ou non. Par la suite, les cédentaires se convertire de plus en plus à l\'Islam et récit progment les réformistes adoptés à leur pour la culture hausse. Les Foulanings-Gida, c\'est-à-dire des aristocrats plus hausse à Isai, modifient aussi la structure politique à créer un système d\'alcool ou sommet de la pyramide il y a l\'immir ou le chère. Mais ces situations sociopolitiques seront contre plus de nos jours unigeria, car elle a été sûre de appuyer par le système de l\'Ende directe-Touroules des Britanniques. En hors du hausse-là, par contre, une cohabitation entre hausse et bousou, a habouti aussi à un métissage, surtout dans la région des Inders. L\'intégration des bogages à la société hausse est très récente et paraît résultée d\'une situation historiquement locale. Les bogages sont à réalité des Targis, c\'est-à-dire toareg. Les hausse les appels les bas absin, par référence à leur implantation, dans l\'air désigné sous le nom de hapsin. En effet, la société Targis se divise en deux catégories en distincts d\'énobles, appelés imagèques. Les hogares de la population des lesquelles sont lesquelles les captefs appelés à clan. Ce sont ces derniers, dont le statut est servile, condisées sous l\'anon de bogage. Beaucoup de ces peuples semblent être un passage de toareg et de population noir. Ils étaient dominés par les toarecs et magéguennes et assimilés par eux sur la base d\'une structure de leur conférence le statut de captef. Mais aujourd\'hui, beaucoup de bogages sont devenus indépendants surtout avec l\'aide du colonisateur français, une politique anti-esclavagiste motivée surtout par la résistance des toarecs et magéguennes à la colonisation. C\'est ainsi que plusieurs bogages libres se sont installés dans le sud, notamment dans le zindère. Ces derniers ont abandonné la plus grande partie de la culture Targis et n\'appartient plus à cette société. Ils ne parlent plus de la chèque mais Hausa ont adopté la culture Hausa et se sont soumis au chef locaux. Mais par contre, ils ont gardé certains mots de vie Targis comme le goût de le levage à des bovins et à chance le territoire de façon à concilier. Elle a vage et agriculture.', 'Audio: Histoire des Haoussa', '2026-05-17 12:09:08'),
(44, '94961793', '94961793_6a0826c2477ba4.73020775', 'Fais un résumé complet du livre \"L\'informatique : La voie express vers l\'indépendance\". Présente l\'histoire, les personnages clés, les thèmes et les points essentiels.', 'HTTP 404: <!DOCTYPE html>\n<html class=\"h-full\" lang=\"en-US\" dir=\"ltr\">\n  <head>\n    <meta charset=\"utf-8\">\n    <meta name=\"viewport\" content=\"width=device-width', 'Livre: L\'informatique : La voie express vers l\'indépendance', '2026-05-17 14:40:23'),
(45, '94961793', '94961793_6a0826c2477ba4.73020775', 'Explique en détail le livre \"L\'informatique : La voie express vers l\'indépendance\". Présente les thèmes principaux, l\'auteur, le résumé et ce qu\'on peut retenir.', 'HTTP 404: <!DOCTYPE html>\n<html class=\"h-full\" lang=\"en-US\" dir=\"ltr\">\n  <head>\n    <meta charset=\"utf-8\">\n    <meta name=\"viewport\" content=\"width=device-width', 'Livre: L\'informatique : La voie express vers l\'indépendance', '2026-05-17 14:42:03'),
(46, '94961793', '94961793_6a0826c2477ba4.73020775', 'Explique en détail le livre \"L\'informatique : La voie express vers l\'indépendance\". Présente les thèmes principaux, l\'auteur, le résumé et ce qu\'on peut retenir.', 'HTTP 404: <!DOCTYPE html>\n<html class=\"h-full\" lang=\"en-US\" dir=\"ltr\">\n  <head>\n    <meta charset=\"utf-8\">\n    <meta name=\"viewport\" content=\"width=device-width', 'Livre: L\'informatique : La voie express vers l\'indépendance', '2026-05-17 14:42:55'),
(47, '94961793', '94961793_6a0826c2477ba4.73020775', 'Explique en détail le livre \"L\'informatique : La voie express vers l\'indépendance\". Présente les thèmes principaux, l\'auteur, le résumé et ce qu\'on peut retenir.', 'Bonjour ! En tant qu\'Eduna, je vais vous faire un résumé complet du document que vous avez fourni, en me basant uniquement sur le texte de la \"Fiche 2\" concernant le livre \"L\'informatique : La voie express vers l\'indépendance\".\n\n**Résumé du livre \"L\'informatique : La voie express vers l\'indépendance\"**\n\nCe texte présente une réflexion sur l\'importance de l\'informatique et propose une feuille de route pour les jeunes, particulièrement les bacheliers nigériens.\n\n**1. L\'Auteur (Présentation contextuelle)**\n\nLe document est présenté à travers le témoignage et les réflexions d\'une personne (Aboubacar Hamidou Ridouane), qui est développeur mobile full stack et co-fondateur de la start-up Ninotech au Niger. Cela donne une perspective concrète et motivante sur les opportunités du numérique.\n\n**2. Thèmes Principaux et Points Essentiels**\n\nLe livre aborde plusieurs aspects fondamentaux :\n\n* **La Prise de Conscience Post-Bac :**\n    * Il reconnaît la confusion et le vide que ressentent les jeunes après le baccalauréat, souvent face à des choix d\'orientation difficiles.\n    * Il critique la méconnaissance de l\'informatique, car elle est souvent réduite à la simple bureautique (traitement de texte, tableurs), ce qui limite les ambitions.\n    * Il insiste sur le fait que l\'informatique est un domaine beaucoup plus vaste.\n\n* **La Définition Vaste de l\'Informatique :**\n    * Le livre clarifie que l\'informatique ne se limite pas à la bureautique. Elle englobe des domaines porteurs d\'avenir tels que :\n        * Le développement logiciel (applications mobiles, sites web, logiciels métiers).\n        * Le design numérique (interfaces utilisateur, expérience utilisateur).\n        * La data science (analyse et exploitation de données).\n        * L\'intelligence artificielle (machines apprenantes, automatisation).\n        * La cybersécurité (protection des données et systèmes).\n    * Il positionne l\'informatique comme la \"colonne vertébrale de la société moderne\", structurant l\'économie, les communications, la santé, l\'éducation, etc.\n\n* **Le Déclic et la Persévérance :**\n    * Le', 'Livre: L\'informatique : La voie express vers l\'indépendance', '2026-05-17 14:52:20'),
(48, '94961793', '94961793_6a0826c2477ba4.73020775', 'qui est l\'auteur de ce livre ?', 'L\'auteur de ce livre est **Aboubacar Hamidou Ridouane**.\n\nIl est décrit comme étant :\n* Âgé de 24 ans.\n* En préparation d\'un mémoire de Master 2 en Informatique Fondamentale et Appliquée à l\'Université Abdou Moumouni de Niamey.\n* Développeur mobile full stack (maîtrisant le développement côté client et côté serveur).\n* Co-fondateur de la start-up Ninotech au Niger.', 'Livre: L\'informatique : La voie express vers l\'indépendance', '2026-05-17 14:53:31'),
(49, '94961793', '94961793_6a0826c2477ba4.73020775', 'Décris et analyse cette image en détail.', '## Description et Analyse de l\'Image\n\nCette image est une illustration promotionnelle ou informative, probablement tirée d\'un article, d\'une présentation technique, ou d\'un matériel de marketing lié à l\'intelligence artificielle (IA) et au *machine learning*. Elle utilise un style graphique numérique, vif et futuriste, typique des produits technologiques.\n\n### Description Détaillée des Éléments Visuels\n\n**1. Titre Principal :**\n* En haut, un titre en lettres capitales blanches sur un fond bleu foncé, encadré par un cadre rouge vif. Le texte est : **\"LLM QUANTIFIÉ\"**.\n    * **Analyse :** \"LLM\" signifie *Large Language Model* (Grand Modèle de Langage). \"Quantifié\" fait référence au processus de quantification, une technique de compression des modèles d\'IA pour réduire leur taille et leurs besoins en mémoire, ce qui est un sujet central dans l\'optimisation des modèles d\'IA.\n\n**2. Tableau Comparatif :**\nL\'image présente un tableau comparatif divisé en deux colonnes, illustrant des exigences de mémoire (en Go) pour différents types de modèles ou techniques.\n\n* **Colonne 1 : Méthode Standard**\n    * **Ligne 1 (Standard) :** \"Méthode Standard\" (en haut).\n    * **Ligne 2 (Fine-tuning) :** \"Fine-tuning Standard (16-bit)\".\n    * **Exigence de Mémoire :** \"> 780 Go\" (en rouge, barré d\'un \"X\").\n        * **Analyse :** Cela indique que la méthode standard, en utilisant des modèles en précision 16-bit, nécessite une quantité de mémoire GPU très élevée (plus de 780 Go), ce qui est une contrainte significative.\n\n* **Colonne 2 :**\n    * **Ligne 1 (Mémoire GPU requise) :** \"Mémoire GPU requise\".\n    * **Ligne 2 (QLoRA) :** \"QLoRA (4-bit)\".\n    * **Exigence de Mémoire :** \"< 48 Go\" (en vert/jaune).\n        * **Analyse :** Cela suggère que la technique QLoRA (Quantized Low-Rank Adaptation) permet de réduire drastiquement l\'exigence de mémoire GPU', 'Image analysée', '2026-05-17 14:56:14'),
(50, '94961793', '94961793_6a0826c2477ba4.73020775', 'Transcris et résume : Histoire des Haoussa', 'Nous sommes à 1815. Mohamed Bello, succede à son père le grand Usmantan Foujo, Autrond Lampeer de Sokoto. Desormais, il rend sur une population à majorité hausa. Un grand défi latan, évite à tout prix une rebellion décidée. Les mythiques cités hausa. Créé vers le 10ème siècle par les descendants du legendary Bayadita, elles vont donner naissance au peuple hausa. Un peuple dont le commerce et l\'artisanat vont dessiner le territoire et les propres frontières. Un peuple avec une forte capacité à similatrice qui va au fil des siècles, exercier une forte attraction chez les ethnivoises. Majorité dans le nord du Nigeria et dans le sud du Niger jusqu\'au Lac-Chap, il constitue l\'un des groupes ethniques les plus importantes d\'Afrique. Le Lang classé dans les langues afroasiatiques, elle a trois emplois avec plus de 63 millions de victoires. Les migrations, les brassages, la culture, la diversité et la sociabilité sont des termes qui définissent les hausseurs. Aujourd\'hui nous allons raconter le histoire et comme c\'est de plusieurs peuples, elles commencent par une legendne. Le Sahara, le plus fast-desert au monde. L\'étambérature peut viatendre 55°. Mais une appartueuse était ainsi à réalité, dans un passé lointain, le Sahara était bubblé humide et verdoien. La progrèsive désertification a poussé les populations à s\'installer de par et d\'autres de cette monocente bande aride qui nécessitent d\'avancer au fil des siècles. C\'est ce qui arrive à, selon la legendne, à un certain biajita, originère de Baddad et s\'insuite, installé vers le 17e siècle dans les massifs de la hier. Suit à la décication accrue des cet région, il décidaire de migrer vers le sud. Il arriveur au Roi-Mbécanorie, connu sur le nord de Canemme et qui s\'était formé autour du Lacchade. Au Canemme, biajita épousa une princesse et a eu un fils du nord de Birham. Après un prêtre affrontement avec le Roi du Canemme, il quitte à la terre des Canories et s\'installer à Daura. La legendne veut que biajita étuie un serpent qui vivait dans un puits à Kusugu, suit à Daura. En effet, le serpent terrorisé la population et la privédo ne l\'autorise à ses vies une mal et vannredis. Malgré les avertissements, il s\'arrandit au puits un jédit pour aller chercher de l\'eau. C\'est de là que proviendrez son nom, biajita, qui signifie celui qui ne comprenait pas. Une fois qu\'il commence à appuyer de l\'eau, il comprit pourquoi la population l\'a vertisé car le serpent l\'attaque, mais il lui coupe à la tête avec son épée et c\'est ainsi que le serpent futit. Daura mal a rende d\'aura, avait promis la moitié de son royaume à qui conclurent le serpent du puits. Mais très intelligemment, biajita refuse à lui demander à sa mai un mariage à la place. Du jamais vu, étant donné que l\'héline restait ses debatterts durant toute leur vie et que la société était profondément matriarchale, n\'aient à moi d\'auramment qui se sentait rédevable acceptant sa demande et les deux finir par se marier. De cette ignonne Nakibau, qui a son tour, à laquelle un compagné de leur uncle Biram des mefer de Bau et fils de la princesse Canuri, dirigeait les 7 états haussas, dit haussas bakwai, qui sont connus sous la nôde d\'aura, canot, carcinat, zaria, gobire, ranno et Biram. Cependant, biajita eut un autre fils illégitim avec une conquibine appelée Karbogari, s\'adernit une 7 fils, et c\'est ainsi dirigeait les 7 autres états haussas illégitim surfacie, dit banza bakwai, et son connus sous la nôde gouari, kibi, gorropha, illorine, nupé, illoie, esamphara. Donc l\'histoire des haussas s\'est l\'arrancant entre un des prières originaires de Paltat et une rentre qui rendient sur un peuple de la gildure. Mais l\'histoire des haussas s\'est aussi les cité et d\'à. Au fil des siècles, il développer une langue commune et mentaire des meurs semblables. Leur cité devenait un à un des villes fortifiées qui contrôlait leur campagne environnante respective. Elle s\'organisait selon le système de la Sarauta, un modèle politique tristructurais avec l\'annomination d\'un roi au dirigeant, le Serkin Kassa, qui était conseillé par des dignitaires composés d\'un conseil à chef-concentre, et d\'un bonheur de la gildure. La personne du roi n\'avait aucun caractère sacré et lui parlait dignitaire, il pouvait être déposé par eux. Chaque état était indépendant, mais les incitations du gouvernement et les rapports entre le ville était semblable. La vie était très mouvementée d\'insensuciété en pleine expansion, un système fiscal de la ville qui était un système de la ville qui était un système de la ville qui était un système de la ville qui était un système de la ville qui était un système de la ville qui était un système de l\'expansion, un système fiscal très élaboré, contribuant à la naissance d\'une économie complexe dans laquelle l\'agriculture l\'a commerçue mais aussi l\'artisanat se commune pour assurer le développement des villes. Il y avait une telle production de chefs que les cités étaient protégés par des rampards qui pouvaient atteindre 15 mètres. Ce système économique était très profitable au mensaud Saranta, c\'est à des renembles. Aller profiter aussi à un autre dans le social qui l\'a l\'umime donné de naissance. Il s\'agit des hommes riches ou mensauds arziquis. Il dominait le marché et disposé de capitaux importants et de clientèle extrêmement ramifié la plupart étaient des mensauds Saranta. À côté de la aristocratie et de la bourgeoisie, nous avions les hommes pauvres appelés mensauds Talauchi, qui, en paraphrasingaro Max, était les proletters de l\'époque, exploité sous pain. Après la chronique de canon, il semble que ce soit des lettres malinqués, venus du mali, au 14 siècle, qui aurait introduit l\'islam à canon et d\'un ensemble du pays hausse. Si elle commençait à mananger, fûrait d\'ailleurs à l\'origine de l\'intégration des cités hausse au commerce transahariens. La ville de canon et de Cartina était de loin les plus riches. Ils en même que du la rente mensamousa, homony, ses villes étaient arrattachées à l\'ambire. Dès lors, la culture hausse est l\'infinance directe de la civilisation méditerranée en automat grâce au caravan, qui ne cessait de relier les cités et à hausse au pours Arabes et au colonie de marchands tripolitaires ou fonds de la chale Algérie, établissueuse et marchée. Elles ont pour laquelle la langue hausse est riche d\'emplois à la langue Arabes. Cette culture fortement assimilationniste va exercer aussi une attraction croissante sur les populations voisines dont elles intègrent progressivement des fraquements de plus en plus nombreuses. Le 12 avril 1993, Mohammed Tourette, ravers sonny Barou, fils de sonny Alibair, lors de la bataille d\'Ampharu, est devient amperer du sang-gai créant ainsi la dénascité Asquia. Entre 1500 et 1500 un, il occuple d\'Indy sans part de l\'Empire du Mali et même ensuite la guerre singe tout contre le roi à les mistes nasserais du voyom mocie. En 1912, il fait alliance avec Kantaroa de Kibil en dépanse à Bakuai et marche vers l\'Est où il annexe les Etats-Hausas de carcinat de Gobi-Ré de Kanau. Cette intégration des cités Hausa de l\'ouestée du Nord dans l\'Empire de Gao va mettre à marche la grande machine asmérationniste des Hausas. Beaucoup de sang-gai, surtout de l\'ethnisarmant, vont être aussaisés avec le temps. Mais le processus de aussaisisation a touché aussi d\'autres étniques comme les Beriberi ou Kanuri à l\'Est, les Targi, au Nord, et les Guari, noupé et Europa au Sud, ainsi que la fraction arabe de la région Sud-Aneze. La simulation de ces peuples va contribuer davantage à amplifier la distribution géographique des Hausas et à créer une grande diversité et technique au sein du groupe. Le 16e siècle est marqué par la bravrenne Aminat de Zazau, dont son règne est natée vers 1666. Mohamed Belaud, soutenre de ce couteau, dit la plus tard, en 1836, dans son livre intitulé Ifac Almey Sur, que la Rène Aminat a forcé qu\'à China, Kanau et d\'autres régions Hausa allurent de l\'omage et qu\'il a été la première à être à bire un gouvernement réunissant tous les Hausa. Mais le terme Hausa n\'était utilisé qu\'à partir du 16e siècle de notre air car les gens se nommaient même en fonction de la ville du Roi-Mespecific-Douille provenée, vu que le peuple Hausa était un mouvement constant. Ces individus aient à le même corregine, s\'organisait un groupe et consulé un autre critère de division social. Ces groupes de régines étaient désignées selon les Dalettes sous les termes de Azili, Zoria, Iridandhi ou Kabila. Les individus de chaque groupe étaient liés par le même gâteau, c\'est-à-dire, héritage patrilinaire. La part de l\'ence à ces groupes est considérée comme de natuges proches de celles de la parenté. Leur mamele se qualifie de un ou à automatie frère de ma mère. Dans certaines situations ces groupes de régines pouvait représenter des cartas si ils viennent littéralement métier héreditaires. À cette époque on ne pouvait être malame ou m\'affaraure-ci ou faire d\'autres méchés que si l\'on avait hérité de l\'accès à ses professions. Une situation qui persiste jusqu\'à nos jours dans une silté à disparaître. En plus de l\'origine, un autre facteur très important pour cette population semi-nomade était la nationalité, la partenace à un casse automatie à un état. La suitoyenité de l\'individus est en termes de référence fondamental entre hausse. Cette identité est à tix de matérialisé à l\'aide de scarification faciale, imposée à chaque enfant au septième jour de son existence. L\'origine pouvait être liée aussi à la nationalité. Par exemple, on pouvait trouver des gobiraws, cartinaois, ou des cartinaois, canaois. Ce qui signifie que des habitants du gobir pouvait être originaire de cartina et que des habitants de cartina pouvaient être originaire de canon. Il existe par ailleurs une forte loyerté des immigrés à Simile qui soit hausse au nom envers leur collectivité d\'accueil. D\'ailleurs, plusieurs classes aristocratiques hausse ont d\'autres origines ethniques. C\'est le cas des Babarberi d\'origine canorie et des bafoulani d\'origine pule. Un premier nous allons parler du cas Babarberi. Pour mieux expliquer ces métissages, nous devons retourner à arrière jusqu\'au septième siècle bien avant l\'époque de Bayadjida. Un effet c\'est à cette époque qu\'elle est le canemme. Cette empire va par la suite atteinte son projet avec Donnema d\'Ibalami, qui réagit de 1220 à 1259, et est en dit l\'empire vers le faisant et le nil. Après la mort de Donnema, le roi me connu des crées du succession et s\'affaiblit au fur et à mesure que les années passées. Au 14e siècle, les sausses et les boladaves venus de l\'Est poussent les canories à se réfiger à l\'ouest du Lac Chagd. Il déplaçait ainsi le centre à l\'ouest, fondaire le roi de Bournu, en 1925, et est à dire le remunation sur toutes les espaces soutiens du Lac Chagd à l\'actuel région de Tilabéry et de la région de Laïr jusqu\'au plateau central du Nigéria. Certains de ces canories imposèrent leur égemonie un pays à Hausa, adoptèrent leur culture et devèrent une classe aristocratique au sein du groupe Hausa. Mais plus tard, quand les cités Hausa vont se libérer de l\'actuel canorie, dont le centre était à Bournu, ils vont conserver une certaine prestige dans la aristocratie. Ils créent par la suite d\'autres Etats Hausa entre le 17e et le 19e siècle, au nord de l\'espace formé par les Hausa Baguay, parmi ces Etats, nous pouvons citer le sultanat de Damagaram, dont la capitale se trouvait dans l\'actuel région des Ineres. Ils firent par la suite des Ampharales dans des Banzabaguay, donc puissant État qui furent conquiver la fin du 18e siècle par les envahiser Gobi Raoua. Ils font être aussi à l\'origine de l\'Etat d\'Area dans le Dalol Maury aux invuyerons du 18e siècle. L\'Etat d\'Area a fuit surtout connu pour sa résistance au reforme Mistypull. Ensuite, nous allons parler des Bafoulani. Mais avant d\'entrer sous le vif du sujet, introduisons un homme qui va être à l\'origine de ses métissages entre Pull et Hausa. Usmandant Foto, dit le Toronto, est né le 15 décembre 1754 à Marata dans l\'actuel Niger. Son père était un Pull originaire du Foto-Toro. Usmand a ainsi euçu d\'une famille de lettre Pull, Hausa Isé et Salé au Gobi depuis le 17e siècle. A 1774, Usmand finit par reprocher au musulman du Gobi de ne pas observer strictement les règles du courant et décide de mener la prédication au Gobi et dans les États-House-Avoisans. A 1795, avec ses partisans à majorter Hausa, il décide de renverser les élites du Gobi désormais considéré comme des noms musulmans. A 1802, après une tentative d\'assassinement, quel Toronto fuit à la Frontière Nord-ouest du Gobi afin de se rallier les normes Pulles que les Hausa appellent les Foulanindagi. Après une ultime tentative de considération, une fois le Serk in Gobi salit de son côté les Serk et des autres États Hausa et déclare la guerre au Toronto. Du côté de l\'autre camp, Danfoujo a érigé Rua et devient le Serkin Musulmi. Il va bien essayer un pouvoir à la fois politique et religieux. Il peut maintenant en ablée au G-Hard, rassembler une armée et la commander. Il a aimé le pays Hausa notamment grâce à la pluie du Sultan Daga-des Mohammed Bakri et de Giri Kaelgrés. Danfoujo est par ailleurs soutenu par la pays-en-arie Hausa qui souffre des taxes et des cités. Le 21 juin 1804, il remporte une victoire sur l\'armée de Yunfa à Tapkin Quato. Il se proclame comme en dehors des guayants et rend sur le Gobi. De 1804 à 1808, il s\'emparque de Kanau et annexe par la suite d\'autres États Hausa, d\'abord qu\'Accinar, en Sud-Saria, Nupé et Kibbi, ainsi que le nord de l\'actuel Cameroun. Il désigne ensuite des émires et les ministrés de l\'Éterritoire Conquis. A 1808, il gagne la bataille décisive d\'Alkalawa lors de la Quelle Yunfa futue. Il fond dans suite la ville de Sukoto à 1809 dont il fait sa capitale. Danfoujo se retrouve désormais à la tête de l\'empere de Sukoto, dans lequel il applique les principes coraniques. Déjà âgé au début de la guerre, il transmet à 1815 le titre de souten de Sukoto à son fils Mohammed Belou. Cette nouvelle aristocratie modifia très sensiblement la structure du monde Hausa. Il y a eu un partage entre conquérants minoritaires dressant les tandats d\'un Islam orthodoxe et Hausa musulmabono. Par la suite, les sedentaires se convertent de plus en plus à l\'Islam et récit progmaat les réformistes adaptés à leur tour la culture Hausa. Les Foulanines Guida, c\'est à dire des aristocrates pils Hausa Isé, modifiaire aussi la structure politique en créant un système d\'alcool au sommet de la pyramide il y a l\'immir ou la chère. Mais ces situations sociopolitiques seront contre plus de nos jours, unigeria, car elle a été sûre à payer par le système de l\'Ende directe de rouls et de britanniques. En or du Hausa lande par contre, une cohabitation entre Hausa et Bousou a abouté aussi à un métisage surtout dans la région des Inders. L\'intégration des bougages à la société Hausa est très récente et paraît résultée d\'une situation historiquement locale. Les bougages sont à réalité des Targi, c\'est-à-dire toareg. Les Hausa les appellent les bas-absines par référence à leur implantation d\'enlaire désignés sous le nom de hapsine. En effet, la société Targi se divise en deux catégories, en distincts dénobles, appelés imagéquennes et des captifs appelés à clanne. Ce sont ces derniers dont le statut est servi, qu\'on désigne sous le nom de bougage. Beaucoup de ces peuples semblent être un passage de toareg et de population noire. Ils étaient dominés par les toareg et magéguennes et assimilés par eux sur la base d\'une structure de leur conférence le statut de captif. Mais aujourd\'hui, beaucoup de bougages sont devenus indépendants, surtout avec l\'aide du colonisateur français, qui monnaie vers la fin du 19e siècle une politique anti-esclavagiste motivée surtout par la résistance des toareg et magéguennes à la colonisation. C\'est ainsi que plusieurs bougages libres se sont installés dans le sud, notamment dans le zindère. Ces derniers ont abandonné la plus grande partie de la culture Targi et n\'appartient plus à cette société. Ils ne parlent plus de ma chèque mais Hausa, on adoptait la culture Hausa et se sont soumis au chef locaux. Mais par contre, ils ont gardé certains mots de vie Targi, comme le goût de l\'élevage, des bovins et agences leur territoire de façon à concilier. Élevage et agriculture.', 'Audio: Histoire des Haoussa', '2026-05-17 14:59:21');
INSERT INTO `EdunaConversation` (`idConversation`, `idUser`, `session_id`, `user_message`, `assistant_response`, `book_context`, `created_at`) VALUES
(51, '94961793', '94961793_6a0826c2477ba4.73020775', 'Transcris et résume : Histoire des Haoussa', 'Nous sommes à 1815. Mohamed Bello, succede à son père le grand Usmantan Foujo, Autrond Lampeer de Sokoto. Desormais, il rend sur une population à majorité hausa. Un grand défi latan, évite à tout prix une rebellion décidée. Les mythiques cités hausa. Créé vers le 10ème siècle par les descendants du legendary Bayadita, elles vont donner naissance au peuple hausa. Un peuple dont le commerce et l\'artisanat vont dessiner le territoire et les propres frontières. Un peuple avec une forte capacité à similatrice qui va au fil des siècles, exercier une forte attraction chez les ethnivoises. Majorité dans le nord du Nigeria et dans le sud du Niger jusqu\'au Lac-Chap, il constitue l\'un des groupes ethniques les plus importantes d\'Afrique. Le Lang classé dans les langues afroasiatiques, elle a trois emplois avec plus de 63 millions de victoires. Les migrations, les brassages, la culture, la diversité et la sociabilité sont des termes qui définissent les hausseurs. Aujourd\'hui nous allons raconter le histoire et comme c\'est de plusieurs peuples, elles commencent par une legendne. Le Sahara, le plus fast-desert au monde. L\'étambérature peut viatendre 55°. Mais une appartueuse était ainsi à réalité, dans un passé lointain, le Sahara était bubblé humide et verdoien. La progrèsive désertification a poussé les populations à s\'installer de par et d\'autres de cette monocente bande aride qui nécessitent d\'avancer au fil des siècles. C\'est ce qui arrive à, selon la legendne, à un certain biajita, originère de Baddad et s\'insuite, installé vers le 17e siècle dans les massifs de la hier. Suit à la décication accrue des cet région, il décidaire de migrer vers le sud. Il arriveur au Roi-Mbécanorie, connu sur le nord de Canemme et qui s\'était formé autour du Lacchade. Au Canemme, biajita épousa une princesse et a eu un fils du nord de Birham. Après un prêtre affrontement avec le Roi du Canemme, il quitte à la terre des Canories et s\'installer à Daura. La legendne veut que biajita étuie un serpent qui vivait dans un puits à Kusugu, suit à Daura. En effet, le serpent terrorisé la population et la privédo ne l\'autorise à ses vies une mal et vannredis. Malgré les avertissements, il s\'arrandit au puits un jédit pour aller chercher de l\'eau. C\'est de là que proviendrez son nom, biajita, qui signifie celui qui ne comprenait pas. Une fois qu\'il commence à appuyer de l\'eau, il comprit pourquoi la population l\'a vertisé car le serpent l\'attaque, mais il lui coupe à la tête avec son épée et c\'est ainsi que le serpent futit. Daura mal a rende d\'aura, avait promis la moitié de son royaume à qui conclurent le serpent du puits. Mais très intelligemment, biajita refuse à lui demander à sa mai un mariage à la place. Du jamais vu, étant donné que l\'héline restait ses debatterts durant toute leur vie et que la société était profondément matriarchale, n\'aient à moi d\'auramment qui se sentait rédevable acceptant sa demande et les deux finir par se marier. De cette ignonne Nakibau, qui a son tour, à laquelle un compagné de leur uncle Biram des mefer de Bau et fils de la princesse Canuri, dirigeait les 7 états haussas, dit haussas bakwai, qui sont connus sous la nôde d\'aura, canot, carcinat, zaria, gobire, ranno et Biram. Cependant, biajita eut un autre fils illégitim avec une conquibine appelée Karbogari, s\'adernit une 7 fils, et c\'est ainsi dirigeait les 7 autres états haussas illégitim surfacie, dit banza bakwai, et son connus sous la nôde gouari, kibi, gorropha, illorine, nupé, illoie, esamphara. Donc l\'histoire des haussas s\'est l\'arrancant entre un des prières originaires de Paltat et une rentre qui rendient sur un peuple de la gildure. Mais l\'histoire des haussas s\'est aussi les cité et d\'à. Au fil des siècles, il développer une langue commune et mentaire des meurs semblables. Leur cité devenait un à un des villes fortifiées qui contrôlait leur campagne environnante respective. Elle s\'organisait selon le système de la Sarauta, un modèle politique tristructurais avec l\'annomination d\'un roi au dirigeant, le Serkin Kassa, qui était conseillé par des dignitaires composés d\'un conseil à chef-concentre, et d\'un bonheur de la gildure. La personne du roi n\'avait aucun caractère sacré et lui parlait dignitaire, il pouvait être déposé par eux. Chaque état était indépendant, mais les incitations du gouvernement et les rapports entre le ville était semblable. La vie était très mouvementée d\'insensuciété en pleine expansion, un système fiscal de la ville qui était un système de la ville qui était un système de la ville qui était un système de la ville qui était un système de la ville qui était un système de la ville et d\'une expansion, un système fiscal très élaboré, contribuant à la naissance d\'une économie complexe dans laquelle l\'agriculture l\'occomérす mais aussi l\'artisanat se communit pour assurer le développement des villes. Il y avait une telle production de chefs que les cités étaient protégés par des rampards qui pouvaient atteindre 15 mètres. Ce système économique qui était très profitable au mensaud Sarantha, c\'est un des renombles, elle est profité aussi à un autre dans le social qui l\'a même donné de naissance. il s\'agit des hommes riches ou masous arziquis. Il dominait le marché et disposé de capitaux importants et de clientèle extrêmement ramifié, mais la plupart étaient des masous farautas. À côté de la aristocratie et de la bourgeoisie, nous avions les hommes pauvres appelés masous talauti, qui, en paraphrasingaro max, étaient les proletters de l\'époque exploités sous pays. D\'après la chronique de canon, il semble que ce soit des lettres et commerçants malinqués, venus du mali au XIV siècle, qui aurait introduit l\'islam à canon et dans l\'ensemble du pays hausse. Si elle commençait à manning, fûret d\'ailleurs à l\'origine de l\'intégration des cités hausse aux commerces transahariens. La ville de canon et de Cartina était de loin les plus riches. Ils en même que du la reine de Mansa Musa, homony, ces villes étaient rattachées à l\'Ampire. Des l\'or, la culture hausa, spéciale l\'infinance directe de la civilisation méditerranéeenne, notamment grâce au caravan, qui nécessitait de relier les cités et ta hausa aux porz arabes et aux colonies de marchands tripolitaires ou fonds de la chale algérie, établissueuse et marchée. Elles ont pour laquelle la langue hausa est riche d\'emplois à la langue arabe. Cette culture fortement assimilationniste va exercer aussi une attraction croissante sur les populations voisines dont elles intègrent progressivement des fractions de plus en plus nombreuses. Le 12 avril 1993, Muhammad Tourette, renvers sonny barot, fils de sonny alibair, lors de la bataille d\'Anfaru, est devient un peu heure du sangay. Il a créé ici la dénascité Asquia. Entre 1501 et 1501, il occupe l\'Indy sans part de l\'Empire du Mali et mène ensuite la guerre sainte contre le roi à l\'immisternancérie de Valleaux-Moussi. En 1912, il fait alliance avec Kantaroi à l\'équibil, un des banzapakwai et marcher vers l\'Est où il annexe les Etats-Hausa de vaccina de Gobi-Ré de Kanon. Cette intégration des cités Hausa de l\'ouest des dinors dans l\'Empire de Gaou va mettre à marche la grande machine à simulationniste des Hausa. Beaucoup de sangay surtout de l\'étniserman vont être aussaisés avec le temps. Mais le processus de aussaisisation a touché aussi d\'autres étnés comme les Beriberi ou Kanori à l\'Est, les Targi, au nord, et les Guari, Noupé et Europa au Sud ainsi que la fraction arabe de la région Sud-Aneze. La simulation de ces peuples va contribuer davantage à amplifier la distribution géographique de Hausa et accréer une grande diversité ethnique au sein du groupe. Le 16e siècle est marqué par la bravrenne Aminat de Zazau dont son reigne indaté vers 1666. Mohamed Belou, sur le temps de ce couteau, dirait plus tard à 1866 dans son livre intitulé Ifak almei Sur que la rène Aminat a forcé Kaksina, Kanon et d\'autres régions Hausa allouant rendre hommage et qu\'il a été la première à être à lire un gouvernement réunissant tous les Hausa. Mais le terme Hausa n\'était utilisé qu\'à partir du 16e siècle de notre air car les gens se nommaient même en fonction de la ville du Roi, un spécifique doile provenie, vu que le peuple Hausa était en mouvement constant. Ces individus aient à le même corégin s\'organiser un groupe et consulier un autre critère de division sociale. Ces groupes d\'origine étaient désignées selon les Galettes sous les termes d\'Asili, Zoria, Iridandhi ou Kabila. Les individus de chaque groupe étaient liés par le même gâteau, c\'est-à-dire héritage patrilinaire. La parlanche à ces groupes est considérée comme le natuage proche de celle de la parenté. Leur mâmre se qualifit de un ou à automatie frère de même mer. Dans certaines situations, ses groupes d\'origine pouvaient représenter des cartes signées littéralement métier héritaires. A cette époque, on ne pouvait être mal à moi, ou m\'affaraure-ci, ou faire d\'autres méchés que si l\'on avait hérité de l\'accès à ses professions. Une situation qui persiste jusqu\'à nos jours en s\'il temps à disparaître. En plus de l\'origine, un autre facteur très important pour cette population semi-nomade était la nationalité, la partenace à un casse à automatie à un état. La suitoyenité de l\'invue est un terme de référence fondamental entre hausse. Cette identité étatique se matérialisée à l\'aide de scarification faciale, imposée à chaque affin ou septième juge de son existence. Leur gine pouvait être lié aussi à la nationalité, par exemple, on pouvait trouver des goberaois, cartesinaois, ou des cartesinaois, canaois. Ce qui signifie que des habitants du gobeur pouvait être originaire de carcinat et que des habitants de carcinat pouvaient être originaire de cano. Il existe par ailleurs une forte loyerté des immigrés assimilés qui se soient hausse à ou non envers leur collectivité d\'accueil. D\'ailleurs, plusieurs classes aristocratiques hausse ont d\'autres origines ethniques. C\'est le cas des Babarberi d\'origine canorie et des bafoulani d\'origine pule. En premier, nous allons parler du cas Babarberi. Pour mieux expliquer ces métissages, nous devons retourner à Narière jusqu\'au septième siècle, puis en avant l\'époque de Bayadjida. Un effet c\'est à cette époque qu\'en est le canemme. Cette empire va parler suite à Tenson à pojée avec Donnema, d\'Ibalami, qui réagient de 1220 à 1259 et est en dit l\'empire vers le faisant et le nil. Après la mort de Donnema, le roi me connu des crées de succession et sa faibli au fur et à mesure que les années passées. Au XIV siècle, les sausses et les pouladaves venus de l\'Est poussent les canories à se réfiger à l\'ouest du Lac-Chad. Il déplaçait ainsi le centre à l\'ouest, fondaire le roi me de Bournu, en 1935, et à étendre leur domination sur toutes les espaces soutient du Lac-Chad à l\'actuel région de Tila-Béry et de la région de Laïr jusqu\'au plateau central du Nigeria. Certains de ces canories imposèrent leur égemonie un pays hausse à adopter leur culture et dever une place aristocratique au sein du groupe hausse. Mais plus tard, quand les cités hausse à l\'ouest, vont se libérer de la tutelle canorie, dont le centre était à Bournu, ils vont conserver une certaine prestige dans la aristocratie. Ils créèrent par la suite d\'autres états hausse à l\'interlure du septième et les 19e siècle, au nord de l\'espace formé par les hausse à Baguay, parmi ces états, nous pouvons citer le sultanat de Dama-Garam, dont la capitale se trouvait dans l\'actuel région des Inders, ils firent par la suite des Ampharales, des Banzabagwai, dont puissant état qu\'ils furent conqués vers la fin du 18e siècle par les envahisseurs Gobi Raouh, ils vont être aussi à l\'origine de l\'état d\'Araewa dans le Dallol Mauri aux invités du 12e siècle. L\'état d\'Araewa fut surtout connu pour sa résistance au reforme Mistyple. Ensuite, nous allons parler des Bafoulani. Mais avant d\'entrer sous le vif du sujet, introduisons un nom qui va être à l\'origine de ce métisage entre Poul et Hausa. Usman dan Fuchou, d\'Irottorodo, est né le 15 décembre 1754 à Marata, dans l\'actuel Niger. Son père était un Poul originaire du Fout-A-Toro. Usman est ainsi issue d\'une famille de lettre Poul hausa Isé et Sellehogobir depuis le 17e siècle. À 1764, Usman finit par reprocher au musulman du Gobi de ne pas observer strictement les règles du courant et décide de mener la prédication au Gobi et dans les États-Haus en voisin. À 1795, avec ses partisans à Majoujute-Hausa, il décide de renverser les élites du Gobi désormais considéré comme des non musulmans. À 1802, après une tentative d\'assassinementquée, le Torodo fuit à la French Air Northwest du Gobi afin de s\'arrêter les numéres belles que les haussas appellent les Foulanindagi après une ultime tentative de considération, une faille, le Serkin Gobi, s\'allait de son côté les Serkides des autres États-Hausa et déclarait la guerre au Torodo. Du côté de l\'autre camp, dans le Foujou Air G. R.W. il devait le Serkin Musulmis. Il va bien essayer un pouvoir à la fois politique et religieux. Il peut maintenant en ablée au G-Had, rassembler une armée et la commander. Il a un veil paye Hausa, notamment grâce à la pluie du Sultan Daga-des, Mohammed Bakri et des guerriers K.Gress. Dans le Foujou, et par ailleurs soutenu par la pays-enerie Hausa, qui souffre des taxes de cité. Le 21 juin 1804, il ramporte une victoire sur l\'armée de Yunfa à Takin Kwato. Il se proclame commander de Guayan et réunit sur le Gobi. De 1804, en 1808, il s\'emparque de Kanho et annexe par la suite d\'autres Etats-Hausa, d\'abord qu\'Accinar, en Sud-Saria, Nupé et Kibbi, ainsi que le nord de l\'actuel Kamerun. Il désigne ensuite des émirs pour administrer les territoires conqués. En 1808, il gagne la bataille décisive d\'Alkalawa lors de laquelle Yunfa futit. Il fond dans suite la ville de Sukoto en 1809, dont il fait sa capitale. Dans le Foujou, se retrouve désormais à la tête de l\'Ampire de Sukoto, dans lequel il applique les principes coraniques. Déjà âgé au début de la guerre, il transmette, en 1815, le titre de souten de Sukoto à son fils Mohamed Belou. Cette nouvelle aristocratie modifie à très sensibilement la structure du monde Hausa. Il est lui un partage entre conquérants minoritaires, dressant les tandats de l\'Islam orthodoxe et Hausa musulman Bruno. Par la suite, les sedentaires se convertire de plus en plus à l\'Islam et récit progma les réformistes adaptés à leur pour la culture Hausa. Les Foulanins Guida, c\'est-à-dire des aristocrats de Pelle Hausa Isé, modifient aussi la structure politique à créant un système d\'alquel ou sommet de la pyramide il y a l\'immir ou la chère. Mais ses situations sociopolitiques se rendent contre plus de nos jours au Nigeria, quand elle a été sûre tu appuyer vers le système de l\'indirecte ou le débritainique. En ordre du Hausa lande par contre, une cohabitation entre Hausa et Bousou a abouti aussi à un métissage, surtout dans la région des Inders. L\'intégration des bogages à la société Hausa est ré récente et paraît résultée d\'une situation historiquement locale. Les bogages sont à réaliter des targis c\'est à dire toareg. Les Hausa les appellent les bas absines par référence à leur implantation dans l\'air désigné sous l\'unode absine. En effet, la société targis se divise en deux catégories en distincts dénaubles, appelés imagéquaines et des captifs appelés à clan. Ce sont ces derniers, dont le statut est servile, qu\'on désigne sous l\'unode bogage. Beaucoup de ses peuples semblent être un passage de toareg et de population noire. Une zette de minais par les toareg émagequaines est assimilée par eux sur la base d\'une structure de leur conférence le statut de captif. Mais aujourd\'hui, beaucoup de bogages sont devenus indépendants, surtout avec l\'aide du colonisateur français, qui monnaie vers la fin du 19e siècle, une politique antisclavagiste motivée surtout par la résistance des toareg émagequaines à la colonisation. C\'est ainsi que plusieurs bogages libres se sont installés dans le sud notamment dans le Zinders. Ces derniers ont abandonné la plus grande partie de la culture targie, et n\'appartient plus à cette société. Ils ne parlent plus à ma chèque mais Hausa, ont adopté la culture Hausa et se sont soumis au chef locaux. Mais par contre, ils ont gardé certains mots de vie targie, comme le goût de l\'élevage des bovins et agences leur territoire de façon à concilier, élevage et agriculture.', 'Audio: Histoire des Haoussa', '2026-05-17 16:40:51'),
(52, '94961793', '94961793_6a0826c2477ba4.73020775', 'Bonjour, quelle est la capitale du Niger ?', 'Clés: status,message', NULL, '2026-05-17 17:01:41'),
(53, '94961793', '94961793_6a0826c2477ba4.73020775', 'quelle est la capitale du Niger ?', 'Clés: status,message', NULL, '2026-05-17 17:02:20'),
(54, '94961793', '94961793_6a0826c2477ba4.73020775', 'Explique en détail le livre \"Le temps sous contrôle\". Présente les thèmes principaux, l\'auteur, le résumé et ce qu\'on peut retenir.', 'Erreur : CUDA out of memory. Tried to allocate 3.15 GiB. GPU 0 has a total capacity of 14.56 GiB of which 1.63 GiB is free. Including non-PyTorch memory, this process has 12.93 GiB memory in use. Of the allocated memory 11.50 GiB is allocated by PyTorch, and 1.30 GiB is reserved by PyTorch but unallocated. If reserved but unallocated memory is large try setting PYTORCH_ALLOC_CONF=expandable_segments:True to avoid fragmentation.  See documentation for Memory Management  (https://pytorch.org/docs/stable/notes/cuda.html#environment-variables)', 'Livre: Le temps sous contrôle', '2026-05-17 17:02:49'),
(55, '94961793', '94961793_6a0826c2477ba4.73020775', 'bonjour', 'Clés: status,message', NULL, '2026-05-17 17:25:09'),
(56, '94961793', '94961793_6a0826c2477ba4.73020775', 'bonjour', 'Clés: status,message', NULL, '2026-05-17 17:25:34'),
(57, '94961793', '94961793_6a0826c2477ba4.73020775', 'Explique en détail le livre \"L\'informatique : La voie express vers l\'indépendance\". Présente les thèmes principaux, l\'auteur, le résumé et ce qu\'on peut retenir.', 'Bonjour ! En tant qu\'Eduna, l\'IA éducative d\'EduNiger, je vais vous présenter l\'analyse du document que vous m\'avez fourni concernant le livre \"L\'informatique : La voie express vers l\'indépendance\".\n\nVoici un résumé détaillé des thèmes principaux, de l\'auteur, du résumé et des points clés à retenir, basés uniquement sur le texte fourni :\n\n### **Présentation de l\'Auteur**\n\nL\'auteur est **Aboubacar Hamidou Ridouane**, âgé de 24 ans. Il est actuellement en préparation de son mémoire de Master 2 en Informatique Fondamentale et Appliquée à l\'Université Abdou Moumouni de Niamey. Il est développeur mobile full stack, ce qui signifie qu\'il maîtrise à la fois :\n*   Le développement côté client (mobile, interface utilisateur).\n*   Le développement côté serveur (backend, gestion des données, API).\nCette double compétence lui permet de concevoir des solutions complètes et intégrées pour répondre aux besoins du numérique. Parallèlement, il est co-fondateur de la start-up **Ninotech**, une jeune entreprise technologique au Niger, dont l\'ambition est d\'apporter des solutions innovantes adaptées aux réalités locales et de favoriser l\'émergence d\'acteurs du numérique.\n\n### **Résumé et Thèmes Principaux du Livre**\n\nCe livre s\'adresse spécifiquement aux **jeunes bacheliers nigériens** qui font face à des choix d\'orientation difficiles et à une méconnaissance des opportunités offertes par l\'informatique.\n\n**1. La Prise de Conscience (Démystifier l\'Informatique)**\n*   **Le vide post-bac :** Le livre reconnaît la confusion et le manque de direction après le baccalauréat. Il dénonce la pression sociale et le fait que beaucoup réduisent l\'informatique à la simple maîtrise de logiciels de bureautique (traitement de texte, tableurs), alors que c\'est un domaine beaucoup plus vaste.\n*   **Ce qu\'on ne vous a jamais dit :** L\'informatique est présentée comme un domaine bien plus large et porteur d\'avenir, englobant :\n    *   Le développement logiciel (applications mobiles, sites web, logiciels métiers).\n    *   Le design numérique (interfaces utilisateurs, expérience utilisateur).', 'Livre: L\'informatique : La voie express vers l\'indépendance', '2026-05-17 17:27:25'),
(58, '94961793', '94961793_6a0826c2477ba4.73020775', 'Transcris et résume : Histoire des Haoussa', 'Nous sommes à 1815. Mohamed Bello, succede à son père le grand Usmantan Foujo, Autrond Lampeer de Sokoto. Desormais, il rend sur une population à majorité hausa. Un grand défi latan, évite à tout prix une rebellion décidée. Les mythiques cités hausa. Créé vers le 10ème siècle par les descendants du legendary Bayadita, elles vont donner naissance au peuple hausa. Un peuple dont le commerce et l\'artisanat vont dessiner le territoire et les propres frontières. Un peuple avec une forte capacité à similatrice qui va au fil des siècles, exercier une forte attraction chez les ethnivoises. Majorité dans le nord du Nigeria et dans le sud du Niger jusqu\'au Lac-Chap, il constitue l\'un des groupes ethniques les plus importantes d\'Afrique. Le Lang classé dans les langues afroasiatiques, elle a trois emplois avec plus de 63 millions de victoires. Les migrations, les brassages, la culture, la diversité et la sociabilité sont des termes qui définissent les hausseurs. Aujourd\'hui nous allons raconter le histoire et comme c\'est de plusieurs peuples, elles commencent par une legendne. Le Sahara, le plus fast-desert au monde. L\'étambérature peut viatendre 55°. Mais une appartueuse était ainsi à réalité, dans un passé lointain, le Sahara était bubblé humide et verdoien. La progrèsive désertification a poussé les populations à s\'installer de par et d\'autres de cette monocente bande aride qui nécessitent d\'avancer au fil des siècles. C\'est ce qui arrive à, selon la legendne, à un certain biajita, originère de Baddad et s\'insuite, installé vers le 17e siècle dans les massifs de la hier. Suit à la décication accrue des cet région, il décidaire de migrer vers le sud. Il arriveur au Roi-Mbécanorie, connu sur le nord de Canemme et qui s\'était formé autour du Lacchade. Au Canemme, biajita épousa une princesse et a eu un fils du nord de Birham. Après un prêtre affrontement avec le Roi du Canemme, il quitte à la terre des Canories et s\'installer à Daura. La legendne veut que biajita étuie un serpent qui vivait dans un puits à Kusugu, suit à Daura. En effet, le serpent terrorisé la population et la privédo ne l\'autorise à ses vies une mal et vannredis. Malgré les avertissements, il s\'arrandit au puits un jédit pour aller chercher de l\'eau. C\'est de là que proviendrez son nom, biajita, qui signifie celui qui ne comprenait pas. Une fois qu\'il commence à appuyer de l\'eau, il comprit pourquoi la population l\'a vertisé car le serpent l\'attaque, mais il lui coupe à la tête avec son épée et c\'est ainsi que le serpent futit. Daura mal a rende d\'aura, avait promis la moitié de son royaume à qui conclurent le serpent du puits. Mais très intelligemment, biajita refuse à lui demander à sa mai un mariage à la place. Du jamais vu, étant donné que l\'héline restait ses debatterts durant toute leur vie et que la société était profondément matriarchale, n\'aient à moi d\'auramment qui se sentait rédevable acceptant sa demande et les deux finir par se marier. De cette ignonne Nakibau, qui a son tour, à laquelle un compagné de leur uncle Biram des mefer de Bau et fils de la princesse Canuri, dirigeait les 7 états haussas, dit haussas bakwai, qui sont connus sous la nôde d\'aura, canot, carcinat, zaria, gobire, ranno et Biram. Cependant, biajita eut un autre fils illégitim avec une conquibine appelée Karbogari, s\'adernit une 7 fils, et c\'est ainsi dirigeait les 7 autres états haussas illégitim surfacie, dit banza bakwai, et son connus sous la nôde gouari, kibi, gorropha, illorine, nupé, illoie, esamphara. Donc l\'histoire des haussas s\'est l\'arrancant entre un des prières originaires de Paltat et une rentre qui rendient sur un peuple de la gildure. Mais l\'histoire des haussas s\'est aussi les cité et d\'à. Au fil des siècles, il développer une langue commune et mentaire des meurs semblables. Leur cité devenait un à un des villes fortifiées qui contrôlait leur campagne environnante respective. Elle s\'organisait selon le système de la Sarauta, un modèle politique tristructurais avec l\'annomination d\'un roi au dirigeant, le Serkin Kassa, qui était conseillé par des dignitaires composés d\'un conseil à chef-concentre, et d\'un bonheur de la gildure. La personne du roi n\'avait aucun caractère sacré et lui parlait dignitaire, il pouvait être déposé par eux. Chaque état était indépendant, mais les incitations du gouvernement et les rapports entre le ville était semblable. La vie était très mouvementée d\'insensuciété en pleine expansion, un système fiscal de la ville qui était un système de la ville qui était un système de la ville qui était un système de la ville qui était un système de la ville qui était un système de la ville qui était une expansion, un système fiscal très élaboré, contribuant à la naissance d\'une économie complexe dans laquelle l\'agriculture l\'ocommerce mais aussi l\'artisamment se communit pour assurer le développement des villes. Il y avait une telle production de chefs que les cités étaient protégés par des rampards qui pouvaient atteindre 15 mètres. Ce système économique qui était très profitable au mensaud Sarata c\'était à des renombles, elle est profite aussi à un autre dans le social qui l\'a l\'umime donné de naissance. Il s\'agit des hommes riches ou mensauds arziquis. Il dominait le marché et disposait de capitaux importants et de clientèle extrêmement ramifié mais la plupart étaient des mensauds Sarata. À côté de la aristocratie et de la bourgeoisie, nous avions les hommes pauvres appelés mensauds Talauchi, qui en parafrazant Karl Marx était les proletters de l\'époque exploités sous pays. Après la chronique de canon, il semble que ce soit des lettres et commerçants malinqués, venus du mali au 14 siècle, qui ont réintroduits l\'islam à canon et dans l\'ensemble du pays hausse. C\'est comme un dingue, fur d\'ailleurs à l\'origine de l\'intégration des cités hausse et au commerce trans-sahariens. La ville de canon et de Cartina était de loin les plus riches. Ils en même que du laurante mensamousa homony, c\'est ville était de rattaché à l\'Ampire. Des l\'or, la culture hausse, est l\'infinance directe de la civilisation méditerranée en automat grâce au caravan, qui ne saisait de régler les cités et à hausse aux pours arabes et aux colonies de marchands tripolitaires au fondant de l\'actual Algérie, établissueuse et marchée. Elles ont pour laquelle la langue hausse est riche d\'emplois à la langue arabe. Cette culture fortement assimilationniste va exercer aussi une attraction croissante sur les populations voisines dont elles intègrent progressivement des fraquements de plus en plus nombreuses. Le 12 avril 1993, Mohammed Tourette, ravère sonibaro fils de soniai bière lors de la bataille d\'Ampharu et devient amperer du sangai, créant ainsi la dénascité Asquia. Entre 1500 et 1500 un, il occuple d\'Indy sans part de l\'Empire du Mali et même ensuite la guerre singe tout contre le roi à les mystes nasseries du voyom Moussi. En 1912, il fait alliance avec Kantaro à l\'équibil, un des banzapacois et marches vers l\'Est, où il annexe les Etats-Hausas de carcinats de gobir et de canon. Cette intégration des cités Haussas, de l\'ouestée du Nord dans l\'Empire de Gao, va mettre à marche la grande machine asmirationniste des Haussas. Beaucoup de sangais, surtout de l\'étniserman, vont être aussaisés avec le temps. Mais le processus de aussaisisation, a touché aussi d\'autres étnies comme les Beriberis ou Kanuri à l\'Est, les Targi, au Nord, et les Guari, noupé et Europa au Sud, ainsi que la fraction arabe de la région Sud-Aneze. La simulation de ces peuples va contribuer davantage à amplifier la distribution géographique des Haussas et à créer une grande diversité et technique au sein du groupe. Le 16e siècle est marqué par la bravrenne Aminat de Zazau, dont son règne est natée vers 1666. Mohamed Belaud, soutenre de ce couteau, dit la plus tard, en 1836, dans son livre intitulé, Ifac Almey Sur, que la Rène Aminat a forcé qu\'à tous les Haussas, à l\'urandre homage et qu\'il a été la première à être à lire un gouvernement réunissant tous les Haussas. Mais le terme Haussas n\'était utilisé qu\'à partir du 16e siècle de notre air qu\'à les gens, ce nommé, même en fonction de la ville du Roi, un spécifique doile provené, vu que le peuple Haussas était un mouvement constant. Ces individus aient à le même corregine, s\'organisait un groupe et construit un autre critère de division sociale. Ces groupes de régines étaient désignées selon les Dalettes, sous les termes de Asili, Zoria, Iridandhi, ou Kabila. Les individus de chaque groupe étaient liés par le même gâteau, c\'est-à-dire, héritage patrilinaire. La part de l\'ence à ces groupes est considérée comme de natuges proches de celles de la parenté. Leur mamele, se qualifie de un ou à, automatie, frère de même mer. Dans certaines situations, ces groupes de régines pouvaient représenter des cartes si ils viennent littéralement métier héritaires. À cette époque, on ne pouvait être malame ou m\'affaraure-ci ou faire d\'autres méchés que si l\'on avait hérité de l\'accès à ses professions. Une situation qui persiste jusqu\'à nos jours dans le silitaire disparaître. En plus de l\'origine, un autre facteur très important pour cette population semi-nomade était la nationalité, la partenace à un casse automatique à un état. La suitoyenité de l\'individu est en termes de référence fondamental entre hausse. Cette identité est à tix de matérialisé à l\'aide de scarification faciale, imposée à chaque enfant au septième jour de son existence. L\'origine pouvait être liée aussi à la nationalité. Par exemple, on pouvait trouver des gobirawa, katinawa, ou des katinawa, kanawa. Ce qui signifie que des habitants du gobir pouvaient être originaires de katinawa et que des habitants de katinawa pouvaient être originaires de kanon. Il existe par ailleurs une forte loyerité des immigrés à Simile qui soit hausse au nom et qui soit hausse au nom envers leur collectivité d\'accueil. D\'ailleurs, plusieurs classes aristocratiques hausse ont d\'autres origines ethniques. C\'est le cas des Babarberi, d\'origine kanori et des bafoulani d\'origine pule. Un premier nous allons parler du cas Babarberi. Pour mieux expliquer ce métisage, nous devons retourner à l\'arrière jusqu\'au septième siècle bien avant l\'époque de Bayadjida. Un effet c\'est à cette époque qu\'elle est le canemme. Cette empire va par la suite atteinte son appogée avec Donnema d\'Ibalami, qui régnant de 1220 à 1259 et est en dit l\'empire vers le faisant et le nil. Après la moire de Donnema, le roi me connu des crées du succession et s\'affaiblit au fur et à mesure que les années passées. Au 14e siècle, les sausses et les boladaves venus de l\'Est pouce les kanori à se réfiger à l\'ouest du Lacchard. Il déplaçait ainsi le centre à l\'ouest, fondaire le roi de Bournu, en 1925, et est en dit l\'ordomination sur toutes les espaces aux soutiens du Lacchard à l\'actuel région de Tilabéry et de la région de Laïr jusqu\'au plateau central du Nigeria. Certains de ces kanori imposèrent leur égemonie un pays à Hausa, adoptèrent leur culture et devèrent une classe aristocratique au sein du groupe Hausa. Même plus tard, quand les cités Hausa vont se libérer de l\'actuel canori, dont le centre était à Bournu, ils vont conserver une certaine prestige dans la aristocratie. Ils créent par la suite d\'autres états Hausa entre le 17e et le 19e siècle, au nord de l\'espace formé par les Hausa Baguay, parmi ces états, nous pouvons citer le sultanat de Damagaram, dont la capitale se trouvait dans l\'actuel région des Ineres. Ils firent par la suite des Ampharales dans des Banzabaguay, donc puissant état qui furent conquis vers la fin du 18e siècle par les envahisseurs Gobi Raouh. Ils font être aussi à l\'origine de l\'état d\'Araewa dans le Dalol Mauri, aux invités du 12e siècle. L\'état d\'Araewa fut surtout connu pour sa résistance au reforme miste-pul. Ensuite, nous allons parler des Bafoulani, mais avant d\'entrer sous le vif du sujet, introduisons un homme qui va être à l\'origine de ses médecins entre Pul et Hausa. Usment dans le Foto, dit le Toronto, est né le 15 décembre 1754 à Marata dans l\'actuel Niger. Son père était un pul originaire du Foto-Toro. Usment est ainsi issu d\'une famille de lettre Pul, Hausa Isé et Salé au Gobi, depuis le 17e siècle. A 1774, Usment finit par reprocher au musulman du Gobi, de ne pas observer strictement les règles du courant et d\'ici de mener la prédication au Gobi et dans les États-House-Avoisans. A 1795, avec ses partisans à majorter Hausa, il décide de renverser les élites du Gobi désormais considéré comme des noms musulmans. A 1802, après une tentative d\'assassinement, quel Toronto fuit à la Frontière Nord-ouest du Gobi, afin de se rallier les normes bulles que les Hausa appellent les Foulanindagi. Après une ultime tentative de considération, une fois le Serk in Gobi salit de son côté les Serk et des autres États Hausa et déclare la guerre au Toronto. Du côté de l\'autre camp, Danfoujo a érigé R.O. et devient le Serkin Musulmi. Il va bien essayer un pouvoir à la fois politique et religieux. Il peut maintenant en ablée au G-Hard, ressembler une armée et la commander. Il a aimé le pays Hausa notamment grâce à la pluie du Sultan Daga-des, Mohammed Bakri, et de guerrier Calgrès. Danfoujo est par ailleurs soutenu par la paysanerie Hausa qui souffre des taxes de cité. Le 21 juin 1804, il remporte une victoire sur l\'armée de Yulfa à Tapkin Quato. Il se proclame comme en dehors des guayants et rentre sur le Gobi. De 1804 à 1808, il s\'emparque de Kanau et annexe par la suite d\'autres États Hausa, d\'abord qu\'à destination à Sud-Saria, Nupé et Kibbi, ainsi que le nord de l\'actuel Cameroun. Il désigne ensuite des émirs pour administrer les territoires conqués. A 1808, il gagne la bataille décisive d\'Alkalawa lors de la Calguin-Fa future. Il fond dans suite la ville de Sukoto à 1809 dont il fait sa capitale. Danfoujo se retrouve désormais à la tête de l\'empere de Sukoto, dans lequel il applique les principes coloniques. Déjà âgés au début de la guerre, il transmette à 1815 le titre de souten de Sukoto à son fils Mohammed Belou. Cette nouvelle aristocratie modifiaissance civilement la structure du monde Hausa. Il y a une partage entre conquérants minoritaires dressant les tandats de l\'Islam orthodoxe et Hausa musulmabunot. Par la suite, les sedentaires se convertir de plus en plus à l\'Islam et récit progmaat les réformistes adoptés à l\'heure pour la culture Hausa. Les Foulanins guida, c\'est à dire des aristocrates pils Hausa Isé, modifiaire aussi la structure politique en créant un système d\'alcool au sommet de la pyramide il y a l\'immir ou la chère. Mais ces situations sociopolitiques seront contre plus de nos jours, unigeria, car elle a été sûre à appuyer par le système de l\'Ende directe-Rules des Britanniques. En or du Hausa lande par contre, une cohabitation entre Hausa et Bousou a abouté aussi à un métissage surtout dans la région des Inders. L\'intégration des bougages à la société Hausa est très récente et paraît résultée d\'une situation historiquement locale. Les bougages sont à réalité des Targis, c\'est-à-dire le poireg. Les Hausa les appellent les bas-absines par référence à leur implantation d\'enlaire désignés sous le nom de hapsine. En effet, la société Targis se divise en deux catégories, en distincts dénobles, appelés imagéquaines et des captifs appelés à clan. Ce sont ces derniers dont le statut est servi, qu\'on désigne sous le nom de bougage. Beaucoup de ses peuples semblent être un passage de toareg et de population noire. Ils étaient dominés par les toareg et magéguennes et assimilés par eux sur la base d\'une structure de leur conférence le statut de captif. Mais aujourd\'hui, beaucoup de bougages sont devenus indépendants, surtout avec l\'aide du colonisateur français, qui monnaie vers la fin du 19e siècle une politique anti-esclavagiste motivée surtout par la résistance des toareg et magéguennes à la colonisation. C\'est ainsi que plusieurs bougages libres se sont installés dans le sud, notamment dans le zindère. Ces derniers ont abandonné la plus grande partie de la culture Targis et n\'appartient plus à cette société. Ils ne parlent plus de ma chèque mais Hausa, ont adopté la culture Hausa et se sont soumis au chef locaux. Mais par contre, ils ont gardé certains mots de vie Targis comme le goût de l\'élevage des beaux-vains et agences leur territoire de façon à concilier. Elle a vaché agriculture.', 'Audio: Histoire des Haoussa', '2026-05-17 17:30:43'),
(59, '94961793', '94961793_6a0826c2477ba4.73020775', 'propose moi un code en java', 'Bonjour ! En tant qu\'Eduna, je peux vous aider avec ça.\n\nPuisque vous me demandez un **code en Java** en relation avec le livre **\"Histoire des Haoussa\"**, je vais vous proposer un exemple de code qui pourrait servir à **modéliser ou gérer des informations** tirées de cet ouvrage.\n\nComme je n\'ai pas le contenu exact du livre sous les yeux, je vais créer une structure simple (une classe) pour représenter des **personnages** ou des **événements historiques** haoussa.\n\nVoici un exemple de code Java :\n\n```java\nimport java.util.ArrayList;\nimport java.util.List;\n\n/**\n * Classe représentant un événement historique ou une figure clé de l\'histoire des Haoussa.\n */\npublic class HistoriqueHaoussa {\n\n    private String titre;\n    private String date;\n    private String description;\n    private String region;\n\n    // Constructeur\n    public HistoriqueHaoussa(String titre, String date, String description, String region) {\n        this.titre = titre;\n        this.date = date;\n        this.description = description;\n        this.region = region;\n    }\n\n    // Getters et Setters (pour accéder aux données)\n    public String getTitre() {\n        return titre;\n    }\n\n    public String getDate() {\n        return date;\n    }\n\n    public String getDescription() {\n        return description;\n    }\n\n    public String getRegion() {\n        return region;\n    }\n\n    @Override\n    public String toString() {\n        return \"--- Événement Historique ---\" +\n               \"\\nTitre: \" + titre +\n               \"\\nDate: \" + date +\n               \"\\nRégion: \" + region +\n               \"\\nDescription: \" + description;\n    }\n\n    /**\n     * Méthode pour ajouter un nouvel événement à une liste.\n     * @param historique L\'objet HistoriqueHaoussa à ajouter.\n     */\n    public void ajouterHistorique(HistoriqueHaoussa historique) {\n        // Dans un vrai système, on pourrait vérifier la date pour éviter les doublons\n        System.out.println(\"Ajout de l\'événement : \" + historique.getTitre());\n    }\n\n    public static', 'Livre: Histoire des Haoussa', '2026-05-17 17:36:13'),
(60, '94961793', '94961793_6a0826c2477ba4.73020775', 'propose moi un code en c qui affiche salut', '```c\n#include <stdio.h>\n\nint main() {\n    printf(\"salut\\n\");\n    return 0;\n}\n```', NULL, '2026-05-17 17:37:30'),
(61, '94961793', '94961793_6a0826c2477ba4.73020775', 'explique moi la force gravitationnelle', 'La force gravitationnelle est une **force fondamentale** de l\'univers, décrite par la **Loi universelle de la gravitation** de **Isaac Newton**.\n\nVoici une explication détaillée de ce qu\'elle est, de son principe et de ses implications :\n\n---\n\n### 1. Définition de la Force Gravitationnelle\n\nLa force gravitationnelle est l\'**attraction mutuelle** qui existe entre deux corps qui possèdent une masse. En termes simples, c\'est la force qui fait que tout objet ayant une masse attire tout autre objet ayant une masse, et inversement.\n\n* **Elle est toujours attractive :** Deux objets massifs s\'attirent.\n* **Elle est proportionnelle à la masse :** Plus les objets ont une grande masse, plus la force gravitationnelle entre eux est forte.\n* **Elle est inversement proportionnelle au carré de la distance :** La force diminue très rapidement à mesure que la distance entre les objets augmente. Si vous doublez la distance, la force entre eux devient quatre fois plus faible.\n\n### 2. La Loi de la Gravitation Universelle de Newton\n\nLa formule mathématique qui quantifie cette force est la **Loi de la Gravitation Universelle** :\n\n$$\\text{F} = G \\frac{m_1 m_2}{r^2}$$\n\nOù :\n* **$F$** est l\'intensité de la force gravitationnelle.\n* **$G$** est la **constante gravitationnelle universelle** (une constante physique qui mesure la force de gravité, dont la valeur est très petite, mais qui est cruciale pour les calculs).\n* **$m_1$** et **$m_2$** sont les masses des deux corps.\n* **$r$** est la distance entre les centres de masse des deux corps.\n\n**Ce que cette formule nous dit :**\n1. **Plus les objets sont massifs ($m_1 m_2$ élevé), plus la force est grande.**\n2. **Plus les objets sont éloignés ($r$ grand), plus la force est faible (elle décroît rapidement).**\n\n### 3. Implications et Exemples Clés\n\n#### A. La Gravité Terrestre\nLa force gravitationnelle est ce qui nous maintient sur Terre. La masse de la Terre est immense, et elle exerce une attraction constante sur toute matière présente à sa surface, nous retenant.\n\n', NULL, '2026-05-17 19:06:02'),
(62, '94961793', '94961793_6a0826c2477ba4.73020775', 'Explique en détail le livre \"L\'informatique : La voie express vers l\'indépendance\". Présente les thèmes principaux, l\'auteur, le résumé et ce qu\'on peut retenir.', 'En tant qu\'Eduna, voici une explication détaillée du livre \"L\'informatique : La voie express vers l\'indépendance\" basée sur le contenu fourni.\n\n---\n\n## Analyse du Livre : \"L\'informatique : La voie express vers l\'indépendance\"\n\nCe livre est un ouvrage de type **témoignage et de guide motivationnel** qui vise à **démystifier et valoriser le domaine de l\'informatique** pour un public spécifique : les jeunes bacheliers nigériens. Il se positionne comme un outil d\'orientation et un levier vers l\'autonomie professionnelle.\n\n### 1. L\'Auteur et le Contexte\n\nL\'auteur, **Aboubacar Hamidou Ridouane**, sert de **témoignage personnel** pour appuyer les arguments du livre.\n\n* **Formation et Compétences :** Il est développeur mobile full stack, ce qui signifie qu\'il maîtrise à la fois :\n    * Le développement côté client (applications mobiles, interface utilisateur).\n    * Le développement côté serveur (backend, gestion des données, API).\n    * Cette double compétence lui permet de concevoir des solutions numériques complètes.\n* **Entrepreneuriat :** Il est également co-fondateur de la start-up **Ninotech** au Niger, soulignant une ambition d\'apporter des solutions technologiques adaptées aux réalités locales et de favoriser l\'émergence d\'acteurs du numérique.\n* **Contexte Académique :** Il prépare actuellement son mémoire de Master 2 en Informatique à l\'Université Abdou Moumouni de Niamey.\n\n### 2. Thèmes Principaux Abordés dans le Livre\n\nLe livre est structuré autour de deux grandes parties : la **prise de conscience** et **l\'apprentissage libérateur**.\n\n#### **Partie I : Prise de conscience (Démanteler les idées reçues)**\n\nCette partie vise à corriger les malentendus courants autour de l\'informatique.\n\n1. **Le Vide Post-Bac et les Doutes :**\n    * **Problème identifié :** Après le baccalauréat, les jeunes se sentent perdus et ont des interrogations sur leur avenir.\n    * **Erreur courante :** Beaucoup réduisent l\'informatique à la simple **bureautique** (traitement de texte', 'Livre: L\'informatique : La voie express vers l\'indépendance', '2026-05-17 19:08:02'),
(63, '94961793', '94961793_6a0826c2477ba4.73020775', 'Décris et analyse cette image en détail.', '## Description et Analyse de l\'Image\n\nCette image est une capture d\'écran ou une illustration promotionnelle, probablement tirée d\'un article de blog, d\'une vidéo, ou d\'une publication sur la technologie de l\'intelligence artificielle (IA) et du machine learning (ML), spécifiquement en ce qui concerne le **fine-tuning** de modèles de langage (LLM) et les exigences matérielles.\n\n### Description Détaillée des Éléments\n\nL\'image est dominée par un fond de couleur rouge/orange vif, qui donne un aspect d\'alerte ou de mise en avant. Au centre, on trouve un panneau d\'information structuré, divisé en plusieurs sections :\n\n**1. Titre Principal :**\nEn haut, en lettres capitales blanches, on lit : **\"LLM QUANTIFIÉ\"**. Cela indique clairement que le sujet concerne la quantification des modèles de langage (LLM Quantization), une technique visant à réduire la taille et la complexité des modèles pour une meilleure efficacité.\n\n**2. Colonnes d\'Information :**\nLe contenu est organisé en deux colonnes principales, chacune décrivant un type de fine-tuning et ses exigences matérielles :\n\n* **Colonne de Gauche (Fine-tuning Standard) :**\n    * **Titre :** \"Fine-tuning Standard\"\n    * **Exigence :** \"Fine-tuning Standard (16-bit)\" (Indiquant que le standard utilise des données en 16 bits).\n\n* **Colonne de Droite (Mémoire GPU) :**\n    * **Titre :** \"Mémoire GPU requise\" (Indiquant l\'exigence de mémoire sur la carte graphique).\n    * **Exigence Spécifique :** \"> 780 Go\" (Ce chiffre est barré d\'un **X** rouge, signifiant que c\'est une exigence *non remplie* ou *inacceptable* pour ce scénario).\n\n* **Option Alternative (Quantification) :**\n    * Une section inférieure, de couleur orange, présente une alternative :\n        * **Option :** \"QLoRA (4-bit)\"\n        * **Exigence :** \"< 48 Go\" (Ce chiffre est accompagné d\'un **éclair** jaune, suggérant que c\'est une exigence *satisfaite* ou *acceptable*).\n\n**3. Élément', 'Image analysée', '2026-05-17 19:10:05'),
(64, '94961793', '94961793_6a0826c2477ba4.73020775', 'qui est l\'auteur de ce livre ?', 'L\'auteur de ce livre est **Aboubacar Hamidou Ridouane**.', 'Livre: L\'informatique : La voie express vers l\'indépendance', '2026-05-17 19:10:55');
INSERT INTO `EdunaConversation` (`idConversation`, `idUser`, `session_id`, `user_message`, `assistant_response`, `book_context`, `created_at`) VALUES
(65, '94961793', '94961793_6a0826c2477ba4.73020775', 'Transcris et résume : Histoire des Haoussa', 'Nous sommes à 1815. Mohamed Bello, succede à son père le grand Usmantan Foujo, Autrond Lampeer de Sokoto. Desormais, il rend sur une population à majorité hausa. Un grand défi latan, évite à tout prix une rebellion décidée. Les mythiques cités hausa. Créé vers le 10ème siècle par les descendants du legendary Bayadita, elles vont donner naissance au peuple hausa. Un peuple dont le commerce et l\'artisanat vont dessiner le territoire et les propres frontières. Un peuple avec une forte capacité à similatrice qui va au fil des siècles, exercier une forte attraction chez les ethnivoises. Majorité dans le nord du Nigeria et dans le sud du Niger jusqu\'au Lac-Chap, il constitue l\'un des groupes ethniques les plus importantes d\'Afrique. Le Lang classé dans les langues afroasiatiques, elle a trois emplois avec plus de 63 millions de victoires. Les migrations, les brassages, la culture, la diversité et la sociabilité sont des termes qui définissent les hausseurs. Aujourd\'hui nous allons raconter le histoire et comme c\'est de plusieurs peuples, elles commencent par une legendne. Le Sahara, le plus fast-desert au monde. L\'étambérature peut viatendre 55°. Mais une appartueuse était ainsi à réalité, dans un passé lointain, le Sahara était bubblé humide et verdoien. La progrèsive désertification a poussé les populations à s\'installer de par et d\'autres de cette monocente bande aride qui nécessitent d\'avancer au fil des siècles. C\'est ce qui arrive à, selon la legendne, à un certain biajita, originère de Baddad et s\'insuite, installé vers le 17e siècle dans les massifs de la hier. Suit à la décication accrue des cet région, il décidaire de migrer vers le sud. Il arriveur au Roi-Mbécanorie, connu sur le nord de Canemme et qui s\'était formé autour du Lacchade. Au Canemme, biajita épousa une princesse et a eu un fils du nord de Birham. Après un prêtre affrontement avec le Roi du Canemme, il quitte à la terre des Canories et s\'installer à Daura. La legendne veut que biajita étuie un serpent qui vivait dans un puits à Kusugu, suit à Daura. En effet, le serpent terrorisé la population et la privédo ne l\'autorise à ses vies une mal et vannredis. Malgré les avertissements, il s\'arrandit au puits un jédit pour aller chercher de l\'eau. C\'est de là que proviendrez son nom, biajita, qui signifie celui qui ne comprenait pas. Une fois qu\'il commence à appuyer de l\'eau, il comprit pourquoi la population l\'a vertisé car le serpent l\'attaque, mais il lui coupe à la tête avec son épée et c\'est ainsi que le serpent futit. Daura mal a rende d\'aura, avait promis la moitié de son royaume à qui conclurent le serpent du puits. Mais très intelligemment, biajita refuse à lui demander à sa mai un mariage à la place. Du jamais vu, étant donné que l\'héline restait ses debatterts durant toute leur vie et que la société était profondément matriarchale, n\'aient à moi d\'auramment qui se sentait rédevable acceptant sa demande et les deux finir par se marier. De cette ignonne Nakibau, qui a son tour, à laquelle un compagné de leur uncle Biram des mefer de Bau et fils de la princesse Canuri, dirigeait les 7 états haussas, dit haussas bakwai, qui sont connus sous la nôde d\'aura, canot, carcinat, zaria, gobire, ranno et Biram. Cependant, biajita eut un autre fils illégitim avec une conquibine appelée Karbogari, s\'adernit une 7 fils, et c\'est ainsi dirigeait les 7 autres états haussas illégitim surfacie, dit banza bakwai, et son connus sous la nôde gouari, kibi, gorropha, illorine, nupé, illoie, esamphara. Donc l\'histoire des haussas s\'est l\'arrancant entre un des prières originaires de Paltat et une rentre qui rendient sur un peuple de la gildure. Mais l\'histoire des haussas s\'est aussi les cité et d\'à. Au fil des siècles, il développer une langue commune et mentaire des meurs semblables. Leur cité devenait un à un des villes fortifiées qui contrôlait leur campagne environnante respective. Elle s\'organisait selon le système de la Sarauta, un modèle politique tristructurais avec l\'annomination d\'un roi au dirigeant, le Serkin Kassa, qui était conseillé par des dignitaires composés d\'un conseil à chef-concentre, et d\'un bonheur de la gildure. La personne du roi n\'avait aucun caractère sacré et lui parlait dignitaire, il pouvait être déposé par eux. Chaque état était indépendant, mais les incitations du gouvernement et les rapports entre le ville était semblable. La vie était très mouvementée d\'insensuciété en pleine expansion, un système fiscal de la ville qui était un système de la ville qui était un système de la ville qui était un système de la ville qui était un système de la ville qui était un système de la ville qui était un système de l\'expansion, un système fiscal de la ville qui contribue à la naissance d\'une économie complexe dans laquelle l\'agriculture l\'ocommerçue mais aussi l\'artisanat se communit pour assurer le développement des villes. Il y avait une telle production de chefs que les cités étaient protégées par des rampards qui pouvaient atteindre 15 mètres. Ce système économique qui était très profitable au mensaud salata c\'était à des renembles, à les profiter aussi à un autre dans le social qui la lui-même donne naissance, et il s\'agit des hommes riches ou mensauds arziquis. Il dominait le marché et disposait de capitaux importants et de clientèle extrêmement ramifié mais la plupart étaient des mensauds salata. À côté de la aristocratie et de la bourgeoisie, nous avions les hommes pauvres appelés mensauds talauti qui, en paraphrasing Karl Marx, étaient les proletters de l\'époque exploités sous pays. D\'après la chronique de canon, il semble que ce soit des lettres et commerçants malinqués, venus du mali aux 14 siècles, qui ont réintroduits l\'islam à canon et dans l\'ensemble du pays hausse. C\'est comme un dingue, fur d\'ailleurs à l\'origine de l\'intégration des cités hausse et au commerce transahariens. La ville de canon et de Cartina étaient de loin les plus riches. Ils en même que du la lorraine de Mansa Musa, homie, s\'évils étaient de rattaché à l\'Ampire. Des lors, la culture hausse se biselle l\'infinance directe de la civilisation méditerranée en automat grâce au caravan, qui nécessitait de régler les cités et ta hausse aux pours arabes et aux colonies de marchands tripolitaires ou fonds de la chale algérie, établisseur ses marchés. Elles ont pour laquelle la langue hausse est riche d\'emprunt à la langue arabe. Cette culture fortement assimilationniste va exercer aussi une attraction croissant sur les populations voisines dont elles entègrent progressivement des fraquements de plus en plus nombreuses. Le 12 avril 1993, Mohammed Tourette, ravers sonibaro fils de soniai libert lors de la bataille d\'Anfaru et devient amperer du sangay créant ainsi la dinastité Asquia. Entre 1500 et 1500 un, il occuple d\'Indy sans part de l\'Empire du Mali et même ensuite la guerre singe tout contre le roi à les mistes nasserais du voyom mocie. En 1912, il fait alliance avec Kantaro à l\'équibil, un des banzapacois et marches vers l\'Est, où il annexe les Etats-Hausas de carcinats de gobir et de canon. Cette intégration des cités Hausa de l\'ouestée du Nord dans l\'Empire de Gao va mettre à marche la grande machine assimilationniste des Hausas. Beaucoup de sangay surtout de l\'étniserman vont être aussaisés avec le temps. Mais le processus de aussaisisation a touché aussi d\'autres étnies comme les Beriberry ou Kanuri à l\'Est, les Targi, au Nord, et les Guari, noupé et Europa au Sud, ainsi que la fraction arabe de la région Sud-Aneze. La simulation de ces peuples va contribuer davantage à amplifier la distribution géographique des Hausas et à créer une grande diversité et technique au sein du groupe. Le 16e siècle est marqué par la bravrenne Aminat de Zazau, dont son règne est natée vers 1666. Mohamed Belaud, soutenre de ce couteau, dit la plus tard, en 1836, dans son livre intitulé Ifac Almey Sur, que la Rène Aminat a forcé qu\'à murderer canon et d\'autres régions Hausa à l\'urandre homage et qu\'il a été la première à être à lire un gouvernement réunissant tous les Hausas. Mais le terme Hausa n\'était utilisé qu\'à partir du 16e siècle de notre air car les gens se nommaient même en fonction de la ville du Roi-Mespecific-Douille provenée, vu que le peuple Hausa était un mouvement constant. Ces individus aient à le même corregine, s\'organisait un groupe et consulié un autre critère de division sociale. Ces groupes de régines étaient désignées selon les Dalettes, sous les termes de Asili, Zoria, Iridandhi, ou Kabila. Les individus de chaque groupe étaient liés par le même gâteau, c\'est-à-dire, héritage, patrilinaire. La part de l\'ence à ces groupes est considérée comme de natuges proches de celles de la parenté. Leur mamele, ce qualifient d\'un ou à automatis frère de ma mère. Dans certaines situations ces groupes de régines pouvait représenter des cardas si ils viennent littéralement métier héreditaires. À cette époque on ne pouvait être malame ou m\'affaraure-ci ou faire d\'autres méchés que si l\'on avait hérité de l\'accès à ses professions. Une situation qui persiste jusqu\'à nos jours dans une siltat disparaître. En plus de l\'origine, un autre facteur très important pour cette population semi-nomade était la nationalité, la partenace à un casse automalie à un état. La suitoyenité de l\'individu est en termes de référence fondamental entre hausse. Cette identité est à tix de matérialisé à l\'aide de scarification faciale, imposée à chaque enfant au septième jour de son existence. L\'origine pouvait être liée aussi à la nationalité. Par exemple, on pouvait trouver des gobiraws, cartinaois, ou des cartinaois, canaois. Ce qui signifie que des habitants du gobir pouvait être originaire de cartinaois et que des habitants de cartinaois pouvait être originaire de canon. Il existe par ailleurs une forte loyerité des immigrés assimilés qui soient hausse aux noms envers leur collectivité d\'accueil. D\'ailleurs, plusieurs class aristocratic hausse ont d\'autres origines ethniques. C\'est le cas des Babarberi, d\'origine canorie et des bafoulani d\'origine pull. Un premier nous allons parler du cas Babarberi. Pour mieux expliquer ces métissages, nous devons retourner à un arrière jusqu\'au septième siècle bien avant l\'époque de Bayadjida. Un effet c\'est à cette époque qu\'elle n\'est le quanème. Cette empire va par la suite atteinte son projet avec Donnema, d\'y balanque, qui régnant de 1220 à 1259 et est indile un pire vers le faisant et le nil. Après la mort de Donnema, le roi me connu des crées du succession et sa faibli au fur et à mesure que les années passées. Au 14e siècle, les sausses et les boladaves venus de l\'Est pouce les canories à se réfiger à l\'ouest du Lac Chagd. Il déplaçait ainsi leur centre à l\'ouest, fondaire le roi me de Bournu, en 1925, et à étendre leur domination sur toutes les espaces sur ceci en du Lac Chagd, à l\'actuel région de Tilabéry, et de la région de la Ir, jusqu\'au plateau central du Nigeria. Certains de ces canories imposèrent leur égemony un pays haussas, adoptés leur culture, et devait une classe aristocratique au sein du groupe haussas. Même plus tard, quand les cités haussas vont se libérer de l\'actuel canorie, dont le centre était à Bournu, ils vont conserver une certaine prestige dans les pays haussas. Ils créent par la suite d\'autres états haussas entre le 17e et le 19e siècle, au nord de l\'espace formé par les haussas baguay, parmi ces états, nous pouvons citer le sultanat de Dame-Garamme, dont la capitale se trouvait dans l\'actuel région des Ineres. Ils firent par la suite des Ampharales dans les Banzabaguay, donc puissant être à l\'origine de l\'état d\'Araewa dans le Dalol-Mauri, et la suite de l\'Araewa qui est la suite de l\'Araewa qui est la suite de l\'Araewa qui est la suite de l\'Araewa Quand en visite de l\'Swim, ils font être aussi à l\'origine de l\'état d\'Araewa dans le Dalol-Mauri, aux invueul Nous allons parler des Bafoulani, mais avant d\'intresser sur le Vif du Syjé, introduisons un homme qui va être à l\'origine de sa application entre Poul et haussas. 54 à Marata dans l\'actuel Niger. Son père était un pouls originaire du Foudator. Usman est ainsi issu d\'une famille de lettre pouls house Isé esthélée au gobir depuis le 17e siècle. A 1764, Usman finit par reprocher au musulman du gobir de ne pas observer strictement les règles du courant et décide de mener la prédication au gobir et dans les états house à voisins. A 1795, avec ses partisans à Majoujute House, il décide de renverser les élites du gobir désormais considéré comme des noms musulmans. A 1802, après une tentative d\'assassinement qu\'il a touré le fuir à la frontière nord-ouest du gobir afin de se rallier les noms de pouls que les house a appelés les Foulanindagi. Après une ultime tentative de considération, une faille, le Serkin gobir, salie de son côté les Serkets des autres états house à édéclar la guerre autorodone. Du côté de l\'autre camp, Dan Foujou est érégié à roi et devient le Serkin musulman. Il va bien essayer un pouvoir à la fois politique et religieux, il peut maintenant en appelé au Jihad, rassembler une armée et la commande. Il a un veil à pouls house à la pluie du sultan Daga Dess, Mohammed Bakri et de guerrier quel gress. Dan Foujou est par ailleurs soutenu par la pouls-en-arie house qui souffre des taxes de cité. Le 21 juin 1804, il remporte une victoire sur l\'armée de Yunfa à Takin Kwado. Il se proclame comme un d\'heure de Guayan, et renseurent le gobir. De 1804, en 1808, il s\'emparbe de Kanho et Annex par la suite d\'autres états house à d\'abord qu\'Accinar, insu-t-Saria, Nupé et Kibibi ainsi que le nord de l\'actuel Kamerun. Il désigne ensuite des émirs pour administrer les territoires conqués. En 1808, il gagne la bataille décisive d\'Alkalawa lors de laquelle Yunfa futue. Il fond dans suite la ville de Sokoto en 1809 dont il fait sa capitale. Dans le Foujo, se retrouve désormais à la tête de l\'Ampire de Sokoto dans lequel il applique les principes coraniques. Déjà âgé au début de la guerre, il transmet à 1815 le titre de soutenu de Sokoto à son fils Mohamed Belou. Cette nouvelle aristocratie modifie à très sensiblement la structure du monde house. Il est lui un partage entre conquérants minoritaires, dressant les tandats de l\'Unislam orthodoxe, et hausse à musulmane ou non. Par la suite, les sedentaires se convertire de plus en plus à l\'Islam et récit progma les réformistes adaptés à leur tour la culture hausse. Les Foulanines Guida, c\'est à dire des aristocrats plus hausse à Isé, modifient aussi la structure politique à créant un système d\'alkale ou se met de la pyramide il y a l\'immir ou la chère. Mais cette situation sociopolitique se rend contre plus de nos jours, unisaria, qu\'elle a été sûre de appuyer par le système de l\'indirecte rouls de britannique. En hors du houseland, par contre, une cohabitation entre hausse et bousou a abouti aussi à un métissage, surtout dans la région des Inders. L\'intégration des bougages à la société hausse est très récente et paraît résultée d\'une situation historiquement locale. Les bougages sont à réaliter des targis, c\'est à dire toareg. Les hausse à les appels les bas absin, par référence à leur implantation, dans l\'aïr designées sous l\'unode absin. En effet, la société targis se divise en deux catégories, en distincts d\'énobles, appelés imagéquennes et des captifs appelés acclannes. Ce sont ces derniers dont le statut est servile, condisines sous l\'unode bougage. Beaucoup de ces peuples semblent être un passage de toareg et de population noir. Une zette de minais par les toareg et magéguennes est assimilée par eux sur la base d\'une structure de leur conférence le statut de captif. Mais aujourd\'hui, beaucoup de bougages sont devenus indépendants, surtout avec l\'aide du colonisateur français, qui monnaie vers la fin du 19e siècle une politique antisclavagiste motivée surtout par la résistance des toareg et magéguennes à la colonisation. C\'est ainsi que plusieurs bougages libres se sont installés dans le sud, notamment dans le Indère. Ces derniers ont abandonné la plus grande partie de la culture targie et n\'appartient plus à cette société. Ils ne parlent plus de ma chèque mais Hausa ont adopté la culture Hausa et se sont soumis au chef locaux. Mais par contre, ils ont gardé certains mots de vie targie, comme le goût de le levage des bovins et agences leur territoire de façon à concilier, élevage et agriculture.', 'Audio: Histoire des Haoussa', '2026-05-17 19:13:59'),
(66, '94961793', '94961793_6a101eccabf436.85124839', 'bonjour', 'HTTP 404: <!DOCTYPE html>\n<html class=\"h-full\" lang=\"en-US\" dir=\"ltr\">\n  <head>\n    <meta charset=\"utf-8\">\n    <meta name=\"viewport\" content=\"width=device-width', NULL, '2026-05-22 11:15:57');

-- --------------------------------------------------------

--
-- Structure de la table `EdunaFavorite`
--

CREATE TABLE `EdunaFavorite` (
  `idFavorite` int NOT NULL,
  `idConversation` int NOT NULL,
  `idUser` varchar(256) NOT NULL,
  `note` varchar(500) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- --------------------------------------------------------

--
-- Structure de la table `EdunaFeedback`
--

CREATE TABLE `EdunaFeedback` (
  `idFeedback` int NOT NULL,
  `idConversation` int NOT NULL,
  `idUser` varchar(256) NOT NULL,
  `rating` tinyint NOT NULL COMMENT '1-5 étoiles',
  `comment` text,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- --------------------------------------------------------

--
-- Structure de la table `EdunaPDF`
--

CREATE TABLE `EdunaPDF` (
  `idPDF` int NOT NULL,
  `idBook` varchar(256) DEFAULT NULL,
  `filename` varchar(500) NOT NULL,
  `filepath` varchar(1000) NOT NULL,
  `title` varchar(500) DEFAULT NULL,
  `content_text` longtext,
  `content_embedding` longtext,
  `page_count` int DEFAULT NULL,
  `file_size` int DEFAULT NULL,
  `last_accessed` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- --------------------------------------------------------

--
-- Structure de la table `EdunaPDFQuery`
--

CREATE TABLE `EdunaPDFQuery` (
  `idQuery` int NOT NULL,
  `idUser` varchar(256) NOT NULL,
  `idPDF` int DEFAULT NULL,
  `idBook` varchar(256) DEFAULT NULL,
  `query` text NOT NULL,
  `response` text NOT NULL,
  `relevant_pages` varchar(500) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- --------------------------------------------------------

--
-- Structure de la table `EdunaSession`
--

CREATE TABLE `EdunaSession` (
  `idSession` int NOT NULL,
  `idUser` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `last_interaction` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `message_count` int DEFAULT '1',
  `context` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `preferences` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `EdunaSession`
--

INSERT INTO `EdunaSession` (`idSession`, `idUser`, `session_id`, `last_interaction`, `message_count`, `context`, `preferences`) VALUES
(2, '94961793', '94961793_6a101eccabf436.85124839', '2026-05-22 11:15:57', 1, 'Dernier sujet: bonjour', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `failed_jobs`
--

CREATE TABLE `failed_jobs` (
  `id` bigint UNSIGNED NOT NULL,
  `uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `connection` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `queue` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `payload` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `exception` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `failed_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `invitation_logs`
--

CREATE TABLE `invitation_logs` (
  `id` bigint UNSIGNED NOT NULL,
  `agent_id` bigint UNSIGNED NOT NULL,
  `generated_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `expires_at` timestamp NOT NULL,
  `generated_by` bigint UNSIGNED DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `invitation_logs`
--

INSERT INTO `invitation_logs` (`id`, `agent_id`, `generated_code`, `expires_at`, `generated_by`, `created_at`, `updated_at`) VALUES
(1, 1, 'FRUL-2766', '2025-06-11 10:18:45', NULL, '2025-06-10 10:18:45', '2025-06-10 10:18:45'),
(2, 2, 'TKKZ-4215', '2025-06-13 09:02:52', NULL, '2025-06-12 09:02:52', '2025-06-12 09:02:52'),
(3, 3, 'QUYF-3787', '2025-06-24 07:51:13', NULL, '2025-06-23 07:51:13', '2025-06-23 07:51:13'),
(4, 4, 'AFXR-1172', '2025-06-24 09:34:00', NULL, '2025-06-23 09:34:00', '2025-06-23 09:34:00'),
(5, 5, 'JMIG-6577', '2025-06-24 09:41:09', NULL, '2025-06-23 09:41:09', '2025-06-23 09:41:09'),
(6, 6, '3RVC-3822', '2025-06-24 10:08:59', NULL, '2025-06-23 10:08:59', '2025-06-23 10:08:59'),
(7, 7, 'KHU6-3956', '2025-06-25 08:14:04', NULL, '2025-06-24 08:14:04', '2025-06-24 08:14:04'),
(8, 8, 'IGFP-4388', '2025-06-25 08:30:52', NULL, '2025-06-24 08:30:52', '2025-06-24 08:30:52'),
(9, 9, 'OOZS-4570', '2025-06-25 08:31:33', NULL, '2025-06-24 08:31:33', '2025-06-24 08:31:33'),
(10, 10, 'CBVN-3735', '2025-06-25 08:33:07', NULL, '2025-06-24 08:33:07', '2025-06-24 08:33:07'),
(11, 11, '1ZP0-5096', '2025-06-25 08:33:45', NULL, '2025-06-24 08:33:45', '2025-06-24 08:33:45'),
(12, 12, 'TPC8-6510', '2025-06-25 08:34:14', NULL, '2025-06-24 08:34:14', '2025-06-24 08:34:14'),
(13, 13, 'RMNB-7589', '2025-06-25 11:59:07', NULL, '2025-06-24 11:59:07', '2025-06-24 11:59:07'),
(14, 14, 'WXSQ-9130', '2025-06-25 12:00:32', NULL, '2025-06-24 12:00:32', '2025-06-24 12:00:32'),
(15, 15, 'QJOK-1819', '2025-06-25 12:58:55', NULL, '2025-06-24 12:58:55', '2025-06-24 12:58:55'),
(16, 16, 'J55I-6761', '2025-06-25 13:01:07', NULL, '2025-06-24 13:01:07', '2025-06-24 13:01:07'),
(17, 17, 'VNL4-6787', '2025-06-25 13:01:59', NULL, '2025-06-24 13:01:59', '2025-06-24 13:01:59'),
(18, 18, 'OP0K-3206', '2025-06-25 13:02:30', NULL, '2025-06-24 13:02:30', '2025-06-24 13:02:30'),
(19, 19, 'CWKP-8933', '2025-06-25 13:02:57', NULL, '2025-06-24 13:02:57', '2025-06-24 13:02:57'),
(20, 20, 'WD0Y-5790', '2025-06-25 13:03:24', NULL, '2025-06-24 13:03:24', '2025-06-24 13:03:24'),
(21, 21, 'ZBJB-8321', '2025-06-25 13:03:46', NULL, '2025-06-24 13:03:46', '2025-06-24 13:03:46'),
(22, 22, 'ESGG-8025', '2025-06-25 13:04:16', NULL, '2025-06-24 13:04:16', '2025-06-24 13:04:16'),
(23, 23, 'B05E-6615', '2025-06-26 08:38:13', NULL, '2025-06-25 08:38:13', '2025-06-25 08:38:13'),
(24, 24, 'KZ6T-2860', '2025-06-26 08:39:59', NULL, '2025-06-25 08:39:59', '2025-06-25 08:39:59'),
(25, 25, 'EVP2-3613', '2025-07-04 13:47:31', NULL, '2025-07-03 13:47:31', '2025-07-03 13:47:31'),
(26, 26, 'AI4F-2275', '2025-07-05 07:13:33', NULL, '2025-07-04 07:13:33', '2025-07-04 07:13:33'),
(27, 27, 'NR95-2767', '2025-07-05 07:39:11', NULL, '2025-07-04 07:39:11', '2025-07-04 07:39:11'),
(28, 28, 'N7CB-1864', '2025-07-05 07:40:35', NULL, '2025-07-04 07:40:35', '2025-07-04 07:40:35'),
(29, 29, '5U0N-6438', '2025-07-05 08:38:03', NULL, '2025-07-04 08:38:03', '2025-07-04 08:38:03'),
(30, 30, 'CM53-5651', '2025-07-05 08:40:05', NULL, '2025-07-04 08:40:05', '2025-07-04 08:40:05'),
(31, 31, '15FP-7257', '2025-07-05 08:50:34', NULL, '2025-07-04 08:50:34', '2025-07-04 08:50:34'),
(32, 32, 'U9XV-1400', '2025-07-05 08:55:35', NULL, '2025-07-04 08:55:35', '2025-07-04 08:55:35'),
(33, 33, '91U2-9960', '2025-07-05 08:59:36', NULL, '2025-07-04 08:59:36', '2025-07-04 08:59:36'),
(34, 34, 'F5GD-4874', '2025-07-05 09:02:19', NULL, '2025-07-04 09:02:19', '2025-07-04 09:02:19'),
(35, 35, 'JPXA-8971', '2025-07-05 09:02:49', NULL, '2025-07-04 09:02:49', '2025-07-04 09:02:49'),
(36, 36, '3D1L-8378', '2025-07-05 09:14:58', NULL, '2025-07-04 09:14:58', '2025-07-04 09:14:58'),
(37, 37, 'ENCZ-8128', '2025-07-05 09:20:06', NULL, '2025-07-04 09:20:06', '2025-07-04 09:20:06'),
(38, 38, 'VPPE-6104', '2025-07-05 09:25:44', NULL, '2025-07-04 09:25:44', '2025-07-04 09:25:44'),
(39, 39, 'BJK0-3336', '2025-07-05 09:38:11', NULL, '2025-07-04 09:38:11', '2025-07-04 09:38:11'),
(40, 40, 'VIIC-9540', '2025-07-05 09:44:00', NULL, '2025-07-04 09:44:00', '2025-07-04 09:44:00'),
(41, 41, 'OHBW-8183', '2025-07-05 09:45:22', NULL, '2025-07-04 09:45:22', '2025-07-04 09:45:22'),
(42, 42, 'VFVF-4943', '2025-07-05 09:59:59', NULL, '2025-07-04 09:59:59', '2025-07-04 09:59:59'),
(43, 43, '9POH-8405', '2025-07-05 10:05:49', NULL, '2025-07-04 10:05:49', '2025-07-04 10:05:49'),
(44, 44, '4D9D-1973', '2025-07-05 10:16:21', NULL, '2025-07-04 10:16:21', '2025-07-04 10:16:21'),
(45, 45, 'QTAT-1771', '2025-07-05 10:19:58', NULL, '2025-07-04 10:19:58', '2025-07-04 10:19:58'),
(46, 46, 'GDG0-5329', '2025-07-05 10:24:08', NULL, '2025-07-04 10:24:08', '2025-07-04 10:24:08'),
(47, 47, 'GR5Z-3482', '2025-07-05 10:27:53', NULL, '2025-07-04 10:27:53', '2025-07-04 10:27:53'),
(48, 48, 'UX1Q-1554', '2025-07-05 10:28:39', NULL, '2025-07-04 10:28:39', '2025-07-04 10:28:39'),
(49, 49, 'TPYG-1590', '2025-07-05 10:31:09', NULL, '2025-07-04 10:31:09', '2025-07-04 10:31:09'),
(50, 50, 'L65R-1499', '2025-07-24 13:07:05', NULL, '2025-07-23 13:07:05', '2025-07-23 13:07:05'),
(51, 51, 'CZPO-8267', '2025-08-23 13:04:29', NULL, '2025-08-22 13:04:29', '2025-08-22 13:04:29'),
(52, 52, 'RVXB-9246', '2026-03-17 19:14:11', NULL, '2026-03-16 19:14:11', '2026-03-16 19:14:11'),
(53, 53, '8VZM-2477', '2026-03-17 19:23:45', NULL, '2026-03-16 19:23:45', '2026-03-16 19:23:45'),
(54, 54, 'AMTN-7391', '2026-03-17 19:32:10', NULL, '2026-03-16 19:32:10', '2026-03-16 19:32:10'),
(55, 55, 'IDYD-8129', '2026-03-17 19:47:44', NULL, '2026-03-16 19:47:44', '2026-03-16 19:47:44'),
(56, 56, 'NP6K-8367', '2026-03-23 12:42:47', NULL, '2026-03-22 12:42:47', '2026-03-22 12:42:47'),
(57, 57, 'WZII-9473', '2026-03-25 12:40:40', NULL, '2026-03-24 12:40:40', '2026-03-24 12:40:40'),
(58, 58, 'LZDX-4623', '2026-03-25 13:08:51', NULL, '2026-03-24 13:08:51', '2026-03-24 13:08:51'),
(59, 59, 'IWHO-9348', '2026-03-25 13:21:14', NULL, '2026-03-24 13:21:14', '2026-03-24 13:21:14'),
(60, 60, 'MBCG-2958', '2026-03-25 14:15:22', NULL, '2026-03-24 14:15:22', '2026-03-24 14:15:22'),
(61, 61, 'L32P-8055', '2026-03-26 07:37:17', NULL, '2026-03-25 07:37:17', '2026-03-25 07:37:17'),
(62, 62, 'IDGY-8013', '2026-03-26 08:29:17', NULL, '2026-03-25 08:29:17', '2026-03-25 08:29:17'),
(63, 63, 'UPOK-4757', '2026-04-01 13:45:19', NULL, '2026-03-31 13:45:19', '2026-03-31 13:45:19'),
(64, 64, '9QVR-2337', '2026-04-29 13:03:29', NULL, '2026-04-28 13:03:29', '2026-04-28 13:03:29'),
(65, 65, 'U2FH-6881', '2026-06-03 17:05:34', NULL, '2026-06-02 17:05:34', '2026-06-02 17:05:34');

-- --------------------------------------------------------

--
-- Structure de la table `jobs`
--

CREATE TABLE `jobs` (
  `id` bigint UNSIGNED NOT NULL,
  `queue` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `payload` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `attempts` tinyint UNSIGNED NOT NULL,
  `reserved_at` int UNSIGNED DEFAULT NULL,
  `available_at` int UNSIGNED NOT NULL,
  `created_at` int UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `job_batches`
--

CREATE TABLE `job_batches` (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `total_jobs` int NOT NULL,
  `pending_jobs` int NOT NULL,
  `failed_jobs` int NOT NULL,
  `failed_job_ids` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `options` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `cancelled_at` int DEFAULT NULL,
  `created_at` int NOT NULL,
  `finished_at` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `Like`
--

CREATE TABLE `Like` (
  `idNumber` varchar(256) NOT NULL,
  `idBook` varchar(256) NOT NULL,
  `date` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- Déchargement des données de la table `Like`
--

INSERT INTO `Like` (`idNumber`, `idBook`, `date`) VALUES
('90637132', 'OPEN0023', '2026-05-04 11:17:51'),
('90637132', 'OPEN0027', '2026-05-04 11:27:53'),
('94961793', 'OPEN0001', '2026-05-12 11:27:02'),
('94961793', 'OPEN0023', '2026-04-08 18:30:33'),
('94961793', 'OPEN0024', '2026-04-08 18:25:33'),
('94961793', 'OPEN0025', '2026-04-08 18:37:59');

--
-- Déclencheurs `Like`
--
DELIMITER $$
CREATE TRIGGER `AFTER_DELETE_LIKE` AFTER DELETE ON `Like` FOR EACH ROW BEGIN
    UPDATE `Book` SET numberLike=numberLike-1 WHERE `idBook`=OLD.idBook;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `AFTER_INSERT_LIKE` AFTER INSERT ON `Like` FOR EACH ROW BEGIN
    UPDATE `Book` SET numberLike=numberLike+1 WHERE `idBook`=NEW.idBook;
    DELETE FROM `NoLike` WHERE idNumber=NEW.idNumber AND `idBook`=NEW.idBook;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `Loand`
--

CREATE TABLE `Loand` (
  `idLoand` int NOT NULL,
  `idReservation` int NOT NULL,
  `idBook` varchar(256) NOT NULL,
  `idAgentGiver` varchar(256) NOT NULL,
  `idAgentRecover` varchar(256) DEFAULT NULL,
  `dateLoand` datetime NOT NULL,
  `realReturnDate` datetime NOT NULL,
  `actualReturnDate` datetime DEFAULT NULL,
  `closing` tinyint DEFAULT '0',
  `view` tinyint DEFAULT '0',
  `idStruct` int NOT NULL,
  `idUser` varchar(100) NOT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- Déchargement des données de la table `Loand`
--

INSERT INTO `Loand` (`idLoand`, `idReservation`, `idBook`, `idAgentGiver`, `idAgentRecover`, `dateLoand`, `realReturnDate`, `actualReturnDate`, `closing`, `view`, `idStruct`, `idUser`, `updated_at`, `created_at`) VALUES
(66, 10072, 'OPEN0023', 'Bachir Abdoul Kader', 'Dille', '2026-04-06 17:13:41', '2026-04-07 17:13:41', '2026-06-03 14:56:15', 1, 1, 1, '94961793', '2026-06-03 12:56:15', '2026-04-06 15:13:41'),
(67, 10073, 'OPEN0006', 'Bachir Abdoul Kader', NULL, '2026-04-11 08:53:08', '2026-04-15 08:53:08', NULL, 0, 1, 1, '94961793', '2026-04-11 06:53:08', '2026-04-11 06:53:08'),
(68, 10074, 'OPEN0013', 'Bachir Abdoul Kader', NULL, '2026-04-11 16:18:32', '2026-04-14 16:18:32', NULL, 0, 1, 1, '94961793', '2026-04-11 14:18:32', '2026-04-11 14:18:32');

-- --------------------------------------------------------

--
-- Structure de la table `migrations`
--

CREATE TABLE `migrations` (
  `id` int UNSIGNED NOT NULL,
  `migration` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `batch` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `migrations`
--

INSERT INTO `migrations` (`id`, `migration`, `batch`) VALUES
(1, '0001_01_01_000000_create_users_table', 1),
(2, '0001_01_01_000001_create_cache_table', 1),
(3, '0001_01_01_000002_create_jobs_table', 1),
(4, '2019_12_14_000001_create_personal_access_tokens_table', 2),
(5, '2025_06_09_085959_create_personal_access_tokens_table', 3),
(6, '2025_06_09_105834_create_users_table', 4);

-- --------------------------------------------------------

--
-- Structure de la table `NoLike`
--

CREATE TABLE `NoLike` (
  `idNumber` varchar(256) NOT NULL,
  `idBook` varchar(256) NOT NULL,
  `date` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- Déchargement des données de la table `NoLike`
--

INSERT INTO `NoLike` (`idNumber`, `idBook`, `date`) VALUES
('90637132', 'OPEN0025', '2026-05-07 15:56:17'),
('94961793', 'OPEN0022', '2026-04-08 18:43:44'),
('94961793', 'OPEN0027', '2026-03-31 15:15:12');

--
-- Déclencheurs `NoLike`
--
DELIMITER $$
CREATE TRIGGER `AFTER_DELETE_NOLIKE` AFTER DELETE ON `NoLike` FOR EACH ROW BEGIN
    UPDATE `Book` SET numberNoLike=numberNoLike-1 WHERE `idBook`=OLD.idBook;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `AFTER_INSERT_NOLIKE` AFTER INSERT ON `NoLike` FOR EACH ROW BEGIN
    UPDATE `Book` SET numberNoLike=numberNoLike+1 WHERE `idBook`=NEW.idBook;
    DELETE FROM `Like` WHERE idNumber=NEW.idNumber AND `idBook`=NEW.idBook;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `Notification`
--

CREATE TABLE `Notification` (
  `idNotification` int NOT NULL,
  `idNumber` varchar(256) DEFAULT NULL,
  `date` datetime NOT NULL,
  `message` text NOT NULL,
  `state` tinyint DEFAULT '0',
  `type` int DEFAULT '0',
  `reference` int DEFAULT '0',
  `view` tinyint DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- Déchargement des données de la table `Notification`
--

INSERT INTO `Notification` (`idNotification`, `idNumber`, `date`, `message`, `state`, `type`, `reference`, `view`) VALUES
(1040238, NULL, '2025-01-29 23:21:09', 'Un nouveau livre de cette catgorie est mainteant disponiple.', 0, 4, 28, 0),
(1040239, NULL, '2025-04-13 13:09:11', 'Un nouveau livre de cette catgorie est mainteant disponiple.', 0, 4, 29, 0),
(1040240, NULL, '2025-06-17 14:05:42', 'Un nouveau livre de cette catgorie est mainteant disponiple.', 0, 4, 30, 0),
(1040241, NULL, '2025-06-19 03:52:45', 'Un nouveau livre de cette catégorie est maintenant disponible.', 0, 4, 1, 0),
(1040242, NULL, '2025-06-19 03:54:01', 'Un nouveau livre de cette catégorie est maintenant disponible.', 0, 4, 2, 0),
(1040243, NULL, '2025-06-19 03:54:01', 'Un nouveau livre de cette catégorie est maintenant disponible.', 0, 4, 3, 0),
(1040244, NULL, '2025-06-24 10:40:35', 'Un nouveau livre de cette catégorie est maintenant disponible.', 0, 4, 31, 0),
(1040245, NULL, '2025-06-24 10:48:34', 'Un nouveau livre de cette catégorie est maintenant disponible.', 0, 4, 32, 0),
(1040246, NULL, '2025-06-24 10:49:38', 'Un nouveau livre de cette catégorie est maintenant disponible.', 0, 4, 33, 0),
(1040247, NULL, '2025-06-24 10:50:04', 'Un nouveau livre de cette catégorie est maintenant disponible.', 0, 4, 34, 0),
(1040248, NULL, '2025-06-24 10:51:13', 'Un nouveau livre de cette catégorie est maintenant disponible.', 0, 4, 35, 0),
(1040249, NULL, '2025-06-24 10:52:04', 'Un nouveau livre de cette catégorie est maintenant disponible.', 0, 4, 36, 0),
(1040250, NULL, '2025-06-24 16:00:24', 'Un nouveau livre de cette catégorie est maintenant disponible.', 0, 4, 37, 0),
(1040251, NULL, '2025-06-24 16:03:35', 'Un nouveau livre de cette catégorie est maintenant disponible.', 0, 4, 38, 0),
(1040252, NULL, '2025-06-24 16:04:03', 'Un nouveau livre de cette catégorie est maintenant disponible.', 0, 4, 39, 0),
(1040253, NULL, '2025-06-24 16:04:50', 'Un nouveau livre de cette catégorie est maintenant disponible.', 0, 4, 40, 0),
(1040254, NULL, '2025-06-24 16:06:12', 'Un nouveau livre de cette catégorie est maintenant disponible.', 0, 4, 41, 0),
(1040255, NULL, '2025-06-24 17:25:37', 'Un nouveau livre de cette catégorie est maintenant disponible.', 0, 4, 42, 0),
(1040256, NULL, '2025-06-25 10:41:21', 'Un nouveau livre de cette catégorie est maintenant disponible.', 0, 4, 43, 0),
(1040257, NULL, '2025-10-11 10:51:42', 'Un nouveau livre de cette catégorie est maintenant disponible.', 0, 4, 44, 0),
(1040258, NULL, '2025-10-13 11:16:56', 'Un nouveau livre de cette catégorie est maintenant disponible.', 0, 4, 45, 0);

-- --------------------------------------------------------

--
-- Structure de la table `password_reset_tokens`
--

CREATE TABLE `password_reset_tokens` (
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `personal_access_tokens`
--

CREATE TABLE `personal_access_tokens` (
  `id` bigint UNSIGNED NOT NULL,
  `tokenable_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `tokenable_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `token` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `abilities` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `last_used_at` timestamp NULL DEFAULT NULL,
  `expires_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `personal_access_tokens`
--

INSERT INTO `personal_access_tokens` (`id`, `tokenable_type`, `tokenable_id`, `name`, `token`, `abilities`, `last_used_at`, `expires_at`, `created_at`, `updated_at`) VALUES
(1, 'App\\Models\\StructAgent', '1', 'authToken', '3604d1a204e6a5ce02a0964001562095ae1132753935eafc535a4f79073caf82', '[\"*\"]', NULL, NULL, '2025-06-10 10:39:34', '2025-06-10 10:39:34'),
(2, 'App\\Models\\StructAgent', '1', 'authToken', '121ada631cd73cf18834e70b6a7d50b9e55fbff48fb5e9f844a63203264fc088', '[\"*\"]', NULL, NULL, '2025-06-10 10:40:37', '2025-06-10 10:40:37'),
(3, 'App\\Models\\StructAgent', '1', 'authToken', 'd20b167859c29ad63e0a28927d9f77d3f3008e10f410137e2f622659c052d3b7', '[\"*\"]', NULL, NULL, '2025-06-12 08:11:31', '2025-06-12 08:11:31'),
(4, 'App\\Models\\StructAgent', '1', 'authToken', 'b8c196a50319630320a0d44ad75ae9e4e0b4343887996978c2d0a6b5d5160f33', '[\"*\"]', NULL, NULL, '2025-06-12 08:12:45', '2025-06-12 08:12:45'),
(5, 'App\\Models\\StructAgent', '1', 'authToken', '98d332c772d52b6d694c299cb02c27e2dab06ce56848aeec6736f1aac5b6f081', '[\"*\"]', NULL, NULL, '2025-06-12 08:48:26', '2025-06-12 08:48:26'),
(6, 'App\\Models\\StructAgent', '1', 'authToken', 'bfd569ab5363ca69788ec8d8fb7b0c299fcf11e49bfdcec1f63a8e179a047ec7', '[\"*\"]', '2025-06-12 09:37:41', NULL, '2025-06-12 08:54:34', '2025-06-12 09:37:41'),
(7, 'App\\Models\\StructAgent', '2', 'authToken', 'd0c4f8d60d1e536ef8dd5f86d154482b0d05db91158608537a2a081403e31cb4', '[\"*\"]', NULL, NULL, '2025-06-12 09:05:46', '2025-06-12 09:05:46'),
(8, 'App\\Models\\StructAgent', '2', 'authToken', 'c9ffced217e624e3fb07779066a3d578b5ed30b50c827f10daa37baa9a98e665', '[\"*\"]', '2025-06-14 08:25:55', NULL, '2025-06-12 09:09:14', '2025-06-14 08:25:55'),
(9, 'App\\Models\\StructAgent', '2', 'authToken', '55fa85df25572b8e0a1147006565304ec6b6e21c340e8bff1762513d563f6c0e', '[\"*\"]', '2025-06-12 10:00:44', NULL, '2025-06-12 09:44:37', '2025-06-12 10:00:44'),
(10, 'App\\Models\\StructAgent', '1', 'authToken', '363a200c5a4d2e7014fbbf0d261db2b9e2c8c1f6680ec8ce2cfd7b63876a229c', '[\"*\"]', '2025-06-12 10:11:47', NULL, '2025-06-12 10:01:10', '2025-06-12 10:11:47'),
(11, 'App\\Models\\StructAgent', '1', 'authToken', '77980d7f29cc4eb18108900d2f4d750eaa5f20090c0c14dd4db2c54ba8e9d19e', '[\"*\"]', '2025-06-12 10:22:18', NULL, '2025-06-12 10:12:05', '2025-06-12 10:22:18'),
(12, 'App\\Models\\StructAgent', '1', 'authToken', '099b8687e189b149a77be60052db24480d01c9b9656db6f89accccbbb34c59ac', '[\"*\"]', '2025-06-14 09:02:01', NULL, '2025-06-12 10:22:31', '2025-06-14 09:02:01'),
(13, 'App\\Models\\StructAgent', '1', 'authToken', 'f2e6d0f7a2256b3868d6ea7aaa2a55450c3802cf19ee005dda324c89b891071f', '[\"*\"]', NULL, NULL, '2025-06-14 09:02:16', '2025-06-14 09:02:16'),
(14, 'App\\Models\\StructAgent', '1', 'authToken', 'e4c9720624ec91933d7c08e941d071ea03b0661a0b64907cfa69040ce4839970', '[\"*\"]', NULL, NULL, '2025-06-14 09:55:58', '2025-06-14 09:55:58'),
(15, 'App\\Models\\StructAgent', '1', 'authToken', '939a8062edcd790c69493dd057070547c2531c7c518bb957727084754f8b0fb2', '[\"*\"]', NULL, NULL, '2025-06-14 09:56:41', '2025-06-14 09:56:41'),
(16, 'App\\Models\\StructAgent', '1', 'authToken', 'a8282493b2f94a5bb576dcd9476d96eecc94f85e6e8bb23e006153b61cd1cc9f', '[\"*\"]', NULL, NULL, '2025-06-14 13:59:45', '2025-06-14 13:59:45'),
(17, 'App\\Models\\StructAgent', '1', 'authToken', 'ca4d989be4abc3e1043dd3fc2e960f61580f4021d5ae219399a7a072735a67ee', '[\"*\"]', NULL, NULL, '2025-06-14 14:00:51', '2025-06-14 14:00:51'),
(18, 'App\\Models\\StructAgent', '1', 'authToken', '8f2579e3b1279accacb85e1cc4b813e29f805946faffb64d6f4a132ca9203048', '[\"*\"]', NULL, NULL, '2025-06-14 14:07:34', '2025-06-14 14:07:34'),
(19, 'App\\Models\\StructAgent', '1', 'authToken', 'eb9b23119f9b536b9521360395904f34876f33e44e400a3b0dac100374c3654d', '[\"*\"]', NULL, NULL, '2025-06-15 13:57:12', '2025-06-15 13:57:12'),
(20, 'App\\Models\\StructAgent', '1', 'authToken', '84645065a59fcdef3422c5dbce2be2b460e6c792094719b1a090127783661f0a', '[\"*\"]', NULL, NULL, '2025-06-15 20:33:07', '2025-06-15 20:33:07'),
(21, 'App\\Models\\StructAgent', '1', 'authToken', '32e9d57854999263d13b11634f11d38ef0289aaa466be4be86ed12a7ddbfe386', '[\"*\"]', NULL, NULL, '2025-06-16 20:49:20', '2025-06-16 20:49:20'),
(22, 'App\\Models\\StructAgent', '1', 'authToken', 'ad6435b04b08b8674894d10954335c77ea667b4bf781fde5f2f72357c8518866', '[\"*\"]', NULL, NULL, '2025-06-16 20:53:18', '2025-06-16 20:53:18'),
(23, 'App\\Models\\StructAgent', '1', 'authToken', '1d70152c94349767710f226c3543c3c02c289c31e9a75f953365096d92bb4ef1', '[\"*\"]', NULL, NULL, '2025-06-17 09:34:50', '2025-06-17 09:34:50'),
(24, 'App\\Models\\StructAgent', '1', 'authToken', 'c9b2ff5a8b4a9748010b02dde5e619a40c42634a0825a1cfa884e0b3769fe168', '[\"*\"]', NULL, NULL, '2025-06-17 09:36:07', '2025-06-17 09:36:07'),
(25, 'App\\Models\\StructAgent', '1', 'authToken', '4498e969c6952256722d6e5eef7553284c43af8214eb3f7a9fa0503224f73748', '[\"*\"]', NULL, NULL, '2025-06-17 09:42:17', '2025-06-17 09:42:17'),
(26, 'App\\Models\\StructAgent', '1', 'authToken', '841886d807da624b787fda3a7ae179f732a0694b022977e06525f3b193a36195', '[\"*\"]', NULL, NULL, '2025-06-17 09:53:42', '2025-06-17 09:53:42'),
(27, 'App\\Models\\StructAgent', '1', 'authToken', '405017d65e2f2cc2b83d1dee7caaa78fdad220db4cecd39b30724d52dc41047a', '[\"*\"]', NULL, NULL, '2025-06-17 10:03:09', '2025-06-17 10:03:09'),
(28, 'App\\Models\\StructAgent', '1', 'authToken', 'a1abba21d923ef34c7842f3526d4d47f50b11d55c7d7761c8f1bd89a390f24b8', '[\"*\"]', NULL, NULL, '2025-06-17 10:03:59', '2025-06-17 10:03:59'),
(29, 'App\\Models\\StructAgent', '1', 'authToken', 'd4e8afc3f8fb2d8f44e8803d033cc7ff1c3d61ef3556a2bd01d4f953738aba9a', '[\"*\"]', NULL, NULL, '2025-06-17 10:07:19', '2025-06-17 10:07:19'),
(30, 'App\\Models\\StructAgent', '1', 'authToken', '0ef42ba77fa5420a308b7315886515ffd3ba854060d96267e3b80c627889e3fc', '[\"*\"]', NULL, NULL, '2025-06-17 10:08:04', '2025-06-17 10:08:04'),
(31, 'App\\Models\\StructAgent', '1', 'authToken', '28611fc5489d97cb21ec7f782133085fefad393222e226ed4edadc7557a9a44c', '[\"*\"]', NULL, NULL, '2025-06-17 10:27:39', '2025-06-17 10:27:39'),
(32, 'App\\Models\\StructAgent', '1', 'authToken', '90407438dc2291765928c166d2ec0287519ec8765d2137e33ea92bc1ff3cabad', '[\"*\"]', NULL, NULL, '2025-06-17 10:30:00', '2025-06-17 10:30:00'),
(33, 'App\\Models\\StructAgent', '1', 'authToken', '524c9317efde46e7bb50baeb7de45091e6084d6e1f84e5170b3f8db9b6b2bb4c', '[\"*\"]', NULL, NULL, '2025-06-17 10:30:28', '2025-06-17 10:30:28'),
(34, 'App\\Models\\StructAgent', '1', 'authToken', 'b170b6b5b4685fdcc3e47d3dcee9269ab117ad73ae36811ce277f949de20428a', '[\"*\"]', NULL, NULL, '2025-06-17 10:31:50', '2025-06-17 10:31:50'),
(35, 'App\\Models\\StructAgent', '1', 'authToken', '627fb46f534d9b9bd5046df87397f11ff0765181d8a4349fc61d996a1656d925', '[\"*\"]', NULL, NULL, '2025-06-17 10:33:35', '2025-06-17 10:33:35'),
(36, 'App\\Models\\StructAgent', '1', 'authToken', 'b79b98a1a1c60fbc16290bb35f6c7e0d87bf857254c5f7b50fe0ffd00cb61555', '[\"*\"]', NULL, NULL, '2025-06-17 10:35:59', '2025-06-17 10:35:59'),
(37, 'App\\Models\\StructAgent', '1', 'authToken', '4d267732d2f37e1483fe7f27c961d97447fef3995331805420aec9c03f844bb5', '[\"*\"]', NULL, NULL, '2025-06-17 11:06:40', '2025-06-17 11:06:40'),
(38, 'App\\Models\\StructAgent', '1', 'authToken', '229a1d9ee192228a32c27b990a628d2aabe297884e61dcf574446cab89ae7747', '[\"*\"]', NULL, NULL, '2025-06-17 11:06:49', '2025-06-17 11:06:49'),
(39, 'App\\Models\\StructAgent', '1', 'authToken', 'cd686141512b46fad482bd50593e5ee6e4be137bc22c730d972f5e1f1cecc72a', '[\"*\"]', NULL, NULL, '2025-06-17 11:10:11', '2025-06-17 11:10:11'),
(40, 'App\\Models\\StructAgent', '1', 'authToken', 'ef28c8d1423884b596c171a4e421145c2581da94cc3bb27c4d13fc2ed599eef8', '[\"*\"]', NULL, NULL, '2025-06-17 11:18:27', '2025-06-17 11:18:27'),
(41, 'App\\Models\\StructAgent', '1', 'authToken', '1de62a419cdd3613954b9852301f7c370f48b41fbe5cbb5ded9a9a2597cacd87', '[\"*\"]', NULL, NULL, '2025-06-17 11:47:36', '2025-06-17 11:47:36'),
(42, 'App\\Models\\StructAgent', '1', 'authToken', 'e0f65ab028cb3b69f49ab9cb426049b43d171b261731b5d1055824bdb6909fa9', '[\"*\"]', NULL, NULL, '2025-06-17 11:53:30', '2025-06-17 11:53:30'),
(43, 'App\\Models\\StructAgent', '1', 'authToken', 'f202846e237aff1da8b3aba73298412beefaf410458c23f2818a66aa708cc2d1', '[\"*\"]', NULL, NULL, '2025-06-17 12:29:30', '2025-06-17 12:29:30'),
(44, 'App\\Models\\StructAgent', '1', 'authToken', 'b701df8cc62ea2890af7ee37ea5f9cf5c87c89e7f45871035b558317bd907450', '[\"*\"]', NULL, NULL, '2025-06-17 12:43:50', '2025-06-17 12:43:50'),
(45, 'App\\Models\\StructAgent', '1', 'authToken', 'e9837ff090e8b3717d0e8d621dd4dcc06d071f1fd95a4288cc54fc4e4e389210', '[\"*\"]', NULL, NULL, '2025-06-17 13:36:23', '2025-06-17 13:36:23'),
(46, 'App\\Models\\StructAgent', '1', 'authToken', '97f35fa48164fd8bed37a3d72bcfb51ac987a3196d45793d54e516a532ada655', '[\"*\"]', NULL, NULL, '2025-06-17 13:44:40', '2025-06-17 13:44:40'),
(47, 'App\\Models\\StructAgent', '1', 'authToken', 'f4ea6deccbe75892fcc436b8e2844a75c07051374dba3ed5e213eafa2415f55c', '[\"*\"]', NULL, NULL, '2025-06-17 14:41:08', '2025-06-17 14:41:08'),
(48, 'App\\Models\\StructAgent', '1', 'authToken', '2ae647c10dfa6fc22e0c054db92dc6854cae5d603d8c30491fdd0f051c717f19', '[\"*\"]', NULL, NULL, '2025-06-17 14:47:05', '2025-06-17 14:47:05'),
(49, 'App\\Models\\StructAgent', '1', 'authToken', 'c374dcd45bc4b96c96aec89d88db96d94f5b2b0246a26ef514dfee379ccc648b', '[\"*\"]', NULL, NULL, '2025-06-17 14:48:27', '2025-06-17 14:48:27'),
(50, 'App\\Models\\StructAgent', '1', 'authToken', 'a83712889261fc9f140a2ce0b08fc2f3e93fdd534821f7f04c40926e5a7276ab', '[\"*\"]', NULL, NULL, '2025-06-17 14:49:20', '2025-06-17 14:49:20'),
(51, 'App\\Models\\StructAgent', '1', 'authToken', '4b8e01b8cbceb2ee7b8755a23975f91d622be070d2acddf31ff8904c57db5b6c', '[\"*\"]', NULL, NULL, '2025-06-17 14:50:17', '2025-06-17 14:50:17'),
(52, 'App\\Models\\StructAgent', '1', 'authToken', '19557c68bcf05953112117e55c98888c505aa30c92c4049b07bd03dcf57a70e1', '[\"*\"]', NULL, NULL, '2025-06-17 14:51:29', '2025-06-17 14:51:29'),
(53, 'App\\Models\\StructAgent', '1', 'authToken', 'f82239229f593a1519a9bf9917cab229c0d4c5d1b0e25c8fdedaaf08cfc18db6', '[\"*\"]', NULL, NULL, '2025-06-17 14:53:18', '2025-06-17 14:53:18'),
(54, 'App\\Models\\StructAgent', '1', 'authToken', '502f2491e1a04ee7cd6bba150e7f0d4de5ea8093ea45d8c49b38d7eb7a40395d', '[\"*\"]', NULL, NULL, '2025-06-17 14:56:19', '2025-06-17 14:56:19'),
(55, 'App\\Models\\StructAgent', '1', 'authToken', 'cda6275210a35cbed957f82da3ebe9075734c3775e8ae27f9a91f881f647cf24', '[\"*\"]', NULL, NULL, '2025-06-17 14:57:23', '2025-06-17 14:57:23'),
(56, 'App\\Models\\StructAgent', '1', 'authToken', '42fb23b060f94fa03c2ec9ffa50a83609ca1fb8e5aeecb2cec7d2130b61b85f0', '[\"*\"]', NULL, NULL, '2025-06-17 15:12:10', '2025-06-17 15:12:10'),
(57, 'App\\Models\\StructAgent', '1', 'authToken', '919d2502c4e0466c40e485a6f605ce24a5ba3011e12e77d7a6edf9c5d739ee7b', '[\"*\"]', NULL, NULL, '2025-06-17 15:12:49', '2025-06-17 15:12:49'),
(58, 'App\\Models\\StructAgent', '1', 'authToken', '3d8daa395504bc34ef739bcfa52de5395b76000cf0074023bbe349881d595e31', '[\"*\"]', NULL, NULL, '2025-06-17 15:14:09', '2025-06-17 15:14:09'),
(59, 'App\\Models\\StructAgent', '1', 'authToken', '38eb7a996c85240ecd8d75d820cf23677a0fe16d5a623d2140cf9969743b29c0', '[\"*\"]', NULL, NULL, '2025-06-17 15:17:27', '2025-06-17 15:17:27'),
(60, 'App\\Models\\StructAgent', '1', 'authToken', '3ec2c8df6ec42d1ccee3c19493c82429a49de8c2f55ed90d74753d15fd73ee49', '[\"*\"]', NULL, NULL, '2025-06-17 15:23:25', '2025-06-17 15:23:25'),
(61, 'App\\Models\\StructAgent', '1', 'authToken', '30fe8a2358a0f5fe9830d5abfb02730740dcd53585e2f81992e374db5ad44b3d', '[\"*\"]', NULL, NULL, '2025-06-17 15:26:01', '2025-06-17 15:26:01'),
(62, 'App\\Models\\StructAgent', '1', 'authToken', '12a9f49f4a178a6028e19fe23bf84350c3e5062f0cea3c2b1d452026e01ea45d', '[\"*\"]', NULL, NULL, '2025-06-17 15:43:39', '2025-06-17 15:43:39'),
(63, 'App\\Models\\StructAgent', '1', 'authToken', '53e297fbf64232454dd48d01cc0ad43cba182582619cee100384a321e5b5b710', '[\"*\"]', NULL, NULL, '2025-06-17 15:44:47', '2025-06-17 15:44:47'),
(64, 'App\\Models\\StructAgent', '1', 'authToken', 'd242db4db7bb5caae6e106b38d25275bf166c86f02bdf18bac38f69f09ed1387', '[\"*\"]', NULL, NULL, '2025-06-17 15:45:45', '2025-06-17 15:45:45'),
(65, 'App\\Models\\StructAgent', '1', 'authToken', '2365e8199a594941c984824da593fbeba3d933579dca5aa217505bc413c352d7', '[\"*\"]', NULL, NULL, '2025-06-17 15:49:40', '2025-06-17 15:49:40'),
(66, 'App\\Models\\StructAgent', '1', 'authToken', '25a5beccb64ae6ae074119b5fe89a14f21a49fb1bc511b51ec5e2b3ec9bb6162', '[\"*\"]', NULL, NULL, '2025-06-17 15:50:09', '2025-06-17 15:50:09'),
(67, 'App\\Models\\StructAgent', '1', 'authToken', '4190eee0f3be72bebcd1550b1bc22203b872d720378e5f53054433188876c5e5', '[\"*\"]', NULL, NULL, '2025-06-17 15:51:15', '2025-06-17 15:51:15'),
(68, 'App\\Models\\StructAgent', '1', 'authToken', '71349aa6b5d7646bd8be41eef7231e5d6a0701352765150a1c804d62b3da0396', '[\"*\"]', NULL, NULL, '2025-06-17 15:51:46', '2025-06-17 15:51:46'),
(69, 'App\\Models\\StructAgent', '1', 'authToken', '74487b7b537918b834bee2c2fa6f82462d971aaffe78c5be0ed25d750737aa28', '[\"*\"]', NULL, NULL, '2025-06-17 15:53:47', '2025-06-17 15:53:47'),
(70, 'App\\Models\\StructAgent', '1', 'authToken', 'adc3e55f596817a43521a4cfa17def4e4b260df55fa81c68b383c9cf9887faa9', '[\"*\"]', NULL, NULL, '2025-06-17 19:20:20', '2025-06-17 19:20:20'),
(71, 'App\\Models\\StructAgent', '1', 'authToken', 'd0dde0f5a1ac38d5c5e0d6f92278e6fbf8a14b97e060f25090a3d3cc35cfb99a', '[\"*\"]', NULL, NULL, '2025-06-17 20:02:51', '2025-06-17 20:02:51'),
(72, 'App\\Models\\StructAgent', '2', 'authToken', 'f4ad8e91482f6e499a3fcb861b8e8d6e500961c2bf99700b9cc9365cead69e14', '[\"*\"]', NULL, NULL, '2025-06-17 20:03:39', '2025-06-17 20:03:39'),
(73, 'App\\Models\\StructAgent', '1', 'authToken', '048686702a3a8bd7cfcff8ae15d656b5d8ddee45dc82f0415bb1a8b528c77d56', '[\"*\"]', NULL, NULL, '2025-06-17 20:10:47', '2025-06-17 20:10:47'),
(74, 'App\\Models\\StructAgent', '1', 'authToken', '664b9e939390ff84a7951e22d6aa20a6d235b09b0965a522231a5559b39eef32', '[\"*\"]', NULL, NULL, '2025-06-17 20:41:27', '2025-06-17 20:41:27'),
(75, 'App\\Models\\StructAgent', '1', 'authToken', '83b66fe5b0b46f6548e0c1cd9c9060bc601915b584dac9e0795577bc56599237', '[\"*\"]', NULL, NULL, '2025-06-17 20:49:04', '2025-06-17 20:49:04'),
(76, 'App\\Models\\StructAgent', '1', 'authToken', '08fd86dfaf2fb7e673ece5d792c0604e4c667b92a7b24e44f3da2af0585f3d62', '[\"*\"]', NULL, NULL, '2025-06-17 21:04:07', '2025-06-17 21:04:07'),
(77, 'App\\Models\\StructAgent', '1', 'authToken', '968062b7cc7380845fabb9d10ce2cc899538092f7bca8620230ea151127a371b', '[\"*\"]', NULL, NULL, '2025-06-17 21:04:55', '2025-06-17 21:04:55'),
(78, 'App\\Models\\StructAgent', '1', 'authToken', '20ccf44da69a552f7953db06fc37a2c6d61ecbe06227c17ce387a5bf7d7c887f', '[\"*\"]', NULL, NULL, '2025-06-17 21:07:05', '2025-06-17 21:07:05'),
(79, 'App\\Models\\StructAgent', '1', 'authToken', 'cd08d15d827740ccfb41ad5be08f0ac7f2261d7c6369bef7e622e1540170cb0b', '[\"*\"]', NULL, NULL, '2025-06-17 21:23:58', '2025-06-17 21:23:58'),
(80, 'App\\Models\\StructAgent', '1', 'authToken', '6a9c8ab21bca5cc08c05f8b5937f5531b8cacfebeb973957d7c8b288c85c7078', '[\"*\"]', NULL, NULL, '2025-06-17 21:30:42', '2025-06-17 21:30:42'),
(81, 'App\\Models\\StructAgent', '1', 'authToken', '26d66a97246e8a5509f831cdc897ac050074b3aedbce00d87a41f9ec4de440d4', '[\"*\"]', NULL, NULL, '2025-06-17 21:37:02', '2025-06-17 21:37:02'),
(82, 'App\\Models\\StructAgent', '1', 'authToken', '36455076c0be99c978055d6b2b90d2bd79151e9c17d6b31b49fab236bd4bc94f', '[\"*\"]', NULL, NULL, '2025-06-17 21:39:12', '2025-06-17 21:39:12'),
(83, 'App\\Models\\StructAgent', '2', 'authToken', 'b1f39fa26cf704723b63aa4313c6b0d1224a997104877abc34f5f88613a23d00', '[\"*\"]', NULL, NULL, '2025-06-17 21:56:25', '2025-06-17 21:56:25'),
(84, 'App\\Models\\StructAgent', '1', 'authToken', 'ec9b6b2908bda56e523df281852af3caf4d82c72403c2d4c0af4dc62e7383e1f', '[\"*\"]', NULL, NULL, '2025-06-18 08:27:05', '2025-06-18 08:27:05'),
(85, 'App\\Models\\StructAgent', '1', 'authToken', 'b15f08664e0384b025f5094aaad4eb6ebd99e0d03e5fe7547f937918d625a173', '[\"*\"]', NULL, NULL, '2025-06-18 20:09:04', '2025-06-18 20:09:04'),
(86, 'App\\Models\\StructAgent', '1', 'authToken', '8211461a06b382e78d15750746a08a5ec37104eb5d06c2cf4e17fac2ec0818dd', '[\"*\"]', NULL, NULL, '2025-06-19 14:05:21', '2025-06-19 14:05:21'),
(87, 'App\\Models\\StructAgent', '1', 'authToken', '700c064a64a15f7fe7a271a35a1eb461bbb102e8852d1ef48dcf4a226d11edbe', '[\"*\"]', NULL, NULL, '2025-06-19 14:10:24', '2025-06-19 14:10:24'),
(88, 'App\\Models\\StructAgent', '1', 'authToken', 'f419f408ddba04d246f38b9e89b563656a9b3b4f037c4efa08526a5d325007a3', '[\"*\"]', NULL, NULL, '2025-06-19 14:16:35', '2025-06-19 14:16:35'),
(89, 'App\\Models\\StructAgent', '1', 'authToken', '254c8c27c529b6c19547225e66228bbed7f806766cb3b9173bba34e98bebe700', '[\"*\"]', NULL, NULL, '2025-06-19 15:24:03', '2025-06-19 15:24:03'),
(90, 'App\\Models\\StructAgent', '1', 'authToken', 'd77b6cb2c541687ef334b8f001ecf959e9d431e7a7a199466d4a9476201c0d4b', '[\"*\"]', NULL, NULL, '2025-06-19 15:26:36', '2025-06-19 15:26:36'),
(91, 'App\\Models\\StructAgent', '1', 'authToken', 'cc0d427cdd826c898e13bfd5c79c5e9fd0970333ab76c3f0d1baa1e126049cd5', '[\"*\"]', NULL, NULL, '2025-06-19 15:45:01', '2025-06-19 15:45:01'),
(92, 'App\\Models\\StructAgent', '1', 'authToken', '4870df7e90e09bd45a33e632bec29bd10c32373911c730d41380dd3b24411a82', '[\"*\"]', NULL, NULL, '2025-06-19 15:50:09', '2025-06-19 15:50:09'),
(93, 'App\\Models\\StructAgent', '1', 'authToken', '7c1adfba75d46d935fce48102a2dc7e89b701a77c54b02f2e10575efb723822a', '[\"*\"]', NULL, NULL, '2025-06-19 15:56:57', '2025-06-19 15:56:57'),
(94, 'App\\Models\\StructAgent', '1', 'authToken', '4e0c82d50708e535561cc02ab8b6e19b5383064136febaaca3b3f78a196f05a2', '[\"*\"]', NULL, NULL, '2025-06-19 16:25:13', '2025-06-19 16:25:13'),
(95, 'App\\Models\\StructAgent', '2', 'authToken', '2e72e1755c0772b174455f9442616de10be196cf462aaa13434c25652c61886e', '[\"*\"]', '2025-06-23 07:30:19', NULL, '2025-06-23 07:30:19', '2025-06-23 07:30:19'),
(96, 'App\\Models\\StructAgent', '2', 'authToken', '9801e36a7cf365577f7bad256cdf352a9c10e91c86dbb8e26d28c3e0547b2b68', '[\"*\"]', '2025-06-23 07:42:22', NULL, '2025-06-23 07:42:21', '2025-06-23 07:42:22'),
(97, 'App\\Models\\StructAgent', '2', 'authToken', '5637153bb78ffdf27c696e75158c274af666caf623c5b135ca315c91ea6ffe5c', '[\"*\"]', '2025-06-23 07:46:49', NULL, '2025-06-23 07:42:42', '2025-06-23 07:46:49'),
(98, 'App\\Models\\StructAgent', '1', 'authToken', '43f27b60a3e1e9d52103ffd7246e47237e1ee63ba6cdadd2c6be098cdee7b6ff', '[\"*\"]', '2025-06-23 07:47:14', NULL, '2025-06-23 07:47:09', '2025-06-23 07:47:14'),
(99, 'App\\Models\\StructAgent', '2', 'authToken', '6bb7b9457f005eb69eefda5a6dc7e1fdc14abbd9ce3a4ea2d35e623d817ab217', '[\"*\"]', NULL, NULL, '2025-06-23 07:48:28', '2025-06-23 07:48:28'),
(100, 'App\\Models\\StructAgent', '3', 'authToken', 'fe03540dadef311eb8ce7c8f8a849da0bceae2c8ebd56d2f9f49bc8448d324e9', '[\"*\"]', '2025-06-23 07:55:15', NULL, '2025-06-23 07:54:57', '2025-06-23 07:55:15'),
(101, 'App\\Models\\StructAgent', '3', 'authToken', '02c643ca25a38429655b6b97e0e01d123475600e444818fdda29a72daec86a34', '[\"*\"]', '2025-06-23 07:57:08', NULL, '2025-06-23 07:57:08', '2025-06-23 07:57:08'),
(102, 'App\\Models\\StructAgent', '3', 'authToken', '676a6d02eec82cb81c92ac65ccf695e0842b4e68994d33b1692db74d330483c5', '[\"*\"]', '2025-06-23 08:15:23', NULL, '2025-06-23 08:15:23', '2025-06-23 08:15:23'),
(103, 'App\\Models\\StructAgent', '3', 'authToken', 'dd0a57e61cb002d297ebf8f9630093c23763ebc1aab8e2a8ddd45dbaa5e23299', '[\"*\"]', '2025-06-23 09:34:00', NULL, '2025-06-23 08:26:09', '2025-06-23 09:34:00'),
(104, 'App\\Models\\StructAgent', '3', 'authToken', '44be77c9e29383d1b3aa9269677c27af13afba22730ae43b65288bbee52eabe4', '[\"*\"]', '2025-06-23 09:41:09', NULL, '2025-06-23 09:39:18', '2025-06-23 09:41:09'),
(105, 'App\\Models\\StructAgent', '5', 'authToken', 'c4a0da5f59d29f0628b92026148106046c778a38e09b04018d8618f9874474ad', '[\"*\"]', '2025-06-23 09:51:13', NULL, '2025-06-23 09:41:29', '2025-06-23 09:51:13'),
(106, 'App\\Models\\StructAgent', '5', 'authToken', '43df3d2a9af0d7c6e31a28dbb10e202208c15f994d318fa2ec8625acf7a3f2c4', '[\"*\"]', '2025-06-23 09:52:56', NULL, '2025-06-23 09:52:37', '2025-06-23 09:52:56'),
(107, 'App\\Models\\StructAgent', '3', 'authToken', 'd7dffdb6149973a75a9b326f7482ebd7179147d8e166cc46850febb7c0c6c5e6', '[\"*\"]', '2025-06-23 09:55:58', NULL, '2025-06-23 09:53:05', '2025-06-23 09:55:58'),
(108, 'App\\Models\\StructAgent', '3', 'authToken', '2800b2afa58b8b4e91953cf8e3151da0f74545f5a2dd46b77f5c59b09fd60c43', '[\"*\"]', NULL, NULL, '2025-06-23 09:56:06', '2025-06-23 09:56:06'),
(109, 'App\\Models\\StructAgent', '3', 'authToken', 'e87e87581d5bb0de691562972a7ab069b99d7e17ff60f971f417c0c900757398', '[\"*\"]', '2025-06-23 09:57:17', NULL, '2025-06-23 09:57:16', '2025-06-23 09:57:17'),
(110, 'App\\Models\\StructAgent', '5', 'authToken', '18cb6dc7142503fdea5dda8465c6f787f69cb42c138f81dd67af29d32ecff8de', '[\"*\"]', '2025-06-23 09:58:28', NULL, '2025-06-23 09:57:47', '2025-06-23 09:58:28'),
(111, 'App\\Models\\StructAgent', '5', 'authToken', 'f9aaf84aefb9262afb3217ec5fb31353b2da36eea837f4a8792e527ad74b6818', '[\"*\"]', '2025-06-23 09:58:39', NULL, '2025-06-23 09:58:39', '2025-06-23 09:58:39'),
(112, 'App\\Models\\StructAgent', '5', 'authToken', 'bf4f84322c4024e3c3713a6beec6c34637be7e755fa76d835d4220953ec0b165', '[\"*\"]', '2025-06-23 10:01:48', NULL, '2025-06-23 10:01:40', '2025-06-23 10:01:48'),
(113, 'App\\Models\\StructAgent', '3', 'authToken', '63da9119f9b4d31319f183bc2f1f23c15e7b668b2ff1600fb4fac88c9938c237', '[\"*\"]', '2025-06-23 10:08:59', NULL, '2025-06-23 10:08:18', '2025-06-23 10:08:59'),
(114, 'App\\Models\\StructAgent', '6', 'authToken', '4dd251673a4f1a2c5da8b2c9444a3b95b223fee7ac0f804469c5e46bdc0c94af', '[\"*\"]', '2025-06-23 10:09:24', NULL, '2025-06-23 10:09:11', '2025-06-23 10:09:24'),
(115, 'App\\Models\\StructAgent', '3', 'authToken', '32761b999e35dd40454faf07223f9bdfb36b1208f8fa436193646612813fd9e5', '[\"*\"]', '2025-06-23 10:40:09', NULL, '2025-06-23 10:17:51', '2025-06-23 10:40:09'),
(116, 'App\\Models\\StructAgent', '2', 'authToken', '5dd8eccb7603eabf35fcdb4a5a1c6bc8e2c13c94e351dacfedf626934bf30d56', '[\"*\"]', '2025-06-23 11:01:10', NULL, '2025-06-23 11:00:38', '2025-06-23 11:01:10'),
(117, 'App\\Models\\StructAgent', '1', 'authToken', 'ac586907bf2e96a04e7eca50634e96f43a45fa13ca75cef9d7a913d3c79ac543', '[\"*\"]', '2025-06-23 11:02:20', NULL, '2025-06-23 11:02:10', '2025-06-23 11:02:20'),
(118, 'App\\Models\\StructAgent', '3', 'authToken', 'c1f1d68d2659d4638ae31218d8f66e67aabb362603c86f0434ef706d875d13c9', '[\"*\"]', '2025-06-23 11:22:06', NULL, '2025-06-23 11:02:28', '2025-06-23 11:22:06'),
(119, 'App\\Models\\StructAgent', '3', 'authToken', 'abeee39c381e77b24e1c6081ac492c2858155b2b8bb1e302557791b799fd8c72', '[\"*\"]', '2025-06-23 12:15:07', NULL, '2025-06-23 11:22:25', '2025-06-23 12:15:07'),
(120, 'App\\Models\\StructAgent', '2', 'authToken', '75ee8fec2d86b1cf0d6e0042f84e34e89cdae6f3db12ccd4c67994e82bc3341f', '[\"*\"]', NULL, NULL, '2025-06-23 12:18:37', '2025-06-23 12:18:37'),
(121, 'App\\Models\\StructAgent', '1', 'authToken', 'f8c8b3cf783424f36d3a6c92a2abbec655807fad7dbcd8a6a2575c9628b4ecb4', '[\"*\"]', NULL, NULL, '2025-06-23 20:11:45', '2025-06-23 20:11:45'),
(122, 'App\\Models\\StructAgent', '1', 'authToken', 'cf92b266c4777ca26a8a2c29facb6af9a6876805c307713f1a628522df9df7f0', '[\"*\"]', NULL, NULL, '2025-06-23 20:52:15', '2025-06-23 20:52:15'),
(123, 'App\\Models\\StructAgent', '1', 'authToken', 'd1c251e06a8f849cbd1cb1ca13573fc763bab5c3d12d1c31bff8613a2b556437', '[\"*\"]', NULL, NULL, '2025-06-23 20:56:00', '2025-06-23 20:56:00'),
(124, 'App\\Models\\StructAgent', '1', 'authToken', 'a83f533d94e1173766f0a10fb6a98e4be12cb28412be8b98021ca9813e7eee6a', '[\"*\"]', NULL, NULL, '2025-06-23 21:04:41', '2025-06-23 21:04:41'),
(125, 'App\\Models\\StructAgent', '1', 'authToken', '16ded910844baec3d4b21ade4b291b954fece27ab8daa36e7b57afdd0bfc6513', '[\"*\"]', NULL, NULL, '2025-06-23 21:06:27', '2025-06-23 21:06:27'),
(126, 'App\\Models\\StructAgent', '1', 'authToken', '39e96cb178af9b3c1517125d84f7eb109ef5950d86171ff2e54a788b92a13916', '[\"*\"]', NULL, NULL, '2025-06-23 21:09:53', '2025-06-23 21:09:53'),
(127, 'App\\Models\\StructAgent', '1', 'authToken', 'a62aa51a55fe324047d68b5988d9490ac8f5bf3fcc295214b5d83a1275fe410a', '[\"*\"]', NULL, NULL, '2025-06-23 22:17:18', '2025-06-23 22:17:18'),
(128, 'App\\Models\\StructAgent', '1', 'authToken', '32bebe8570e9870751ab0641078b5caac6f35fa04f132c76e1016f6a50f12396', '[\"*\"]', NULL, NULL, '2025-06-23 22:17:58', '2025-06-23 22:17:58'),
(129, 'App\\Models\\StructAgent', '1', 'authToken', '2ef5aa6a6edf650916cd204b55e43be86c97925b48994bcd151bb3a361c9c4ca', '[\"*\"]', NULL, NULL, '2025-06-23 22:19:34', '2025-06-23 22:19:34'),
(130, 'App\\Models\\StructAgent', '1', 'authToken', '6b800a9c1b92b6857ab45f73fba448e98100d183398480b98300069f258f853b', '[\"*\"]', NULL, NULL, '2025-06-23 22:25:54', '2025-06-23 22:25:54'),
(131, 'App\\Models\\StructAgent', '1', 'authToken', '2bbfbc465c17f2615aff75434604ccd400638c33763b96ec0131a50780bb4b4b', '[\"*\"]', NULL, NULL, '2025-06-23 22:31:34', '2025-06-23 22:31:34'),
(132, 'App\\Models\\StructAgent', '1', 'authToken', '948693266001f2798ba7d7815054f0c0f0f1a49f56d8f138ec809fe5a7718da1', '[\"*\"]', NULL, NULL, '2025-06-23 22:34:30', '2025-06-23 22:34:30'),
(133, 'App\\Models\\StructAgent', '1', 'authToken', 'aa10992a611d876b632c2a3900351d155f2becd28ad3c27492d36a84792cbabf', '[\"*\"]', NULL, NULL, '2025-06-24 07:42:53', '2025-06-24 07:42:53'),
(134, 'App\\Models\\StructAgent', '2', 'authToken', '381ca454efc832cac9238803172ee5107e68fed4feba8419d8c240e303ea6d9d', '[\"*\"]', NULL, NULL, '2025-06-24 07:48:46', '2025-06-24 07:48:46'),
(135, 'App\\Models\\StructAgent', '3', 'authToken', 'bd457b4321173cd49d27c0da9cb9e8e4c452318df9b504e4403e9311287c9079', '[\"*\"]', NULL, NULL, '2025-06-24 07:52:15', '2025-06-24 07:52:15'),
(136, 'App\\Models\\StructAgent', '3', 'authToken', 'e97a300fb492b94fb0b1954443fe070bb1d5c72fb3d6729f15edf0249231f722', '[\"*\"]', NULL, NULL, '2025-06-24 08:13:26', '2025-06-24 08:13:26'),
(137, 'App\\Models\\StructAgent', '7', 'authToken', 'e9a6aef9b1526f4486a89c084d3f22cfda40bb915ed75aa29ab9176a79254a37', '[\"*\"]', NULL, NULL, '2025-06-24 08:14:42', '2025-06-24 08:14:42'),
(138, 'App\\Models\\StructAgent', '1', 'authToken', '56b840cc1537cd1a7c24158bc920afa1e60d755a73e76e59e80338c56462d3e6', '[\"*\"]', NULL, NULL, '2025-06-24 08:36:33', '2025-06-24 08:36:33'),
(139, 'App\\Models\\StructAgent', '9', 'authToken', '9b3912140687004590f621e28dfe353d82309544d17c26aa04d109cf6fb98e4d', '[\"*\"]', NULL, NULL, '2025-06-24 08:45:21', '2025-06-24 08:45:21'),
(140, 'App\\Models\\StructAgent', '11', 'authToken', '96bb788155a66b723e610236bf384a0dd44770748b673a51faa89ba29f3ab329', '[\"*\"]', NULL, NULL, '2025-06-24 08:47:56', '2025-06-24 08:47:56'),
(141, 'App\\Models\\StructAgent', '3', 'authToken', 'd6d0b1f0328d19456f77a66f48772b12728bb0a03e1917fb2f2b38aecd53085b', '[\"*\"]', NULL, NULL, '2025-06-24 09:00:10', '2025-06-24 09:00:10'),
(142, 'App\\Models\\StructAgent', '7', 'authToken', '133bfee6d6e1d65e3594b398bb622c9e6215937ca63678dcd9546fa63f333709', '[\"*\"]', NULL, NULL, '2025-06-24 09:04:28', '2025-06-24 09:04:28'),
(143, 'App\\Models\\StructAgent', '11', 'authToken', 'b7f7e0e1c3434641e41982323eab094963b2114541d539e361455117cf0f0645', '[\"*\"]', NULL, NULL, '2025-06-24 09:11:52', '2025-06-24 09:11:52'),
(144, 'App\\Models\\StructAgent', '11', 'authToken', '9a39d3c5fcdc256cea9dedec10e69bd2fad993fb7d0b2f8d45bf89a78cf4c9d3', '[\"*\"]', NULL, NULL, '2025-06-24 09:34:29', '2025-06-24 09:34:29'),
(145, 'App\\Models\\StructAgent', '1', 'authToken', '7e64443d244d0795f868c8a9afce6f0bcc8342910b817f5c1545c4498d84d84b', '[\"*\"]', NULL, NULL, '2025-06-24 09:58:24', '2025-06-24 09:58:24'),
(146, 'App\\Models\\StructAgent', '1', 'authToken', '5e6ad1ee7b6a2f4a91852b37e51497f6c010522a3e9161a562ddedbb67d65389', '[\"*\"]', NULL, NULL, '2025-06-24 11:44:17', '2025-06-24 11:44:17'),
(147, 'App\\Models\\StructAgent', '3', 'authToken', '95bf14c8f4692e53545f0d0a45b2a0466d79e5353604993c579150bb48016f15', '[\"*\"]', NULL, NULL, '2025-06-24 11:53:26', '2025-06-24 11:53:26'),
(148, 'App\\Models\\StructAgent', '13', 'authToken', '2a67aee9ddb6fa3fddd7ef742317b96bd95bdd25508e535951893f9434217df4', '[\"*\"]', NULL, NULL, '2025-06-24 11:59:27', '2025-06-24 11:59:27'),
(149, 'App\\Models\\StructAgent', '14', 'authToken', '786f429cde81054db52dfcdec38e62a69b5411006df6329f3bcdc746d4daf4a7', '[\"*\"]', NULL, NULL, '2025-06-24 12:01:30', '2025-06-24 12:01:30'),
(150, 'App\\Models\\StructAgent', '7', 'authToken', '24648c516d821b764cad88d4aaf33331a6a331f3462753a40b762d5d81b54389', '[\"*\"]', NULL, NULL, '2025-06-24 12:57:33', '2025-06-24 12:57:33'),
(151, 'App\\Models\\StructAgent', '3', 'authToken', 'cb455d55c3e0f858db7b22b804d23196fb90b2efddbb436a52b22dd92beeb926', '[\"*\"]', NULL, NULL, '2025-06-24 12:58:32', '2025-06-24 12:58:32'),
(152, 'App\\Models\\StructAgent', '15', 'authToken', '459098eaddfa4fe335e32bf8b3e201f192947555bb413677bb8c27e10c79a06f', '[\"*\"]', NULL, NULL, '2025-06-24 12:59:17', '2025-06-24 12:59:17'),
(153, 'App\\Models\\StructAgent', '3', 'authToken', '84f8df9598dc001dd127d3e37e4fd935d019e7d8d4c59b3a9226c2f712feb6ad', '[\"*\"]', NULL, NULL, '2025-06-24 13:00:06', '2025-06-24 13:00:06'),
(154, 'App\\Models\\StructAgent', '16', 'authToken', 'dbb26cff3f0f11a29a0b2610946b5e93ede9e5d27e39d66f2c05104b50d9647e', '[\"*\"]', NULL, NULL, '2025-06-24 13:01:15', '2025-06-24 13:01:15'),
(155, 'App\\Models\\StructAgent', '16', 'authToken', 'e6e3ba9a6a20dd0c9bad3c4729583ec06e2da6a728f7e724d0afbb95b076bd20', '[\"*\"]', NULL, NULL, '2025-06-24 13:30:22', '2025-06-24 13:30:22'),
(156, 'App\\Models\\StructAgent', '16', 'authToken', 'a936e296d0db503bf2ef25b3e5a03bcf19791b923787d73e5f61a37acc00ece0', '[\"*\"]', NULL, NULL, '2025-06-24 13:47:13', '2025-06-24 13:47:13'),
(157, 'App\\Models\\StructAgent', '21', 'authToken', '538c64587d977aa473709a5f8c81753433590fb9d67d0b9390c3989f18e7cf10', '[\"*\"]', NULL, NULL, '2025-06-24 13:48:06', '2025-06-24 13:48:06'),
(158, 'App\\Models\\StructAgent', '17', 'authToken', '112a7a4c99bab243ff16746e004a1f6876205c0b4a29b1e1afd6999155a3ca6f', '[\"*\"]', NULL, NULL, '2025-06-24 13:49:59', '2025-06-24 13:49:59'),
(159, 'App\\Models\\StructAgent', '18', 'authToken', '15f99f90466035bdcebb07fd7c279a85ba69143b68a15b49f05bbcb6eb597397', '[\"*\"]', NULL, NULL, '2025-06-24 13:51:04', '2025-06-24 13:51:04'),
(160, 'App\\Models\\StructAgent', '20', 'authToken', 'a8c6df28a58810a21df3f9c95e66fa85ee111b350a944ebb9047d53e699ca4d9', '[\"*\"]', NULL, NULL, '2025-06-24 13:52:07', '2025-06-24 13:52:07'),
(161, 'App\\Models\\StructAgent', '16', 'authToken', 'ccd30453b11d4009facd3369d34a1ff0190a6736c2cec4903bd310f69bd1df93', '[\"*\"]', NULL, NULL, '2025-06-24 13:59:23', '2025-06-24 13:59:23'),
(162, 'App\\Models\\StructAgent', '19', 'authToken', '6e957f568381357693ba8303cdd788ebc392a3b1bdbb4160c875c01dcc5b218e', '[\"*\"]', NULL, NULL, '2025-06-24 14:03:20', '2025-06-24 14:03:20'),
(163, 'App\\Models\\StructAgent', '22', 'authToken', '71eb42c22b56c30a99af20ef5261407082c5fa2273931e60244b5bf12b784e03', '[\"*\"]', NULL, NULL, '2025-06-24 14:15:50', '2025-06-24 14:15:50'),
(164, 'App\\Models\\StructAgent', '20', 'authToken', '33fb66aea9394650a1797e6f901915cf89c94289ff215cbf23764ad2f5e5b7b5', '[\"*\"]', NULL, NULL, '2025-06-24 14:17:33', '2025-06-24 14:17:33'),
(165, 'App\\Models\\StructAgent', '16', 'authToken', '2ee64aaf22a7c5de7dadb7a133c915ed55ab77a65de7a8d31e0de841ee399991', '[\"*\"]', NULL, NULL, '2025-06-24 14:26:39', '2025-06-24 14:26:39'),
(166, 'App\\Models\\StructAgent', '16', 'authToken', '0c1f09d53e3b2ad27c4384ae1075e540debf9eef8ad6839da85b550dd5f2d32b', '[\"*\"]', NULL, NULL, '2025-06-24 14:30:27', '2025-06-24 14:30:27'),
(167, 'App\\Models\\StructAgent', '22', 'authToken', 'b66c9b04de714979916796f1849b9eca716886de2687f8a21aab98023d9ddc41', '[\"*\"]', NULL, NULL, '2025-06-24 14:38:22', '2025-06-24 14:38:22'),
(168, 'App\\Models\\StructAgent', '16', 'authToken', 'c560f9ea17c6392da341b260198c823f5c10764ee67d5ceadbf3f89d0bcccc66', '[\"*\"]', NULL, NULL, '2025-06-24 14:55:37', '2025-06-24 14:55:37'),
(169, 'App\\Models\\StructAgent', '17', 'authToken', '974b1dc107f532dfa229c1f47bc0dab0f815ea2a8bbb5d968aaff21878c1062c', '[\"*\"]', NULL, NULL, '2025-06-24 15:13:21', '2025-06-24 15:13:21'),
(170, 'App\\Models\\StructAgent', '21', 'authToken', 'c86c3abe6278ad65a4ed9dda5d0ce42472eaeb23a84fb48223b40084377e225c', '[\"*\"]', NULL, NULL, '2025-06-24 15:13:45', '2025-06-24 15:13:45'),
(171, 'App\\Models\\StructAgent', '20', 'authToken', '6b21fd22f680b59ee211a7d13b901fdd171895481be25c3115390180e42d8b37', '[\"*\"]', NULL, NULL, '2025-06-24 15:16:33', '2025-06-24 15:16:33'),
(172, 'App\\Models\\StructAgent', '16', 'authToken', '240d3f04dc8d152b88b759300761afbb0d8330a93d1a0d66d3b54477612657fe', '[\"*\"]', NULL, NULL, '2025-06-24 15:16:37', '2025-06-24 15:16:37'),
(173, 'App\\Models\\StructAgent', '19', 'authToken', '78dc6d5661cd95bcfcd8da015ea9ff20e24a1458fbcc8e90e3b7a7387ed2d867', '[\"*\"]', NULL, NULL, '2025-06-24 15:19:14', '2025-06-24 15:19:14'),
(174, 'App\\Models\\StructAgent', '17', 'authToken', '10b0a74fcee0b95be3ba320ef581a1f59608a23b44eb221482082462f662c151', '[\"*\"]', NULL, NULL, '2025-06-24 15:20:52', '2025-06-24 15:20:52'),
(175, 'App\\Models\\StructAgent', '22', 'authToken', 'f9ff94546c708b1076b43ba5d68258cc3c571e53341f976a621da95fa81d12b3', '[\"*\"]', NULL, NULL, '2025-06-24 15:40:00', '2025-06-24 15:40:00'),
(176, 'App\\Models\\StructAgent', '20', 'authToken', '498dba331701e7bb19e883f251e8399458bdd70416556e73a95730abfb7778ad', '[\"*\"]', NULL, NULL, '2025-06-25 06:12:04', '2025-06-25 06:12:04'),
(177, 'App\\Models\\StructAgent', '16', 'authToken', 'e4c324e148f5338e9484396cb20ad5f7c30c0c7a53d30e4d97c6e6ff891b751a', '[\"*\"]', NULL, NULL, '2025-06-25 06:12:22', '2025-06-25 06:12:22'),
(178, 'App\\Models\\StructAgent', '19', 'authToken', '9174486bf2072a3fb9c69b226bfc297257d03f389432d7ecc0dc7aec06a07ee9', '[\"*\"]', NULL, NULL, '2025-06-25 06:30:50', '2025-06-25 06:30:50'),
(179, 'App\\Models\\StructAgent', '16', 'authToken', '19caf1f46432fa5a6f7e65b58aeeddd498f1741b383afecd30a56a4983c11442', '[\"*\"]', NULL, NULL, '2025-06-25 06:31:45', '2025-06-25 06:31:45'),
(180, 'App\\Models\\StructAgent', '16', 'authToken', '51d738dba0e23df39af8776631180588046dd490f2012e3723cf03e2b0a28258', '[\"*\"]', NULL, NULL, '2025-06-25 06:33:20', '2025-06-25 06:33:20'),
(181, 'App\\Models\\StructAgent', '20', 'authToken', '21c757e5493018e9a82c3ce3d5b5cc74a3e90d981e8ce2f7dcb270af84975bb0', '[\"*\"]', NULL, NULL, '2025-06-25 06:35:23', '2025-06-25 06:35:23'),
(182, 'App\\Models\\StructAgent', '3', 'authToken', 'b1ec42114eb03b2d22db467a29595049cbc4a5a5cf1022a032a753919a065a1b', '[\"*\"]', NULL, NULL, '2025-06-25 08:37:09', '2025-06-25 08:37:09'),
(183, 'App\\Models\\StructAgent', '23', 'authToken', '04103c8840234370dc0ac948a2c38b7a9a55b66109fa10c1e6d6e950360d5d8c', '[\"*\"]', NULL, NULL, '2025-06-25 08:38:27', '2025-06-25 08:38:27'),
(184, 'App\\Models\\StructAgent', '24', 'authToken', 'b1812b8ef2c26bcceaa15d0f66dcbdcefefba3fe96f350ada63fd35c941f6abf', '[\"*\"]', NULL, NULL, '2025-06-25 08:40:50', '2025-06-25 08:40:50'),
(185, 'App\\Models\\StructAgent', '24', 'authToken', 'a058d3f4e7d32e5bf14a5a96a2d0df3ffc1d15f03d9a5a961804053a54300e19', '[\"*\"]', NULL, NULL, '2025-06-25 19:48:50', '2025-06-25 19:48:50'),
(186, 'App\\Models\\StructAgent', '24', 'authToken', '11821f3cb6d61df190570645b687fca8e1d1b6908b8dae108aaf42202d20de7e', '[\"*\"]', NULL, NULL, '2025-06-25 20:04:13', '2025-06-25 20:04:13'),
(187, 'App\\Models\\StructAgent', '24', 'authToken', 'a7bb757cddb9beb38c3fb483a42666152663f6d2df3a8cf19a89ac20b2193b2d', '[\"*\"]', NULL, NULL, '2025-06-25 20:20:31', '2025-06-25 20:20:31'),
(188, 'App\\Models\\StructAgent', '24', 'authToken', '65aab462d4400fadae150e0a92e21ab356530da6144a6c5d2c0aa5705a1e74ad', '[\"*\"]', NULL, NULL, '2025-06-25 20:45:25', '2025-06-25 20:45:25'),
(189, 'App\\Models\\StructAgent', '24', 'authToken', 'cc6083e70f2bdd6eb2d6034368c56864363cd7c8f0e7990fbdc3d6f81f28ea34', '[\"*\"]', NULL, NULL, '2025-06-25 20:45:55', '2025-06-25 20:45:55'),
(190, 'App\\Models\\StructAgent', '24', 'authToken', 'c5463ad186c7dc0dc95290b5e306b3a8435b66525846678a7f92866c3a61e055', '[\"*\"]', NULL, NULL, '2025-06-25 20:46:28', '2025-06-25 20:46:28'),
(191, 'App\\Models\\StructAgent', '24', 'authToken', 'd625f85af19d81cb491d2ffe728f6b1f9801b7f925ccece9f4ae955e4337b3aa', '[\"*\"]', NULL, NULL, '2025-06-25 20:46:45', '2025-06-25 20:46:45'),
(192, 'App\\Models\\StructAgent', '24', 'authToken', '67b6fb64a4f6a334eec4b672ebe1db11cc6c5f87014464525075584e2f637597', '[\"*\"]', NULL, NULL, '2025-06-25 21:07:20', '2025-06-25 21:07:20'),
(193, 'App\\Models\\StructAgent', '24', 'authToken', 'd117eec82e3cc2c4c08bbea0a5859956f8de1875e648a484bcd36747cd9f7f2a', '[\"*\"]', NULL, NULL, '2025-06-25 21:18:13', '2025-06-25 21:18:13'),
(194, 'App\\Models\\StructAgent', '24', 'authToken', '76fb9963d62eaa2b9201ca5617bc0b65ac6b60705537a1bbea271281133dd148', '[\"*\"]', NULL, NULL, '2025-06-25 23:05:40', '2025-06-25 23:05:40'),
(195, 'App\\Models\\StructAgent', '16', 'authToken', 'df5fc23cf479826464f537630b8aa0d7c95c89881765f1d15fd602ce8978aa60', '[\"*\"]', NULL, NULL, '2025-06-27 09:53:23', '2025-06-27 09:53:23'),
(196, 'App\\Models\\StructAgent', '24', 'authToken', '963e2b8a196e356d185e6a821556ded9eefcff551cc58010abc032150bdcde29', '[\"*\"]', NULL, NULL, '2025-06-28 07:43:09', '2025-06-28 07:43:09'),
(197, 'App\\Models\\StructAgent', '24', 'authToken', '007c370710c7ab4f08348f7681b1832976b3e19c66167cdc61ffbc9c9ff0fdad', '[\"*\"]', NULL, NULL, '2025-06-28 13:22:55', '2025-06-28 13:22:55'),
(198, 'App\\Models\\StructAgent', '24', 'authToken', 'b9ac574e95905689c423fc4d63842dfda02f8756716525c15f01de0855276093', '[\"*\"]', NULL, NULL, '2025-06-28 14:21:22', '2025-06-28 14:21:22'),
(199, 'App\\Models\\StructAgent', '24', 'authToken', '62c4d76c5a1595e6a4ba4a11311c850e1f22194f66df62d7d6c21e73a503aec3', '[\"*\"]', NULL, NULL, '2025-06-28 14:22:38', '2025-06-28 14:22:38'),
(200, 'App\\Models\\StructAgent', '3', 'authToken', 'd4cf3cb4609ac1cb173bb65cc981a888795faded61f4e5dc0d6267ddd323c3bf', '[\"*\"]', NULL, NULL, '2025-06-28 15:05:14', '2025-06-28 15:05:14'),
(201, 'App\\Models\\StructAgent', '16', 'authToken', '439345ee2920921554ee16be4c5eb04a1773038cb52ee46f6dba819df6f01557', '[\"*\"]', NULL, NULL, '2025-06-28 15:06:19', '2025-06-28 15:06:19'),
(202, 'App\\Models\\StructAgent', '24', 'authToken', '702e6c05ed944fb26f90dcc6d93ea181940573dad65ff09f147742ed405fe760', '[\"*\"]', NULL, NULL, '2025-07-01 15:02:59', '2025-07-01 15:02:59'),
(203, 'App\\Models\\StructAgent', '24', 'authToken', 'a0d5eeedf0e2a54ab559fa2f44660e0afacbc3640881122fd8d5183a7e09095a', '[\"*\"]', NULL, NULL, '2025-07-02 08:55:41', '2025-07-02 08:55:41'),
(204, 'App\\Models\\StructAgent', '24', 'authToken', '035e54a499fad90f2d321eeccf0001ea128774421f7fef77b770ba6b3914ff25', '[\"*\"]', NULL, NULL, '2025-07-02 09:29:45', '2025-07-02 09:29:45'),
(205, 'App\\Models\\StructAgent', '3', 'authToken', '5c39d9dede46cf02586c65805e196ada6123c611eaf85aef6a75f5d7347775c7', '[\"*\"]', NULL, NULL, '2025-07-02 09:51:43', '2025-07-02 09:51:43'),
(206, 'App\\Models\\StructAgent', '3', 'authToken', '767f884f3aa86a343b31a01fc2d9da2be111e472c07ea838ff4f16ce362d3eb7', '[\"*\"]', NULL, NULL, '2025-07-02 10:09:00', '2025-07-02 10:09:00'),
(207, 'App\\Models\\StructAgent', '24', 'authToken', '79dccf1b821e6ede123480000fddee83ed2c9bb2687e60800624910b2aa0fe53', '[\"*\"]', NULL, NULL, '2025-07-02 10:31:20', '2025-07-02 10:31:20'),
(208, 'App\\Models\\StructAgent', '3', 'authToken', 'bfad89569d857bd51f816632361785e06b88b6dc0075c03407c1d9aa5c69d40f', '[\"*\"]', NULL, NULL, '2025-07-02 15:08:31', '2025-07-02 15:08:31'),
(209, 'App\\Models\\StructAgent', '3', 'authToken', '281bdacfd340eb7e3b8f70080cf28c327b6f4fdeeabf815a68e53850cd94b619', '[\"*\"]', NULL, NULL, '2025-07-02 15:09:16', '2025-07-02 15:09:16'),
(210, 'App\\Models\\StructAgent', '3', 'authToken', '688b2c832bcd772b7b02ba6d4b7af6b03d428435e9508e63c73b51a3b0412a27', '[\"*\"]', NULL, NULL, '2025-07-02 15:11:53', '2025-07-02 15:11:53'),
(211, 'App\\Models\\StructAgent', '16', 'authToken', '29caf8da56c99a7e67c6c5e30c1a8d995c9166f39ba291f4b5c60b59bbb9cc2c', '[\"*\"]', NULL, NULL, '2025-07-02 15:16:32', '2025-07-02 15:16:32'),
(212, 'App\\Models\\StructAgent', '16', 'authToken', '028544e97bab13302d0dda103892ba3b771a5056a2d3b050483bfce6ce36b6a0', '[\"*\"]', NULL, NULL, '2025-07-02 15:25:40', '2025-07-02 15:25:40'),
(213, 'App\\Models\\StructAgent', '3', 'authToken', '5cc8d96b058084d993a7e2c8ed8199d555ba1a490c5648c4aabc47b4238846bf', '[\"*\"]', NULL, NULL, '2025-07-03 12:47:32', '2025-07-03 12:47:32'),
(214, 'App\\Models\\StructAgent', '16', 'authToken', '043f68ef90fb60d143981f2810547fdd318c038755d7a59b624157f9be31fb06', '[\"*\"]', NULL, NULL, '2025-07-03 12:47:41', '2025-07-03 12:47:41'),
(215, 'App\\Models\\StructAgent', '3', 'authToken', '139e3dfc0cd62c18c8d10e28ebf3e5bccb806c5f89c97560dc40ee7920bf3469', '[\"*\"]', NULL, NULL, '2025-07-03 13:36:37', '2025-07-03 13:36:37'),
(216, 'App\\Models\\StructAgent', '16', 'authToken', '723c0615a8186cb28d33bf6b3e251bc9702ddf3095e2962d6e992be2334fa5b5', '[\"*\"]', NULL, NULL, '2025-07-03 13:39:02', '2025-07-03 13:39:02'),
(217, 'App\\Models\\StructAgent', '25', 'authToken', '19b0c33720c54f08777a5c5c6dfbed2b88bc01e3a29ad6a9ba189b40dc45d3bc', '[\"*\"]', NULL, NULL, '2025-07-03 13:50:09', '2025-07-03 13:50:09'),
(218, 'App\\Models\\StructAgent', '3', 'authToken', 'c96679cb96118d7aecdad8f560df19620324c15327c71b2cc72279ea5aa23ffc', '[\"*\"]', NULL, NULL, '2025-07-04 07:07:47', '2025-07-04 07:07:47'),
(219, 'App\\Models\\StructAgent', '16', 'authToken', '04adb7f27428f3a99b2e26fb38dbf95b47baa0857cda0e74fd5c1624c24d1666', '[\"*\"]', NULL, NULL, '2025-07-04 07:08:15', '2025-07-04 07:08:15'),
(220, 'App\\Models\\StructAgent', '3', 'authToken', '8022ed49ed69e70a9ef1b8cd74e356fc8667e3518cfca9edc5ee2604a5595a18', '[\"*\"]', NULL, NULL, '2025-07-04 07:09:22', '2025-07-04 07:09:22'),
(221, 'App\\Models\\StructAgent', '26', 'authToken', '79ba312356025538b55571c2f69cbec602cd2e9e037d284fb988b076e1af7859', '[\"*\"]', NULL, NULL, '2025-07-04 07:19:39', '2025-07-04 07:19:39'),
(222, 'App\\Models\\StructAgent', '26', 'authToken', 'd257105002f2a40cfb3e83ba68899e27b47b1832d488921609d3235609aaca36', '[\"*\"]', NULL, NULL, '2025-07-04 07:30:02', '2025-07-04 07:30:02'),
(223, 'App\\Models\\StructAgent', '26', 'authToken', '240518470560e665b1709fd9d5c2a89ecf69e53a9cab521e914cd4d9793bf6b6', '[\"*\"]', NULL, NULL, '2025-07-04 07:38:45', '2025-07-04 07:38:45'),
(224, 'App\\Models\\StructAgent', '3', 'authToken', '5e90d375ab0ca9f5c0e2de20c736d6393494ce12f76ecb984171aeee8d31c377', '[\"*\"]', NULL, NULL, '2025-07-04 07:39:57', '2025-07-04 07:39:57'),
(225, 'App\\Models\\StructAgent', '28', 'authToken', '8ca1faa27a1b2c4a0bb089a24f447589dda7e263e4e7992f63ef2bd131b4e55e', '[\"*\"]', NULL, NULL, '2025-07-04 07:40:48', '2025-07-04 07:40:48'),
(226, 'App\\Models\\StructAgent', '3', 'authToken', '479dc86edc3ba503852def6e3b2d8225350fa5ae2d419ec3b040e0a0adf4276c', '[\"*\"]', NULL, NULL, '2025-07-04 07:46:35', '2025-07-04 07:46:35'),
(227, 'App\\Models\\StructAgent', '3', 'authToken', 'ab21a802793f489dbfc1f5dda50cbd892e8979281934d8ec769ff72ea513e2d7', '[\"*\"]', NULL, NULL, '2025-07-04 08:28:01', '2025-07-04 08:28:01'),
(228, 'App\\Models\\StructAgent', '3', 'authToken', '34e45662b7c13fecb681c1c9d2691cdcc5de46e73f12bae6a0ed966cbce95015', '[\"*\"]', NULL, NULL, '2025-07-04 08:29:44', '2025-07-04 08:29:44'),
(229, 'App\\Models\\StructAgent', '3', 'authToken', 'bf7a489300e9d8b97a032885e077b8c6abb3c8589a0fb616494576b3bb481df7', '[\"*\"]', NULL, NULL, '2025-07-04 08:35:28', '2025-07-04 08:35:28'),
(230, 'App\\Models\\StructAgent', '30', 'authToken', '48c92d2636f91465fb577f273baa9c5e8f8989e5866a30e8d39414f477d8203b', '[\"*\"]', NULL, NULL, '2025-07-04 08:41:06', '2025-07-04 08:41:06'),
(231, 'App\\Models\\StructAgent', '29', 'authToken', '9716308aad6f2a7bc85ecec0472c34ffe48d6b3cc0cb38eb76ee553e7c03df6c', '[\"*\"]', NULL, NULL, '2025-07-04 08:44:12', '2025-07-04 08:44:12'),
(232, 'App\\Models\\StructAgent', '30', 'authToken', '47e311e4b5f5680a55db945953bd51f35aea580593ac542834fa0e32e6dfc3c3', '[\"*\"]', NULL, NULL, '2025-07-04 08:51:50', '2025-07-04 08:51:50'),
(233, 'App\\Models\\StructAgent', '31', 'authToken', '9009b159376bd978b53a7874aa7ef93997e0f35a92d2a8204427e046779601d1', '[\"*\"]', NULL, NULL, '2025-07-04 08:52:02', '2025-07-04 08:52:02'),
(234, 'App\\Models\\StructAgent', '29', 'authToken', '5eac2f267e494fb92d715764333fbc42c97293ebbff76d0ba499a08c475405b9', '[\"*\"]', NULL, NULL, '2025-07-04 08:53:53', '2025-07-04 08:53:53'),
(235, 'App\\Models\\StructAgent', '32', 'authToken', 'a3fc2c9d8add88ea09b189a162dd51c25fa5e534223ef480f260da3a1c8fb609', '[\"*\"]', NULL, NULL, '2025-07-04 08:56:35', '2025-07-04 08:56:35'),
(236, 'App\\Models\\StructAgent', '33', 'authToken', 'ac1e18f215e456e0a87e8de27f5e3ae40dcd5943699afedc24b0bbb58a767ff8', '[\"*\"]', NULL, NULL, '2025-07-04 09:00:14', '2025-07-04 09:00:14'),
(237, 'App\\Models\\StructAgent', '33', 'authToken', '924c4d12b3c1a983d686ee2b982c83cb5c5e39657eab492b49109eac33c48968', '[\"*\"]', NULL, NULL, '2025-07-04 09:07:20', '2025-07-04 09:07:20'),
(238, 'App\\Models\\StructAgent', '35', 'authToken', '7834e9396086d7a11795200e5b384f153298ebfe487d8dadaacfdfba234c8489', '[\"*\"]', NULL, NULL, '2025-07-04 09:13:13', '2025-07-04 09:13:13'),
(239, 'App\\Models\\StructAgent', '36', 'authToken', 'e762f2ec9e917feb427adcf2c617f0f83d93138fe1f10565d18076999354d058', '[\"*\"]', NULL, NULL, '2025-07-04 09:17:06', '2025-07-04 09:17:06'),
(240, 'App\\Models\\StructAgent', '37', 'authToken', '90a14cfd1cb8fe921128cd7dee58f37015704f60705e8499a5457820c9fe9666', '[\"*\"]', NULL, NULL, '2025-07-04 09:22:40', '2025-07-04 09:22:40'),
(241, 'App\\Models\\StructAgent', '37', 'authToken', '5bc1da96a0e025a2353f087a43eb5267493b5b02c0783b0f7f857db9ba56fb2e', '[\"*\"]', NULL, NULL, '2025-07-04 09:29:45', '2025-07-04 09:29:45'),
(242, 'App\\Models\\StructAgent', '38', 'authToken', '0ee5d3871d51e6e2d8e7b9256161ffe83e2ede989f34f29d427333f6461fdfcc', '[\"*\"]', NULL, NULL, '2025-07-04 09:33:00', '2025-07-04 09:33:00'),
(243, 'App\\Models\\StructAgent', '37', 'authToken', '3c427d31845947ef44128c6136031e0d5121136ca7559c2ce16df8130ca33add', '[\"*\"]', NULL, NULL, '2025-07-04 09:37:23', '2025-07-04 09:37:23'),
(244, 'App\\Models\\StructAgent', '39', 'authToken', 'e4da32b82707d7dfce7caa35ddbaabe440acb3b12e642bb65efb875b405c44a3', '[\"*\"]', NULL, NULL, '2025-07-04 09:39:44', '2025-07-04 09:39:44'),
(245, 'App\\Models\\StructAgent', '3', 'authToken', 'c2b7afacc42dc546fbe205b603481d0b1b0982637442a07b1055017b6b7e9dec', '[\"*\"]', NULL, NULL, '2025-07-04 09:40:31', '2025-07-04 09:40:31'),
(246, 'App\\Models\\StructAgent', '3', 'authToken', '1c67d222c9542247de4de4f9f295bb0b59e85da49099c379c51614f851772e97', '[\"*\"]', NULL, NULL, '2025-07-04 09:40:50', '2025-07-04 09:40:50'),
(247, 'App\\Models\\StructAgent', '40', 'authToken', 'abbd7efa6ec81144e597772a06c85e6393a2ab2674cbe2b42e085e48b0d7dfdf', '[\"*\"]', NULL, NULL, '2025-07-04 09:44:17', '2025-07-04 09:44:17'),
(248, 'App\\Models\\StructAgent', '39', 'authToken', '65476a7a60ca66a5c10b210b97343313d35a9e642a2995c8865b04247a053d9d', '[\"*\"]', NULL, NULL, '2025-07-04 09:50:54', '2025-07-04 09:50:54'),
(249, 'App\\Models\\StructAgent', '41', 'authToken', 'a80963327961d73ed8bcb07ec7980495f8b8b152f778d367b6f4a07eab04a0ea', '[\"*\"]', NULL, NULL, '2025-07-04 09:53:47', '2025-07-04 09:53:47'),
(250, 'App\\Models\\StructAgent', '3', 'authToken', '6bf2a27c1fb5943a87d620c744851c32893e2eefdb88ad8cfb27f4ce79be19ac', '[\"*\"]', NULL, NULL, '2025-07-04 09:58:16', '2025-07-04 09:58:16'),
(251, 'App\\Models\\StructAgent', '42', 'authToken', 'd92f7800badf08cbcdc81d74276bfdf9400d32fb14bcdb6985428731573319d3', '[\"*\"]', NULL, NULL, '2025-07-04 10:03:07', '2025-07-04 10:03:07'),
(252, 'App\\Models\\StructAgent', '42', 'authToken', '09e79596a3a6292fa980e7807f3fe0c70441055a1ba8f20aae7926e1c7208c1d', '[\"*\"]', NULL, NULL, '2025-07-04 10:07:05', '2025-07-04 10:07:05'),
(253, 'App\\Models\\StructAgent', '43', 'authToken', '8314afec8d09f7560007bf575e326b129fcd93638d38455e4c955c6fa3f50b26', '[\"*\"]', NULL, NULL, '2025-07-04 10:11:26', '2025-07-04 10:11:26'),
(254, 'App\\Models\\StructAgent', '3', 'authToken', '99bd6022570c9cf5ef59d8545882b352283dea9f76c2d4fabd2aaec224b80e12', '[\"*\"]', NULL, NULL, '2025-07-04 10:13:33', '2025-07-04 10:13:33'),
(255, 'App\\Models\\StructAgent', '44', 'authToken', '43b1b330c25c541d5d1f66ce2fac8cc578197879fc4b98793a2436ba7043120f', '[\"*\"]', NULL, NULL, '2025-07-04 10:18:36', '2025-07-04 10:18:36'),
(256, 'App\\Models\\StructAgent', '45', 'authToken', '1c14033d0a74b4fb76ad7bee9ae1f9620d213559920daf80292ca30f3cbaeaab', '[\"*\"]', NULL, NULL, '2025-07-04 10:20:50', '2025-07-04 10:20:50'),
(257, 'App\\Models\\StructAgent', '46', 'authToken', '55a6e211427bf0e63f42b0be10a79944e69bbe4312b4d5b40f2ce1b517b15e71', '[\"*\"]', NULL, NULL, '2025-07-04 10:26:59', '2025-07-04 10:26:59'),
(258, 'App\\Models\\StructAgent', '48', 'authToken', 'a4d82ca1817f6fb7e4822b30e2413504fb1fc1d2be8a4bb8dfba0bea14bc4156', '[\"*\"]', NULL, NULL, '2025-07-04 10:33:05', '2025-07-04 10:33:05'),
(259, 'App\\Models\\StructAgent', '48', 'authToken', 'df93c5b805dd476de9f2771a18e448d8704b066b28b1bfc7efbe47af54e313b6', '[\"*\"]', NULL, NULL, '2025-07-04 10:34:53', '2025-07-04 10:34:53'),
(260, 'App\\Models\\StructAgent', '49', 'authToken', '269b3ebe4590b1361bfdbb53edfde7fab96db6d667a67da1e0b9016c65640bd1', '[\"*\"]', NULL, NULL, '2025-07-04 10:35:00', '2025-07-04 10:35:00'),
(261, 'App\\Models\\StructAgent', '48', 'authToken', '450d7c336ae49bd796fda260d67697a3a09262147d6864e3bedf051eabcb7650', '[\"*\"]', NULL, NULL, '2025-07-04 10:36:54', '2025-07-04 10:36:54'),
(262, 'App\\Models\\StructAgent', '49', 'authToken', '210a6555e3600156c71a1ae230fbb33679485a29e3466fe03300f3820e07eab9', '[\"*\"]', NULL, NULL, '2025-07-04 10:38:19', '2025-07-04 10:38:19');
INSERT INTO `personal_access_tokens` (`id`, `tokenable_type`, `tokenable_id`, `name`, `token`, `abilities`, `last_used_at`, `expires_at`, `created_at`, `updated_at`) VALUES
(263, 'App\\Models\\StructAgent', '44', 'authToken', '4de1e112582fe9cb86acd8129e709a3c8dbf03f6fc29edc12286d9b289e78357', '[\"*\"]', NULL, NULL, '2025-07-04 10:41:27', '2025-07-04 10:41:27'),
(264, 'App\\Models\\StructAgent', '49', 'authToken', '0ce92baa634f5ad1419500b611f9dfe1182d8a62dba6e90f8aa3540e9fd983b5', '[\"*\"]', NULL, NULL, '2025-07-04 10:41:30', '2025-07-04 10:41:30'),
(265, 'App\\Models\\User', '717a1c39-9676-46b4-af36-b3a5a69a5a02', 'auth_token', 'a30879049e1b8d486cad84b69838ec4353dc6784162e3fa5216e52a1a67427f1', '[\"*\"]', NULL, NULL, '2025-07-09 09:37:43', '2025-07-09 09:37:43'),
(266, 'App\\Models\\User', '717a1c39-9676-46b4-af36-b3a5a69a5a02', 'auth_token', '97112938dc6bb11dbe8406671fb2f87ea430886ef750147dd8e428f4d31bea92', '[\"*\"]', NULL, NULL, '2025-07-09 09:38:12', '2025-07-09 09:38:12'),
(267, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', 'f323d13069468e5b2d19778477fec636430ccffe9e2218383e26cd59245311dc', '[\"*\"]', NULL, NULL, '2025-07-09 09:45:32', '2025-07-09 09:45:32'),
(268, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', 'c93e7930239996da57903bdfe183f3896583ea60171e990826c0fc9a94ba8c97', '[\"*\"]', NULL, NULL, '2025-07-09 10:42:46', '2025-07-09 10:42:46'),
(269, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '6c800ce18d519efb9e03569495bb32e2b8aa4231418a179a10fb67ea4952642f', '[\"*\"]', NULL, NULL, '2025-07-09 12:37:42', '2025-07-09 12:37:42'),
(270, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '901d33211c781336c688391782edf3151a05af980b9873cb857ba3d67690d3d0', '[\"*\"]', NULL, NULL, '2025-07-09 12:44:18', '2025-07-09 12:44:18'),
(271, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '1344e12b3aaed6e4715b370458ab47c041675dc6fffc5971bbb9ebd559713783', '[\"*\"]', NULL, NULL, '2025-07-09 12:48:42', '2025-07-09 12:48:42'),
(272, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '1f025f3a751c027e92275548d84a505f2b3c8f82c8373ac4199781344eeb6474', '[\"*\"]', NULL, NULL, '2025-07-09 12:49:25', '2025-07-09 12:49:25'),
(273, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '6274b00e4bc41435d71f54767dcdea2e59c28c459ea10939f1da392eb33680f4', '[\"*\"]', NULL, NULL, '2025-07-09 12:49:44', '2025-07-09 12:49:44'),
(274, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '6703cb102db125d00e9e5e8613ca83d87fca98491546d7f91985dc2d59d51d04', '[\"*\"]', NULL, NULL, '2025-07-09 12:49:55', '2025-07-09 12:49:55'),
(275, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '5ee6657d48f830516b51facbe10b3ed84e466465dbd9c9eec6e963e988ed3e44', '[\"*\"]', NULL, NULL, '2025-07-09 12:53:08', '2025-07-09 12:53:08'),
(276, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '5dc4b0cd095636a4a1b20fcd21d763ee76f6486ed758d500597b35ac7c19973b', '[\"*\"]', NULL, NULL, '2025-07-09 13:20:22', '2025-07-09 13:20:22'),
(277, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '1e539416a926b6399d7c6baafa1374749ca6734f648fda7a1364ea02d5f6e9ab', '[\"*\"]', NULL, NULL, '2025-07-09 13:20:25', '2025-07-09 13:20:25'),
(278, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', 'bbdb51fc16648e0b2ba10310a0259e1277fe225c7acd40f2491965fe38ee9722', '[\"*\"]', NULL, NULL, '2025-07-09 13:20:57', '2025-07-09 13:20:57'),
(280, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '3bf0cebcbdcff9021bd9bf8ee9dc27b64b14235df1ad0bea4a199ee472a7064e', '[\"*\"]', NULL, NULL, '2025-07-10 13:27:51', '2025-07-10 13:27:51'),
(281, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '02069db09c44875a76c9c078266961e11705a9edc63926c9fa9b0f1312db872f', '[\"*\"]', NULL, NULL, '2025-07-10 13:28:06', '2025-07-10 13:28:06'),
(282, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', 'b2aaaf923ba7aee7271296076963c744734a343aeb18987e7658182fa464c1e4', '[\"*\"]', NULL, NULL, '2025-07-10 13:28:17', '2025-07-10 13:28:17'),
(283, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '3a7d9890f96237a71b48a90942ea66d76bc64c8cea20449c681ba0fda70c7ff6', '[\"*\"]', NULL, NULL, '2025-07-10 13:29:58', '2025-07-10 13:29:58'),
(284, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', 'fd2ebcf768cb1da3dea03592be05024621dc1d34244c7d1450420d2a857d94c3', '[\"*\"]', NULL, NULL, '2025-07-10 13:30:16', '2025-07-10 13:30:16'),
(285, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '9361b7cda35c32d89e39ff612a01de3ec8f292e6fddd964617fc123a6972103a', '[\"*\"]', NULL, NULL, '2025-07-10 14:55:32', '2025-07-10 14:55:32'),
(286, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '4a3eb634cf6b6305750140be68720d3d58be45aa92a1340091cfd0400d8375c6', '[\"*\"]', NULL, NULL, '2025-07-11 10:13:45', '2025-07-11 10:13:45'),
(287, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '56b733ff0ee9ed56edd40035556c20bd3a6e46a8cf2ebc19e93e1c769cde1083', '[\"*\"]', NULL, NULL, '2025-07-11 10:13:55', '2025-07-11 10:13:55'),
(288, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '48d6a37c03c8f4d17bd92a5638601c6d37ce1e2c94481da94372f47b7d7a3dcc', '[\"*\"]', NULL, NULL, '2025-07-11 10:14:02', '2025-07-11 10:14:02'),
(289, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '179a31d43bfa1957df0a81e827a54598a2861a9367ea674c6a7af4518b4f8556', '[\"*\"]', NULL, NULL, '2025-07-11 10:15:17', '2025-07-11 10:15:17'),
(290, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '6845c46f520c83f086971218a9b0453cefbd35fce3c1e9232a637cdac426e256', '[\"*\"]', NULL, NULL, '2025-07-11 10:30:42', '2025-07-11 10:30:42'),
(291, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', 'c8d66ecb33d3cca47f95336080cca68a03243cc1ae0f394df07586ba9de16f46', '[\"*\"]', NULL, NULL, '2025-07-11 10:38:05', '2025-07-11 10:38:05'),
(292, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', 'bf51d085ad7a8f06cb2e714faf89f276db855328e6154bbd7a2c858369186221', '[\"*\"]', NULL, NULL, '2025-07-11 10:38:22', '2025-07-11 10:38:22'),
(295, 'App\\Models\\StructAgent', '3', 'authToken', '2f6f0809ce8a3b7cc59658ba42d872376948ea00dc375bf29d8aeb1d42d23a6c', '[\"*\"]', NULL, NULL, '2025-07-12 08:31:37', '2025-07-12 08:31:37'),
(297, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', 'a5f84f2e2fad6e3cc2045e629bf35548b21d0d0afcb17a17521c9296fe8ab8eb', '[\"*\"]', NULL, NULL, '2025-07-12 12:41:01', '2025-07-12 12:41:01'),
(298, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '422ff64294a27a1e9781b08b5037327276f567dc8c60aa79eebbc017c20aee0e', '[\"*\"]', NULL, NULL, '2025-07-12 12:41:42', '2025-07-12 12:41:42'),
(299, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', 'ed14673b0f2a43c8ed2230d6417d1a5548431a152af26d7b624d8db24c4edc39', '[\"*\"]', '2025-07-14 07:18:56', NULL, '2025-07-12 12:46:12', '2025-07-14 07:18:56'),
(301, 'App\\Models\\StructAgent', '26', 'authToken', '04c728f6ebe8281c18055202a81d390dff419f104d6422c7830b00f0ca93fbba', '[\"*\"]', NULL, NULL, '2025-07-14 08:53:46', '2025-07-14 08:53:46'),
(302, 'App\\Models\\StructAgent', '26', 'authToken', '56df0aece8b51b92096fa33f1877feb98fcb32e1b6a709cdce714edb08f1ad09', '[\"*\"]', NULL, NULL, '2025-07-14 09:12:47', '2025-07-14 09:12:47'),
(303, 'App\\Models\\StructAgent', '26', 'authToken', '21c9fa1e9f4e55349a479c3b066782c74e58bb8cee3da6c22f22598c66c39b97', '[\"*\"]', NULL, NULL, '2025-07-14 09:27:51', '2025-07-14 09:27:51'),
(304, 'App\\Models\\StructAgent', '26', 'authToken', '0562d0e7468355cbd55ecec1d8296d2c981a13a5067ca0f2e9607ae73f1a7713', '[\"*\"]', NULL, NULL, '2025-07-14 09:48:19', '2025-07-14 09:48:19'),
(305, 'App\\Models\\StructAgent', '26', 'authToken', '795dffcc4d89f5e1d56d82b57ac2031f4828bb1db7362ca9c94804aaea03ed9f', '[\"*\"]', NULL, NULL, '2025-07-14 09:48:58', '2025-07-14 09:48:58'),
(306, 'App\\Models\\StructAgent', '3', 'authToken', '3a4370dde5219b28e7905b2ad9f7ee4d12e088a0e45b6f67e8b815f99b9d8606', '[\"*\"]', NULL, NULL, '2025-07-14 10:04:05', '2025-07-14 10:04:05'),
(307, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '0e5490b19aab34425cdd99b2a7e4151bcb85b9a126c6c8b652edf88e032f011f', '[\"*\"]', '2025-07-15 07:44:32', NULL, '2025-07-15 07:44:21', '2025-07-15 07:44:32'),
(310, 'App\\Models\\StructAgent', '3', 'authToken', '88f7e92ef191d0f3295b1be2b5d8eb6d59bc57ce07cd1fc609851fc0e7267298', '[\"*\"]', NULL, NULL, '2025-07-15 08:17:37', '2025-07-15 08:17:37'),
(311, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '5832f330c30db9722e59799a69248f8b9e6300b08fe0698f9fcb893f00f840af', '[\"*\"]', '2025-07-15 08:42:36', NULL, '2025-07-15 08:18:17', '2025-07-15 08:42:36'),
(312, 'App\\Models\\StructAgent', '26', 'authToken', '9a347feae3f1621d3a82ef8e2c288ad8a6d17389d0c40ef96e0aa341b552aaca', '[\"*\"]', NULL, NULL, '2025-07-15 08:42:54', '2025-07-15 08:42:54'),
(313, 'App\\Models\\StructAgent', '26', 'authToken', '79991294ba0e5cb44b2283a24fd5201cca4b507dd5da98065afaba190bb158c4', '[\"*\"]', NULL, NULL, '2025-07-15 11:42:29', '2025-07-15 11:42:29'),
(314, 'App\\Models\\StructAgent', '26', 'authToken', 'fc6ae0fbc2d5d815fd1ee289046a0ce2474ff6689a324596eb41a906edbce014', '[\"*\"]', NULL, NULL, '2025-07-15 11:43:19', '2025-07-15 11:43:19'),
(315, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', 'eb7622757895dfc5832bb658adc0e40a744674250f915d86edd8e98c3e35f661', '[\"*\"]', '2025-07-16 08:19:06', NULL, '2025-07-16 07:43:16', '2025-07-16 08:19:06'),
(319, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '85b99f5476cf173164d060bd6c3d8dd586e257f8f7d1ab76ad9288e403bbd0ee', '[\"*\"]', NULL, NULL, '2025-07-16 09:37:27', '2025-07-16 09:37:27'),
(321, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', 'ed39f9456834ebadb767c7355264e6a3a569e05f8fb9f18f5e38d2b954792e77', '[\"*\"]', '2025-07-16 15:19:41', NULL, '2025-07-16 12:06:11', '2025-07-16 15:19:41'),
(327, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '7c1f391930b0ef9a131450444a4cd6548279226df98b43c33026d5d30a85149d', '[\"*\"]', '2025-07-16 13:45:14', NULL, '2025-07-16 13:27:33', '2025-07-16 13:45:14'),
(328, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '2849219fb5aa0ebedf94ab49db6381fb80ee89b229e4b3e17e73772184783875', '[\"*\"]', '2025-07-16 14:09:33', NULL, '2025-07-16 13:54:22', '2025-07-16 14:09:33'),
(329, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '71ba52fce471d3fe7002e680bee41014944121f1fedc9a04932b8c90bf125125', '[\"*\"]', '2025-07-16 15:19:41', NULL, '2025-07-16 14:37:58', '2025-07-16 15:19:41'),
(330, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', 'e5f60e16ce6f26ec9f6aaecdc35bc52e3228061ce90c517e1f17b89443052953', '[\"*\"]', '2025-07-16 15:12:39', NULL, '2025-07-16 15:09:11', '2025-07-16 15:12:39'),
(331, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '7fb9eb75242e08c18f3f213112ed8e93aacc032a94fe02afccd25c7c6b23b050', '[\"*\"]', '2025-07-16 15:14:48', NULL, '2025-07-16 15:11:36', '2025-07-16 15:14:48'),
(333, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', 'e58b4077275d13f8755d5757b15f259661ab075f5ca9603a802b002f98f56b57', '[\"*\"]', '2025-07-17 10:42:12', NULL, '2025-07-17 10:42:10', '2025-07-17 10:42:12'),
(334, 'App\\Models\\StructAgent', '26', 'authToken', '6600a14ad3ac435cd626fab8a003cfda0da77a4b7daf986c30edcef0ded60e3a', '[\"*\"]', '2025-07-19 10:23:50', NULL, '2025-07-19 10:23:43', '2025-07-19 10:23:50'),
(335, 'App\\Models\\StructAgent', '26', 'authToken', 'c8df3e8414bc0527cdbde4f85e972e613415834b4a1ca4c777e6a3a8d0645959', '[\"*\"]', '2025-07-19 11:20:06', NULL, '2025-07-19 11:15:51', '2025-07-19 11:20:06'),
(336, 'App\\Models\\StructAgent', '26', 'authToken', '33488e2a373775862dc71d42956e01a6e5825fb58a9c8b8be7e83c9591b7eeb4', '[\"*\"]', '2025-07-19 11:35:40', NULL, '2025-07-19 11:20:54', '2025-07-19 11:35:40'),
(337, 'App\\Models\\StructAgent', '26', 'authToken', 'a5c94457f3834a0d6c17ab7f788b6dd41fa6b100a1830c80a73452c8daaaaf04', '[\"*\"]', '2025-07-19 11:45:42', NULL, '2025-07-19 11:35:49', '2025-07-19 11:45:42'),
(338, 'App\\Models\\StructAgent', '26', 'authToken', '51d1dbaf3157970f71142a2b7ee6a24730ab882a7047d1ca0390cbef0aeef3c4', '[\"*\"]', '2025-07-19 11:50:54', NULL, '2025-07-19 11:47:30', '2025-07-19 11:50:54'),
(339, 'App\\Models\\StructAgent', '26', 'authToken', '203e54a7748ae9c42fd060628aa7c60a00b1ad6b0e2ae6f798c984667b8ece05', '[\"*\"]', '2025-07-20 09:38:15', NULL, '2025-07-20 09:37:56', '2025-07-20 09:38:15'),
(340, 'App\\Models\\StructAgent', '26', 'authToken', '23a4d599e55b402a362b98580da371fd132de2f7f59e3e39dc2b5e7ae0318e1c', '[\"*\"]', '2025-07-20 09:47:56', NULL, '2025-07-20 09:38:26', '2025-07-20 09:47:56'),
(341, 'App\\Models\\StructAgent', '26', 'authToken', '2b3f416d011dc47acb59581143737365ab70b240284368e070c5a8140d742d4a', '[\"*\"]', '2025-07-20 10:41:19', NULL, '2025-07-20 09:50:06', '2025-07-20 10:41:19'),
(342, 'App\\Models\\StructAgent', '26', 'authToken', '939405159606b482947fe6cadf3d5db22313f0582f874460a73f8776494ade68', '[\"*\"]', '2025-07-20 11:05:52', NULL, '2025-07-20 10:41:39', '2025-07-20 11:05:52'),
(343, 'App\\Models\\StructAgent', '26', 'authToken', '8d67d1ab0adf583b2b007660453cad9334570e0a0e93f3f3bd3bdefff2ce0ac0', '[\"*\"]', '2025-07-20 11:06:10', NULL, '2025-07-20 11:06:02', '2025-07-20 11:06:10'),
(344, 'App\\Models\\StructAgent', '26', 'authToken', 'f98451b0facc24517aac02891bcc2c141d360715dc8a607da21e274d37a66e6f', '[\"*\"]', '2025-07-20 11:07:21', NULL, '2025-07-20 11:06:34', '2025-07-20 11:07:21'),
(345, 'App\\Models\\StructAgent', '26', 'authToken', 'eff1d9d082d6897e3fb213c67ffca55c96bee063d461aa3578d676786c7294fe', '[\"*\"]', '2025-07-20 11:09:19', NULL, '2025-07-20 11:07:36', '2025-07-20 11:09:19'),
(346, 'App\\Models\\StructAgent', '26', 'authToken', '0277b571020d61deddc33a6a8cc7e18286828a39e2a817e5d6127c1a1f635d53', '[\"*\"]', '2025-07-20 11:15:03', NULL, '2025-07-20 11:10:57', '2025-07-20 11:15:03'),
(347, 'App\\Models\\StructAgent', '26', 'authToken', 'f7a372f745765bf03bf3981fe99b02f99088ca16e2edc820ac7553ed87a5002e', '[\"*\"]', '2025-07-23 10:51:22', NULL, '2025-07-23 10:17:34', '2025-07-23 10:51:22'),
(348, 'App\\Models\\StructAgent', '26', 'authToken', 'dd3612e1338a703f4696badf74571bc65cb87e723bebf43e9107a58733a816e6', '[\"*\"]', '2025-07-23 11:04:06', NULL, '2025-07-23 10:52:04', '2025-07-23 11:04:06'),
(349, 'App\\Models\\StructAgent', '26', 'authToken', '0f8316d1ee45efefb2180b5c0aa967df79e5f661121ce2b82056fdedcf37a765', '[\"*\"]', '2025-07-23 11:06:36', NULL, '2025-07-23 11:04:32', '2025-07-23 11:06:36'),
(350, 'App\\Models\\StructAgent', '26', 'authToken', '23084ff3a78f41b291e11a6d65e54d81fec73da2eda9661a05f643091023263e', '[\"*\"]', '2025-07-23 11:10:37', NULL, '2025-07-23 11:06:45', '2025-07-23 11:10:37'),
(351, 'App\\Models\\StructAgent', '26', 'authToken', 'cf2a235ee5d25f7b5e160773207061622d9c7b99c68e0bc98fa1e0e5dd0ee5e8', '[\"*\"]', '2025-07-23 12:13:38', NULL, '2025-07-23 11:59:44', '2025-07-23 12:13:38'),
(352, 'App\\Models\\StructAgent', '26', 'authToken', '5e809b076e3bc2add48656c874ce294c58ffa32347dfb190c6915ddd0c0bd3dc', '[\"*\"]', '2025-07-23 12:21:14', NULL, '2025-07-23 12:20:27', '2025-07-23 12:21:14'),
(353, 'App\\Models\\StructAgent', '26', 'authToken', '00dbc7a2defa10879b8ca5ca2af4e3369da99aaab704e9b180c65a2b7dc94093', '[\"*\"]', '2025-07-23 12:23:06', NULL, '2025-07-23 12:21:35', '2025-07-23 12:23:06'),
(354, 'App\\Models\\StructAgent', '26', 'authToken', 'daafad64fb990b3472b50ccc60421f247599474734669c38d7f031f3f1a31064', '[\"*\"]', '2025-07-23 13:11:06', NULL, '2025-07-23 12:31:00', '2025-07-23 13:11:06'),
(355, 'App\\Models\\StructAgent', '26', 'authToken', '34f25267f3a3edf8c295e00ba0369b8ed7f5c0626848dfd9cb3eb8d304b6c9e3', '[\"*\"]', NULL, NULL, '2025-07-23 12:43:23', '2025-07-23 12:43:23'),
(356, 'App\\Models\\StructAgent', '26', 'authToken', '977dc61f7ab9b0645c0e4b7de9066e03b5e14c3cd7e718274bd9d21692220cbb', '[\"*\"]', NULL, NULL, '2025-07-23 13:05:28', '2025-07-23 13:05:28'),
(357, 'App\\Models\\StructAgent', '3', 'authToken', 'a7610172d5a495487a25e1fd98b9a2afd817e882ffafcf57c75d4c2430ed128f', '[\"*\"]', NULL, NULL, '2025-07-23 13:06:07', '2025-07-23 13:06:07'),
(358, 'App\\Models\\StructAgent', '26', 'authToken', '6c9455c292e248d840d42ae2b3b198b14ab1484e5ad4c7da6f3b8f784892ff6c', '[\"*\"]', NULL, NULL, '2025-07-23 13:06:42', '2025-07-23 13:06:42'),
(359, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', 'b0decc264f154139046b29ed0cc1197e56a9ae68c8ff52b4821e57361725b7ae', '[\"*\"]', '2025-07-23 13:18:29', NULL, '2025-07-23 13:18:28', '2025-07-23 13:18:29'),
(360, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '0a361ff3954f36bd106b98f2682e811a0e728aaca20c30a0cbe110d38b481e80', '[\"*\"]', '2025-07-23 13:31:41', NULL, '2025-07-23 13:18:53', '2025-07-23 13:31:41'),
(361, 'App\\Models\\StructAgent', '26', 'authToken', '84e2439134e8f771954f3266d1db97dc9f552de7910d24de4df85787c88f100d', '[\"*\"]', '2025-08-06 18:43:44', NULL, '2025-08-06 18:23:48', '2025-08-06 18:43:44'),
(362, 'App\\Models\\StructAgent', '26', 'authToken', 'c6fae0c0bafb2c8d972750f25cc3848090bb907178fccac9ddfe2de3452755aa', '[\"*\"]', '2025-08-09 08:28:16', NULL, '2025-08-09 07:44:09', '2025-08-09 08:28:16'),
(363, 'App\\Models\\StructAgent', '26', 'authToken', '5038f490a6d3dcfc63c03b174c324171c27edefad7b7be0655af7a09d870cad0', '[\"*\"]', '2025-08-09 08:43:55', NULL, '2025-08-09 08:43:39', '2025-08-09 08:43:55'),
(365, 'App\\Models\\User', '774e389c-925f-4c14-8966-835cb4180836', 'auth_token', 'cf5ea6e49c2b290233e7979dc608d8166ea1e318090edc34e6b0ff9a80fd05c7', '[\"*\"]', NULL, NULL, '2025-08-16 07:20:55', '2025-08-16 07:20:55'),
(366, 'App\\Models\\User', 'ccf841cf-2f87-4634-9d42-eaac993519c2', 'auth_token', '6f94f0f42f967503c35f511509b75aff693811d543675c72ad70d7b331ce42ef', '[\"*\"]', NULL, NULL, '2025-08-16 07:24:00', '2025-08-16 07:24:00'),
(371, 'App\\Models\\StructAgent', '26', 'authToken', '60c2bcca4e2f92551f66ee57f9d310d4901afc5eccd9809474aad1827a38b0f2', '[\"*\"]', '2025-08-18 15:17:04', NULL, '2025-08-18 15:16:54', '2025-08-18 15:17:04'),
(372, 'App\\Models\\StructAgent', '26', 'authToken', '29b3db262d306f5d6cc6877a818f8231d39ae9bc42874a0e1391bf8103f9517c', '[\"*\"]', '2025-08-18 15:21:44', NULL, '2025-08-18 15:21:41', '2025-08-18 15:21:44'),
(373, 'App\\Models\\StructAgent', '26', 'authToken', '1fe19a579ec16160be6d3dd1547aff0979d3ecd9dc292ecb868e3e1b01c73fdf', '[\"*\"]', '2025-08-18 15:29:29', NULL, '2025-08-18 15:25:12', '2025-08-18 15:29:29'),
(374, 'App\\Models\\StructAgent', '26', 'authToken', '1b97d38ab077c2255e1816b04731d6951255ef7d5fa7a4dde3670ee897531bf3', '[\"*\"]', '2025-08-18 15:29:41', NULL, '2025-08-18 15:29:33', '2025-08-18 15:29:41'),
(376, 'App\\Models\\StructAgent', '26', 'authToken', '0fd93e17b5d4ed4771783bbef5c26f4de7d60b137ee7dd40c62639b60e1b349c', '[\"*\"]', '2025-08-22 10:15:53', NULL, '2025-08-22 10:15:11', '2025-08-22 10:15:53'),
(377, 'App\\Models\\StructAgent', '26', 'authToken', '96ae5a909704906b6d9b4a1bc0a4e8a7e1dee71070eeb211be654d36e0981a38', '[\"*\"]', '2025-08-22 10:26:03', NULL, '2025-08-22 10:25:32', '2025-08-22 10:26:03'),
(379, 'App\\Models\\StructAgent', '26', 'authToken', 'ce396fa11bf309511a70f7c2393c83d565a0685400d4351070f44cec0914af1f', '[\"*\"]', '2025-08-22 10:30:53', NULL, '2025-08-22 10:26:35', '2025-08-22 10:30:53'),
(380, 'App\\Models\\StructAgent', '26', 'authToken', '90d6cf291c15359b1d376839a169025a8384480a00713bafbabed5235732a52f', '[\"*\"]', '2025-08-22 12:33:21', NULL, '2025-08-22 10:33:52', '2025-08-22 12:33:21'),
(384, 'App\\Models\\StructAgent', '51', 'authToken', '254649169a7345df096eb5650fba4a47b67044d5e6a1bde604a1a77bc126a72a', '[\"*\"]', '2025-08-22 13:07:16', NULL, '2025-08-22 13:04:38', '2025-08-22 13:07:16'),
(386, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', '1468654e9f2867321eccc0082c1fe3de314637a69e7408b98d74251c1e8433a8', '[\"*\"]', '2025-09-22 07:51:26', NULL, '2025-09-22 07:46:27', '2025-09-22 07:51:26'),
(387, 'App\\Models\\StructAgent', '51', 'authToken', '0aa330bc1ab9915f75e8092ed912841b6eb203bf38205f544ea2a9d919887463', '[\"*\"]', '2025-10-03 13:11:31', NULL, '2025-10-03 13:09:47', '2025-10-03 13:11:31'),
(388, 'App\\Models\\StructAgent', '51', 'authToken', '25865e08e52fea4be7be09064cfd2ad941b9e0dfe4d128cccccedc9ac72ab74e', '[\"*\"]', '2025-10-06 10:32:14', NULL, '2025-10-06 10:26:03', '2025-10-06 10:32:14'),
(392, 'App\\Models\\User', 'dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'auth_token', 'c21ed959a1ab131cf589e00dfb6625151d979f1bef48bef3106b5088bc1a4e48', '[\"*\"]', '2025-10-13 10:19:55', NULL, '2025-10-11 09:13:44', '2025-10-13 10:19:55'),
(393, 'App\\Models\\StructAgent', '51', 'authToken', 'c82ffcf832c265f254b565d3bfb8d0480f356ec36f7d2e9526bb6161d4dfcdcb', '[\"*\"]', '2025-10-13 11:30:26', NULL, '2025-10-11 10:39:33', '2025-10-13 11:30:26'),
(394, 'App\\Models\\StructAgent', '51', 'authToken', 'c72ee820fc1ca1409fd5f0097bf8d2f0dc3f35729401cc6d8eb3be76bbd467a1', '[\"*\"]', '2025-10-14 08:28:55', NULL, '2025-10-14 08:27:41', '2025-10-14 08:28:55'),
(395, 'App\\Models\\StructAgent', '51', 'authToken', 'f885b3e136c6d78f152d8085db44edda865ccfc32f58473b5e4bd1198f0f5dc0', '[\"*\"]', '2025-12-07 08:41:58', NULL, '2025-12-02 10:53:45', '2025-12-07 08:41:58'),
(396, 'App\\Models\\StructAgent', '51', 'authToken', '8fd94d4be539898ab78e548767432ffb1672fc7a18ed5e69ab4b0cbf15ae280b', '[\"*\"]', '2025-12-20 08:52:19', NULL, '2025-12-20 08:24:34', '2025-12-20 08:52:19'),
(397, 'App\\Models\\StructAgent', '51', 'authToken', '7acea9bcf99d5f4879c0fdaf28ed7b38d6e65079fb29fe78a7fb987777ae2981', '[\"*\"]', '2026-03-04 15:58:04', NULL, '2026-03-04 13:24:57', '2026-03-04 15:58:04'),
(402, 'App\\Models\\StructAgent', '3', 'authToken', '316ca3fedbec1d3274f7fdc970e20919e0b0251a416ce8023bdabb740cb2dc00', '[\"*\"]', '2026-03-16 19:18:15', NULL, '2026-03-16 19:18:06', '2026-03-16 19:18:15'),
(411, 'App\\Models\\StructAgent', '55', 'authToken', '2610992d527e7255a7973399f999d1cdbeb201faf4dbdeefc826d42344e287cf', '[\"*\"]', '2026-03-18 02:50:30', NULL, '2026-03-18 02:50:22', '2026-03-18 02:50:30'),
(415, 'App\\Models\\StructAgent', '55', 'authToken', 'fe6c24c63ae898433f67be96ea908656a1b65ba8911cc12ce0df0e8d2bbca266', '[\"*\"]', '2026-03-18 15:25:52', NULL, '2026-03-18 14:49:27', '2026-03-18 15:25:52'),
(416, 'App\\Models\\StructAgent', '55', 'authToken', '3ee76c1e44d7b6287cae5a7a9550193db2674e6fc0cc9e78e4092a4945f70e67', '[\"*\"]', '2026-03-22 00:41:50', NULL, '2026-03-21 09:56:44', '2026-03-22 00:41:50'),
(419, 'App\\Models\\StructAgent', '56', 'authToken', '82999ac2298af66df65cb0df993e0fbf2e1de9ba76701383edb7597da2051564', '[\"*\"]', '2026-03-22 12:43:14', NULL, '2026-03-22 12:43:08', '2026-03-22 12:43:14'),
(420, 'App\\Models\\StructAgent', '56', 'authToken', 'c86f38fe583564b7d026e8adbc62e873285878beb987a18dc9882feca33f4dd5', '[\"*\"]', '2026-03-22 13:47:45', NULL, '2026-03-22 13:27:08', '2026-03-22 13:47:45'),
(421, 'App\\Models\\StructAgent', '56', 'authToken', 'ddfb44c1edf63985a35433a3dcd03cd6189107eaaae1db79c25d7d003a1ae717', '[\"*\"]', '2026-03-22 13:49:10', NULL, '2026-03-22 13:49:08', '2026-03-22 13:49:10'),
(422, 'App\\Models\\StructAgent', '56', 'authToken', 'b1c4d57354e1f09678038cb72c5a0b82297154e7f3b46c4c0e6a32bec4f44c03', '[\"*\"]', '2026-03-22 14:04:51', NULL, '2026-03-22 13:50:54', '2026-03-22 14:04:51'),
(424, 'App\\Models\\StructAgent', '56', 'authToken', 'c43cf367285d1726b7af9046bb4d430bbe0e036b1fde6418d0f34e5afe4b6c54', '[\"*\"]', NULL, NULL, '2026-03-22 19:09:19', '2026-03-22 19:09:19'),
(425, 'App\\Models\\StructAgent', '56', 'authToken', '34827d1e7c6ffcf50be9bcd5fae2910aa21bfe416263d06ab1f302e0652c22a8', '[\"*\"]', NULL, NULL, '2026-03-22 19:09:44', '2026-03-22 19:09:44'),
(426, 'App\\Models\\StructAgent', '56', 'authToken', 'd82721a5dba2087df2cd0adfedcf80bd2295a05867bf4958ff4b1a6601b35176', '[\"*\"]', NULL, NULL, '2026-03-22 19:22:35', '2026-03-22 19:22:35'),
(427, 'App\\Models\\StructAgent', '56', 'authToken', 'bed700594fdf8bcd26bedf355f5115a4d0b3aad884782b36b2499509a53bcac7', '[\"*\"]', NULL, NULL, '2026-03-22 19:22:37', '2026-03-22 19:22:37'),
(431, 'App\\Models\\StructAgent', '56', 'authToken', 'a3103f17c5e8f8d12cd70df7c6bf320e898e21b35ce4b5bc63a1dd758c8c747e', '[\"*\"]', '2026-03-24 07:34:22', NULL, '2026-03-24 07:34:09', '2026-03-24 07:34:22'),
(432, 'App\\Models\\StructAgent', '56', 'authToken', '6287785c314213973cb437d46321bea86235f22d7ca5440a2c58ce1ca2250019', '[\"*\"]', '2026-03-24 08:13:52', NULL, '2026-03-24 08:13:40', '2026-03-24 08:13:52'),
(433, 'App\\Models\\StructAgent', '56', 'authToken', '07626a4d52099a701566c21e0420e0ef39837d6b2429fc9bed7e1a135f82c61c', '[\"*\"]', NULL, NULL, '2026-03-24 08:26:28', '2026-03-24 08:26:28'),
(434, 'App\\Models\\StructAgent', '56', 'authToken', 'b4f406d8838bf28672593b3560c5c943856062ed8bfcd231abbe55ae1b229080', '[\"*\"]', NULL, NULL, '2026-03-24 08:27:16', '2026-03-24 08:27:16'),
(435, 'App\\Models\\StructAgent', '56', 'authToken', 'd9a6b4cb51100266cfe946d77e899e49c1b46fdd06499d3440289189fb263fa6', '[\"*\"]', '2026-03-24 09:08:50', NULL, '2026-03-24 08:29:16', '2026-03-24 09:08:50'),
(436, 'App\\Models\\StructAgent', '56', 'authToken', '6984028c5aac00a3be60bdf339a42a634b46da44450d241ec7bf8f0fd151307b', '[\"*\"]', '2026-03-24 09:42:57', NULL, '2026-03-24 09:42:41', '2026-03-24 09:42:57'),
(437, 'App\\Models\\StructAgent', '56', 'authToken', '3b9f2e835c4eaf8bae679a97647a7a4658af3da1c9caae2301fcb7dc23a1966a', '[\"*\"]', NULL, NULL, '2026-03-24 10:01:35', '2026-03-24 10:01:35'),
(438, 'App\\Models\\StructAgent', '56', 'authToken', 'a6ce5b9f05e934bce320650dc30457d5337ac4a01da4db45d73f06fb3cd98613', '[\"*\"]', NULL, NULL, '2026-03-24 10:11:20', '2026-03-24 10:11:20'),
(439, 'App\\Models\\StructAgent', '56', 'authToken', '043691f2a576ec167293bc1218d43f9d476181126d558defe59c1379629f7998', '[\"*\"]', NULL, NULL, '2026-03-24 10:22:07', '2026-03-24 10:22:07'),
(440, 'App\\Models\\StructAgent', '56', 'authToken', '705d50b877670bc7b3040150e90adfcf1c00c0ab59a00dea3353553d022269f5', '[\"*\"]', NULL, NULL, '2026-03-24 10:22:33', '2026-03-24 10:22:33'),
(441, 'App\\Models\\StructAgent', '56', 'authToken', '4b1862323346fcf8a3bcc7443bba9de4182941ef131c65e91f3ea694bbebc8f3', '[\"*\"]', NULL, NULL, '2026-03-24 10:26:53', '2026-03-24 10:26:53'),
(442, 'App\\Models\\StructAgent', '51', 'authToken', '7fa56e52373200db21e5bb9a99d21a4980c94a1c8ee3cf24d6421a5d1d51a24a', '[\"*\"]', NULL, NULL, '2026-03-24 10:32:03', '2026-03-24 10:32:03'),
(443, 'App\\Models\\StructAgent', '56', 'authToken', '48176b426fc32cbb8ce802ad038b76367bb177043d622a5eacd58c75f7638310', '[\"*\"]', NULL, NULL, '2026-03-24 10:43:45', '2026-03-24 10:43:45'),
(444, 'App\\Models\\StructAgent', '56', 'authToken', '6893e58ff2a3c0b988305f7c31a5be86da6baf231397f4643880aab054d04353', '[\"*\"]', NULL, NULL, '2026-03-24 10:51:08', '2026-03-24 10:51:08'),
(445, 'App\\Models\\StructAgent', '56', 'authToken', 'e540954d6b82965201b5a4a8cbaa23a5369606ce16523a7c8aa8c96010d41643', '[\"*\"]', NULL, NULL, '2026-03-24 10:56:36', '2026-03-24 10:56:36'),
(446, 'App\\Models\\StructAgent', '56', 'authToken', '804e77710ac1617c337c48072bba88ac5f854d0dce297ce8c77ede8046030171', '[\"*\"]', NULL, NULL, '2026-03-24 11:02:54', '2026-03-24 11:02:54'),
(447, 'App\\Models\\StructAgent', '56', 'authToken', '2cac6399680db0201e26558da2dd4cfb089eee486fda38819fdea148641c0f2f', '[\"*\"]', NULL, NULL, '2026-03-24 11:09:20', '2026-03-24 11:09:20'),
(448, 'App\\Models\\StructAgent', '56', 'authToken', '4557516c8e3670fd32488c1a18127a58c3edd8078bf3fc93990b3d74b6a9e9b9', '[\"*\"]', NULL, NULL, '2026-03-24 11:11:49', '2026-03-24 11:11:49'),
(449, 'App\\Models\\StructAgent', '56', 'authToken', '2c55a4f8819c4a49f0fe4385d73d697d6befe13ea73496719373b593c7a886b9', '[\"*\"]', NULL, NULL, '2026-03-24 11:50:54', '2026-03-24 11:50:54'),
(450, 'App\\Models\\StructAgent', '55', 'authToken', '05be499294cfb94768de6ff8fc861c41ed5157c87686b36ef080d1a617bbd278', '[\"*\"]', NULL, NULL, '2026-03-24 12:29:05', '2026-03-24 12:29:05'),
(451, 'App\\Models\\StructAgent', '56', 'authToken', 'e49ef700f6dcffcc250f8ccfc05a1a0538c226a6f8786e5bbeebf5db4c3a8bc3', '[\"*\"]', NULL, NULL, '2026-03-24 12:34:59', '2026-03-24 12:34:59'),
(452, 'App\\Models\\StructAgent', '56', 'authToken', '5725d048cb19e4ede20a021f8f73bf3ef9633a4233c2c74688144914c8c7d9ec', '[\"*\"]', NULL, NULL, '2026-03-24 12:36:17', '2026-03-24 12:36:17'),
(453, 'App\\Models\\StructAgent', '3', 'authToken', '47b31977444a94455ffd40a28819800da3df0b6d5135a79e9a5030c3ceb577b1', '[\"*\"]', NULL, NULL, '2026-03-24 12:38:52', '2026-03-24 12:38:52'),
(454, 'App\\Models\\StructAgent', '57', 'authToken', '5337f44b831084e5b16d9db2ab0e2f7b0f8e4e43b1f0d97d86f8257b650cc774', '[\"*\"]', NULL, NULL, '2026-03-24 12:40:48', '2026-03-24 12:40:48'),
(455, 'App\\Models\\StructAgent', '3', 'authToken', '952436ea2c9968ac5b0a39e680720cdaf2556dfd7315d53c2ccf3bb0ca0cc15f', '[\"*\"]', NULL, NULL, '2026-03-24 13:06:44', '2026-03-24 13:06:44'),
(456, 'App\\Models\\StructAgent', '58', 'authToken', '7718674b595c19818845ef0f5e7b3f13ef41571c7e1d3fcc1ef7b5e497aa6d38', '[\"*\"]', NULL, NULL, '2026-03-24 13:09:05', '2026-03-24 13:09:05'),
(457, 'App\\Models\\StructAgent', '3', 'authToken', 'c87f414f410644ea158b82107a338ee96a362935f00bbf95e65f5ced1157afe4', '[\"*\"]', NULL, NULL, '2026-03-24 13:19:40', '2026-03-24 13:19:40'),
(458, 'App\\Models\\StructAgent', '59', 'authToken', '4083a0c0c59c31edfbbee82742ffa263fd43be6a2f975dd54a0fe2162a4b30d2', '[\"*\"]', NULL, NULL, '2026-03-24 13:21:25', '2026-03-24 13:21:25'),
(459, 'App\\Models\\StructAgent', '59', 'authToken', 'f46cce7576bbc4d38ec4814a925aa2011ce881827757b57daa7139f052b2571d', '[\"*\"]', NULL, NULL, '2026-03-24 13:49:21', '2026-03-24 13:49:21'),
(460, 'App\\Models\\StructAgent', '59', 'authToken', 'a2ff26755deb8cda19b4f5d615a00dda33ab7c357bd807c50758f1d412a3e5e1', '[\"*\"]', NULL, NULL, '2026-03-24 14:17:08', '2026-03-24 14:17:08'),
(461, 'App\\Models\\StructAgent', '59', 'authToken', '4df56d76324cb474ec123940849295b13d0eeae01bac9d885a6f34c3682c660e', '[\"*\"]', NULL, NULL, '2026-03-25 07:31:38', '2026-03-25 07:31:38'),
(462, 'App\\Models\\StructAgent', '61', 'authToken', '3bd0a5119be008e571e130db36f345d7ddeafad19e57f8221cf67b876c3b5a7f', '[\"*\"]', NULL, NULL, '2026-03-25 07:41:01', '2026-03-25 07:41:01'),
(463, 'App\\Models\\StructAgent', '61', 'authToken', 'd151f966c6a2bcb756cc037f795b1b1fd774fb7959b46003e953be0326c2a98f', '[\"*\"]', NULL, NULL, '2026-03-25 07:41:09', '2026-03-25 07:41:09'),
(464, 'App\\Models\\StructAgent', '61', 'authToken', '753d48838b0672de3482e1b3ba8cf8265bbe20ce873c93a13c2483dacbe6fb72', '[\"*\"]', NULL, NULL, '2026-03-25 07:58:43', '2026-03-25 07:58:43'),
(465, 'App\\Models\\StructAgent', '57', 'authToken', '6fa294296c0023e153d24b13bd36f1dd785dcdd0df06095bfb81d7e828cbdc9d', '[\"*\"]', NULL, NULL, '2026-03-25 08:03:51', '2026-03-25 08:03:51'),
(466, 'App\\Models\\StructAgent', '59', 'authToken', '636d6b204b9f9f7597f7bd286c40be28739b6149d3aa9afa5e465947d9fd7b76', '[\"*\"]', NULL, NULL, '2026-03-25 08:07:59', '2026-03-25 08:07:59'),
(467, 'App\\Models\\StructAgent', '59', 'authToken', '0bb659daed91517ae0a43f57da7e8923fce465fd1c4370f0ba1d166b02619c2c', '[\"*\"]', NULL, NULL, '2026-03-25 08:13:17', '2026-03-25 08:13:17'),
(468, 'App\\Models\\StructAgent', '57', 'authToken', '04a95cf7b1da7102ee2b66f0f538cfa2eeedddd275899504a981f037a553dc22', '[\"*\"]', NULL, NULL, '2026-03-25 08:14:35', '2026-03-25 08:14:35'),
(469, 'App\\Models\\StructAgent', '3', 'authToken', '68d301e1492eff19fb5140d9b65b08701164f1e470d7d4210237eebe47e4d7d4', '[\"*\"]', NULL, NULL, '2026-03-25 08:23:05', '2026-03-25 08:23:05'),
(470, 'App\\Models\\StructAgent', '3', 'authToken', 'f8ee116065b9f7f4a9e27f6a7e5215d9635cd847f2df9ff0d76ac3243f844f58', '[\"*\"]', NULL, NULL, '2026-03-25 08:25:20', '2026-03-25 08:25:20'),
(471, 'App\\Models\\StructAgent', '62', 'authToken', 'ba6db90f2999a157f185ffd91efb40716a51bf6aeb443724c0683249430f849c', '[\"*\"]', NULL, NULL, '2026-03-25 08:29:34', '2026-03-25 08:29:34'),
(472, 'App\\Models\\StructAgent', '3', 'authToken', '8ab464126c165bb4185fc2cb3197a5d15e6f73d59da5c341b4118642cd1e399c', '[\"*\"]', NULL, NULL, '2026-03-25 09:12:44', '2026-03-25 09:12:44'),
(473, 'App\\Models\\StructAgent', '59', 'authToken', 'da30c8a108a46db3985ea7ebb4688d8918665f80ab5a142eb91a7004a18e15e4', '[\"*\"]', NULL, NULL, '2026-03-26 20:51:39', '2026-03-26 20:51:39'),
(474, 'App\\Models\\StructAgent', '59', 'authToken', '2c70ff5ef6c465e4301b952ae5b76b7e79f9b6e809a59c83817a9db9d47e6c06', '[\"*\"]', NULL, NULL, '2026-03-28 08:41:34', '2026-03-28 08:41:34'),
(475, 'App\\Models\\StructAgent', '62', 'authToken', 'd88ac833e5257789c5466175b9e4b50aee8f3d18ea96ef9962b9d6c5d30c64f7', '[\"*\"]', NULL, NULL, '2026-03-28 10:05:46', '2026-03-28 10:05:46'),
(476, 'App\\Models\\StructAgent', '62', 'authToken', 'b869f4e972f39c6ae5e3cef89100814bf5eb9c32ab56e92facf420d8650c41fa', '[\"*\"]', NULL, NULL, '2026-03-28 15:01:39', '2026-03-28 15:01:39'),
(477, 'App\\Models\\StructAgent', '59', 'authToken', '5234cc21286078d6cd6a53cdeeeec1c91992aab8b1a27c432fca32d495cc19e5', '[\"*\"]', NULL, NULL, '2026-03-29 07:20:00', '2026-03-29 07:20:00'),
(478, 'App\\Models\\StructAgent', '59', 'authToken', 'e159f7a1d4e37bd783c59ba7f8911a6641f67313c83aa138a1bb9ee983705c7d', '[\"*\"]', NULL, NULL, '2026-03-29 07:21:32', '2026-03-29 07:21:32'),
(479, 'App\\Models\\StructAgent', '59', 'authToken', '9ea8e6abdc5eaeb4711c037ea22f0876895b89385c02a0bf86bbc1be903f3701', '[\"*\"]', NULL, NULL, '2026-03-29 09:23:34', '2026-03-29 09:23:34'),
(480, 'App\\Models\\StructAgent', '59', 'authToken', 'a52bf03e6b9d144ab63958985193601c1f5829178fc00a76d5598bbaa312fb3a', '[\"*\"]', NULL, NULL, '2026-03-29 09:29:30', '2026-03-29 09:29:30'),
(481, 'App\\Models\\StructAgent', '62', 'authToken', '626b5fe8896b869cd482ab999ad7da8d356b9c4b7a0e8271d680cd5ebf8f3a67', '[\"*\"]', NULL, NULL, '2026-03-29 10:32:40', '2026-03-29 10:32:40'),
(482, 'App\\Models\\StructAgent', '62', 'authToken', '6bee45cb0d69b8ed811abd0c93a19f6ed86a46c6e6e6175777c608cede83eea3', '[\"*\"]', NULL, NULL, '2026-03-29 20:06:10', '2026-03-29 20:06:10'),
(483, 'App\\Models\\StructAgent', '62', 'authToken', 'c9869e2580abc49b9f1abf915a56feca53f2717f02982cbca12e4de90411d17e', '[\"*\"]', NULL, NULL, '2026-03-30 06:33:59', '2026-03-30 06:33:59'),
(484, 'App\\Models\\StructAgent', '62', 'authToken', 'de1d6d1117a6d8261a158f19d4c512467a6ff599aadd3ef9a72e4fec5382d4ac', '[\"*\"]', NULL, NULL, '2026-03-30 09:51:43', '2026-03-30 09:51:43'),
(485, 'App\\Models\\StructAgent', '62', 'authToken', 'e9d07b41ac1467ae5549749dc8395e488c27d39b6ad881fb47301d7612382b55', '[\"*\"]', NULL, NULL, '2026-03-31 05:59:36', '2026-03-31 05:59:36'),
(486, 'App\\Models\\StructAgent', '57', 'authToken', 'dc313f4a846eca06501ed148894190ad62f889bc46a2299fee5fed11cb468aff', '[\"*\"]', NULL, NULL, '2026-03-31 08:30:12', '2026-03-31 08:30:12'),
(487, 'App\\Models\\StructAgent', '59', 'authToken', '42ab126e8177a72fb1242c1a3275dd6aa5f31035ef4cef3b766d1d449e91ffdc', '[\"*\"]', NULL, NULL, '2026-03-31 08:30:43', '2026-03-31 08:30:43'),
(488, 'App\\Models\\StructAgent', '3', 'authToken', '550768c7cbdf5991055efd1cf970e823cf70b06f0c999c97e44be0415558de3b', '[\"*\"]', NULL, NULL, '2026-03-31 08:51:52', '2026-03-31 08:51:52'),
(489, 'App\\Models\\StructAgent', '3', 'authToken', 'ddeb9e97035af50b5e767e13dac36647aec0c0a7178e3dc0a934f2f6e8bf5596', '[\"*\"]', NULL, NULL, '2026-03-31 13:42:49', '2026-03-31 13:42:49'),
(490, 'App\\Models\\StructAgent', '63', 'authToken', '3525befe7901abbdd51c3d75d9612b452178391e8dfbafbd026a6fd3659ff77e', '[\"*\"]', NULL, NULL, '2026-03-31 13:45:27', '2026-03-31 13:45:27'),
(491, 'App\\Models\\StructAgent', '3', 'authToken', '76d5c5f1176d97e4f5154a023b4907d52fa8d5812a9920d91a9fbfe46606819c', '[\"*\"]', NULL, NULL, '2026-03-31 13:53:40', '2026-03-31 13:53:40'),
(492, 'App\\Models\\StructAgent', '62', 'authToken', '1e811952004bc7308ed032ac95b22e4c63265a6960e0582c90638374f73f3fc3', '[\"*\"]', NULL, NULL, '2026-03-31 13:57:06', '2026-03-31 13:57:06'),
(493, 'App\\Models\\StructAgent', '3', 'authToken', '31ea2c8f54af4844272e02ceff4eb1ab9db042aba58c715def56f73c1d3324ea', '[\"*\"]', NULL, NULL, '2026-03-31 14:35:52', '2026-03-31 14:35:52'),
(494, 'App\\Models\\StructAgent', '57', 'authToken', '027bfb17018d722e6b1efa503ce50630b8fe6cd600797a9d40dd7d0db32de201', '[\"*\"]', NULL, NULL, '2026-03-31 14:36:15', '2026-03-31 14:36:15'),
(495, 'App\\Models\\StructAgent', '62', 'authToken', '4f5d5bbe8c9b2f46ca1230994c6b78669e85899f25a9500ffb23551c19890d0d', '[\"*\"]', NULL, NULL, '2026-03-31 19:12:24', '2026-03-31 19:12:24'),
(496, 'App\\Models\\StructAgent', '62', 'authToken', '5b8749ac99303d3d9d085d9cb418fa0f9d9d403f1417ac4a7fc2c1736b41ae0b', '[\"*\"]', NULL, NULL, '2026-04-01 06:01:07', '2026-04-01 06:01:07'),
(497, 'App\\Models\\StructAgent', '62', 'authToken', 'b0c239935fbde15487f34b1a922ebb911589acd047529f5529fd8caabc77dc51', '[\"*\"]', NULL, NULL, '2026-04-01 06:27:48', '2026-04-01 06:27:48'),
(498, 'App\\Models\\StructAgent', '57', 'authToken', '6f952beca0869b527d17ee7a909a83e355247625a82c736b13c8dbac211bad96', '[\"*\"]', NULL, NULL, '2026-04-01 06:36:23', '2026-04-01 06:36:23'),
(499, 'App\\Models\\StructAgent', '62', 'authToken', 'a7c9d4fc8a4756d99a92152911b9d44701878d04d3337cdaf71b7033e647d1f9', '[\"*\"]', NULL, NULL, '2026-04-01 07:26:11', '2026-04-01 07:26:11'),
(500, 'App\\Models\\StructAgent', '62', 'authToken', 'd1015a41582a9165e5407f3cd4e1db6e285a569b3518d6e2e8d8bfa67603f70e', '[\"*\"]', NULL, NULL, '2026-04-01 09:55:50', '2026-04-01 09:55:50'),
(501, 'App\\Models\\StructAgent', '62', 'authToken', 'ada3667731665861244d34030fbc808aebbc22dd43b80ad12bd5e94991b47395', '[\"*\"]', NULL, NULL, '2026-04-01 10:31:24', '2026-04-01 10:31:24'),
(502, 'App\\Models\\StructAgent', '62', 'authToken', '7e778dd0ef286fe84744d1c62f4aff1afb55dda5b6d68093ccff1a564e9befef', '[\"*\"]', NULL, NULL, '2026-04-02 14:46:56', '2026-04-02 14:46:56'),
(503, 'App\\Models\\StructAgent', '62', 'authToken', '9d6483e070503ef3e53f32bf6e152d150408a4b82739aac0d6ad37935af956f8', '[\"*\"]', NULL, NULL, '2026-04-04 09:11:55', '2026-04-04 09:11:55'),
(504, 'App\\Models\\StructAgent', '62', 'authToken', 'fc6f4b23141e28bb4583ae3b6f2f757dad9dd35210e1b49f75db1ab7a783159a', '[\"*\"]', NULL, NULL, '2026-04-04 11:51:40', '2026-04-04 11:51:40'),
(505, 'App\\Models\\StructAgent', '62', 'authToken', '83aeefd5ede9e544158fc81a386462cc6e1ba951df88a2b5fff0a4d6ea9c69ba', '[\"*\"]', NULL, NULL, '2026-04-06 07:33:01', '2026-04-06 07:33:01'),
(506, 'App\\Models\\StructAgent', '62', 'authToken', '7ddf088c9949bd0bb6a33f48d3ded548f87da9b608bcd6bd15e2d0a670357412', '[\"*\"]', NULL, NULL, '2026-04-06 15:04:31', '2026-04-06 15:04:31'),
(507, 'App\\Models\\StructAgent', '3', 'authToken', '08de813df8940c15a8306e989071b703ae9f77860c4678109cad593a574960f6', '[\"*\"]', NULL, NULL, '2026-04-09 07:34:03', '2026-04-09 07:34:03'),
(508, 'App\\Models\\StructAgent', '57', 'authToken', 'c878e94558068fae7cc719445f517ff7b11f915c183b79f4f9ec57ef07248606', '[\"*\"]', NULL, NULL, '2026-04-09 07:39:38', '2026-04-09 07:39:38'),
(509, 'App\\Models\\StructAgent', '62', 'authToken', '13f6ad1ff39906796ec15cbd4030fa62dd976a547fdd4c300fa827a9b2f56b6b', '[\"*\"]', NULL, NULL, '2026-04-11 06:50:21', '2026-04-11 06:50:21'),
(510, 'App\\Models\\StructAgent', '62', 'authToken', '5790253c09f2f72be6d7c284559c135e0165a6cdbf3542f895c480da6c2e30ee', '[\"*\"]', NULL, NULL, '2026-04-11 14:15:41', '2026-04-11 14:15:41'),
(511, 'App\\Models\\StructAgent', '62', 'authToken', 'acdb792ff87207d6a11f37ab7a8b2e7f5417701a4a2980b5372eb06af7af89c3', '[\"*\"]', NULL, NULL, '2026-04-13 15:59:43', '2026-04-13 15:59:43'),
(512, 'App\\Models\\StructAgent', '62', 'authToken', '5c999644baf2665fe45a4505f53bdd07c9a432903f460d6cd0f5252577ebe343', '[\"*\"]', NULL, NULL, '2026-04-17 15:02:32', '2026-04-17 15:02:32'),
(513, 'App\\Models\\StructAgent', '62', 'authToken', 'acb97da8791db803c97863ba118cb64987cfc1af844b3afd3e98a10b55528d9f', '[\"*\"]', NULL, NULL, '2026-04-17 16:06:25', '2026-04-17 16:06:25'),
(514, 'App\\Models\\StructAgent', '62', 'authToken', '30d39efc1779fd339db16d4bf17af3bb8517bcec4b6760f0a054a9c2c59dd74e', '[\"*\"]', NULL, NULL, '2026-04-18 06:13:59', '2026-04-18 06:13:59'),
(515, 'App\\Models\\StructAgent', '62', 'authToken', 'c0344d7f895f055bc1a468fdd0d5227db7957ec0ff0d741f47ce015f187bde0e', '[\"*\"]', NULL, NULL, '2026-04-18 07:46:17', '2026-04-18 07:46:17'),
(516, 'App\\Models\\StructAgent', '62', 'authToken', 'f9071c8dd5ecbf4437bb15413149fec8ba099309a34fde1ccc776f62aae1db3e', '[\"*\"]', NULL, NULL, '2026-04-19 08:55:05', '2026-04-19 08:55:05'),
(517, 'App\\Models\\StructAgent', '62', 'authToken', '4d0f3698acfb485bbd08130daee570ab01be8a80f91b349fc5946410793b3bfa', '[\"*\"]', NULL, NULL, '2026-04-19 09:15:42', '2026-04-19 09:15:42'),
(518, 'App\\Models\\StructAgent', '62', 'authToken', '13a2ddd6252f8550e9e518a50d5409d1715f891ce55a969a20fca21590134744', '[\"*\"]', NULL, NULL, '2026-04-19 09:40:33', '2026-04-19 09:40:33'),
(519, 'App\\Models\\StructAgent', '62', 'authToken', 'a17abe4481f3dabed259169e1ed9214bf8dc7990f7dde6d4323230b078d2c83e', '[\"*\"]', NULL, NULL, '2026-04-28 07:52:33', '2026-04-28 07:52:33'),
(520, 'App\\Models\\StructAgent', '62', 'authToken', 'ea2aa0140e15a9769960edac7b7c47166a3522504012994ad3ca39221e9a0948', '[\"*\"]', NULL, NULL, '2026-04-28 08:04:16', '2026-04-28 08:04:16'),
(521, 'App\\Models\\StructAgent', '62', 'authToken', '5aeefe24e5b9950d14837c9007380ffe98b0464f578fcae3ccdd2f638f08e4a6', '[\"*\"]', NULL, NULL, '2026-04-28 12:51:23', '2026-04-28 12:51:23'),
(522, 'App\\Models\\StructAgent', '3', 'authToken', '37aef6cdc3bf8f47ae84116fa0cb6a9527c84deb352cfadf245bec904517345a', '[\"*\"]', NULL, NULL, '2026-04-28 13:00:30', '2026-04-28 13:00:30'),
(523, 'App\\Models\\StructAgent', '64', 'authToken', '52fb9ec548a885938ba4cb94d0c8e09928eb7cd621ca4bfae7fdc934e2700c6b', '[\"*\"]', NULL, NULL, '2026-04-28 13:04:18', '2026-04-28 13:04:18'),
(524, 'App\\Models\\StructAgent', '62', 'authToken', 'd94715206e5c46368e7b91d50051854b628e63a1fb8b3785ec809d17da160827', '[\"*\"]', NULL, NULL, '2026-04-28 15:04:10', '2026-04-28 15:04:10'),
(525, 'App\\Models\\StructAgent', '62', 'authToken', '30ef9bf0eb0f00f70967c516fc691162bed9fed3da0d8f6cc78c7c78d9a3e701', '[\"*\"]', NULL, NULL, '2026-04-28 15:04:58', '2026-04-28 15:04:58'),
(526, 'App\\Models\\StructAgent', '62', 'authToken', '5aa657cf48656d83980ebb6c09b45c87a356dd0425aecc20dd4d9d7ba92804f4', '[\"*\"]', NULL, NULL, '2026-04-28 15:06:05', '2026-04-28 15:06:05'),
(527, 'App\\Models\\StructAgent', '62', 'authToken', 'db1ed03011864b6e7a900a7ce13b13326174688972c4b7d5adae75be53204b62', '[\"*\"]', NULL, NULL, '2026-04-28 15:08:10', '2026-04-28 15:08:10'),
(528, 'App\\Models\\StructAgent', '62', 'authToken', '2e9b7538165c93b1c677e28d648befec5cfb2335c3239c136b90f625b3908dc1', '[\"*\"]', NULL, NULL, '2026-04-29 07:41:15', '2026-04-29 07:41:15'),
(529, 'App\\Models\\StructAgent', '62', 'authToken', 'd8cb9a0f584892e482a2f3d9e7d4a7133400072b2f8c3e63988f2b35fb168e82', '[\"*\"]', NULL, NULL, '2026-05-06 14:45:43', '2026-05-06 14:45:43'),
(530, 'App\\Models\\StructAgent', '62', 'authToken', '193379a4d07d3e30c6bc1ef74ff636e6ab00ff451a0ee0ac4703d0f883142dca', '[\"*\"]', NULL, NULL, '2026-05-06 14:46:36', '2026-05-06 14:46:36'),
(531, 'App\\Models\\StructAgent', '62', 'authToken', '210866a6ced0f7781519f82e74346da3b28c212ebc0d8f42c5ad956066559fc7', '[\"*\"]', NULL, NULL, '2026-05-06 15:14:35', '2026-05-06 15:14:35'),
(532, 'App\\Models\\StructAgent', '3', 'authToken', 'fa839df85d790321cbbd6c6fca52ab6778d3503b199f4c58e6193b1a1eb64dfe', '[\"*\"]', NULL, NULL, '2026-05-06 15:15:15', '2026-05-06 15:15:15'),
(533, 'App\\Models\\StructAgent', '3', 'authToken', '75fcd3706928c6b33beb746a38bab8a190cfebe7fbbdfc19bbb2acbfaa411eb1', '[\"*\"]', NULL, NULL, '2026-05-06 15:16:09', '2026-05-06 15:16:09'),
(534, 'App\\Models\\StructAgent', '3', 'authToken', 'f2d1e4d3a29313d8d2e7b366f43fec97e47e5b7a9e23e014821a3f6bc010a2cb', '[\"*\"]', NULL, NULL, '2026-05-08 09:18:59', '2026-05-08 09:18:59'),
(535, 'App\\Models\\StructAgent', '62', 'authToken', '6b9d1e4c76b08f9f3d54f7b2e86108a8c08ddfeab031ba76dda772b884b09aa3', '[\"*\"]', NULL, NULL, '2026-05-08 09:19:51', '2026-05-08 09:19:51'),
(536, 'App\\Models\\StructAgent', '62', 'authToken', '948d4b207e224fcd0a83cf44b0acb0ea419ba9e67b03e6b0e1a279e90f34cfc8', '[\"*\"]', NULL, NULL, '2026-05-11 15:10:54', '2026-05-11 15:10:54'),
(537, 'App\\Models\\StructAgent', '62', 'authToken', '20dbff3fae9516480bf704ea94326b2f10abc0c875d0336e265d2ce6084fd93b', '[\"*\"]', NULL, NULL, '2026-05-12 06:55:56', '2026-05-12 06:55:56'),
(538, 'App\\Models\\StructAgent', '3', 'authToken', 'f4eb3aec8abf715a52420cfc47b8433d5fdc3927d4c9205cfd535080e70598cd', '[\"*\"]', NULL, NULL, '2026-05-13 14:37:42', '2026-05-13 14:37:42'),
(539, 'App\\Models\\StructAgent', '62', 'authToken', '1e2f90dafcb1a5bcc3c33565125d10d2bcdd0648ac1bafc56dfdff6a4c478e61', '[\"*\"]', NULL, NULL, '2026-05-13 14:38:10', '2026-05-13 14:38:10'),
(540, 'App\\Models\\StructAgent', '62', 'authToken', 'da00b0da22c67a6e66432cef3d79727833725af31d646cfd2a07a602d393b0bd', '[\"*\"]', NULL, NULL, '2026-05-21 06:46:05', '2026-05-21 06:46:05'),
(541, 'App\\Models\\StructAgent', '3', 'authToken', '150352cb2dd03234ec749c1d9fbd1a73862947158c966f321e4eeedfeb855fd2', '[\"*\"]', NULL, NULL, '2026-06-02 17:03:23', '2026-06-02 17:03:23'),
(542, 'App\\Models\\StructAgent', '65', 'authToken', '826325979d4374b4f9a040893a0b4464383a5c41a00201dd81194a65e6db7ae2', '[\"*\"]', NULL, NULL, '2026-06-02 17:06:10', '2026-06-02 17:06:10'),
(543, 'App\\Models\\StructAgent', '65', 'authToken', '6214db2a60115e85faa3503d25e2bd890aef442f249576eb8d408c48e916859a', '[\"*\"]', NULL, NULL, '2026-06-03 12:53:05', '2026-06-03 12:53:05');

-- --------------------------------------------------------

--
-- Structure de la table `Reservation`
--

CREATE TABLE `Reservation` (
  `idReservation` int NOT NULL,
  `idNumber` varchar(256) NOT NULL,
  `idBook` varchar(256) DEFAULT NULL,
  `idStruct` int NOT NULL,
  `date` datetime DEFAULT NULL,
  `numberOfDay` tinyint DEFAULT '0',
  `expireDate` datetime DEFAULT NULL,
  `deliveryDate` datetime DEFAULT NULL,
  `state` tinyint DEFAULT '1',
  `treat` tinyint DEFAULT '1',
  `view` tinyint DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- Déchargement des données de la table `Reservation`
--

INSERT INTO `Reservation` (`idReservation`, `idNumber`, `idBook`, `idStruct`, `date`, `numberOfDay`, `expireDate`, `deliveryDate`, `state`, `treat`, `view`, `created_at`, `updated_at`) VALUES
(10072, '94961793', 'OPEN0023', 1, NULL, 1, '2026-04-08 17:11:43', '2026-04-06 17:13:41', 4, 1, 0, '2026-04-06 17:11:43', '2026-04-06 17:11:43'),
(10073, '94961793', 'OPEN0006', 1, NULL, 4, '2026-04-13 08:52:03', '2026-04-11 08:53:08', 4, 1, 0, '2026-04-11 08:52:03', '2026-04-11 08:52:03'),
(10074, '94961793', 'OPEN0013', 1, NULL, 3, '2026-04-13 16:17:20', '2026-04-11 16:18:32', 4, 1, 0, '2026-04-11 16:17:20', '2026-04-11 16:17:20'),
(10078, '94961793', 'OPEN0006', 1, NULL, 7, '2026-04-21 11:45:03', NULL, 1, 0, 0, '2026-04-19 11:45:03', '2026-04-19 11:45:03'),
(10080, '90637132', 'OPEN0023', 1, NULL, 2, '2026-06-05 16:16:22', NULL, 2, 1, 0, '2026-06-03 16:16:22', '2026-06-03 16:16:22'),
(10081, '90637132', 'OPEN0023', 1, NULL, 3, '2026-06-06 12:48:15', NULL, 1, 0, 0, '2026-06-04 12:48:15', '2026-06-04 12:48:15'),
(10082, '90637132', 'OPEN0024', 1, NULL, 3, '2026-06-06 13:09:52', NULL, 2, 1, 0, '2026-06-04 13:09:52', '2026-06-04 13:09:52');

-- --------------------------------------------------------

--
-- Structure de la table `Sanction`
--

CREATE TABLE `Sanction` (
  `idSanction` int NOT NULL,
  `idNumber` varchar(256) DEFAULT NULL,
  `description` text NOT NULL,
  `date` datetime NOT NULL,
  `state` tinyint DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- --------------------------------------------------------

--
-- Structure de la table `sessions`
--

CREATE TABLE `sessions` (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint UNSIGNED DEFAULT NULL,
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_agent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `payload` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `last_activity` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `sessions`
--

INSERT INTO `sessions` (`id`, `user_id`, `ip_address`, `user_agent`, `payload`, `last_activity`) VALUES
('aoLRPZf7UdBWqp1vbKxs3EYmLGcMVsMMqi43Sz6u', NULL, '127.0.0.1', 'PostmanRuntime/7.44.0', 'YTozOntzOjY6Il90b2tlbiI7czo0MDoiMVU1Sm1GbkNvRkxZeTdqTzdmQnF1YW9pbTlNV1huYTRXS2ZXdTFyMiI7czo5OiJfcHJldmlvdXMiO2E6MTp7czozOiJ1cmwiO3M6MjE6Imh0dHA6Ly9sb2NhbGhvc3Q6ODAwMCI7fXM6NjoiX2ZsYXNoIjthOjI6e3M6Mzoib2xkIjthOjA6e31zOjM6Im5ldyI7YTowOnt9fX0=', 1750147079),
('BkxbI2RHwsGt61LO4g9oh9o3MVk0jPL8reGEo43E', NULL, '127.0.0.1', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36', 'YTozOntzOjY6Il90b2tlbiI7czo0MDoiRXNDM2RKWDdSUmxqN2hWWmFFTHN3MVBCOFY0Vm9GTG84dk1ieWYxVyI7czo5OiJfcHJldmlvdXMiO2E6MTp7czozOiJ1cmwiO3M6MjE6Imh0dHA6Ly9sb2NhbGhvc3Q6ODAwMCI7fXM6NjoiX2ZsYXNoIjthOjI6e3M6Mzoib2xkIjthOjA6e31zOjM6Im5ldyI7YTowOnt9fX0=', 1749889599),
('DLav1nNCoK7bTb3WdQcd5ZTiZIY5arG1VVcBuycV', NULL, '41.203.139.119', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36', 'YTozOntzOjY6Il90b2tlbiI7czo0MDoidjdLMEY3Vm5KSTAwcDVJNjJTaVlwNFNZdHo3RlVVNkZob0l3S1U2ciI7czo5OiJfcHJldmlvdXMiO2E6MTp7czozOiJ1cmwiO3M6MjA6Imh0dHBzOi8vdGVsZXNhZmUubmV0Ijt9czo2OiJfZmxhc2giO2E6Mjp7czozOiJvbGQiO2E6MDp7fXM6MzoibmV3IjthOjA6e319fQ==', 1750254192),
('fM8IuZH9TXkbuLJgFilrkcVkxVeIDbNtzM14UrRY', NULL, '127.0.0.1', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36', 'YTozOntzOjY6Il90b2tlbiI7czo0MDoiYmNTc2ZWZHVoWERVRVZqaHZmVWVNenBNbGJjUlRXaTBHYUZabWNCaiI7czo5OiJfcHJldmlvdXMiO2E6MTp7czozOiJ1cmwiO3M6MjE6Imh0dHA6Ly9sb2NhbGhvc3Q6ODAwMCI7fXM6NjoiX2ZsYXNoIjthOjI6e3M6Mzoib2xkIjthOjA6e31zOjM6Im5ldyI7YTowOnt9fX0=', 1749635581),
('u6MsMwA03VYWg4AxTVBjUWK4BesSLIlLlgypWzDf', NULL, '127.0.0.1', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36', 'YTozOntzOjY6Il90b2tlbiI7czo0MDoicGxjVmhBTUZIYXBTalRSbGtOMUJDN293akFYUERISkw4TjN0TEVLaCI7czo5OiJfcHJldmlvdXMiO2E6MTp7czozOiJ1cmwiO3M6MjE6Imh0dHA6Ly9sb2NhbGhvc3Q6ODAwMCI7fXM6NjoiX2ZsYXNoIjthOjI6e3M6Mzoib2xkIjthOjA6e31zOjM6Im5ldyI7YTowOnt9fX0=', 1749641515),
('wmXYSv1QfowP7gIKDcUlABHTN2lhTJ7RIFOAicAD', NULL, '127.0.0.1', 'insomnia/11.2.0', 'YTozOntzOjY6Il90b2tlbiI7czo0MDoiOXZ5dkhwaWQzZkhoS010SEJOOU5YRTZHYXhDUFRBVUhxMnZoeUVBNyI7czo5OiJfcHJldmlvdXMiO2E6MTp7czozOiJ1cmwiO3M6MjE6Imh0dHA6Ly9sb2NhbGhvc3Q6ODAwMCI7fXM6NjoiX2ZsYXNoIjthOjI6e3M6Mzoib2xkIjthOjA6e31zOjM6Im5ldyI7YTowOnt9fX0=', 1750107222);

-- --------------------------------------------------------

--
-- Structure de la table `StructBook`
--

CREATE TABLE `StructBook` (
  `idStruct` int NOT NULL,
  `idBook` varchar(256) NOT NULL,
  `date` datetime NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- Déchargement des données de la table `StructBook`
--

INSERT INTO `StructBook` (`idStruct`, `idBook`, `date`, `created_at`, `updated_at`) VALUES
(1, 'HA0021', '2026-05-21 09:24:32', '2026-05-21 07:24:32', '2026-05-21 07:24:32'),
(1, 'OPEN0001', '2026-05-12 09:04:26', '2026-05-12 07:04:26', '2026-05-12 07:04:26'),
(1, 'OPEN0006', '2026-03-25 10:38:13', '2026-03-25 09:38:13', '2026-03-25 09:38:13'),
(1, 'OPEN0012', '2026-03-25 10:45:50', '2026-03-25 09:45:50', '2026-03-25 09:45:50'),
(1, 'OPEN0013', '2026-03-25 11:39:06', '2026-03-25 10:39:06', '2026-03-25 10:39:06'),
(1, 'OPEN0014', '2026-03-30 11:58:43', '2026-03-30 09:58:43', '2026-03-30 09:58:43'),
(1, 'OPEN0017', '2026-03-30 16:13:08', '2026-03-30 14:13:08', '2026-03-30 14:13:08'),
(1, 'OPEN0021', '2026-03-30 16:17:05', '2026-03-30 14:17:05', '2026-03-30 14:17:05'),
(1, 'OPEN0022', '2026-03-30 16:20:01', '2026-03-30 14:20:01', '2026-03-30 14:20:01'),
(1, 'OPEN0023', '2026-04-01 11:58:30', '2026-04-01 09:58:30', '2026-04-01 09:58:30'),
(1, 'OPEN0024', '2026-03-30 17:56:24', '2026-03-30 15:56:24', '2026-03-30 15:56:24'),
(1, 'OPEN0025', '2026-03-30 17:58:37', '2026-03-30 15:58:37', '2026-03-30 15:58:37'),
(1, 'OPEN0027', '2026-03-30 18:03:24', '2026-03-30 16:03:24', '2026-03-30 16:03:24'),
(1, 'TEST0001', '2026-03-29 12:35:25', '2026-03-29 10:35:25', '2026-03-29 10:35:25'),
(2, 'TD0001', '2026-04-28 15:46:41', '2026-04-28 13:46:41', '2026-04-28 13:46:41'),
(2, 'TD0002', '2026-04-28 15:55:26', '2026-04-28 13:55:26', '2026-04-28 13:55:26'),
(2, 'TD0003', '2026-04-28 16:00:47', '2026-04-28 14:00:47', '2026-04-28 14:00:47'),
(2, 'TD0004', '2026-04-28 16:06:09', '2026-04-28 14:06:09', '2026-04-28 14:06:09');

-- --------------------------------------------------------

--
-- Structure de la table `Structure`
--

CREATE TABLE `Structure` (
  `id` int NOT NULL,
  `nameStruct` varchar(256) NOT NULL,
  `description` varchar(10000) DEFAULT NULL,
  `logo` varchar(256) DEFAULT NULL,
  `banner` varchar(256) DEFAULT 'banner.png',
  `author` varchar(256) DEFAULT NULL,
  `adhererNumber` int DEFAULT '0',
  `bookNumber` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `storageLimit` float DEFAULT '16106100000'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- Déchargement des données de la table `Structure`
--

INSERT INTO `Structure` (`id`, `nameStruct`, `description`, `logo`, `banner`, `author`, `adhererNumber`, `bookNumber`, `created_at`, `updated_at`, `location`, `storageLimit`) VALUES
(1, 'OpenLab', 'OpenLab d\'EduNiger est une structure dédiée à la proposition et à la mise en avant des œuvres des auteurs nigériens, favorisant ainsi la valorisation de la littérature locale.', 'logo_1777388265.png', 'banner_1777388133.png', 'eduniger', 307, 23, NULL, '2026-03-25 08:32:01', 'EduNiger', NULL),
(2, 'Kit TD', 'Kit TD est une structure d\'EduNiger dédiée aux candidats au examens, leur offrant des ressources éducatives avec des exercices corriges pour une préparation optimale.', 'logo_1777389030.png', 'banner_1777389030.png', 'eduniger', 0, 0, NULL, '2026-04-28 13:10:30', 'Niger', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `StructureNotifications`
--

CREATE TABLE `StructureNotifications` (
  `id` bigint UNSIGNED NOT NULL,
  `idStruct` int DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `message` text NOT NULL,
  `type` enum('info','warning','success','system') DEFAULT 'info',
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `StructureNotifications`
--

INSERT INTO `StructureNotifications` (`id`, `idStruct`, `title`, `message`, `type`, `created_at`, `updated_at`) VALUES
(2, NULL, 'Pause', 'ceci est une pause', 'info', '2026-03-18 02:58:35', '2026-03-18 02:58:35');

-- --------------------------------------------------------

--
-- Structure de la table `StructUser`
--

CREATE TABLE `StructUser` (
  `idStruct` int NOT NULL,
  `idUser` varchar(256) NOT NULL,
  `date` datetime NOT NULL,
  `isAdmin` tinyint DEFAULT '0',
  `registerNumber` varchar(10000) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- Déchargement des données de la table `StructUser`
--

INSERT INTO `StructUser` (`idStruct`, `idUser`, `date`, `isAdmin`, `registerNumber`, `created_at`, `updated_at`) VALUES
(1, '222222228', '2026-04-15 19:48:50', 0, NULL, '2026-04-15 17:48:50', '2026-04-15 17:48:50'),
(1, '74295044', '2026-04-18 10:57:24', 0, NULL, '2026-04-18 08:57:24', '2026-04-18 08:57:24'),
(1, '80929440', '2026-05-09 21:35:16', 0, NULL, '2026-05-09 19:35:16', '2026-05-09 19:35:16'),
(1, '90637132', '2026-04-16 13:03:09', 0, NULL, '2026-04-16 11:03:09', '2026-04-16 11:03:09'),
(1, '94961793', '2026-03-24 15:49:45', 0, '94961793', '2026-03-24 15:50:27', '2026-03-24 15:50:27');

-- --------------------------------------------------------

--
-- Structure de la table `struct_agent`
--

CREATE TABLE `struct_agent` (
  `id` int NOT NULL,
  `name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `phoneNumber` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` tinyint NOT NULL DEFAULT '0',
  `idStruct` int DEFAULT NULL,
  `isAuthorizedToCreateAccount` tinyint(1) NOT NULL DEFAULT '0',
  `invitationCode` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `codeExpiresAt` datetime DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `struct_agent`
--

INSERT INTO `struct_agent` (`id`, `name`, `email`, `phoneNumber`, `role`, `idStruct`, `isAuthorizedToCreateAccount`, `invitationCode`, `codeExpiresAt`, `password`, `created_at`, `updated_at`) VALUES
(3, 'Abdoul Kader', 'derkariom@gmail.com', '94967688', 2, NULL, 0, NULL, NULL, '$2y$12$TaVKL4W4drXZNDjBRoYd8ubszLRysRq1Oj6PxLYUCvd5KTxEj48lq', '2025-06-23 05:51:13', '2025-06-23 09:22:06'),
(57, 'Ali Dicko', 'ali@gmail.com', '7876545', 1, 1, 1, 'WZII-9473', '2026-03-25 13:40:40', '$2y$12$H1Cjb/qJTOmRWHc4uvYkRevDof.pRUZObOSaxhXyHJev6VWGHAwdm', '2026-03-24 12:40:40', '2026-03-24 12:40:40'),
(62, 'Bachir Abdoul Kader', 'bachirabdoulkader62331@gmail.com', '94961793', 1, 1, 1, 'IDGY-8013', '2026-03-26 09:29:17', '$2y$12$jkPvHPFJCKXbXBLFkAw0dOSHafe9CtYqILU/892NSxofxrluP8PBK', '2026-03-25 08:29:17', '2026-04-28 07:53:28'),
(64, 'Bachir Abdoul Kader', 'kittd@gmail.com', '94961793', 1, 2, 1, '9QVR-2337', '2026-04-29 15:03:29', '$2y$12$baTxJ1Yeo4vEMO6GBJoNY.vez0xcevYR2gk/Yh/teZvAMLoIapjkW', '2026-04-28 13:03:29', '2026-04-28 13:03:29'),
(65, 'Dille', 'oumaroudev01@gmail.com', '90637132', 1, 1, 1, 'U2FH-6881', '2026-06-03 19:05:34', '$2y$12$2vFoZs6.UWDGaYNNXy83Qe7XN55CLmh2ofIlBu8xYtBBcIUhQmVMi', '2026-06-02 17:05:34', '2026-06-02 17:05:34');

-- --------------------------------------------------------

--
-- Structure de la table `Student`
--

CREATE TABLE `Student` (
  `idNumber` varchar(256) NOT NULL,
  `name` varchar(256) NOT NULL,
  `firstName` varchar(256) NOT NULL,
  `section` varchar(256) DEFAULT NULL,
  `department` varchar(256) DEFAULT NULL,
  `isDelegue` tinyint DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- Déchargement des données de la table `Student`
--

INSERT INTO `Student` (`idNumber`, `name`, `firstName`, `section`, `department`, `isDelegue`) VALUES
('5555', 'Kadux', 'Bachir', 'Master-1-IFA', 'MI', 0),
('555666', 'Ras', 'Ttty', 'Master-1-IFA', 'MI', 0),
('60507', 'Illa yacouba', 'Moubarak', 'Master 1 IFA', 'MI', 0),
('62006', 'Aboubacar Hamidou', 'Ridouane', 'Master 1 IFA', 'MI', 0),
('62331', 'Abdoul Kader', 'Bachir', 'Master-1-IFA', 'MI', 1),
('62364', 'Hachomou Lassan', 'Abdoul Rachid', 'Master 1 IFA', 'MI', 0),
('66158', 'Yousoussa', 'Dille', 'Master 1 IFA', 'MI', 0),
('66219', 'Issoufou Dodo', 'Alazi', 'Master-1-IFA', 'MI', 0);

-- --------------------------------------------------------

--
-- Structure de la table `SubscribeBook`
--

CREATE TABLE `SubscribeBook` (
  `idNumber` varchar(256) NOT NULL,
  `idBook` varchar(256) NOT NULL,
  `date` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- Déchargement des données de la table `SubscribeBook`
--

INSERT INTO `SubscribeBook` (`idNumber`, `idBook`, `date`) VALUES
('90637132', 'OPEN0024', '2026-05-07 15:57:04'),
('94961793', 'OPEN0006', '2026-04-11 10:48:21'),
('94961793', 'OPEN0022', '2026-04-08 18:44:17'),
('94961793', 'OPEN0023', '2026-04-08 18:29:36'),
('94961793', 'OPEN0024', '2026-04-08 18:47:29'),
('94961793', 'OPEN0025', '2026-04-08 18:48:43'),
('94961793', 'OPEN0027', '2026-03-31 15:15:34');

--
-- Déclencheurs `SubscribeBook`
--
DELIMITER $$
CREATE TRIGGER `AFTER_DELETE_SUBSCRIBEBOOK` AFTER DELETE ON `SubscribeBook` FOR EACH ROW BEGIN
    UPDATE `Book` SET numberSubscribe=numberSubscribe-1 WHERE `idBook`=OLD.idBook;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `AFTER_INSERT_SUBSCRIBEBOOK` AFTER INSERT ON `SubscribeBook` FOR EACH ROW BEGIN
    UPDATE `Book` SET numberSubscribe=numberSubscribe+1 WHERE `idBook`=NEW.idBook;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `SubscribeCategory`
--

CREATE TABLE `SubscribeCategory` (
  `idNumber` varchar(256) NOT NULL,
  `idCategory` int NOT NULL,
  `date` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- Déclencheurs `SubscribeCategory`
--
DELIMITER $$
CREATE TRIGGER `AFTER_DELETE_SUBSCRIBECATEGORY` AFTER DELETE ON `SubscribeCategory` FOR EACH ROW BEGIN
    UPDATE `Category` SET numberSubscribe=numberSubscribe-1 WHERE `idCategory`=OLD.idCategory;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `AFTER_INSERT_SUBSCRIBECATEGORY` AFTER INSERT ON `SubscribeCategory` FOR EACH ROW BEGIN
    UPDATE `Category` SET numberSubscribe=numberSubscribe+1 WHERE `idCategory`=NEW.idCategory;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `User`
--

CREATE TABLE `User` (
  `idUser` varchar(256) NOT NULL,
  `name` varchar(256) NOT NULL,
  `firstName` varchar(256) NOT NULL,
  `email` varchar(256) NOT NULL,
  `password` varchar(1000) NOT NULL,
  `profile` varchar(256) DEFAULT 'user.png',
  `profession` tinyint DEFAULT '0',
  `isAdmin` tinyint DEFAULT '0',
  `phoneNumber` varchar(50) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `fcm_token` varchar(10000) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- Déchargement des données de la table `User`
--

INSERT INTO `User` (`idUser`, `name`, `firstName`, `email`, `password`, `profile`, `profession`, `isAdmin`, `phoneNumber`, `created_at`, `updated_at`, `fcm_token`) VALUES
('2222222', 'Yanoussa Dille', 'Oumarou', 'test1@gmail.com', '0ffe1abd1a08215353c233d6e009613e95eec4253832a761af28ff37ac5a150c', 'user.png', 2, 0, NULL, '2026-04-15 17:16:42', '2026-04-15 17:16:42', 'fcm_token'),
('22222222', 'Yanoussa Dille', 'Oumarou', 'test28@gmail.com', '0ffe1abd1a08215353c233d6e009613e95eec4253832a761af28ff37ac5a150c', 'user.png', 2, 0, NULL, '2026-04-15 17:19:54', '2026-04-15 17:19:54', 'fcm_token'),
('222222228', 'Yanoussa Dille', 'Oumarou', 'test30@gmail.com', '0ffe1abd1a08215353c233d6e009613e95eec4253832a761af28ff37ac5a150c', 'user.png', 2, 0, NULL, '2026-04-15 17:48:50', '2026-04-15 17:48:50', 'fcm_token'),
('717a1c39-9676-46b4-af36-b3a5a69a5a02', 'Test', 'Test', 'test123d@gmail.com', '$2y$12$kBLhuCAR6fvp4EHwL5QWUu0se9hmoDXoa/wpbWZJhewvXCnZBuoai', 'user.png', 0, 0, '909090562', '2025-07-09 09:37:43', '2025-07-09 09:37:43', ''),
('74295044', 'Yanoussa', 'Dille', 'oumaroudev01@gmail.com', '0ffe1abd1a08215353c233d6e009613e95eec4253832a761af28ff37ac5a150c', 'user.png', 1, 0, NULL, '2026-04-17 16:17:31', '2026-04-17 16:17:31', 'fOk3o_MARBSdPmjpYAU2Nz:APA91bFHLYyqNdRW7hPktaYhloCm4sR4JsZrNUW0F_ttXMxEcyFkBO5dLVG2xkYQpifca5tAlGyoCuaN0kQgUMhCQWOIiGaKvva-UVp7_9vs9uXLlm2fmxM'),
('774e389c-925f-4c14-8966-835cb4180836', 'test', 'Test', 'test@gmail.com', '$2y$12$H3n6EFzLDwsgOq0e803DN.L4SOnQGOXq3Uw8.bAwGUKir.hBghq3i', 'https://baseuam.org/website//website/user.png', 2, 0, '90987767', '2025-08-16 07:20:55', '2025-08-16 07:20:55', ''),
('80929440', 'Aboubacar Hamidou', 'Ridouane', 'aboubacarhamidouridouane@gmail.com', '46437ab18a6657040b4535297ff247b20c535c02263713f88b6a9e17484f1f3f', 'user.png', 2, 0, NULL, '2026-05-09 19:35:16', '2026-05-16 09:16:28', 'cRKTFEeMQ7KuW7i_1_hGm4:APA91bHEmTsAUFNf0dPm8bq_O5asi5NltDwpzAb0srnpSPIGHAeW-pQIaTMANZAsRC__PXVtKDQMEz4rwreoaG7vnZb9NeT8nt4t4QyZ1QCIGU5AU7iJC30'),
('90637131', 'Yanoussa Dille', 'Oumarou', 'oumaroudevelop@gmail.com', '0ffe1abd1a08215353c233d6e009613e95eec4253832a761af28ff37ac5a150c', 'user.png', 2, 0, NULL, '2026-04-15 17:14:44', '2026-04-15 17:14:44', 'fcm_token'),
('90637132', 'Oumarou', 'Yanoussa', 'yanoussaoumarou227@gmail.com', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'user.png', 2, 0, NULL, '2026-04-16 11:03:09', '2026-05-16 11:13:57', 'edtw_IQaRa6d6oY2mcyuVp:APA91bFbgPcZX10gU-CVHFn1iQaQdiRy6PF7ieCP-UySUU2ZoRB1hEqeoimQZni_SblPQfcHsBB7a-W6_igRBlwmY7z1KDF5a4Xvukj5DPLY0eykj0vfLl8'),
('94961793', 'Abdoul Kader', 'Bachir', 'derkariom@gmail.com', 'f08c6528bb8857db707cbab808eec8aed36cdd879b2f41e056b6c9c498be39b0', 'user.png', 4, 0, NULL, '2026-03-24 15:25:37', '2026-08-24 11:07:59', 'dRXfPlANTiue_7Pgn_9XoU:APA91bHufgehHO6bFkb02bzDH-dzGv-Cmf43yxQPGP_o4qNGQ0pAitPYRD0nKjkP1aFVJoc74PfgEqMXgtDnwa3G64yLufpAoyATzZhLbiNOn80wQ09e_1o'),
('ccf841cf-2f87-4634-9d42-eaac993519c2', 'test', 'tst', 'test2@gmail.com', '$2y$12$sfHs.Dm8HPG1ZU1Hxn4aLe2S/wXwLMWCwtceQOwd1boavTC1NbulS', 'https://baseuam.org/website//website/user.png', 2, 0, '88765789', '2025-08-16 07:24:00', '2025-08-16 07:24:00', ''),
('dc048dc9-46c3-49fa-ba22-3f1def569e7a', 'Shad', 'iMoubaraks', 'shad@gmail.com', '$2y$12$PykDnJda2bvF2H/p/tj4mucBajJSslZUenSr8NHDHwLE5C9fIpOq6', 'user.png', 0, 0, '90865469', '2025-07-09 09:45:32', '2025-07-12 12:07:30', '');

-- --------------------------------------------------------

--
-- Structure de la table `UserNotificationRead`
--

CREATE TABLE `UserNotificationRead` (
  `id` bigint UNSIGNED NOT NULL,
  `user_id` int NOT NULL,
  `notification_id` bigint UNSIGNED NOT NULL,
  `read_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `users`
--

CREATE TABLE `users` (
  `id` bigint UNSIGNED NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `email_verified_at` timestamp NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `remember_token` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `Version`
--

CREATE TABLE `Version` (
  `idVersion` varchar(256) NOT NULL,
  `date` datetime NOT NULL,
  `description` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- Déchargement des données de la table `Version`
--

INSERT INTO `Version` (`idVersion`, `date`, `description`) VALUES
('3.1.3', '2024-02-03 07:14:48', 'La premiere version');

-- --------------------------------------------------------

--
-- Structure de la table `View`
--

CREATE TABLE `View` (
  `idNumber` varchar(256) NOT NULL,
  `idBook` varchar(256) NOT NULL,
  `date` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

--
-- Déchargement des données de la table `View`
--

INSERT INTO `View` (`idNumber`, `idBook`, `date`) VALUES
('80929440', 'HA0021', '2026-05-22 11:14:47'),
('80929440', 'OPEN0001', '2026-05-17 17:42:36'),
('80929440', 'OPEN0014', '2026-05-14 09:49:03'),
('80929440', 'OPEN0027', '2026-05-14 09:48:42'),
('90637132', 'HA0021', '2026-07-01 12:55:11'),
('90637132', 'OPEN0001', '2026-05-16 11:54:11'),
('90637132', 'OPEN0006', '2026-05-05 13:43:33'),
('90637132', 'OPEN0013', '2026-05-04 11:35:46'),
('90637132', 'OPEN0014', '2026-05-04 18:49:48'),
('90637132', 'OPEN0017', '2026-05-07 12:13:22'),
('90637132', 'OPEN0021', '2026-05-04 19:03:12'),
('90637132', 'OPEN0022', '2026-05-04 10:56:19'),
('90637132', 'OPEN0023', '2026-05-04 10:43:00'),
('90637132', 'OPEN0024', '2026-05-04 10:51:24'),
('90637132', 'OPEN0025', '2026-05-04 10:46:42'),
('90637132', 'OPEN0027', '2026-05-04 10:45:20'),
('90637132', 'TD0002', '2026-05-15 18:05:17'),
('90637132', 'TD0003', '2026-05-16 11:45:10'),
('94961793', 'HA0021', '2026-05-21 11:25:14'),
('94961793', 'OPEN0001', '2026-05-12 11:05:03'),
('94961793', 'OPEN0006', '2026-04-11 10:47:17'),
('94961793', 'OPEN0012', '2026-04-16 10:44:05'),
('94961793', 'OPEN0013', '2026-04-11 18:17:04'),
('94961793', 'OPEN0014', '2026-04-13 20:01:50'),
('94961793', 'OPEN0021', '2026-04-13 20:04:51'),
('94961793', 'OPEN0022', '2026-04-08 18:38:33'),
('94961793', 'OPEN0023', '2026-04-08 18:17:33'),
('94961793', 'OPEN0024', '2026-04-08 18:25:32'),
('94961793', 'OPEN0025', '2026-04-08 18:25:26'),
('94961793', 'OPEN0027', '2026-04-08 18:25:19'),
('94961793', 'TD0001', '2026-04-28 17:59:38'),
('94961793', 'TD0003', '2026-04-28 18:43:00'),
('94961793', 'TD0004', '2026-04-28 18:07:05'),
('94961793', 'TEST0001', '2026-04-16 11:23:07');

--
-- Déclencheurs `View`
--
DELIMITER $$
CREATE TRIGGER `AFTER_DELETE_VIEW` AFTER DELETE ON `View` FOR EACH ROW BEGIN
    UPDATE `Book` SET numberView=numberView-1 WHERE `idBook`=OLD.idBook;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `AFTER_INSERT_VIEW` AFTER INSERT ON `View` FOR EACH ROW BEGIN
    UPDATE `Book` SET numberView=numberView+1 WHERE `idBook`=NEW.idBook;
END
$$
DELIMITER ;

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `Agent`
--
ALTER TABLE `Agent`
  ADD PRIMARY KEY (`idAgent`);

--
-- Index pour la table `AgentAccount`
--
ALTER TABLE `AgentAccount`
  ADD PRIMARY KEY (`idAgentAccount`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `FK_AGENT_AGENTENTACCOUNT` (`idAgent`);

--
-- Index pour la table `Audio`
--
ALTER TABLE `Audio`
  ADD PRIMARY KEY (`idAudio`);

--
-- Index pour la table `Author`
--
ALTER TABLE `Author`
  ADD PRIMARY KEY (`idAuthor`);

--
-- Index pour la table `Book`
--
ALTER TABLE `Book`
  ADD PRIMARY KEY (`idBook`),
  ADD KEY `FK_AUTHOR_BOOK` (`idAuthor`);

--
-- Index pour la table `BookCategory`
--
ALTER TABLE `BookCategory`
  ADD PRIMARY KEY (`idBook`,`idCategory`),
  ADD KEY `FK_CATEGORY_BOOKCATEGORY` (`idCategory`);

--
-- Index pour la table `cache`
--
ALTER TABLE `cache`
  ADD PRIMARY KEY (`key`);

--
-- Index pour la table `cache_locks`
--
ALTER TABLE `cache_locks`
  ADD PRIMARY KEY (`key`);

--
-- Index pour la table `Category`
--
ALTER TABLE `Category`
  ADD PRIMARY KEY (`idCategory`);

--
-- Index pour la table `EdunaConversation`
--
ALTER TABLE `EdunaConversation`
  ADD PRIMARY KEY (`idConversation`);

--
-- Index pour la table `EdunaSession`
--
ALTER TABLE `EdunaSession`
  ADD PRIMARY KEY (`idSession`);

--
-- Index pour la table `failed_jobs`
--
ALTER TABLE `failed_jobs`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `failed_jobs_uuid_unique` (`uuid`);

--
-- Index pour la table `invitation_logs`
--
ALTER TABLE `invitation_logs`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `jobs`
--
ALTER TABLE `jobs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `jobs_queue_index` (`queue`);

--
-- Index pour la table `job_batches`
--
ALTER TABLE `job_batches`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `Like`
--
ALTER TABLE `Like`
  ADD PRIMARY KEY (`idNumber`,`idBook`),
  ADD KEY `FK_BOOK_LIKE` (`idBook`);

--
-- Index pour la table `Loand`
--
ALTER TABLE `Loand`
  ADD PRIMARY KEY (`idLoand`),
  ADD KEY `FK_RESERVATION_BORROWING` (`idReservation`),
  ADD KEY `FK_STRUCTURE_LOAND` (`idStruct`),
  ADD KEY `FK_USER_LOAND` (`idUser`);

--
-- Index pour la table `migrations`
--
ALTER TABLE `migrations`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `NoLike`
--
ALTER TABLE `NoLike`
  ADD PRIMARY KEY (`idNumber`,`idBook`),
  ADD KEY `FK_BOOK_NOLIKE` (`idBook`);

--
-- Index pour la table `Notification`
--
ALTER TABLE `Notification`
  ADD PRIMARY KEY (`idNotification`),
  ADD KEY `FK_STUDENT_NOTIFICATION` (`idNumber`);

--
-- Index pour la table `password_reset_tokens`
--
ALTER TABLE `password_reset_tokens`
  ADD PRIMARY KEY (`email`);

--
-- Index pour la table `personal_access_tokens`
--
ALTER TABLE `personal_access_tokens`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `personal_access_tokens_token_unique` (`token`),
  ADD KEY `personal_access_tokens_tokenable_type_tokenable_id_index` (`tokenable_type`,`tokenable_id`);

--
-- Index pour la table `Reservation`
--
ALTER TABLE `Reservation`
  ADD PRIMARY KEY (`idReservation`),
  ADD KEY `FK_STUDENT_RESERVATION` (`idNumber`),
  ADD KEY `FK_BOOK_RESERVATION` (`idBook`),
  ADD KEY `idx_reservation_idstruct` (`idStruct`),
  ADD KEY `idx_idNumber` (`idNumber`);

--
-- Index pour la table `Sanction`
--
ALTER TABLE `Sanction`
  ADD PRIMARY KEY (`idSanction`),
  ADD KEY `FK_STUDENT_SANCTION` (`idNumber`);

--
-- Index pour la table `sessions`
--
ALTER TABLE `sessions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `sessions_user_id_index` (`user_id`),
  ADD KEY `sessions_last_activity_index` (`last_activity`);

--
-- Index pour la table `StructBook`
--
ALTER TABLE `StructBook`
  ADD PRIMARY KEY (`idStruct`,`idBook`),
  ADD KEY `FK_Book_STRUCTBOOK` (`idBook`);

--
-- Index pour la table `Structure`
--
ALTER TABLE `Structure`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `StructureNotifications`
--
ALTER TABLE `StructureNotifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `structurenotifications_idstruct_foreign` (`idStruct`);

--
-- Index pour la table `StructUser`
--
ALTER TABLE `StructUser`
  ADD PRIMARY KEY (`idStruct`,`idUser`),
  ADD KEY `FK_USER_STRUCTUSER` (`idUser`);

--
-- Index pour la table `struct_agent`
--
ALTER TABLE `struct_agent`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idStruct` (`idStruct`);

--
-- Index pour la table `Student`
--
ALTER TABLE `Student`
  ADD PRIMARY KEY (`idNumber`);

--
-- Index pour la table `SubscribeBook`
--
ALTER TABLE `SubscribeBook`
  ADD PRIMARY KEY (`idNumber`,`idBook`),
  ADD KEY `FK_BOOK_SUSCRIBEBOOK` (`idBook`);

--
-- Index pour la table `SubscribeCategory`
--
ALTER TABLE `SubscribeCategory`
  ADD PRIMARY KEY (`idNumber`,`idCategory`),
  ADD KEY `FK_CATEGORY_SUSCRIBECATEGORY` (`idCategory`);

--
-- Index pour la table `User`
--
ALTER TABLE `User`
  ADD PRIMARY KEY (`idUser`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `phoneNumber` (`phoneNumber`);

--
-- Index pour la table `UserNotificationRead`
--
ALTER TABLE `UserNotificationRead`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `usernotificationread_unique` (`user_id`,`notification_id`),
  ADD KEY `usernotificationread_notificationid_foreign` (`notification_id`);

--
-- Index pour la table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `users_email_unique` (`email`);

--
-- Index pour la table `Version`
--
ALTER TABLE `Version`
  ADD PRIMARY KEY (`idVersion`);

--
-- Index pour la table `View`
--
ALTER TABLE `View`
  ADD PRIMARY KEY (`idNumber`,`idBook`),
  ADD KEY `FK_BOOK_VIEW` (`idBook`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `AgentAccount`
--
ALTER TABLE `AgentAccount`
  MODIFY `idAgentAccount` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `Audio`
--
ALTER TABLE `Audio`
  MODIFY `idAudio` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=57;

--
-- AUTO_INCREMENT pour la table `Author`
--
ALTER TABLE `Author`
  MODIFY `idAuthor` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=245;

--
-- AUTO_INCREMENT pour la table `Category`
--
ALTER TABLE `Category`
  MODIFY `idCategory` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=47;

--
-- AUTO_INCREMENT pour la table `EdunaConversation`
--
ALTER TABLE `EdunaConversation`
  MODIFY `idConversation` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=67;

--
-- AUTO_INCREMENT pour la table `EdunaSession`
--
ALTER TABLE `EdunaSession`
  MODIFY `idSession` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `failed_jobs`
--
ALTER TABLE `failed_jobs`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `invitation_logs`
--
ALTER TABLE `invitation_logs`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=66;

--
-- AUTO_INCREMENT pour la table `jobs`
--
ALTER TABLE `jobs`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `Loand`
--
ALTER TABLE `Loand`
  MODIFY `idLoand` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=69;

--
-- AUTO_INCREMENT pour la table `migrations`
--
ALTER TABLE `migrations`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT pour la table `Notification`
--
ALTER TABLE `Notification`
  MODIFY `idNotification` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=1040259;

--
-- AUTO_INCREMENT pour la table `personal_access_tokens`
--
ALTER TABLE `personal_access_tokens`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=544;

--
-- AUTO_INCREMENT pour la table `Reservation`
--
ALTER TABLE `Reservation`
  MODIFY `idReservation` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10083;

--
-- AUTO_INCREMENT pour la table `Sanction`
--
ALTER TABLE `Sanction`
  MODIFY `idSanction` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `Structure`
--
ALTER TABLE `Structure`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT pour la table `StructureNotifications`
--
ALTER TABLE `StructureNotifications`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `struct_agent`
--
ALTER TABLE `struct_agent`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=66;

--
-- AUTO_INCREMENT pour la table `UserNotificationRead`
--
ALTER TABLE `UserNotificationRead`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT pour la table `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `AgentAccount`
--
ALTER TABLE `AgentAccount`
  ADD CONSTRAINT `FK_AGENT_AGENTENTACCOUNT` FOREIGN KEY (`idAgent`) REFERENCES `Agent` (`idAgent`) ON DELETE CASCADE;

--
-- Contraintes pour la table `Book`
--
ALTER TABLE `Book`
  ADD CONSTRAINT `FK_AUTHOR_BOOK` FOREIGN KEY (`idAuthor`) REFERENCES `Author` (`idAuthor`) ON DELETE CASCADE;

--
-- Contraintes pour la table `BookCategory`
--
ALTER TABLE `BookCategory`
  ADD CONSTRAINT `FK_BOOK_BOOKCATEGORY` FOREIGN KEY (`idBook`) REFERENCES `Book` (`idBook`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_CATEGORY_BOOKCATEGORY` FOREIGN KEY (`idCategory`) REFERENCES `Category` (`idCategory`) ON DELETE CASCADE;

--
-- Contraintes pour la table `Like`
--
ALTER TABLE `Like`
  ADD CONSTRAINT `FK_BOOK_LIKE` FOREIGN KEY (`idBook`) REFERENCES `Book` (`idBook`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_USER_LIKE` FOREIGN KEY (`idNumber`) REFERENCES `User` (`idUser`) ON DELETE CASCADE;

--
-- Contraintes pour la table `Loand`
--
ALTER TABLE `Loand`
  ADD CONSTRAINT `FK_RESERVATION_BORROWING` FOREIGN KEY (`idReservation`) REFERENCES `Reservation` (`idReservation`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_STRUCTURE_LOAND` FOREIGN KEY (`idStruct`) REFERENCES `Structure` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `FK_USER_LOAND` FOREIGN KEY (`idUser`) REFERENCES `User` (`idUser`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `NoLike`
--
ALTER TABLE `NoLike`
  ADD CONSTRAINT `FK_BOOK_NOLIKE` FOREIGN KEY (`idBook`) REFERENCES `Book` (`idBook`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_USER_NOLIKE` FOREIGN KEY (`idNumber`) REFERENCES `User` (`idUser`) ON DELETE CASCADE;

--
-- Contraintes pour la table `Notification`
--
ALTER TABLE `Notification`
  ADD CONSTRAINT `FK_STUDENT_NOTIFICATION` FOREIGN KEY (`idNumber`) REFERENCES `Student` (`idNumber`) ON DELETE CASCADE;

--
-- Contraintes pour la table `Reservation`
--
ALTER TABLE `Reservation`
  ADD CONSTRAINT `FK_BOOK_RESERVATION` FOREIGN KEY (`idBook`) REFERENCES `Book` (`idBook`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_RESERVATION_STRUCTURE` FOREIGN KEY (`idStruct`) REFERENCES `Structure` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_USER_RESERVATION` FOREIGN KEY (`idNumber`) REFERENCES `User` (`idUser`) ON DELETE CASCADE;

--
-- Contraintes pour la table `Sanction`
--
ALTER TABLE `Sanction`
  ADD CONSTRAINT `FK_STUDENT_SANCTION` FOREIGN KEY (`idNumber`) REFERENCES `Student` (`idNumber`) ON DELETE CASCADE;

--
-- Contraintes pour la table `StructBook`
--
ALTER TABLE `StructBook`
  ADD CONSTRAINT `FK_Book_STRUCTBOOK` FOREIGN KEY (`idBook`) REFERENCES `Book` (`idBook`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_STRUCTURE_STRUCTBOOK` FOREIGN KEY (`idStruct`) REFERENCES `Structure` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `StructureNotifications`
--
ALTER TABLE `StructureNotifications`
  ADD CONSTRAINT `structurenotifications_idstruct_foreign` FOREIGN KEY (`idStruct`) REFERENCES `Structure` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `StructUser`
--
ALTER TABLE `StructUser`
  ADD CONSTRAINT `FK_STRUCTURE_STRUCTUSER` FOREIGN KEY (`idStruct`) REFERENCES `Structure` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_USER_STRUCTUSER` FOREIGN KEY (`idUser`) REFERENCES `User` (`idUser`) ON DELETE CASCADE;

--
-- Contraintes pour la table `struct_agent`
--
ALTER TABLE `struct_agent`
  ADD CONSTRAINT `struct_agent_ibfk_1` FOREIGN KEY (`idStruct`) REFERENCES `Structure` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `SubscribeBook`
--
ALTER TABLE `SubscribeBook`
  ADD CONSTRAINT `FK_BOOK_SUSCRIBEBOOK` FOREIGN KEY (`idBook`) REFERENCES `Book` (`idBook`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_USER_SUSCRIBEBOOK` FOREIGN KEY (`idNumber`) REFERENCES `User` (`idUser`) ON DELETE CASCADE;

--
-- Contraintes pour la table `SubscribeCategory`
--
ALTER TABLE `SubscribeCategory`
  ADD CONSTRAINT `FK_CATEGORY_SUSCRIBECATEGORY` FOREIGN KEY (`idCategory`) REFERENCES `Category` (`idCategory`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_STUDENT_SUSCRIBECATEGORY` FOREIGN KEY (`idNumber`) REFERENCES `Student` (`idNumber`) ON DELETE CASCADE;

--
-- Contraintes pour la table `UserNotificationRead`
--
ALTER TABLE `UserNotificationRead`
  ADD CONSTRAINT `usernotificationread_notificationid_foreign` FOREIGN KEY (`notification_id`) REFERENCES `StructureNotifications` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `usernotificationread_userid_foreign` FOREIGN KEY (`user_id`) REFERENCES `struct_agent` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `View`
--
ALTER TABLE `View`
  ADD CONSTRAINT `FK_BOOK_VIEW` FOREIGN KEY (`idBook`) REFERENCES `Book` (`idBook`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_USER_VIEW` FOREIGN KEY (`idNumber`) REFERENCES `User` (`idUser`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
