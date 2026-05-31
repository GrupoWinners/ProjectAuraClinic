package br.com.clinica.agendamento.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "admin-service",
        url = "${servicos.admin-service.url}")
public interface AdminServiceClient {

    @GetMapping("/api/v1/pacientes/{id}/validar-ativo")
    Boolean validarPacienteAtivo(@PathVariable("id") Long id);

    @GetMapping("/api/v1/medicos/{id}/validar-ativo")
    Boolean validarMedicoAtivo(@PathVariable("id") Long id);

    @GetMapping("/api/v1/convenios/{id}/validar-ativo")
    Boolean validarConvenioAtivo(@PathVariable("id") Long id);
}