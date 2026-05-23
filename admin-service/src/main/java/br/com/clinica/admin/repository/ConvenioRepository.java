package br.com.clinica.admin.repository;

import br.com.clinica.admin.entity.Convenio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Repositório responsável pela persistência e consulta de convênios médicos no banco admin_db.
@Repository
public interface ConvenioRepository extends JpaRepository<Convenio, Long> {

    // Busca convênio pelo CNPJ para validação de unicidade e consultas externas
    Optional<Convenio> findByCnpj(String cnpj);

    // Verifica unicidade do CNPJ antes de cadastrar ou atualizar um convênio
    boolean existsByCnpj(String cnpj);

    // Lista todos os convênios ativos com paginação
    Page<Convenio> findByAtivo(Boolean ativo, Pageable pageable);

    // Valida se um convênio específico está ativo — usado pela integração via Feign no agendamento
    boolean existsByIdAndAtivo(Long id, Boolean ativo);
}
