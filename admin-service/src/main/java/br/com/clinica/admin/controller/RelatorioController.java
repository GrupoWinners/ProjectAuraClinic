package br.com.clinica.admin.controller;

import br.com.clinica.admin.dto.resposta.RelatorioResposta;
import br.com.clinica.admin.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

// Controller REST responsável pelos endpoints de geração de relatórios gerenciais.
// Todos os endpoints são restritos ao perfil ADM — delegam lógica ao RelatorioService.
@RestController
@RequestMapping("/api/v1/relatorios")
@RequiredArgsConstructor
@Tag(name = "Relatórios Gerenciais", description = "Endpoints para geração de relatórios administrativos — acesso restrito ao ADM")
@SecurityRequirement(name = "bearerAuth")
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/consultas-por-periodo")
    @Operation(
        summary = "Consultas por período",
        description = "Retorna estatísticas de consultas realizadas no intervalo informado — requer integração Feign com agendamento-service"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
        @ApiResponse(responseCode = "403", description = "Sem permissão — apenas ADM")
    })
    public ResponseEntity<RelatorioResposta> consultasPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(relatorioService.consultasPorPeriodo(inicio, fim));
    }

    @GetMapping("/pacientes-cadastrados")
    @Operation(
        summary = "Pacientes cadastrados por período",
        description = "Retorna estatísticas de novos pacientes cadastrados no intervalo de datas informado"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
        @ApiResponse(responseCode = "403", description = "Sem permissão — apenas ADM")
    })
    public ResponseEntity<RelatorioResposta> pacientesCadastrados(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(relatorioService.relatoriosPacientesCadastrados(inicio, fim));
    }

    @GetMapping("/consultas-por-especialidade")
    @Operation(
        summary = "Médicos distribuídos por especialidade",
        description = "Retorna a contagem de médicos ativos agrupados por especialidade"
    )
    @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso")
    public ResponseEntity<RelatorioResposta> medicosporEspecialidade() {
        return ResponseEntity.ok(relatorioService.relatorioPorEspecialidade());
    }

    @GetMapping("/convenios-utilizados")
    @Operation(
        summary = "Convênios utilizados",
        description = "Retorna os convênios mais utilizados pelos pacientes cadastrados"
    )
    @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso")
    public ResponseEntity<RelatorioResposta> conveniosUtilizados() {
        return ResponseEntity.ok(relatorioService.relatorioConveniosUtilizados());
    }

    @GetMapping("/consultas-por-medico")
    @Operation(
        summary = "Dados de médico por período",
        description = "Retorna dados administrativos de um médico específico — para consultas do período, integre com agendamento-service"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Médico não encontrado")
    })
    public ResponseEntity<RelatorioResposta> dadosPorMedico(
            @RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(relatorioService.relatorioPorMedico(medicoId, inicio, fim));
    }
}
