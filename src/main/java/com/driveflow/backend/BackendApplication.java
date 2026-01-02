package com.driveflow.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		// Load .env file for local development (if it exists)
		// This won't override existing environment variables
		try {
			Dotenv dotenv = Dotenv.configure()
					.ignoreIfMissing()
					.load();
			// Set environment variables from .env file
			dotenv.entries().forEach(entry -> {
				if (System.getenv(entry.getKey()) == null) {
					System.setProperty(entry.getKey(), entry.getValue());
				}
			});
			System.out.println("✅ Loaded .env file for local development");
		} catch (Exception e) {
			// .env file is optional - only needed for local dev
			System.out.println("ℹ️  No .env file found (this is OK for production)");
		}
		
		// Parse DATABASE_URL from Render and set as system property before Spring Boot starts
		String databaseUrl = System.getenv("DATABASE_URL");
		if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
			try {
				java.net.URI dbUri = new java.net.URI(databaseUrl);
				String jdbcUrl = String.format("jdbc:postgresql://%s:%d%s",
					dbUri.getHost(),
					dbUri.getPort() == -1 ? 5432 : dbUri.getPort(),
					dbUri.getPath());
				
				String userInfo = dbUri.getUserInfo();
				if (userInfo != null && userInfo.contains(":")) {
					String[] credentials = userInfo.split(":", 2);
					System.setProperty("spring.datasource.url", jdbcUrl);
					System.setProperty("spring.datasource.username", credentials[0]);
					if (credentials.length > 1) {
						System.setProperty("spring.datasource.password", credentials[1]);
					}
					System.out.println("✅ Parsed DATABASE_URL from Render environment");
				}
			} catch (Exception e) {
				System.err.println("⚠️ Failed to parse DATABASE_URL: " + e.getMessage());
			}
		} else {
			System.out.println("ℹ️ DATABASE_URL not set or not in postgresql:// format, using application.properties defaults");
		}
		
		SpringApplication.run(BackendApplication.class, args);
	}

}
