package com.pawsclaws.petshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que as migrations do Flyway (script do Turso/SQLite) aplicam
 * corretamente e que o schema resultante é utilizável pelo Hibernate.
 *
 * <p>O perfil {@code test} usa SQLite em memória com o Flyway ligado e
 * {@code ddl-auto: none} — ou seja, o schema vem 100% das migrations.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
class MigrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void migrationsCreateAllTables() {
        List<String> tables = jdbc
                .queryForList("select name from sqlite_master where type = 'table'", String.class)
                .stream()
                .map(String::toLowerCase)
                .toList();

        assertThat(tables).contains(
                "flyway_schema_history",
                "petshops", "users", "clients", "pets",
                "services", "combos", "team_members", "appointments");
    }

    @Test
    void schemaIsUsableByTheApplication() {
        // O TestDataSeeder grava via JPA nas tabelas criadas pelo Flyway.
        assertThat(jdbc.queryForObject("select count(*) from petshops", Long.class)).isPositive();
        assertThat(jdbc.queryForObject("select count(*) from users", Long.class)).isPositive();
        assertThat(jdbc.queryForObject("select count(*) from appointments", Long.class)).isPositive();
        assertThat(jdbc.queryForObject("select count(*) from services", Long.class)).isPositive();
    }
}
