package br.com.clinica.admin.validacao;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

// Anotação customizada de validação de CPF — aplica o algoritmo de verificação dos dígitos.
// Uso: @CpfValido nos campos String de DTOs de entrada.
@Documented
@Constraint(validatedBy = ValidadorCpf.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfValido {
    String message() default "CPF inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
