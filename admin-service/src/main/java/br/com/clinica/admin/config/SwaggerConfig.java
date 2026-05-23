package br.com.clinica.admin.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

// Configuração central do Swagger/OpenAPI para o admin-service.
// Define metadados da API, servidor padrão e esquema de autenticação JWT Bearer.
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Admin Service API — Clínica Médica",
        description = "API de gerenciamento administrativo da clínica: médicos, pacientes, especialidades, convênios, usuários e relatórios.",
        version = "1.0.0",
        contact = @Contact(
            name = "Equipe de Desenvolvimento",
            email = "dev@clinica.com.br"
        ),
        license = @License(
            name = "MIT License"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8081", description = "Servidor de Desenvolvimento Local")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    description = "Token JWT obtido no endpoint POST /api/v1/auth/login — insira no formato: Bearer {token}",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {
    // Configuração declarativa via anotações — sem beans adicionais necessários com springdoc
}
