package br.com.clinica.admin.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

// Handler global de exceções — centraliza o tratamento de erros e garante respostas HTTP padronizadas.
// Intercepta todas as exceções customizadas e de validação antes de chegar ao cliente.
@RestControllerAdvice
@Slf4j
public class TratadorGlobalExcecoes {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<RespostaErro> tratarNaoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        log.warn("Recurso não encontrado: {}", ex.getMessage());
        return construirResposta(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<RespostaErro> tratarDuplicado(RecursoDuplicadoException ex, HttpServletRequest req) {
        log.warn("Recurso duplicado: {}", ex.getMessage());
        return construirResposta(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<RespostaErro> tratarNegocio(RegraDeNegocioException ex, HttpServletRequest req) {
        log.warn("Regra de negócio violada: {}", ex.getMessage());
        return construirResposta(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaErro> tratarValidacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        // Coleta todos os erros de campo para retornar de forma detalhada ao cliente
        List<RespostaErro.ErroValidacao> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new RespostaErro.ErroValidacao(fe.getField(), fe.getDefaultMessage()))
                .toList();

        RespostaErro resposta = RespostaErro.builder()
                .status(400)
                .erro("Requisição Inválida")
                .mensagem("Erro de validação nos campos enviados")
                .caminho(req.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errosValidacao(erros)
                .build();

        return ResponseEntity.badRequest().body(resposta);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaErro> tratarErroGenerico(Exception ex, HttpServletRequest req) {
        log.error("Erro interno não tratado: {}", ex.getMessage(), ex);
        return construirResposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor", req);
    }

    private ResponseEntity<RespostaErro> construirResposta(HttpStatus status, String mensagem, HttpServletRequest req) {
        RespostaErro resposta = RespostaErro.builder()
                .status(status.value())
                .erro(status.getReasonPhrase())
                .mensagem(mensagem)
                .caminho(req.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(resposta, status);
    }
}
