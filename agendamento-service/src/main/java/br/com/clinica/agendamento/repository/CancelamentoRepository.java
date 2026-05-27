package br.com.clinica.agendamento.repository;

import br.com.clinica.agendamento.entity.Cancelamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CancelamentoRepository extends JpaRepository<Cancelamento, Long> {
}