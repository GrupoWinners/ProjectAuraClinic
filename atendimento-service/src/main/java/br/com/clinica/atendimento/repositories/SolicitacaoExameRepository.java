package br.com.clinica.atendimento.repositories;

import br.com.clinica.atendimento.entities.SolicitacaoExame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitacaoExameRepository extends JpaRepository<SolicitacaoExame, Long> {

    List<SolicitacaoExame> findByAtendimentoId(Long atendimentoId);
}