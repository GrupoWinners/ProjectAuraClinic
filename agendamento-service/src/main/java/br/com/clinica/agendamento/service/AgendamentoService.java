package br.com.clinica.agendamento.service;

import br.com.clinica.agendamento.entity.Cancelamento;
import br.com.clinica.agendamento.entity.Consulta;
import br.com.clinica.agendamento.entity.StatusConsulta;
import br.com.clinica.agendamento.entity.TipoConsulta;
import br.com.clinica.agendamento.repository.CancelamentoRepository;
import br.com.clinica.agendamento.repository.ConsultaRepository;
import br.com.clinica.agendamento.dto.ConsultaRequest;
import br.com.clinica.agendamento.dto.CancelamentoRequest;

import br.com.clinica.commons.exception.RegraDeNegocioException;
import br.com.clinica.commons.exception.RecursoNaoEncontradoException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final ConsultaRepository consultaRepository;
    private final CancelamentoRepository cancelamentoRepository;

    @Transactional
    public Consulta agendarConsulta(ConsultaRequest request) {
        boolean horarioOcupado = consultaRepository.existsByMedicoIdAndDataHoraAndStatusNot(
                request.getMedicoId(),
                request.getDataHora(),
                StatusConsulta.CANCELADA
        );

        if (horarioOcupado) {
            throw new RegraDeNegocioException("O médico já possui uma consulta agendada para este horário.");
        }

        Consulta consulta = new Consulta();
        consulta.setPacienteId(request.getPacienteId());
        consulta.setMedicoId(request.getMedicoId());
        consulta.setDataHora(request.getDataHora());
        consulta.setTipo(TipoConsulta.valueOf(request.getTipo().toUpperCase()));
        consulta.setStatus(StatusConsulta.AGENDADA);

        return consultaRepository.save(consulta);
    }

    @Transactional
    public Consulta remarcarConsulta(Long id, ConsultaRequest request) {
        Consulta consultaOriginal = consultaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada para o ID informado."));

        if (consultaOriginal.getStatus() == StatusConsulta.CANCELADA) {
            throw new RegraDeNegocioException("Não é possível remarcar uma consulta que já está cancelada.");
        }

        // Regra de Negócio 10: Altera a original para CANCELADA
        consultaOriginal.setStatus(StatusConsulta.CANCELADA);
        consultaRepository.save(consultaOriginal);

        // Cria o novo registro imutável
        Consulta novaConsulta = new Consulta();
        novaConsulta.setPacienteId(consultaOriginal.getPacienteId());
        novaConsulta.setMedicoId(consultaOriginal.getMedicoId());
        novaConsulta.setDataHora(request.getNovaDataHora());
        novaConsulta.setTipo(consultaOriginal.getTipo());
        novaConsulta.setStatus(StatusConsulta.REMARCADA);
        novaConsulta.setConsultaOriginalId(consultaOriginal.getId());

        return consultaRepository.save(novaConsulta);
    }

    @Transactional
    public void cancelarConsulta(Long id, CancelamentoRequest request) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada para o ID informado."));

        if (consulta.getStatus() == StatusConsulta.CANCELADA) {
            throw new RegraDeNegocioException("Esta consulta já se encontra cancelada.");
        }

        consulta.setStatus(StatusConsulta.CANCELADA);
        consultaRepository.save(consulta);

        Cancelamento cancelamento = new Cancelamento();
        cancelamento.setConsulta(consulta);
        cancelamento.setMotivo(request.getMotivo());
        cancelamento.setCanceladoPor(request.getCanceladoPor());

        cancelamentoRepository.save(cancelamento);
    }

    public List<Consulta> listarConsultas() {
        return consultaRepository.findAll();
    }

    public Consulta buscarPorId(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada."));
    }
}