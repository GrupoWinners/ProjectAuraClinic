package br.com.clinica.admin.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Filtro JWT que intercepta todas as requisições para validar o token Bearer antes de autorizar.
// Extrai o perfil do token e popula o SecurityContext para controle de autorização RBAC.
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFiltroAutenticacao extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Prossegue sem autenticação se o header não estiver presente ou não for Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (jwtService.isTokenValido(token)) {
            String nomeUsuario = jwtService.extrairNomeUsuario(token);
            String perfil = jwtService.extrairPerfil(token);

            // Popula o SecurityContext com a autoridade do perfil extraído do token
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    nomeUsuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + perfil)));

            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("Autenticação JWT válida para usuário: {}, perfil: {}", nomeUsuario, perfil);
        } else {
            log.warn("Token JWT inválido recebido para o path: {}", request.getRequestURI());
        }

        chain.doFilter(request, response);
    }
}
