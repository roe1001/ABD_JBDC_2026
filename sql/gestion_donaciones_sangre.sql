-- Limpieza
DROP SEQUENCE sec_tipo_sangre;
DROP SEQUENCE sec_hospital;
DROP SEQUENCE sec_donacion;
DROP SEQUENCE sec_traspaso;
DROP TABLE donacion CASCADE CONSTRAINTS;
DROP TABLE traspaso CASCADE CONSTRAINTS;
DROP TABLE reserva_hospital CASCADE CONSTRAINTS;
DROP TABLE donante CASCADE CONSTRAINTS;
DROP TABLE tipo_sangre CASCADE CONSTRAINTS;
DROP TABLE hospital CASCADE CONSTRAINTS;

-- Secuencias
CREATE SEQUENCE sec_tipo_sangre;
CREATE SEQUENCE sec_hospital;
CREATE SEQUENCE sec_donacion;
CREATE SEQUENCE sec_traspaso;

-- Tablas
CREATE TABLE hospital (
    id_hospital  INTEGER PRIMARY KEY,
    nombre       VARCHAR2(50) NOT NULL,
    localidad    VARCHAR2(50)
);

CREATE TABLE tipo_sangre (
    id_tipo_sangre INTEGER PRIMARY KEY,
    descripcion    VARCHAR2(10) NOT NULL
);

CREATE TABLE reserva_hospital (
    id_tipo_sangre INTEGER REFERENCES tipo_sangre,
    id_hospital    INTEGER REFERENCES hospital,
    cantidad       FLOAT,
    PRIMARY KEY (id_tipo_sangre, id_hospital)
);

CREATE TABLE donante (
    NIF              VARCHAR2(9) PRIMARY KEY,
    nombre           VARCHAR2(30) NOT NULL,
    ape1             VARCHAR2(30) NOT NULL,
    ape2             VARCHAR2(30),
    fecha_nacimiento DATE,
    id_tipo_sangre   INTEGER REFERENCES tipo_sangre
);

CREATE TABLE donacion (
    id_donacion    INTEGER PRIMARY KEY,
    nif_donante    VARCHAR2(9) REFERENCES donante,
    cantidad       FLOAT NOT NULL,
    fecha_donacion DATE NOT NULL
);

CREATE TABLE traspaso (
    id_traspaso     INTEGER PRIMARY KEY,
    id_hospital     INTEGER REFERENCES hospital,
    id_tipo_sangre  INTEGER REFERENCES tipo_sangre,
    cantidad        FLOAT NOT NULL,
    fecha_traspaso  DATE NOT NULL
);

-- Datos de ejemplo
INSERT INTO tipo_sangre VALUES (sec_tipo_sangre.nextval, 'A+');
INSERT INTO tipo_sangre VALUES (sec_tipo_sangre.nextval, 'B+');
INSERT INTO tipo_sangre VALUES (sec_tipo_sangre.nextval, 'O+');
INSERT INTO tipo_sangre VALUES (sec_tipo_sangre.nextval, 'AB+');

INSERT INTO hospital VALUES (sec_hospital.nextval, 'Hospital General', 'Burgos');
INSERT INTO hospital VALUES (sec_hospital.nextval, 'Hospital Universitario', 'Madrid');
INSERT INTO hospital VALUES (sec_hospital.nextval, 'Hospital del Mar', 'Barcelona');

-- Reservas iniciales (todas las combinaciones tipo x hospital)
INSERT INTO reserva_hospital VALUES (1, 1, 10.0);
INSERT INTO reserva_hospital VALUES (1, 2, 5.0);
INSERT INTO reserva_hospital VALUES (1, 3, 8.0);
INSERT INTO reserva_hospital VALUES (2, 1, 3.0);
INSERT INTO reserva_hospital VALUES (2, 2, 7.0);
INSERT INTO reserva_hospital VALUES (2, 3, 2.0);
INSERT INTO reserva_hospital VALUES (3, 1, 6.0);
INSERT INTO reserva_hospital VALUES (3, 2, 4.0);
INSERT INTO reserva_hospital VALUES (3, 3, 9.0);
INSERT INTO reserva_hospital VALUES (4, 1, 1.0);
INSERT INTO reserva_hospital VALUES (4, 2, 5.0);
INSERT INTO reserva_hospital VALUES (4, 3, 3.0);

INSERT INTO donante VALUES ('12345678A', 'Juan', 'Garcia', 'Lopez',  DATE '1990-01-15', 1);
INSERT INTO donante VALUES ('87654321B', 'Ana',  'Martinez', 'Ruiz', DATE '1985-06-20', 2);
INSERT INTO donante VALUES ('11111111C', 'Luis', 'Fernandez', 'Diaz',DATE '1978-03-10', 3);

-- Traspaso de ejemplo: hospital 1 recibe sangre tipo 1 desde hospital 2
INSERT INTO traspaso VALUES (sec_traspaso.nextval, 2, 1, 2.0, DATE '2026-01-10');
-- Actualizar reserva del hospital destino (1) sumando lo traspasado
-- (ya está reflejado en reserva_hospital de arriba)

COMMIT;
exit;
