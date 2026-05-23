package br.com.clinica.admin.repository;

import br.com.clinica.admin.entity.Medico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Repositório responsável pela persistência e consulta de dados de médicos no banco admin_db.
@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    // Verifica unicidade do CRM antes de cadastrar/atualizar médico
    boolean existsByCrm(String crm);

    // Busca médico pelo CRM para consultas e validações de integração
    Optional<Medico> findByCrm(String crm);

    // Lista médicos de uma especialidade específica (paginado)
    Page<Medico> findByEspecialidade_Id(Long especialidadeId, Pageable pageable);

    // Lista apenas médicos ativos para consultas e agendamentos
    Page<Medico> findByAtivo(Boolean ativo, Pageable pageable);

    // Valida se um médico específico está ativo — usado pela integração via Feign
    boolean existsByIdAndAtivo(Long id, Boolean ativo);
}
