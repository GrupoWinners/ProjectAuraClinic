package br.com.clinica.agendamento.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultaRequest {
    private Long pacienteId;
    private Long medicoId;
    private Long convenioId; // ID que usaremos para chamar o Feign Client!
    private LocalDateTime dataHora;
}