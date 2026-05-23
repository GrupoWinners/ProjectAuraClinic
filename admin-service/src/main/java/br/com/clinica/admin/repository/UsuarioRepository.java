package br.com.clinica.admin.repository;

import br.com.clinica.admin.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Repositório responsável pela persistência e consulta de usuários de sistema no banco admin_db.
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca usuário pelo nome para autenticação no fluxo de login
    Optional<Usuario> findByNomeUsuario(String nomeUsuario);

    // Verifica unicidade do nome de usuário antes de cadastrar
    boolean existsByNomeUsuario(String nomeUsuario);

    // Lista usuários ativos com paginação para a tela de gerenciamento
    Page<Usuario> findByAtivo(Boolean ativo, Pageable pageable);
}
