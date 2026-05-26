package br.com.clinica.agendamento.dto;

import br.com.clinica.agendamento.entity.Consulta;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ConsultaResposta {
    private Long id;
    private Long pacienteId;
    private Long medicoId;
    private LocalDateTime dataHora;
    private String status;
    private String tipo;
    private Long consultaOriginalId;

    public ConsultaResposta(Consulta consulta) {
        this.id = consulta.getId();
        this.pacienteId = consulta.getPacienteId();
        this.medicoId = consulta.getMedicoId();
        this.dataHora = consulta.getDataHora();
        this.status = consulta.getStatus().name();
        this.tipo = consulta.getTipo().name();
        this.consultaOriginalId = consulta.getConsultaOriginalId();
    }
}