package com.ptit.tour.schema;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class FlywayMigrationTest {

    @Test
    void mysqlMigrationsShouldApplyOnEmptyDatabase() {
        String url = System.getenv("PTOUR_TEST_MYSQL_URL");
        String username = System.getenv().getOrDefault("PTOUR_TEST_MYSQL_USER", "root");
        String password = System.getenv().getOrDefault("PTOUR_TEST_MYSQL_PASSWORD", "");
        assumeTrue(url != null && !url.isBlank(),
            "Set PTOUR_TEST_MYSQL_URL to run Flyway migrate verification against an empty MySQL database");

        Flyway flyway = Flyway.configure()
            .dataSource(url, username, password)
            .locations("classpath:db/migration")
            .cleanDisabled(false)
            .load();

        flyway.clean();
        var result = flyway.migrate();

        assertThat(result.migrationsExecuted).isGreaterThan(0);
    }
}
