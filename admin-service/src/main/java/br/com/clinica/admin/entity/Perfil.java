package br.com.clinica.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// Entidade que representa um perfil de acesso (RBAC) no sistema.
// Valores possíveis: ADM, MEDICO, SECRETARIA — conforme definição dos atores UML.
@Entity
@Table(name = "perfis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nome;

    @Column(length = 200)
    private String descricao;

    @Builder.Default
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
    }
}
