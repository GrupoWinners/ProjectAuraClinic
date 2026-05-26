package br.com.clinica.agendamento.service;

import br.com.clinica.agendamento.client.AdminServiceClient;
import br.com.clinica.agendamento.entity.Cancelamento;
import br.com.clinica.agendamento.entity.Consulta;
import br.com.clinica.agendamento.entity.StatusConsulta;
import br.com.clinica.agendamento.entity.TipoConsulta;
import br.com.clinica.agendamento.repository.CancelamentoRepository;
import br.com.clinica.agendamento.repository.ConsultaRepository;
import br.com.clinica.agendamento.dto.ConsultaRequisicao;
import br.com.clinica.agendamento.dto.CancelamentoRequisicao;
import br.com.clinica.agendamento.dto.ConsultaResposta;

import br.com.clinica.commons.exception.RegraDeNegocioException;
import br.com.clinica.commons.exception.RecursoNaoEncontradoException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final ConsultaRepository consultaRepository;
    private final CancelamentoRepository cancelamentoRepository;
    private final AdminServiceClient adminServiceClient;

    @Transactional
    public ConsultaResposta agendarConsulta(ConsultaRequisicao requisicao) {
        Boolean pacienteAtivo = adminServiceClient.validarPacienteAtivo(requisicao.getPacienteId());
        if (pacienteAtivo == null || !pacienteAtivo) {
            throw new RegraDeNegocioException("O paciente informado não está ativo no sistema.");
        }

        Boolean medicoAtivo = adminServiceClient.validarMedicoAtivo(requisicao.getMedicoId());
        if (medicoAtivo == null || !medicoAtivo) {
            throw new RegraDeNegocioException("O médico informado não está ativo no sistema.");
        }

        if (requisicao.getConvenioId() != null) {
            Boolean convenioAtivo = adminServiceClient.validarConvenioAtivo(requisicao.getConvenioId());
            if (convenioAtivo == null || !convenioAtivo) {
                throw new RegraDeNegocioException("O convênio informado não está ativo no sistema.");
            }
        }

        boolean medicoOcupado = consultaRepository.existsByMedicoIdAndDataHoraAndStatusNot(
                requisicao.getMedicoId(),
                requisicao.getDataHora(),
                StatusConsulta.CANCELADA
        );
        if (medicoOcupado) {
            throw new RegraDeNegocioException("O médico já possui uma consulta agendada para este horário.");
        }

        boolean pacienteOcupado = consultaRepository.existsByPacienteIdAndDataHoraAndStatusNot(
                requisicao.getPacienteId(),
                requisicao.getDataHora(),
                StatusConsulta.CANCELADA
        );
        if (pacienteOcupado) {
            throw new RegraDeNegocioException("O paciente já possui uma consulta agendada para este horário.");
        }

        Consulta consulta = new Consulta();
        consulta.setPacienteId(requisicao.getPacienteId());
        consulta.setMedicoId(requisicao.getMedicoId());
        consulta.setDataHora(requisicao.getDataHora());
        consulta.setTipo(TipoConsulta.valueOf(requisicao.getTipo().toUpperCase()));
        consulta.setStatus(StatusConsulta.AGENDADA);

        Consulta consultaSalva = consultaRepository.save(consulta);
        return new ConsultaResposta(consultaSalva);
    }

    @Transactional
    public ConsultaResposta remarcarConsulta(Long id, ConsultaRequisicao requisicao) {
        Consulta consultaOriginal = consultaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada para o ID informado."));

        if (consultaOriginal.getStatus() == StatusConsulta.CANCELADA) {
            throw new RegraDeNegocioException("Não é possível remarcar uma consulta que já está cancelada.");
        }

        LocalDateTime novaData = requisicao.getNovaDataHora();

        boolean medicoOcupado = consultaRepository.existsByMedicoIdAndDataHoraAndStatusNot(
                consultaOriginal.getMedicoId(),
                novaData,
                StatusConsulta.CANCELADA
        );
        if (medicoOcupado) {
            throw new RegraDeNegocioException("O médico já possui uma consulta no horário solicitado para a remarcação.");
        }

        boolean pacienteOcupado = consultaRepository.existsByPacienteIdAndDataHoraAndStatusNot(
                consultaOriginal.getPacienteId(),
                novaData,
                StatusConsulta.CANCELADA
        );
        if (pacienteOcupado) {
            throw new RegraDeNegocioException("O paciente já possui uma consulta no horário solicitado para a remarcação.");
        }

        consultaOriginal.setStatus(StatusConsulta.CANCELADA);
        consultaRepository.save(consultaOriginal);

        Consulta novaConsulta = new Consulta();
        novaConsulta.setPacienteId(consultaOriginal.getPacienteId());
        novaConsulta.setMedicoId(consultaOriginal.getMedicoId());
        novaConsulta.setDataHora(novaData);
        novaConsulta.setTipo(consultaOriginal.getTipo());
        novaConsulta.setStatus(StatusConsulta.REMARCADA);
        novaConsulta.setConsultaOriginalId(consultaOriginal.getId());

        Consulta consultaSalva = consultaRepository.save(novaConsulta);
        return new ConsultaResposta(consultaSalva);
    }

    @Transactional
    public void cancelarConsulta(Long id, CancelamentoRequisicao requisicao) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada para o ID informado."));

        if (consulta.getStatus() == StatusConsulta.CANCELADA) {
            throw new RegraDeNegocioException("Esta consulta já se encontra cancelada.");
        }

        consulta.setStatus(StatusConsulta.CANCELADA);
        consultaRepository.save(consulta);

        Cancelamento cancelamento = new Cancelamento();
        cancelamento.setConsulta(consulta);
        cancelamento.setMotivo(requisicao.getMotivo());
        cancelamento.setCanceladoPor(requisicao.getCanceladoPor());

        cancelamentoRepository.save(cancelamento);
    }

    public List<ConsultaResposta> listarConsultas() {
        return consultaRepository.findAll().stream()
                .map(ConsultaResposta::new)
                .collect(Collectors.toList());
    }

    public ConsultaResposta buscarPorId(Long id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada."));
        return new ConsultaResposta(consulta);
    }
}