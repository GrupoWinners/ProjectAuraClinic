package br.com.clinica.admin.validacao;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

// Anotação customizada de validação de CNPJ — aplica o algoritmo de verificação dos dígitos.
// Uso: @CnpjValido nos campos String de DTOs de entrada.
@Documented
@Constraint(validatedBy = ValidadorCnpj.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CnpjValido {
    String message() default "CNPJ inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
