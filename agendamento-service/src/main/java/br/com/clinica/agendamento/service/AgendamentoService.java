package br.com.clinica.agendamento.service;

import br.com.clinica.agendamento.client.dto.AdminClient;
import br.com.clinica.agendamento.client.dto.ConvenioResponse;
import br.com.clinica.agendamento.dto.CancelamentoRequest;
import br.com.clinica.agendamento.dto.ConsultaRequest;
import br.com.clinica.agendamento.entity.Cancelamento;
import br.com.clinica.agendamento.entity.Consulta;
import br.com.clinica.agendamento.entity.StatusConsulta;
import br.com.clinica.agendamento.repository.CancelamentoRepository;
import br.com.clinica.agendamento.repository.ConsultaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final ConsultaRepository consultaRepository;
    private final CancelamentoRepository cancelamentoRepository;
    private final AdminClient adminClient;

    @Transactional
    public Consulta agendarConsulta(ConsultaRequest request) {
        try {
            ConvenioResponse convenio = adminClient.buscarConvenioPorId(request.getConvenioId());
            if (convenio == null || !convenio.isAtivo()) {
                throw new RuntimeException("Não é possível agendar: O convênio informado não está ativo.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao validar convênio no sistema de administração: " + e.getMessage());
        }

        boolean medicoOcupado = consultaRepository.existsByMedicoIdAndDataHoraAndStatusNot(
                request.getMedicoId(), request.getDataHora(), StatusConsulta.CANCELADA);
        if (medicoOcupado) {
            throw new RuntimeException("Conflito de horário: O médico já possui uma consulta agendada neste momento.");
        }

        boolean pacienteOcupado = consultaRepository.existsByPacienteIdAndDataHoraAndStatusNot(
                request.getPacienteId(), request.getDataHora(), StatusConsulta.CANCELADA);
        if (pacienteOcupado) {
            throw new RuntimeException("Conflito de horário: O paciente já possui uma consulta agendada neste momento.");
        }

        Consulta consulta = Consulta.builder()
                .pacienteId(request.getPacienteId())
                .medicoId(request.getMedicoId())
                .dataHora(request.getDataHora())
                .status(StatusConsulta.AGENDADA)
                .build();

        return consultaRepository.save(consulta);
    }

    // --- REQUISITO: REMARCAR CONSULTA & MÁQUINA DE ESTADOS ---
    @Transactional
    public Consulta remarcarConsulta(Long id, LocalDateTime novaDataHora) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consulta não encontrada."));

        // Regra da Máquina de Estados: Só pode remarcar se estiver AGENDADA ou REMARCADA
        if (consulta.getStatus() == StatusConsulta.CANCELADA || consulta.getStatus() == StatusConsulta.CONCLUIDA) {
            throw new RuntimeException("Não é possível remarcar uma consulta com status: " + consulta.getStatus());
        }

        // Validação de conflito para o novo horário (Médico)
        boolean medicoOcupado = consultaRepository.existsByMedicoIdAndDataHoraAndStatusNot(
                consulta.getMedicoId(), novaDataHora, StatusConsulta.CANCELADA);
        if (medicoOcupado) {
            throw new RuntimeException("Conflito de horário: O médico já possui uma consulta no novo horário solicitado.");
        }

        // Atualiza a data e muda o estado para REMARCADA
        consulta.setDataHora(novaDataHora);
        consulta.setStatus(StatusConsulta.REMARCADA);

        return consultaRepository.save(consulta);
    }

    // --- REQUISITO: CANCELAR CONSULTA COM MOTIVO OBRIGATÓRIO ---
    @Transactional
    public void cancelarConsulta(Long id, CancelamentoRequest request) {
        // Validação do motivo obrigatório
        if (request.getMotivo() == null || request.getMotivo().trim().isEmpty()) {
            throw new RuntimeException("O motivo do cancelamento é obrigatório.");
        }

        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consulta não encontrada."));

        // Regra da Máquina de Estados: Não se pode cancelar o que já foi cancelado ou concluído
        if (consulta.getStatus() == StatusConsulta.CANCELADA || consulta.getStatus() == StatusConsulta.CONCLUIDA) {
            throw new RuntimeException("Esta consulta não pode ser cancelada pois seu status atual é: " + consulta.getStatus());
        }

        // Altera o estado da consulta para CANCELADA
        consulta.setStatus(StatusConsulta.CANCELADA);
        consultaRepository.save(consulta);

        // Salva o registro do cancelamento com a justificativa obrigatória
        Cancelamento cancelamento = Cancelamento.builder()
                .consulta(consulta)
                .motivo(request.getMotivo())
                .dataHoraCancelamento(LocalDateTime.now())
                .build();

        cancelamentoRepository.save(cancelamento);
    }
}