package br.com.clinica.commons.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class RespostaErro {
    private LocalDateTime timestamp;
    private Integer status;
    private String erro;
    private String mensagem;
    private String caminho;
    private List<ErroValidacao> errosValidacao;
}