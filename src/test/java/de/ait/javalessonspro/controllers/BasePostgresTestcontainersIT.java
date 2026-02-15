package de.ait.javalessonspro.controllers;

import org.junit.jupiter.api.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Base class for integration tests that require a PostgreSQL database
 * powered by Testcontainers. It provides a pre-configured PostgreSQL container
 * and dynamically overrides Spring Boot data source properties to point to that container.
 * <p>
 * Additionally, it creates a temporary directory for file uploads and registers it
 * as a Spring property, which can be used by tests that involve file storage.
 * </p>
 *
 * <p><strong>Usage:</strong> Extend this class in your integration test classes
 * (e.g., {@code CarApiPostgresIT}) and write tests as usual. The PostgreSQL container
 * will be started automatically before any tests run and stopped after the JVM exits.</p>
 *
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 15.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@Testcontainers
@DisplayName("Base Testcontainers configuration for PostgreSQL integration tests")
@Tag("testcontainers")
@Tag("postgres")
public class BasePostgresTestcontainersIT  {

    /**
     * Singleton PostgreSQL container configured with a test database name, username, and password.
     * The container is started once before all tests in the suite and is reused across test classes.
     */
    @Container
    protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cars_test_db")
            .withUsername("test")
            .withPassword("test");

    /**
     * Path to a temporary directory used for file uploads during tests.
     * Created before all tests and deleted after all tests.
     */
    protected static Path uploadRootDir;

    /**
     * Creates a temporary directory for file uploads before any tests are executed.
     *
     * @throws IOException if the directory cannot be created
     */
    @BeforeAll
    static void createUploadRootDir() throws IOException {
        uploadRootDir = Files.createTempDirectory("car-docs-upload-");
    }

    /**
     * Cleans up the temporary upload directory after all tests have finished.
     *
     * @throws IOException if the directory cannot be deleted
     */
    @AfterAll
    static void cleanUpUploadRootDir() throws IOException {
        Files.deleteIfExists(uploadRootDir);
    }

    /**
     * Dynamically overrides Spring Boot properties to use the Testcontainers PostgreSQL instance
     * and the temporary upload directory.
     * <p>
     * This method is automatically picked up by Spring's {@code @DynamicPropertySource} mechanism.
     * It registers the following properties:
     * <ul>
     *   <li>{@code spring.datasource.url} – JDBC URL of the running PostgreSQL container</li>
     *   <li>{@code spring.datasource.username} – database username</li>
     *   <li>{@code spring.datasource.password} – database password</li>
     *   <li>{@code spring.datasource.driver-class-name} – forces PostgreSQL driver</li>
     *   <li>{@code app.upload.root-dir} – path to the temporary upload directory</li>
     *   <li>{@code spring.jpa.hibernate.ddl-auto} – set to {@code validate} to prevent Hibernate
     *       from automatically creating/changing the schema (Liquibase is used instead)</li>
     * </ul>
     * </p>
     *
     * @param registry the dynamic property registry to which the properties are added
     */
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("app.upload.root-dir", () -> uploadRootDir.toString());
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }
}
