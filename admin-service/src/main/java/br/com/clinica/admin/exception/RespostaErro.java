package br.com.clinica.admin.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

// DTO padrão de resposta de erro — garante consistência nas respostas de exceção da API.
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RespostaErro {
    private int status;
    private String erro;
    private String mensagem;
    private String caminho;
    private LocalDateTime timestamp;
    private List<ErroValidacao> errosValidacao;

    // Representa um erro de validação de campo específico (Bean Validation)
    @Data
    @AllArgsConstructor
    public static class ErroValidacao {
        private String campo;
        private String mensagem;
    }
}
