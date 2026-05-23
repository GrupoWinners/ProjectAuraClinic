package br.com.clinica.admin.service;

import br.com.clinica.admin.dto.requisicao.EspecialidadeRequisicao;
import br.com.clinica.admin.dto.resposta.EspecialidadeResposta;
import br.com.clinica.admin.entity.Especialidade;
import br.com.clinica.admin.exception.RecursoDuplicadoException;
import br.com.clinica.admin.exception.RecursoNaoEncontradoException;
import br.com.clinica.admin.exception.RegraDeNegocioException;
import br.com.clinica.admin.mapper.EspecialidadeMapper;
import br.com.clinica.admin.repository.EspecialidadeRepository;
import br.com.clinica.admin.repository.MedicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Service que gerencia o cadastro e manutenção de especialidades médicas.
// Garante unicidade da descrição e impede desativação de especialidades vinculadas a médicos ativos.
@Service
@RequiredArgsConstructor
@Slf4j
public class EspecialidadeService {

    private final EspecialidadeRepository especialidadeRepository;
    private final MedicoRepository medicoRepository;

    @Transactional
    public EspecialidadeResposta criarEspecialidade(EspecialidadeRequisicao requisicao) {
        log.info("Criando nova especialidade: {}", requisicao.getDescricao());

        // Valida unicidade da descrição antes de persistir
        if (especialidadeRepository.existsByDescricaoIgnoreCase(requisicao.getDescricao())) {
            throw new RecursoDuplicadoException(
                    "Já existe uma especialidade cadastrada com a descrição: " + requisicao.getDescricao());
        }

        Especialidade especialidade = EspecialidadeMapper.paraEntidade(requisicao);
        especialidade = especialidadeRepository.save(especialidade);
        log.info("Especialidade criada com sucesso: ID={}", especialidade.getId());
        return EspecialidadeMapper.paraResposta(especialidade);
    }

    @Transactional(readOnly = true)
    public List<EspecialidadeResposta> listarEspecialidades() {
        log.debug("Listando todas as especialidades ativas");
        return especialidadeRepository.findByAtivo(true).stream()
                .map(EspecialidadeMapper::paraResposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public EspecialidadeResposta buscarPorId(Long id) {
        log.debug("Buscando especialidade por ID: {}", id);
        Especialidade especialidade = especialidadeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Especialidade com ID " + id + " não encontrada"));
        return EspecialidadeMapper.paraResposta(especialidade);
    }

    @Transactional
    public EspecialidadeResposta atualizarEspecialidade(Long id, EspecialidadeRequisicao requisicao) {
        log.info("Atualizando especialidade ID: {}", id);
        Especialidade especialidade = especialidadeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Especialidade com ID " + id + " não encontrada"));

        // Valida unicidade apenas se a descrição foi alterada
        if (!especialidade.getDescricao().equalsIgnoreCase(requisicao.getDescricao())
                && especialidadeRepository.existsByDescricaoIgnoreCase(requisicao.getDescricao())) {
            throw new RecursoDuplicadoException(
                    "Já existe uma especialidade cadastrada com a descrição: " + requisicao.getDescricao());
        }

        especialidade.setDescricao(requisicao.getDescricao());
        especialidade = especialidadeRepository.save(especialidade);
        log.info("Especialidade atualizada com sucesso: ID={}", especialidade.getId());
        return EspecialidadeMapper.paraResposta(especialidade);
    }

    @Transactional
    public void desativarEspecialidade(Long id) {
        log.info("Desativando especialidade ID: {}", id);
        Especialidade especialidade = especialidadeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Especialidade com ID " + id + " não encontrada"));

        // Valida que não há médicos ativos vinculados a esta especialidade antes de desativar
        boolean possuiMedicosAtivos = medicoRepository
                .findByEspecialidade_Id(id, org.springframework.data.domain.Pageable.unpaged())
                .stream().anyMatch(m -> Boolean.TRUE.equals(m.getAtivo()));

        if (possuiMedicosAtivos) {
            throw new RegraDeNegocioException(
                    "Não é possível desativar uma especialidade que possui médicos ativos vinculados");
        }

        // Soft delete — apenas marca como inativa, preservando o histórico
        especialidade.setAtivo(false);
        especialidadeRepository.save(especialidade);
        log.info("Especialidade desativada com sucesso: ID={}", id);
    }
}
