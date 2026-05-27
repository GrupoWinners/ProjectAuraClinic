package br.com.clinica.agendamento.repository;

import br.com.clinica.agendamento.entity.Consulta;
import br.com.clinica.agendamento.entity.StatusConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    // Verifica se o médico já tem outra consulta no mesmo horário que não foi cancelada
    boolean existsByMedicoIdAndDataHoraAndStatusNot(Long medicoId, LocalDateTime dataHora, StatusConsulta status);

    // Verifica se o paciente já tem outra consulta no mesmo horário que não foi cancelada
    boolean existsByPacienteIdAndDataHoraAndStatusNot(Long pacienteId, LocalDateTime dataHora, StatusConsulta status);
}