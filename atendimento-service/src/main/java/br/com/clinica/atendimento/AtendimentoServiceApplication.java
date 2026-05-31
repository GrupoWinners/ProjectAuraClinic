package br.com.clinica.atendimento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "br.com.clinica")
@EnableFeignClients(basePackages = "br.com.clinica.atendimento.client")
public class AtendimentoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtendimentoServiceApplication.class, args);
    }
}