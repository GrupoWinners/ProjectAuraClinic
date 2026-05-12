package br.com.clinica.commons.util;

public class ValidadorCnpj {
    public static boolean isValido(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) return false;
        return !cnpj.matches("(\\d)\\1{13}");
    }
}