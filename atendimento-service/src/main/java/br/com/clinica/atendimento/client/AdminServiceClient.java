package br.com.clinica.atendimento.client;

import br.com.clinica.atendimento.client.dto.MedicoClientResposta;
import br.com.clinica.atendimento.client.dto.PacienteClientResposta;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "admin-service",
        url = "${servicos.admin-service.url}"
)
public interface AdminServiceClient {

    @GetMapping("/api/v1/pacientes/{id}")
    PacienteClientResposta buscarPacientePorId(@PathVariable("id") Long id);

    @GetMapping("/api/v1/medicos/{id}")
    MedicoClientResposta buscarMedicoPorId(@PathVariable("id") Long id);
}