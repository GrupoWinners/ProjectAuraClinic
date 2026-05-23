package br.com.clinica.admin.service;

import br.com.clinica.admin.dto.requisicao.LoginRequisicao;
import br.com.clinica.admin.dto.requisicao.UsuarioRequisicao;
import br.com.clinica.admin.dto.resposta.LoginResposta;
import br.com.clinica.admin.dto.resposta.UsuarioResposta;
import br.com.clinica.admin.entity.Perfil;
import br.com.clinica.admin.entity.Usuario;
import br.com.clinica.admin.exception.RecursoDuplicadoException;
import br.com.clinica.admin.exception.RecursoNaoEncontradoException;
import br.com.clinica.admin.exception.RegraDeNegocioException;
import br.com.clinica.admin.mapper.UsuarioMapper;
import br.com.clinica.admin.repository.PerfilRepository;
import br.com.clinica.admin.repository.UsuarioRepository;
import br.com.clinica.admin.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// Service que gerencia autenticação e o ciclo de vida de usuários do sistema.
// Aplica BCrypt nas senhas e gera tokens JWT após autenticação bem-sucedida.
@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Fluxo de autenticação: valida credenciais, gera JWT e retorna ao cliente
    @Transactional
    public LoginResposta autenticar(LoginRequisicao requisicao) {
        log.info("Tentativa de login para o usuário: {}", requisicao.getNomeUsuario());

        Usuario usuario = usuarioRepository.findByNomeUsuario(requisicao.getNomeUsuario())
                .orElseThrow(() -> new RegraDeNegocioException("Credenciais inválidas"));

        // Valida se o usuário está ativo antes de autenticar
        if (!usuario.getAtivo()) {
            throw new RegraDeNegocioException("Usuário inativo — contate o administrador");
        }

        // Valida a senha usando BCrypt — nunca compara texto plano
        if (!passwordEncoder.matches(requisicao.getSenha(), usuario.getSenha())) {
            log.warn("Senha incorreta para o usuário: {}", requisicao.getNomeUsuario());
            throw new RegraDeNegocioException("Credenciais inválidas");
        }

        String token = jwtService.gerarToken(usuario);
        LocalDateTime expiracao = LocalDateTime.now().plusHours(24);

        log.info("Login bem-sucedido para o usuário: {}", requisicao.getNomeUsuario());
        return LoginResposta.builder()
                .token(token)
                .tipo("Bearer")
                .nomeUsuario(usuario.getNomeUsuario())
                .perfil(usuario.getPerfil().getNome())
                .expiracao(expiracao)
                .build();
    }

    @Transactional
    public UsuarioResposta criarUsuario(UsuarioRequisicao requisicao) {
        log.info("Criando novo usuário: {}", requisicao.getNomeUsuario());

        // Valida unicidade do nome de usuário antes de cadastrar
        if (usuarioRepository.existsByNomeUsuario(requisicao.getNomeUsuario())) {
            throw new RecursoDuplicadoException(
                    "Já existe um usuário cadastrado com o nome: " + requisicao.getNomeUsuario());
        }

        Perfil perfil = perfilRepository.findById(requisicao.getPerfilId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Perfil com ID " + requisicao.getPerfilId() + " não encontrado"));

        // Criptografa a senha com BCrypt (strength 12) antes de persistir
        String senhaCriptografada = passwordEncoder.encode(requisicao.getSenha());
        Usuario usuario = UsuarioMapper.paraEntidade(requisicao, perfil, senhaCriptografada);
        usuario = usuarioRepository.save(usuario);
        log.info("Usuário criado com sucesso: ID={}", usuario.getId());
        return UsuarioMapper.paraResposta(usuario);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResposta> listarUsuarios(Pageable pageable) {
        log.debug("Listando usuários ativos");
        return usuarioRepository.findByAtivo(true, pageable)
                .map(UsuarioMapper::paraResposta);
    }

    @Transactional
    public UsuarioResposta atualizarPerfil(Long id, Long perfilId) {
        log.info("Atualizando perfil do usuário ID: {}", id);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário com ID " + id + " não encontrado"));

        Perfil perfil = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Perfil com ID " + perfilId + " não encontrado"));

        usuario.setPerfil(perfil);
        usuario = usuarioRepository.save(usuario);
        log.info("Perfil do usuário atualizado: ID={}, perfil={}", id, perfil.getNome());
        return UsuarioMapper.paraResposta(usuario);
    }

    @Transactional
    public UsuarioResposta atualizarUsuario(Long id, UsuarioRequisicao requisicao) {
        log.info("Atualizando usuário ID: {}", id);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário com ID " + id + " não encontrado"));

        if (!usuario.getNomeUsuario().equals(requisicao.getNomeUsuario())
                && usuarioRepository.existsByNomeUsuario(requisicao.getNomeUsuario())) {
            throw new RecursoDuplicadoException(
                    "Já existe um usuário cadastrado com o nome: " + requisicao.getNomeUsuario());
        }

        Perfil perfil = perfilRepository.findById(requisicao.getPerfilId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Perfil com ID " + requisicao.getPerfilId() + " não encontrado"));

        usuario.setNomeUsuario(requisicao.getNomeUsuario());
        // Atualiza a senha criptografada apenas se for fornecida
        if (requisicao.getSenha() != null && !requisicao.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(requisicao.getSenha()));
        }
        usuario.setPerfil(perfil);
        usuario = usuarioRepository.save(usuario);
        return UsuarioMapper.paraResposta(usuario);
    }

    @Transactional
    public void desativarUsuario(Long id) {
        log.info("Desativando usuário ID: {}", id);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário com ID " + id + " não encontrado"));

        // Soft delete — preserva histórico de acesso e auditoria
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
        log.info("Usuário desativado com sucesso: ID={}", id);
    }
}
