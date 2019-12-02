DELIMITER //
    CREATE PROCEDURE crearTaules()
    BEGIN
        CREATE TABLE IF NOT EXISTS Classificacio
        (
            posicio int(11) PRIMARY KEY,
            equip varchar(50),
            victoria int(11),
            derrota int(11),
            empat int(11),
            punts int(11)
        );
        CREATE TABLE IF NOT EXISTS Partits
        (
            id_partit int(11) PRIMARY KEY AUTO_INCREMENT,
            equip_a varchar(50),
            gols_a int(11),
            equip_b varchar(50),
            gols_b int(11)
        );
        CREATE TABLE IF NOT EXISTS Equips
        (
            id_equip int(11) PRIMARY KEY,
            nom_equip varchar(50)
        );
        CREATE TABLE IF NOT EXISTS Jugadors
        (
            id_jugador int(11) PRIMARY KEY,
            nom_jugador varchar(50),
            posicio varchar(50),
            id_equip int(11),
            nom_equip varchar(50),
            FOREIGN KEY (id_equip) REFERENCES Equips(id_equip)
        );
        ALTER TABLE Equips
        ADD COLUMN qualitat int(11);
    END;
//

DELIMITER //
    CREATE PROCEDURE insertarEquips
        (IN `id_equip` int(11), IN `nom_equip` varchar(50), IN `qualitat` int(11))
        BEGIN
            INSERT INTO Equips (id_equip, nom_equip, qualitat)
            VALUES (id_equip, nom_equip, qualitat);
    END;
//

DELIMITER //
    CREATE PROCEDURE insertarJugadors
        (IN `id_jugador` int(11), IN `nom_jugador` varchar(50), IN `posicio` varchar(50), IN `id_equip` int(11), IN `nom_equip` varchar(50))
        BEGIN
            INSERT INTO Jugadors (id_jugador, nom_jugador, posicio, id_equip, nom_equip)
            VALUES (id_jugador, nom_jugador, posicio, id_equip, nom_equip);
    END;
//

DELIMITER //
    CREATE PROCEDURE insertarClassificacio
        (IN `posicio` int(11), IN `equip` varchar(50), IN `victoria` int(11), IN `derrota` int(11), IN `empat` int(11), IN `punts` int(11))
        BEGIN
            INSERT INTO Classificacio (posicio, equip, victoria, derrota, empat, punts)
            VALUES (posicio, equip, victoria, derrota, empat, punts);
    END;
//


DELIMITER //
    CREATE PROCEDURE insertarPartits
        (IN `equip_a` varchar(50), IN `gols_a` int(11), IN `equip_b` varchar(50), IN `gols_b` int(11))
        BEGIN
            INSERT INTO Partits (equip_a, gols_a, equip_b, gols_b)
            VALUES (equip_a, gols_a, equip_b, gols_b);
    END;
//