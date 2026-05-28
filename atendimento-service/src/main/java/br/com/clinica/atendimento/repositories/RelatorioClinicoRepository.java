package br.com.clinica.atendimento.repositories;

import br.com.clinica.atendimento.entity.RelatorioClinico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelatorioClinicoRepository extends JpaRepository<RelatorioClinico, Long> {

    List<RelatorioClinico> findByAtendimentoProntuarioPacienteId(Long pacienteId);
}