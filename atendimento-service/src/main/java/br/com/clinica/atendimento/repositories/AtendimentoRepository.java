package br.com.clinica.atendimento.repositories;

import br.com.clinica.atendimento.entities.Atendimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {

    List<Atendimento> findByProntuarioPacienteId(Long pacienteId);

    List<Atendimento> findByMedicoId(Long medicoId);

    Optional<Atendimento> findByConsultaId(Long consultaId);
}
