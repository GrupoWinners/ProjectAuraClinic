package br.com.clinica.admin.config;

import br.com.clinica.admin.security.JwtFiltroAutenticacao;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Configuração central de segurança do admin-service.
// Define proteção de rotas por perfil RBAC (ADM, MEDICO, SECRETARIA) e integra o filtro JWT.
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFiltroAutenticacao jwtFiltro;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Desabilita CSRF — API stateless com JWT não precisa de proteção CSRF
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoint de login é público — não requer autenticação
                .requestMatchers("/api/v1/auth/login").permitAll()
                // Documentação Swagger acessível sem autenticação
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                // Endpoints de validação para integração Feign entre serviços
                .requestMatchers("/api/v1/medicos/*/validar-ativo").permitAll()
                .requestMatchers("/api/v1/pacientes/*/validar-ativo").permitAll()
                .requestMatchers("/api/v1/convenios/*/validar-ativo").permitAll()
                // Apenas ADM pode executar deleções (soft delete)
                .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole("ADM")
                // Gerenciamento de usuários restrito ao ADM
                .requestMatchers("/api/v1/usuarios/**").hasRole("ADM")
                // Relatórios gerenciais restrito ao ADM
                .requestMatchers("/api/v1/relatorios/**").hasRole("ADM")
                // Todas as outras requisições requerem autenticação
                .anyRequest().authenticated()
            )
            // Adiciona o filtro JWT antes do filtro de autenticação padrão do Spring
            .addFilterBefore(jwtFiltro, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // BCryptPasswordEncoder com strength 12 para armazenamento seguro de senhas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
