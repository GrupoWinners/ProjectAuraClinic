package br.com.clinica.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Configuração global de CORS e MVC para o admin-service.
// Define as origens permitidas e os métodos HTTP aceitos para requisições cross-origin.
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Permite todas as origens durante o desenvolvimento — restringir em produção via application-prod.yml
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:3000",   // Frontend React/Next.js local
                    "http://localhost:4200",   // Angular local
                    "http://localhost:8080"    // API Gateway local
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // Expõe o header Authorization para que o cliente possa ler o token JWT
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600); // Cache da resposta preflight por 1 hora
    }
}
