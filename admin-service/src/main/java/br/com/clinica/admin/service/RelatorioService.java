package br.com.clinica.admin.service;

import br.com.clinica.admin.dto.resposta.RelatorioResposta;
import br.com.clinica.admin.entity.Medico;
import br.com.clinica.admin.entity.Paciente;
import br.com.clinica.admin.exception.RecursoNaoEncontradoException;
import br.com.clinica.admin.repository.ConvenioRepository;
import br.com.clinica.admin.repository.EspecialidadeRepository;
import br.com.clinica.admin.repository.MedicoRepository;
import br.com.clinica.admin.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Service responsável pela geração de relatórios gerenciais administrativos.
// Agrega dados dos repositórios locais — para dados de consultas, depende do agendamento-service via Feign.
@Service
@RequiredArgsConstructor
@Slf4j
public class RelatorioService {

    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final ConvenioRepository convenioRepository;

    // Relatório de consultas realizadas em um período — dados virão do agendamento-service via Feign
    @Transactional(readOnly = true)
    public RelatorioResposta consultasPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        log.info("Gerando relatório de consultas por período entre {} e {}", inicio, fim);

        // Estrutura preparada para integração Feign com agendamento-service (Regra #8 da documentação)
        // O admin-service não possui tabela de consultas — os dados pertencem ao agendamento-service
        Map<String, Object> info = Map.of(
                "inicio", inicio.toString(),
                "fim", fim.toString(),
                "status", "Integração com agendamento-service via Feign ainda não configurada",
                "instrucao", "Injete AgendamentoFeignClient e chame /api/v1/agendamentos/relatorio?inicio=&fim="
        );

        return RelatorioResposta.builder()
                .tipoRelatorio("CONSULTAS_POR_PERIODO")
                .inicio(inicio)
                .fim(fim)
                .totalRegistros(0L)
                .descricao("Consultas realizadas no período (requer Feign com agendamento-service)")
                .dados(info)
                .geradoEm(LocalDateTime.now())
                .build();
    }

    // Relatório de pacientes cadastrados em um período específico
    @Transactional(readOnly = true)
    public RelatorioResposta relatoriosPacientesCadastrados(LocalDateTime inicio, LocalDateTime fim) {
        log.info("Gerando relatório de pacientes cadastrados entre {} e {}", inicio, fim);

        // Busca todos os pacientes ativos e filtra pelo período de criação
        List<Paciente> pacientes = pacienteRepository.findAll().stream()
                .filter(p -> p.getCriadoEm() != null
                        && !p.getCriadoEm().isBefore(inicio)
                        && !p.getCriadoEm().isAfter(fim))
                .toList();

        // Agrupa pacientes por mês para análise temporal
        Map<String, Long> porMes = pacientes.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCriadoEm().getYear() + "-" + String.format("%02d", p.getCriadoEm().getMonthValue()),
                        Collectors.counting()));

        return RelatorioResposta.builder()
                .tipoRelatorio("PACIENTES_CADASTRADOS")
                .inicio(inicio)
                .fim(fim)
                .totalRegistros((long) pacientes.size())
                .descricao("Relatório de pacientes cadastrados no período")
                .dados(porMes)
                .geradoEm(LocalDateTime.now())
                .build();
    }

    // Relatório de médicos ativos agrupados por especialidade
    @Transactional(readOnly = true)
    public RelatorioResposta relatorioPorEspecialidade() {
        log.info("Gerando relatório de médicos por especialidade");

        // Agrega a quantidade de médicos ativos em cada especialidade
        Map<String, Long> medicosPorEspecialidade = medicoRepository
                .findByAtivo(true, Pageable.unpaged())
                .stream()
                .filter(m -> m.getEspecialidade() != null)
                .collect(Collectors.groupingBy(
                        m -> m.getEspecialidade().getDescricao(),
                        Collectors.counting()));

        return RelatorioResposta.builder()
                .tipoRelatorio("MEDICOS_POR_ESPECIALIDADE")
                .totalRegistros(medicosPorEspecialidade.values().stream().mapToLong(Long::longValue).sum())
                .descricao("Distribuição de médicos ativos por especialidade")
                .dados(medicosPorEspecialidade)
                .geradoEm(LocalDateTime.now())
                .build();
    }

    // Relatório de convênios utilizados pelos pacientes cadastrados
    @Transactional(readOnly = true)
    public RelatorioResposta relatorioConveniosUtilizados() {
        log.info("Gerando relatório de convênios utilizados");

        // Conta quantos pacientes ativos estão vinculados a cada convênio
        Map<String, Long> conveniosPorPacientes = pacienteRepository.findByAtivo(true, Pageable.unpaged())
                .stream()
                .filter(p -> Boolean.TRUE.equals(p.getPossuiConvenio()) && p.getConvenio() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getConvenio().getNomeEmpresa(),
                        Collectors.counting()));

        return RelatorioResposta.builder()
                .tipoRelatorio("CONVENIOS_UTILIZADOS")
                .totalRegistros(conveniosPorPacientes.values().stream().mapToLong(Long::longValue).sum())
                .descricao("Convênios mais utilizados pelos pacientes cadastrados")
                .dados(conveniosPorPacientes)
                .geradoEm(LocalDateTime.now())
                .build();
    }

    // Relatório resumo de médico específico com dados do período
    @Transactional(readOnly = true)
    public RelatorioResposta relatorioPorMedico(Long medicoId, LocalDateTime inicio, LocalDateTime fim) {
        log.info("Gerando relatório para médico ID: {}", medicoId);

        // Valida a existência do médico antes de gerar o relatório
        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Médico com ID " + medicoId + " não encontrado"));

        Map<String, Object> dadosMedico = Map.of(
                "medicoId", medico.getId(),
                "nomeCompleto", medico.getNomeCompleto(),
                "crm", medico.getCrm(),
                "especialidade", medico.getEspecialidade() != null ? medico.getEspecialidade().getDescricao() : "N/A",
                "ativo", medico.getAtivo(),
                "observacao", "Para consultas do período, integrar com agendamento-service via Feign"
        );

        return RelatorioResposta.builder()
                .tipoRelatorio("DADOS_MEDICO")
                .inicio(inicio)
                .fim(fim)
                .descricao("Dados do médico: " + medico.getNomeCompleto())
                .dados(dadosMedico)
                .geradoEm(LocalDateTime.now())
                .build();
    }
}
