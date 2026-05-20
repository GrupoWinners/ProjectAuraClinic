package br.com.clinica.admin.repository;

import br.com.clinica.admin.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface MedicoRepository extends JpaRepository<Medico, Long> {
    boolean existsByCrm(String crm);
}
