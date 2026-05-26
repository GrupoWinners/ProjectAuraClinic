package br.com.clinica.agendamento.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaRequisicao {

    @NotNull(message = "O ID do paciente é obrigatório")
    private Long pacienteId;

    @NotNull(message = "O ID do médico é obrigatório")
    private Long medicoId;

    @NotNull(message = "A data e hora são obrigatórias")
    private LocalDateTime dataHora;

    @NotNull(message = "O tipo de consulta é obrigatório")
    private String tipo;

    private LocalDateTime novaDataHora;
}