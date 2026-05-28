package br.com.clinica.atendimento.repository;

import br.com.clinica.atendimento.entity.Prontuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProntuarioRepository extends JpaRepository<Prontuario, Long> {

    Optional<Prontuario> findByPacienteId(Long pacienteId);

    boolean existsByPacienteId(Long pacienteId);
}
