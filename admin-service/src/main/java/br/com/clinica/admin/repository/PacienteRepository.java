package br.com.clinica.admin.repository;

import br.com.clinica.admin.entity.Paciente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Repositório responsável pela persistência e consulta de dados de pacientes no banco admin_db.
@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    // Busca paciente pelo CPF — utilizado na regra de unicidade e consultas externas
    Optional<Paciente> findByCpf(String cpf);

    // Busca paciente pelo RG para validação de duplicidade
    Optional<Paciente> findByRg(String rg);

    // Busca por nome parcial (LIKE) com paginação para listagens na interface
    Page<Paciente> findByNomeCompletoContainingIgnoreCase(String nome, Pageable pageable);

    // Lista todos os pacientes ativos com paginação
    Page<Paciente> findByAtivo(Boolean ativo, Pageable pageable);

    // Verifica unicidade do CPF antes de cadastrar ou atualizar paciente
    boolean existsByCpf(String cpf);

    // Verifica unicidade do RG antes de cadastrar ou atualizar paciente
    boolean existsByRg(String rg);

    // Valida se um paciente específico está ativo — usado pela integração via Feign
    boolean existsByIdAndAtivo(Long id, Boolean ativo);
}
