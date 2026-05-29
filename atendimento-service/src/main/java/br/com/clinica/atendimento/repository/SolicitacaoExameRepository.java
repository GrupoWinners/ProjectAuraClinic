package br.com.clinica.atendimento.repository;

import br.com.clinica.atendimento.entity.SolicitacaoExame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitacaoExameRepository extends JpaRepository<SolicitacaoExame, Long> {

    List<SolicitacaoExame> findByAtendimentoId(Long atendimentoId);
}