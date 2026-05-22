package br.com.clinica.atendimento.repositories;

import br.com.clinica.atendimento.entities.Receita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceitaRepository extends JpaRepository<Receita, Long> {

    List<Receita> findByAtendimentoId(Long atendimentoId);
}