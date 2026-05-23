package br.com.clinica.admin.service;

import br.com.clinica.admin.dto.requisicao.PacienteRequisicao;
import br.com.clinica.admin.dto.resposta.PacienteResposta;
import br.com.clinica.admin.entity.Convenio;
import br.com.clinica.admin.entity.Paciente;
import br.com.clinica.admin.exception.RecursoDuplicadoException;
import br.com.clinica.admin.exception.RecursoNaoEncontradoException;
import br.com.clinica.admin.exception.RegraDeNegocioException;
import br.com.clinica.admin.mapper.PacienteMapper;
import br.com.clinica.admin.repository.ConvenioRepository;
import br.com.clinica.admin.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Service que gerencia o fluxo de cadastro, consulta e desativação de pacientes.
// Aplica validações de unicidade de CPF/RG e vínculo de convênio antes de persistir.
@Service
@RequiredArgsConstructor
@Slf4j
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final ConvenioRepository convenioRepository;

    @Transactional
    public PacienteResposta criarPaciente(PacienteRequisicao requisicao) {
        log.info("Criando novo paciente com CPF: {}", requisicao.getCpf());

        // Valida unicidade do CPF — regra de negócio obrigatória
        if (pacienteRepository.existsByCpf(requisicao.getCpf())) {
            throw new RecursoDuplicadoException("Já existe um paciente cadastrado com o CPF: " + requisicao.getCpf());
        }

        // Valida unicidade do RG — regra de negócio obrigatória
        if (pacienteRepository.existsByRg(requisicao.getRg())) {
            throw new RecursoDuplicadoException("Já existe um paciente cadastrado com o RG: " + requisicao.getRg());
        }

        // Resolve o convênio quando o paciente possui plano de saúde
        Convenio convenio = resolverConvenio(requisicao);

        Paciente paciente = PacienteMapper.paraEntidade(requisicao, convenio);
        paciente = pacienteRepository.save(paciente);
        log.info("Paciente criado com sucesso: ID={}", paciente.getId());
        return PacienteMapper.paraResposta(paciente);
    }

    @Transactional(readOnly = true)
    public Page<PacienteResposta> listarPacientes(Pageable pageable) {
        log.debug("Listando pacientes ativos com paginação");
        return pacienteRepository.findByAtivo(true, pageable)
                .map(PacienteMapper::paraResposta);
    }

    @Transactional(readOnly = true)
    public PacienteResposta buscarPorId(Long id) {
        log.debug("Buscando paciente por ID: {}", id);
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente com ID " + id + " não encontrado"));
        return PacienteMapper.paraResposta(paciente);
    }

    @Transactional(readOnly = true)
    public PacienteResposta buscarPorCpf(String cpf) {
        log.debug("Buscando paciente por CPF");
        Paciente paciente = pacienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente com CPF informado não encontrado"));
        return PacienteMapper.paraResposta(paciente);
    }

    @Transactional(readOnly = true)
    public Page<PacienteResposta> buscarPorNome(String nome, Pageable pageable) {
        log.debug("Buscando pacientes por nome: {}", nome);
        return pacienteRepository.findByNomeCompletoContainingIgnoreCase(nome, pageable)
                .map(PacienteMapper::paraResposta);
    }

    @Transactional
    public PacienteResposta atualizarPaciente(Long id, PacienteRequisicao requisicao) {
        log.info("Atualizando paciente ID: {}", id);
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente com ID " + id + " não encontrado"));

        // Valida unicidade do CPF apenas se for um CPF diferente do atual
        if (!paciente.getCpf().equals(requisicao.getCpf()) && pacienteRepository.existsByCpf(requisicao.getCpf())) {
            throw new RecursoDuplicadoException("Já existe um paciente cadastrado com o CPF: " + requisicao.getCpf());
        }

        // Valida unicidade do RG apenas se for um RG diferente do atual
        if (!paciente.getRg().equals(requisicao.getRg()) && pacienteRepository.existsByRg(requisicao.getRg())) {
            throw new RecursoDuplicadoException("Já existe um paciente cadastrado com o RG: " + requisicao.getRg());
        }

        Convenio convenio = resolverConvenio(requisicao);

        paciente.setNomeCompleto(requisicao.getNomeCompleto());
        paciente.setRg(requisicao.getRg());
        paciente.setCpf(requisicao.getCpf());
        paciente.setEndereco(requisicao.getEndereco());
        paciente.setBairro(requisicao.getBairro());
        paciente.setCidade(requisicao.getCidade());
        paciente.setEstado(requisicao.getEstado());
        paciente.setCep(requisicao.getCep());
        paciente.setTelefone(requisicao.getTelefone());
        paciente.setCelular(requisicao.getCelular());
        paciente.setDataNascimento(requisicao.getDataNascimento());
        paciente.setGenero(requisicao.getGenero());
        paciente.setPossuiConvenio(requisicao.getPossuiConvenio() != null ? requisicao.getPossuiConvenio() : false);
        paciente.setConvenio(convenio);

        paciente = pacienteRepository.save(paciente);
        log.info("Paciente atualizado com sucesso: ID={}", paciente.getId());
        return PacienteMapper.paraResposta(paciente);
    }

    @Transactional
    public void desativarPaciente(Long id) {
        log.info("Desativando paciente ID: {}", id);
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente com ID " + id + " não encontrado"));

        // Soft delete — apenas marca como inativo, preservando o histórico no banco
        paciente.setAtivo(false);
        pacienteRepository.save(paciente);
        log.info("Paciente desativado com sucesso: ID={}", id);
    }

    // Endpoint de validação para integração via Feign com agendamento-service
    @Transactional(readOnly = true)
    public Boolean validarPacienteAtivo(Long id) {
        return pacienteRepository.existsByIdAndAtivo(id, true);
    }

    // Resolve o convênio do paciente validando existência e status ativo
    private Convenio resolverConvenio(PacienteRequisicao requisicao) {
        if (Boolean.TRUE.equals(requisicao.getPossuiConvenio())) {
            if (requisicao.getConvenioId() == null) {
                throw new RegraDeNegocioException(
                        "O ID do convênio é obrigatório quando o paciente possui convênio");
            }
            Convenio convenio = convenioRepository.findById(requisicao.getConvenioId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Convênio com ID " + requisicao.getConvenioId() + " não encontrado"));
            if (!convenio.getAtivo()) {
                throw new RegraDeNegocioException("Não é possível vincular um paciente a um convênio inativo");
            }
            return convenio;
        }
        return null;
    }
}
