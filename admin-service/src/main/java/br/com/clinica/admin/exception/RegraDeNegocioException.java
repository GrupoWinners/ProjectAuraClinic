package br.com.clinica.admin.exception;

// Exceção lançada quando uma regra de negócio é violada (ex: especialidade inativa, convênio inválido).
// Mapeada para HTTP 422 pelo TratadorGlobalExcecoes.
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
