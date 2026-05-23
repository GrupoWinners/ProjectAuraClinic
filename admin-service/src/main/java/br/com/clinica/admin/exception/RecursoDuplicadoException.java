package br.com.clinica.admin.exception;

// Exceção lançada quando há tentativa de cadastrar um recurso com dados já existentes (CPF, CRM, CNPJ).
// Mapeada para HTTP 409 pelo TratadorGlobalExcecoes.
public class RecursoDuplicadoException extends RuntimeException {
    public RecursoDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
