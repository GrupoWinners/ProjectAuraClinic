package br.com.clinica.agendamento.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI agendamentoServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Agendamento Service API")
                        .description("Microsserviço responsável pelo gerenciamento de agendamentos, remarcações e cancelamentos de consultas médicas.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Projeto Aura Clinic")));
    }
}
