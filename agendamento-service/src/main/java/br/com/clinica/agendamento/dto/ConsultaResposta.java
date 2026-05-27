package br.com.clinica.agendamento.dto;

import br.com.clinica.agendamento.entity.Consulta;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ConsultaResposta {

    private Long id;
    private Long pacienteId;
    private Long medicoId;
    private LocalDateTime dataHora;
    private String tipo;
    private String status;
    private Long consultaOriginalId;

    public ConsultaResposta(Consulta consulta) {
        this.id = consulta.getId();
        this.pacienteId = consulta.getPacienteId();
        this.medicoId = consulta.getMedicoId();
        this.dataHora = consulta.getDataHora();
        this.tipo = consulta.getTipo() != null ? consulta.getTipo().name() : null;
        this.status = consulta.getStatus() != null ? consulta.getStatus().name() : null;
        this.consultaOriginalId = consulta.getConsultaOriginalId();
    }
}