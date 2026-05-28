package br.com.clinica.atendimento.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI atendimentoServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Atendimento Service API")
                        .description("Microsserviço responsável pelo domínio clínico: atendimentos, prontuários, relatórios clínicos, encaminhamentos, receitas e solicitações de exame.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Projeto Aura Clinic")));
    }
}