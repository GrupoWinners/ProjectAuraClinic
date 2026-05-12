package br.com.clinica.commons.exception;

import br.com.clinica.commons.dto.RespostaErro;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class TratadorGlobalExcecoes {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<RespostaErro> handleNotFound(RecursoNaoEncontradoException ex, WebRequest request) {
        RespostaErro erro = RespostaErro.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .erro("Não Encontrado")
                .mensagem(ex.getMessage())
                .caminho(request.getDescription(false))
                .build();
        return new ResponseEntity<>(erro, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<RespostaErro> handleBusiness(RegraDeNegocioException ex, WebRequest request) {
        RespostaErro erro = RespostaErro.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .erro("Regra de Negócio")
                .mensagem(ex.getMessage())
                .caminho(request.getDescription(false))
                .build();
        return new ResponseEntity<>(erro, HttpStatus.BAD_REQUEST);
    }
}