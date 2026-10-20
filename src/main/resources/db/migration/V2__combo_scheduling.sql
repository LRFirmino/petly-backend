-- =========================================================================
-- Opção B: combos passam a ser agendáveis, com duração e preço por porte,
-- e os agendamentos passam a referenciar um item de catálogo (serviço/combo).
-- Sintaxe portável (ALTER TABLE ADD/DROP COLUMN) aceita por SQLite e Postgres.
-- =========================================================================

-- Combos: mesmos atributos de agendamento que os serviços.
ALTER TABLE combos ADD COLUMN duration_minutes integer;
ALTER TABLE combos ADD COLUMN price_small      decimal(10, 2);
ALTER TABLE combos ADD COLUMN price_medium     decimal(10, 2);
ALTER TABLE combos ADD COLUMN price_large      decimal(10, 2);

-- Preserva o preço dos combos existentes: o antigo "a partir de" (menor preço)
-- passa a valer para todas as faixas de porte até o dono ajustar por porte.
UPDATE combos
   SET price_small  = price_from,
       price_medium = price_from,
       price_large  = price_from
 WHERE price_from IS NOT NULL;

-- "price_from" e "popularity" deixam de ser digitados:
--   * price_from    -> derivado do menor preço por porte;
--   * popularity    -> passa a ser a contagem real de agendamentos do combo.
ALTER TABLE combos DROP COLUMN price_from;
ALTER TABLE combos DROP COLUMN popularity;

-- Agendamento vinculado a um item de catálogo (polimórfico: SERVICE ou COMBO).
-- Sem FK única porque item_id aponta para services OU combos.
ALTER TABLE appointments ADD COLUMN item_type varchar(32);
ALTER TABLE appointments ADD COLUMN item_id   integer;
CREATE INDEX idx_appointments_item ON appointments (petshop_id, item_type, item_id);
