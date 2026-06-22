package br.com.clinica.admin.validacao;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// Implementação do algoritmo oficial de validação de CNPJ (Módulo 11 com pesos específicos).
// Remove formatação (pontos, barras e traços) antes de validar — aceita "XX.XXX.XXX/XXXX-XX" ou raw.
public class ValidadorCnpj implements ConstraintValidator<CnpjValido, String> {

    @Override
    public boolean isValid(String cnpj, ConstraintValidatorContext context) {
        if (cnpj == null || cnpj.isBlank()) {
            // Nulo e vazio são tratados por @NotBlank — aqui apenas validamos o algoritmo
            return true;
        }

        // Remove pontos, barras, traços e espaços antes de aplicar o algoritmo
        String cnpjLimpo = cnpj.replaceAll("[.\\-/\\s]", "");

        if (cnpjLimpo.length() != 14) return false;
        if (cnpjLimpo.matches("(\\d)\\1{13}")) return false; // Rejeita sequências iguais (00.000.000/0000-00)

        return validarDigitos(cnpjLimpo);
    }

    // Aplica o algoritmo Módulo 11 com os pesos específicos do CNPJ para validar os dígitos verificadores
    private boolean validarDigitos(String cnpj) {
        int[] pesoPrimeiro  = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesoSegundo   = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        // Calcula o primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += Character.getNumericValue(cnpj.charAt(i)) * pesoPrimeiro[i];
        }
        int primeiroDigito = soma % 11 < 2 ? 0 : 11 - (soma % 11);

        if (primeiroDigito != Character.getNumericValue(cnpj.charAt(12))) return false;

        // Calcula o segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 13; i++) {
            soma += Character.getNumericValue(cnpj.charAt(i)) * pesoSegundo[i];
        }
        int segundoDigito = soma % 11 < 2 ? 0 : 11 - (soma % 11);

        return segundoDigito == Character.getNumericValue(cnpj.charAt(13));
    }
}
