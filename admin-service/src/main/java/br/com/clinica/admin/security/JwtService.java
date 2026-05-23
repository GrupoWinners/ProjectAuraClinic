package br.com.clinica.admin.security;

import br.com.clinica.admin.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// Service responsável pela emissão e validação de tokens JWT no admin-service.
// Utiliza HS256 com chave secreta em Base64 configurada via application.yml.
@Service
@Slf4j
public class JwtService {

    @Value("${jwt.chave-secreta}")
    private String chaveSecreta;

    @Value("${jwt.expiracao}")
    private long expiracao; // 86400000 ms = 24 horas

    // Gera o token JWT com claims de usuarioId e perfil embutidos no payload
    public String gerarToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("usuarioId", usuario.getId());
        // Perfil é incluído no token para que outros serviços possam verificar permissões sem consultar o banco
        claims.put("perfil", usuario.getPerfil().getNome());
        return construirToken(claims, usuario.getNomeUsuario(), expiracao);
    }

    private String construirToken(Map<String, Object> claims, String subject, long expiracao) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiracao))
                .signWith(obterChaveAssinatura(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValido(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(obterChaveAssinatura())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            log.warn("Token JWT inválido: {}", ex.getMessage());
            return false;
        }
    }

    // Extrai o nome de usuário (subject) do token para identificação da requisição
    public String extrairNomeUsuario(String token) {
        return extrairClaim(token, Claims::getSubject);
    }

    // Extrai o perfil de acesso do token para autorização RBAC
    public String extrairPerfil(String token) {
        return extrairClaim(token, claims -> claims.get("perfil", String.class));
    }

    public Date extrairExpiracao(String token) {
        return extrairClaim(token, Claims::getExpiration);
    }

    private <T> T extrairClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(obterChaveAssinatura())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return resolver.apply(claims);
    }

    private Key obterChaveAssinatura() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(chaveSecreta));
    }
}
