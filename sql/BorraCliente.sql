DROP SEQUENCE sec_clientes;
DROP SEQUENCE sec_pedidos;

DROP TABLE clientes CASCADE CONSTRAINTS;
DROP TABLE pedidos CASCADE CONSTRAINTS;

CREATE SEQUENCE sec_clientes;
CREATE SEQUENCE sec_pedidos;

CREATE TABLE clientes(
	idCliente	INTEGER PRIMARY KEY,
	nombre		CHAR(20) NOT NULL
);

CREATE TABLE pedidos(
	idPedido	INTEGER PRIMARY KEY,
	idCliente	INTEGER REFERENCES clientes NOT NULL,
	articulo	CHAR(10),
	cantidad	INTEGER,
	precio		NUMERIC(5,2)
);

INSERT INTO clientes VALUES( sec_clientes.nextval, 'PEPE');
INSERT INTO pedidos VALUES( sec_pedidos.nextval, sec_clientes.currval, 'ARTICULO 1', 5, 10);
INSERT INTO pedidos VALUES( sec_pedidos.nextval, sec_clientes.currval, 'ARTICULO 2', 5, 10);

INSERT INTO clientes VALUES( sec_clientes.nextval, 'ANA');

commit;

CREATE OR REPLACE PROCEDURE borrarCliente(p_idCliente IN INTEGER) AS
    v_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM clientes WHERE idCliente = p_idCliente;
    
    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'El cliente no existe');
    END IF;

    DELETE FROM clientes WHERE idCliente = p_idCliente;

    COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -2292 THEN
            RAISE_APPLICATION_ERROR(-20002, 'No se puede borrar el cliente porque tiene pedidos');
        ELSE
            RAISE;
        END IF;
END;
/

exit;