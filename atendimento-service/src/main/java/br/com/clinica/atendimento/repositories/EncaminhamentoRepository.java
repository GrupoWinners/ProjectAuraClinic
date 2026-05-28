package br.com.clinica.atendimento.repositories;

import br.com.clinica.atendimento.entity.Encaminhamento;
import br.com.clinica.atendimento.enums.StatusEncaminhamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EncaminhamentoRepository extends JpaRepository<Encaminhamento, Long> {

    List<Encaminhamento> findByMedicoDestinoId(Long medicoDestinoId);

    List<Encaminhamento> findByMedicoDestinoIdAndStatus(Long medicoDestinoId, StatusEncaminhamento status);
}