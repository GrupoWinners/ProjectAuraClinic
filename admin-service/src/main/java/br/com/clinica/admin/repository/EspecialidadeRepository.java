package br.com.clinica.admin.repository;

import br.com.clinica.admin.entity.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Repositório responsável pela persistência e consulta de especialidades médicas no banco admin_db.
@Repository
public interface EspecialidadeRepository extends JpaRepository<Especialidade, Long> {

    // Lista todas as especialidades ativas disponíveis para vinculação com médicos
    List<Especialidade> findByAtivo(Boolean ativo);

    // Busca especialidade pela descrição exata para validação de duplicidade
    Optional<Especialidade> findByDescricaoIgnoreCase(String descricao);

    // Verifica se já existe uma especialidade com a mesma descrição
    boolean existsByDescricaoIgnoreCase(String descricao);
}
