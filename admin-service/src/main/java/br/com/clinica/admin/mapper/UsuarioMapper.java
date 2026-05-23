package br.com.clinica.admin.mapper;

import br.com.clinica.admin.dto.requisicao.UsuarioRequisicao;
import br.com.clinica.admin.dto.resposta.UsuarioResposta;
import br.com.clinica.admin.entity.Perfil;
import br.com.clinica.admin.entity.Usuario;

// Mapper responsável por converter a entidade Usuario para seus DTOs de entrada e saída.
// A senha NUNCA é copiada para o DTO de resposta — apenas o service aplica o BCrypt na entrada.
public class UsuarioMapper {

    public static Usuario paraEntidade(UsuarioRequisicao requisicao, Perfil perfil, String senhaCriptografada) {
        if (requisicao == null) return null;
        return Usuario.builder()
                .nomeUsuario(requisicao.getNomeUsuario())
                // Senha já deve chegar criptografada pelo service — nunca em texto plano
                .senha(senhaCriptografada)
                .perfil(perfil)
                .build();
    }

    public static UsuarioResposta paraResposta(Usuario usuario) {
        if (usuario == null) return null;

        // Nome do perfil com fallback para evitar NullPointerException
        String nomePerfil = usuario.getPerfil() != null ? usuario.getPerfil().getNome() : null;

        return UsuarioResposta.builder()
                .id(usuario.getId())
                .nomeUsuario(usuario.getNomeUsuario())
                .perfil(nomePerfil)
                .ativo(usuario.getAtivo())
                .criadoEm(usuario.getCriadoEm())
                .atualizadoEm(usuario.getAtualizadoEm())
                .build();
    }
}
