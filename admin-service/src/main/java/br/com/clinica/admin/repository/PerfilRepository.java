package br.com.clinica.admin.repository;

import br.com.clinica.admin.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Repositório responsável pela consulta de perfis de acesso (ADM, MEDICO, SECRETARIA) no banco admin_db.
@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {

    // Busca perfil pelo nome para vinculação com novos usuários
    Optional<Perfil> findByNome(String nome);

    // Verifica existência de um perfil por nome
    boolean existsByNome(String nome);
}
