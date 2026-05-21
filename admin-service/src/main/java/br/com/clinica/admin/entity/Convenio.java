package br.com.clinica.admin.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="convenios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Convenio{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String nome;
}