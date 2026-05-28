package br.com.clinica.atendimento.controller;

import br.com.clinica.atendimento.dto.requisicao.AtualizarStatusEncaminhamentoRequisicao;
import br.com.clinica.atendimento.dto.requisicao.EncaminhamentoRequisicao;
import br.com.clinica.atendimento.dto.resposta.EncaminhamentoResposta;
import br.com.clinica.atendimento.service.EncaminhamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/encaminhamentos")
@RequiredArgsConstructor
public class EncaminhamentoController {

    private final EncaminhamentoService encaminhamentoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EncaminhamentoResposta criar(@RequestBody @Valid EncaminhamentoRequisicao requisicao) {
        return encaminhamentoService.criar(requisicao);
    }

    @GetMapping("/{id}")
    public EncaminhamentoResposta buscarPorId(@PathVariable Long id) {
        return encaminhamentoService.buscarPorId(id);
    }

    @GetMapping("/medico-destino/{medicoId}")
    public List<EncaminhamentoResposta> listarPorMedicoDestino(@PathVariable Long medicoId) {
        return encaminhamentoService.listarPorMedicoDestino(medicoId);
    }

    @PutMapping("/{id}/status")
    public EncaminhamentoResposta atualizarStatus(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarStatusEncaminhamentoRequisicao requisicao
    ) {
        return encaminhamentoService.atualizarStatus(id, requisicao);
    }
}