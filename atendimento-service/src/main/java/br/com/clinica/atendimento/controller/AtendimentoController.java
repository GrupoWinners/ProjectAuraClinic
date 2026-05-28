package br.com.clinica.atendimento.controller;

import br.com.clinica.atendimento.dto.requisicao.AtendimentoRequisicao;
import br.com.clinica.atendimento.dto.requisicao.AtualizarUrgenciaRequisicao;
import br.com.clinica.atendimento.dto.resposta.AtendimentoResposta;
import br.com.clinica.atendimento.service.AtendimentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/atendimentos")
@RequiredArgsConstructor
public class AtendimentoController {

    private final AtendimentoService atendimentoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AtendimentoResposta registrar(@RequestBody @Valid AtendimentoRequisicao requisicao) {
        return atendimentoService.registrar(requisicao);
    }

    @GetMapping("/{id}")
    public AtendimentoResposta buscarPorId(@PathVariable Long id) {
        return atendimentoService.buscarPorId(id);
    }

    @GetMapping("/paciente/{pacienteId}")
    public List<AtendimentoResposta> listarPorPaciente(@PathVariable Long pacienteId) {
        return atendimentoService.listarPorPaciente(pacienteId);
    }

    @PutMapping("/{id}")
    public AtendimentoResposta atualizar(
            @PathVariable Long id,
            @RequestBody @Valid AtendimentoRequisicao requisicao
    ) {
        return atendimentoService.atualizar(id, requisicao);
    }

    @PutMapping("/{id}/urgencia")
    public AtendimentoResposta atualizarUrgencia(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarUrgenciaRequisicao requisicao
    ) {
        return atendimentoService.atualizarUrgencia(id, requisicao);
    }
}