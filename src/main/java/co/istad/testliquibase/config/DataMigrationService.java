// DataMigrationService.java
package co.istad.testliquibase.config;

import co.istad.testliquibase.entities.Role;
import co.istad.testliquibase.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataMigrationService {

    private static final Logger logger = LoggerFactory.getLogger(DataMigrationService.class);

    @Autowired
    @Qualifier("primaryJdbcTemplate")
    private JdbcTemplate primaryJdbcTemplate; // for postgresql14_db

    @Autowired
    @Qualifier("secondaryJdbcTemplate")
    private JdbcTemplate secondaryJdbcTemplate; // for postgresql16_db

    public void migrateData() {

        // Step 1: Fetch all users and roles from the old database
        List<User> users = primaryJdbcTemplate.query("SELECT * FROM users", new BeanPropertyRowMapper<>(User.class));
        List<Role> roles = primaryJdbcTemplate.query("SELECT * FROM roles", new BeanPropertyRowMapper<>(Role.class));

        logger.info("Fetched {} users from postgresql14_db", users.size());
        logger.info("Fetched {} roles from postgresql14_db", roles.size());

        // Step 2: Insert users and roles into the new database
        for (Role role : roles) {
            // Check if the role already exists
            Integer count = secondaryJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM roles WHERE id = ?", Integer.class, role.getId());
            if (count == null || count == 0) {
                logger.info("Inserting role: {}", role);
                secondaryJdbcTemplate.update("INSERT INTO roles (id, role_name, role_description) VALUES (?, ?, ?)",
                        role.getId(), role.getRoleName(), role.getRoleDescription());
            } else {
                logger.info("Role with id {} already exists, skipping insertion", role.getId());
            }
        }

        for (User user : users) {
            // Check if the user already exists
            Integer count = secondaryJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, user.getId());
            if (count == null || count == 0) {
                logger.info("Inserting user: {}", user);
                secondaryJdbcTemplate.update("INSERT INTO users (id, name, email, password) VALUES (?, ?, ?, ?)",
                        user.getId(), user.getName(), user.getEmail(), user.getPassword());

                // Insert user roles in the new database
                for (Role role : user.getRoles()) {
                    logger.info("Inserting user role mapping: userId={}, roleId={}", user.getId(), role.getId());
                    secondaryJdbcTemplate.update("INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)",
                            user.getId(), role.getId());
                }
            } else {
                logger.info("User with id {} already exists, skipping insertion", user.getId());
            }
        }
    }
}