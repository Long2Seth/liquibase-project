package co.istad.testliquibase;

import co.istad.testliquibase.config.DataMigrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestLiquibaseApplication implements CommandLineRunner {

    private final DataMigrationService migrationService;

    public TestLiquibaseApplication(DataMigrationService migrationService) {
        this.migrationService = migrationService;
    }

    public static void main(String[] args) {
        SpringApplication.run(TestLiquibaseApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        migrationService.migrateData();
    }
}