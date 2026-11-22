ALTER TABLE reservas
DROP CONSTRAINT ck_reservas_status;

ALTER TABLE reservas
ADD CONSTRAINT ck_reservas_status
CHECK (
    status IN (
        'PENDENTE',
        'CONFIRMADA',
        'CANCELADA',
        'CONCLUIDA'
    )
);