package br.com.clinica.admin.exception;

// Exceção lançada quando um recurso solicitado não é encontrado no banco de dados.
// Mapeada para HTTP 404 pelo TratadorGlobalExcecoes.
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
