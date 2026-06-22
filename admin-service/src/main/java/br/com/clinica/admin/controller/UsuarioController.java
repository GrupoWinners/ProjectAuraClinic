package br.com.clinica.admin.controller;

import br.com.clinica.admin.dto.requisicao.LoginRequisicao;
import br.com.clinica.admin.dto.requisicao.UsuarioRequisicao;
import br.com.clinica.admin.dto.resposta.LoginResposta;
import br.com.clinica.admin.dto.resposta.UsuarioResposta;
import br.com.clinica.admin.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controller REST responsável pelos endpoints de autenticação e gerenciamento de usuários.
// O endpoint de login é público — todos os demais requerem perfil ADM.
@RestController
@RequiredArgsConstructor
@Tag(name = "Autenticação e Usuários", description = "Endpoints de login e gerenciamento de usuários do sistema")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/api/v1/auth/login")
    @Operation(summary = "Autenticar usuário", description = "Valida credenciais e retorna um token JWT Bearer")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso — token JWT retornado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "422", description = "Credenciais inválidas ou usuário inativo")
    })
    public ResponseEntity<LoginResposta> login(@Valid @RequestBody LoginRequisicao requisicao) {
        return ResponseEntity.ok(usuarioService.autenticar(requisicao));
    }

    @PostMapping("/api/v1/usuarios")
    @Operation(summary = "Criar usuário", description = "Cria novo usuário com perfil de acesso — restrito ao ADM")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
        @ApiResponse(responseCode = "409", description = "Nome de usuário já cadastrado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão — apenas ADM")
    })
    public ResponseEntity<UsuarioResposta> criarUsuario(@Valid @RequestBody UsuarioRequisicao requisicao) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criarUsuario(requisicao));
    }

    @GetMapping("/api/v1/usuarios")
    @Operation(summary = "Listar usuários", description = "Retorna lista paginada de usuários ativos — restrito ao ADM")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<Page<UsuarioResposta>> listarUsuarios(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(usuarioService.listarUsuarios(pageable));
    }

    @PutMapping("/api/v1/usuarios/{id}")
    @Operation(summary = "Atualizar usuário", description = "Atualiza dados de um usuário existente — restrito ao ADM")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<UsuarioResposta> atualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequisicao requisicao) {
        return ResponseEntity.ok(usuarioService.atualizarUsuario(id, requisicao));
    }

    @PutMapping("/api/v1/usuarios/{id}/perfil")
    @Operation(summary = "Atualizar perfil do usuário", description = "Altera as permissões de acesso de um usuário — restrito ao ADM")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário ou perfil não encontrado")
    })
    public ResponseEntity<UsuarioResposta> atualizarPerfil(
            @PathVariable Long id,
            @RequestParam Long perfilId) {
        return ResponseEntity.ok(usuarioService.atualizarPerfil(id, perfilId));
    }

    @DeleteMapping("/api/v1/usuarios/{id}")
    @Operation(summary = "Desativar usuário", description = "Realiza soft delete do usuário — restrito ao ADM")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Usuário desativado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> desativarUsuario(@PathVariable Long id) {
        usuarioService.desativarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
