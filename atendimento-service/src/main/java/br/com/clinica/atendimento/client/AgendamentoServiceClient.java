package br.com.clinica.atendimento.client;

import br.com.clinica.atendimento.client.dto.ConsultaClientResposta;
import br.com.clinica.atendimento.client.dto.ReconsultaClientRequisicao;
import br.com.clinica.atendimento.client.dto.ReconsultaClientResposta;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "agendamento-service",
        url = "${servicos.agendamento-service.url}"
)
public interface AgendamentoServiceClient {

    @GetMapping("/api/v1/consultas/{id}")
    ConsultaClientResposta buscarConsultaPorId(@PathVariable("id") Long id);

    @PostMapping("/api/v1/consultas")
    ReconsultaClientResposta criarReconsulta(@RequestBody ReconsultaClientRequisicao requisicao);
}