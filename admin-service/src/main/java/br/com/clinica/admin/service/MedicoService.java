package br.com.clinica.admin.service;

import br.com.clinica.admin.dto.requisicao.EspecialidadeRequisicao;
import br.com.clinica.admin.dto.requisicao.MedicoRequisicao;
import br.com.clinica.admin.dto.resposta.MedicoResposta;
import br.com.clinica.admin.entity.Especialidade;
import br.com.clinica.admin.entity.Medico;
import br.com.clinica.admin.mapper.MedicoMapper;
import br.com.clinica.admin.repository.EspecialidadeRepository;
import br.com.clinica.admin.repository.MedicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Service que gerencia o fluxo de cadastro, consulta e desativação de médicos.
// Aplica validações de unicidade de CRM e existência de especialidade antes de persistir.
@Service
@RequiredArgsConstructor
@Slf4j
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final EspecialidadeRepository especialidadeRepository;

    @Transactional
    public MedicoResposta criarMedico(MedicoRequisicao requisicao) {
        log.info("Criando novo médico com CRM: {}", requisicao.getCrm());

        // Valida unicidade do CRM antes de persistir — regra de negócio obrigatória
        if (medicoRepository.existsByCrm(requisicao.getCrm())) {
            throw new br.com.clinica.admin.exception.RecursoDuplicadoException(
                    "Já existe um médico cadastrado com o CRM: " + requisicao.getCrm());
        }

        // Valida existência da especialidade informada antes de vincular ao médico
        Especialidade especialidade = especialidadeRepository.findById(requisicao.getEspecialidadeId())
                .orElseThrow(() -> new br.com.clinica.admin.exception.RecursoNaoEncontradoException(
                        "Especialidade com ID " + requisicao.getEspecialidadeId() + " não encontrada"));

        if (!especialidade.getAtivo()) {
            throw new br.com.clinica.admin.exception.RegraDeNegocioException(
                    "Não é possível vincular um médico a uma especialidade inativa");
        }

        Medico medico = MedicoMapper.paraEntidade(requisicao, especialidade);
        medico = medicoRepository.save(medico);
        log.info("Médico criado com sucesso: ID={}, CRM={}", medico.getId(), medico.getCrm());
        return MedicoMapper.paraResposta(medico);
    }

    @Transactional(readOnly = true)
    public Page<MedicoResposta> listarMedicos(Pageable pageable) {
        log.debug("Listando médicos ativos com paginação");
        return medicoRepository.findByAtivo(true, pageable)
                .map(MedicoMapper::paraResposta);
    }

    @Transactional(readOnly = true)
    public MedicoResposta buscarPorId(Long id) {
        log.debug("Buscando médico por ID: {}", id);
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new br.com.clinica.admin.exception.RecursoNaoEncontradoException(
                        "Médico com ID " + id + " não encontrado"));
        return MedicoMapper.paraResposta(medico);
    }

    @Transactional(readOnly = true)
    public MedicoResposta buscarPorCrm(String crm) {
        log.debug("Buscando médico por CRM: {}", crm);
        Medico medico = medicoRepository.findByCrm(crm)
                .orElseThrow(() -> new br.com.clinica.admin.exception.RecursoNaoEncontradoException(
                        "Médico com CRM " + crm + " não encontrado"));
        return MedicoMapper.paraResposta(medico);
    }

    @Transactional(readOnly = true)
    public Page<MedicoResposta> listarPorEspecialidade(Long especialidadeId, Pageable pageable) {
        log.debug("Listando médicos por especialidade ID: {}", especialidadeId);
        // Valida existência da especialidade antes de consultar
        if (!especialidadeRepository.existsById(especialidadeId)) {
            throw new br.com.clinica.admin.exception.RecursoNaoEncontradoException(
                    "Especialidade com ID " + especialidadeId + " não encontrada");
        }
        return medicoRepository.findByEspecialidade_Id(especialidadeId, pageable)
                .map(MedicoMapper::paraResposta);
    }

    @Transactional
    public MedicoResposta atualizarMedico(Long id, MedicoRequisicao requisicao) {
        log.info("Atualizando médico ID: {}", id);
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new br.com.clinica.admin.exception.RecursoNaoEncontradoException(
                        "Médico com ID " + id + " não encontrado"));

        // Valida unicidade do CRM apenas se for um CRM diferente do atual
        if (!medico.getCrm().equals(requisicao.getCrm()) && medicoRepository.existsByCrm(requisicao.getCrm())) {
            throw new br.com.clinica.admin.exception.RecursoDuplicadoException(
                    "Já existe um médico cadastrado com o CRM: " + requisicao.getCrm());
        }

        Especialidade especialidade = especialidadeRepository.findById(requisicao.getEspecialidadeId())
                .orElseThrow(() -> new br.com.clinica.admin.exception.RecursoNaoEncontradoException(
                        "Especialidade com ID " + requisicao.getEspecialidadeId() + " não encontrada"));

        medico.setNomeCompleto(requisicao.getNomeCompleto());
        medico.setCrm(requisicao.getCrm());
        medico.setEspecialidade(especialidade);
        medico = medicoRepository.save(medico);
        log.info("Médico atualizado com sucesso: ID={}", medico.getId());
        return MedicoMapper.paraResposta(medico);
    }

    @Transactional
    public void desativarMedico(Long id) {
        log.info("Desativando médico ID: {}", id);
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new br.com.clinica.admin.exception.RecursoNaoEncontradoException(
                        "Médico com ID " + id + " não encontrado"));

        // Soft delete — apenas marca como inativo, sem remover do banco
        medico.setAtivo(false);
        medicoRepository.save(medico);
        log.info("Médico desativado com sucesso: ID={}", id);
    }

    // Endpoint de validação para integração via Feign com agendamento-service
    @Transactional(readOnly = true)
    public Boolean validarMedicoAtivo(Long id) {
        return medicoRepository.existsByIdAndAtivo(id, true);
    }
}
