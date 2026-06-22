package br.com.clinica.admin.validacao;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// Implementação do algoritmo oficial de validação de CPF (Módulo 11).
// Remove formatação (pontos e traços) antes de validar — aceita "000.000.000-00" ou "00000000000".
public class ValidadorCpf implements ConstraintValidator<CpfValido, String> {

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        if (cpf == null || cpf.isBlank()) {
            // Nulo e vazio são tratados por @NotBlank — aqui apenas validamos o algoritmo
            return true;
        }

        // Remove pontos, traços e espaços antes de aplicar o algoritmo
        String cpfLimpo = cpf.replaceAll("[.\\-\\s]", "");

        if (cpfLimpo.length() != 11) return false;
        if (cpfLimpo.matches("(\\d)\\1{10}")) return false; // Rejeita sequências iguais (111.111.111-11)

        return validarDigitos(cpfLimpo);
    }

    // Aplica o algoritmo Módulo 11 para validar os dois dígitos verificadores do CPF
    private boolean validarDigitos(String cpf) {
        // Calcula o primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int primeiroDigito = 11 - (soma % 11);
        if (primeiroDigito >= 10) primeiroDigito = 0;

        if (primeiroDigito != Character.getNumericValue(cpf.charAt(9))) return false;

        // Calcula o segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int segundoDigito = 11 - (soma % 11);
        if (segundoDigito >= 10) segundoDigito = 0;

        return segundoDigito == Character.getNumericValue(cpf.charAt(10));
    }
}
