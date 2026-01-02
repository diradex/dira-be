package com.driveflow.backend.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        DataSourceProperties properties = new DataSourceProperties();
        
        // Check if DATABASE_URL environment variable is set (Render format)
        String databaseUrl = System.getenv("DATABASE_URL");
        
        // Log for debugging (will appear in Render logs)
        System.out.println("DATABASE_URL from env: " + (databaseUrl != null ? "SET" : "NOT SET"));
        if (databaseUrl != null) {
            System.out.println("DATABASE_URL value: " + databaseUrl.substring(0, Math.min(50, databaseUrl.length())) + "...");
        }
        
        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            try {
                URI dbUri = new URI(databaseUrl);
                String dbUrl = String.format("jdbc:postgresql://%s:%d%s",
                    dbUri.getHost(),
                    dbUri.getPort() == -1 ? 5432 : dbUri.getPort(),
                    dbUri.getPath());
                
                System.out.println("Parsed JDBC URL: " + dbUrl);
                properties.setUrl(dbUrl);
                
                String userInfo = dbUri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] credentials = userInfo.split(":", 2);
                    properties.setUsername(credentials[0]);
                    if (credentials.length > 1) {
                        properties.setPassword(credentials[1]);
                    }
                    System.out.println("Database username: " + credentials[0]);
                }
            } catch (Exception e) {
                System.err.println("Failed to parse DATABASE_URL: " + databaseUrl);
                e.printStackTrace();
                throw new RuntimeException("Failed to parse DATABASE_URL: " + databaseUrl, e);
            }
        } else {
            System.out.println("Using application.properties defaults (DATABASE_URL not in postgresql:// format)");
        }
        
        return properties;
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        System.out.println("Creating DataSource with URL: " + properties.getUrl());
        return properties.initializeDataSourceBuilder().build();
    }
}

