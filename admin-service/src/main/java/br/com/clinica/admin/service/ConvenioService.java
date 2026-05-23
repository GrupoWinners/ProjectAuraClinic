package br.com.clinica.admin.service;

import br.com.clinica.admin.dto.requisicao.ConvenioRequisicao;
import br.com.clinica.admin.dto.resposta.ConvenioResposta;
import br.com.clinica.admin.entity.Convenio;
import br.com.clinica.admin.exception.RecursoDuplicadoException;
import br.com.clinica.admin.exception.RecursoNaoEncontradoException;
import br.com.clinica.admin.mapper.ConvenioMapper;
import br.com.clinica.admin.repository.ConvenioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Service que gerencia o fluxo de cadastro, consulta e desativação de convênios médicos.
// Aplica validação de CNPJ único e controla o status de convênios para integração com agendamento.
@Service
@RequiredArgsConstructor
@Slf4j
public class ConvenioService {

    private final ConvenioRepository convenioRepository;

    @Transactional
    public ConvenioResposta criarConvenio(ConvenioRequisicao requisicao) {
        log.info("Criando novo convênio com CNPJ: {}", requisicao.getCnpj());

        // Valida unicidade do CNPJ antes de persistir — regra de negócio obrigatória
        if (convenioRepository.existsByCnpj(requisicao.getCnpj())) {
            throw new RecursoDuplicadoException(
                    "Já existe um convênio cadastrado com o CNPJ: " + requisicao.getCnpj());
        }

        Convenio convenio = ConvenioMapper.paraEntidade(requisicao);
        convenio = convenioRepository.save(convenio);
        log.info("Convênio criado com sucesso: ID={}", convenio.getId());
        return ConvenioMapper.paraResposta(convenio);
    }

    @Transactional(readOnly = true)
    public Page<ConvenioResposta> listarConvenios(Pageable pageable) {
        log.debug("Listando convênios ativos com paginação");
        return convenioRepository.findByAtivo(true, pageable)
                .map(ConvenioMapper::paraResposta);
    }

    @Transactional(readOnly = true)
    public ConvenioResposta buscarPorId(Long id) {
        log.debug("Buscando convênio por ID: {}", id);
        // Valida a existência do convênio antes de retornar
        Convenio convenio = convenioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Convênio com ID " + id + " não encontrado"));
        return ConvenioMapper.paraResposta(convenio);
    }

    @Transactional
    public ConvenioResposta atualizarConvenio(Long id, ConvenioRequisicao requisicao) {
        log.info("Atualizando convênio ID: {}", id);
        Convenio convenio = convenioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Convênio com ID " + id + " não encontrado"));

        // Valida unicidade do CNPJ apenas se foi alterado
        if (!convenio.getCnpj().equals(requisicao.getCnpj()) && convenioRepository.existsByCnpj(requisicao.getCnpj())) {
            throw new RecursoDuplicadoException(
                    "Já existe um convênio cadastrado com o CNPJ: " + requisicao.getCnpj());
        }

        convenio.setNomeEmpresa(requisicao.getNomeEmpresa());
        convenio.setCnpj(requisicao.getCnpj());
        convenio.setTelefone(requisicao.getTelefone());
        convenio = convenioRepository.save(convenio);
        log.info("Convênio atualizado com sucesso: ID={}", convenio.getId());
        return ConvenioMapper.paraResposta(convenio);
    }

    @Transactional
    public void desativarConvenio(Long id) {
        log.info("Desativando convênio ID: {}", id);
        Convenio convenio = convenioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Convênio com ID " + id + " não encontrado"));

        // Soft delete — mantém o histórico de pacientes vinculados preservado
        convenio.setAtivo(false);
        convenioRepository.save(convenio);
        log.info("Convênio desativado com sucesso: ID={}", id);
    }

    // Endpoint de validação para integração via Feign com agendamento-service
    @Transactional(readOnly = true)
    public Boolean validarConvenioAtivo(Long id) {
        return convenioRepository.existsByIdAndAtivo(id, true);
    }
}
