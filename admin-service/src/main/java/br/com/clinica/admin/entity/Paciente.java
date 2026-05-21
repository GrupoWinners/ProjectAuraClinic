package br.com.clinica.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity 
@Table(name = "pacientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder


public class Paciente{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_completo", nullable = false, length = 150)
    private String nomeCompleto;

    @Column(nullable = false, unique = true, length = 9)
    private String rg;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(name = "endereco", nullable = false, length = 250)
    private String endereco;

    @Column(name = "bairro", nullable = false, length = 100)
    private String bairro;

    @Column(name = "cidade", nullable = false, length = 100)
    private String cidade;

    @Column(name = "estado", nullable = false, length = 2)
    private String estado;

    @Column(name = "cep", nullable = false, length = 8)
    private String cep;

    @Column(name = "telefone", nullable = false, length = 11)
    private String telefone;

    @Column(name = "celular", nullable = true, length = 11 )
    private String celular;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero", nullable = false, length = 20)
    private Genero genero;


    @Builder.Default
    @Column(name = "possui_convenio", nullable = false)
    private Boolean possuiconvenio = false;

 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "convenio_id", nullable = true)
    private Convenio convenio;

    @Builder.Default
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

}