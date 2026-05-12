package br.com.clinica.commons.util;

public class ValidadorCpf {
    public static boolean isValido(String cpf) {
        if (cpf == null || cpf.length() != 11) return false;
        return !cpf.matches("(\\d)\\1{10}");
    }
}