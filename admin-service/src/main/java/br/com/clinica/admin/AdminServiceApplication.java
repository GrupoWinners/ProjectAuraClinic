package br.com.clinica.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Classe principal de inicialização do admin-service — módulo administrativo da clínica médica.
// Responsável pelo gerenciamento de médicos, pacientes, especialidades, convênios, usuários e relatórios.
@SpringBootApplication
public class AdminServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
    }
}