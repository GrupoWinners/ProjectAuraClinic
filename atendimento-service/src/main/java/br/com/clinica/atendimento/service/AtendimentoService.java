package br.com.clinica.atendimento.service;

import br.com.clinica.atendimento.client.AdminServiceClient;
import br.com.clinica.atendimento.client.AgendamentoServiceClient;
import br.com.clinica.atendimento.client.dto.ConsultaClientResposta;
import br.com.clinica.atendimento.client.dto.MedicoClientResposta;
import br.com.clinica.atendimento.client.dto.PacienteClientResposta;
import br.com.clinica.atendimento.dto.requisicao.AtendimentoRequisicao;
import br.com.clinica.atendimento.dto.requisicao.AtualizarUrgenciaRequisicao;
import br.com.clinica.atendimento.dto.resposta.AtendimentoResposta;
import br.com.clinica.atendimento.entity.Atendimento;
import br.com.clinica.atendimento.entity.Prontuario;
import br.com.clinica.atendimento.mapper.AtendimentoMapper;
import br.com.clinica.atendimento.repository.AtendimentoRepository;
import br.com.clinica.commons.exception.IntegracaoException;
import br.com.clinica.commons.exception.RecursoNaoEncontradoException;
import br.com.clinica.commons.exception.RegraDeNegocioException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AtendimentoService {

    private final AtendimentoRepository atendimentoRepository;
    private final ProntuarioService prontuarioService;
    private final AdminServiceClient adminServiceClient;
    private final AgendamentoServiceClient agendamentoServiceClient;

    public AtendimentoResposta registrar(AtendimentoRequisicao requisicao) {
        PacienteClientResposta paciente = buscarPacienteNoAdmin(requisicao.getPacienteId());
        validarPaciente(paciente, requisicao.getPacienteId());

        MedicoClientResposta medico = buscarMedicoNoAdmin(requisicao.getMedicoId());
        validarMedico(medico, requisicao.getMedicoId());

        ConsultaClientResposta consulta = buscarConsultaNoAgendamento(requisicao.getConsultaId());
        validarConsulta(consulta, requisicao);

        Prontuario prontuario = prontuarioService.buscarOuCriarPorPacienteId(requisicao.getPacienteId());

        Atendimento atendimento = AtendimentoMapper.paraEntidade(requisicao, prontuario);
        Atendimento atendimentoSalvo = atendimentoRepository.save(atendimento);

        log.info("Atendimento registrado com sucesso. ID={}, consultaId={}, pacienteId={}, medicoId={}",
                atendimentoSalvo.getId(),
                atendimentoSalvo.getConsultaId(),
                requisicao.getPacienteId(),
                atendimentoSalvo.getMedicoId());

        return AtendimentoMapper.paraResposta(atendimentoSalvo);
    }

    public AtendimentoResposta buscarPorId(Long id) {
        Atendimento atendimento = buscarEntidadePorId(id);
        return AtendimentoMapper.paraResposta(atendimento);
    }

    public List<AtendimentoResposta> listarPorPaciente(Long pacienteId) {
        return atendimentoRepository.findByProntuarioPacienteId(pacienteId)
                .stream()
                .map(AtendimentoMapper::paraResposta)
                .toList();
    }

    public AtendimentoResposta atualizar(Long id, AtendimentoRequisicao requisicao) {
        Atendimento atendimento = buscarEntidadePorId(id);

        atendimento.setSintomas(requisicao.getSintomas());
        atendimento.setDiagnostico(requisicao.getDiagnostico());
        atendimento.setEscopoMedico(requisicao.getEscopoMedico());
        atendimento.setObservacoes(requisicao.getObservacoes());

        if (requisicao.getNivelUrgencia() != null) {
            atendimento.setNivelUrgencia(requisicao.getNivelUrgencia());
        }

        Atendimento atendimentoAtualizado = atendimentoRepository.save(atendimento);

        log.info("Atendimento atualizado com sucesso. ID={}", atendimentoAtualizado.getId());

        return AtendimentoMapper.paraResposta(atendimentoAtualizado);
    }

    public AtendimentoResposta atualizarUrgencia(Long id, AtualizarUrgenciaRequisicao requisicao) {
        Atendimento atendimento = buscarEntidadePorId(id);
        atendimento.setNivelUrgencia(requisicao.getNivelUrgencia());

        Atendimento atendimentoAtualizado = atendimentoRepository.save(atendimento);

        log.info("Urgência do atendimento atualizada. ID={}, urgencia={}",
                atendimentoAtualizado.getId(),
                atendimentoAtualizado.getNivelUrgencia());

        return AtendimentoMapper.paraResposta(atendimentoAtualizado);
    }

    public Atendimento buscarEntidadePorId(Long id) {
        return atendimentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento com ID " + id + " não encontrado"));
    }

    private PacienteClientResposta buscarPacienteNoAdmin(Long pacienteId) {
        try {
            return adminServiceClient.buscarPacientePorId(pacienteId);
        } catch (FeignException.NotFound ex) {
            throw new RecursoNaoEncontradoException("Paciente com ID " + pacienteId + " não encontrado");
        } catch (FeignException ex) {
            throw new IntegracaoException("Erro ao buscar paciente no admin-service");
        }
    }

    private MedicoClientResposta buscarMedicoNoAdmin(Long medicoId) {
        try {
            return adminServiceClient.buscarMedicoPorId(medicoId);
        } catch (FeignException.NotFound ex) {
            throw new RecursoNaoEncontradoException("Médico com ID " + medicoId + " não encontrado");
        } catch (FeignException ex) {
            throw new IntegracaoException("Erro ao buscar médico no admin-service");
        }
    }

    private ConsultaClientResposta buscarConsultaNoAgendamento(Long consultaId) {
        try {
            return agendamentoServiceClient.buscarConsultaPorId(consultaId);
        } catch (FeignException.NotFound ex) {
            throw new RecursoNaoEncontradoException("Consulta com ID " + consultaId + " não encontrada");
        } catch (FeignException ex) {
            throw new IntegracaoException("Erro ao buscar consulta no agendamento-service");
        }
    }

    private void validarPaciente(PacienteClientResposta paciente, Long pacienteId) {
        if (paciente == null || Boolean.FALSE.equals(paciente.getAtivo())) {
            throw new RegraDeNegocioException("Paciente com ID " + pacienteId + " está inativo ou inválido");
        }
    }

    private void validarMedico(MedicoClientResposta medico, Long medicoId) {
        if (medico == null || Boolean.FALSE.equals(medico.getAtivo())) {
            throw new RegraDeNegocioException("Médico com ID " + medicoId + " está inativo ou inválido");
        }
    }

    private void validarConsulta(ConsultaClientResposta consulta, AtendimentoRequisicao requisicao) {
        if (consulta == null) {
            throw new RecursoNaoEncontradoException("Consulta com ID " + requisicao.getConsultaId() + " não encontrada");
        }

        if (!requisicao.getPacienteId().equals(consulta.getPacienteId())) {
            throw new RegraDeNegocioException("Paciente informado não corresponde ao paciente da consulta");
        }

        if (!requisicao.getMedicoId().equals(consulta.getMedicoId())) {
            throw new RegraDeNegocioException("Médico informado não corresponde ao médico da consulta");
        }

        if (!"AGENDADA".equals(consulta.getStatus())) {
            throw new RegraDeNegocioException("Consulta precisa estar com status AGENDADA para registrar atendimento");
        }
    }
}