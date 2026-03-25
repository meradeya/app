package com.meradeya.app.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Meradeya API",
        version = "0.0.1",
        description = "Public-facing REST API for the Meradeya marketplace platform.",
        contact = @Contact(name = "Meradeya Team", url = "https://github.com/meradeya")
    ),
    servers = {
        @Server(url = "http://localhost:8080/", description = "Local development"),
        @Server(url = "https://api.staging.meradeya.com/", description = "Staging"),
        @Server(url = "https://api.meradeya.com/", description = "Production")
    },
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {

}
