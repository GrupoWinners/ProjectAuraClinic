package br.com.clinica.atendimento.service;

import br.com.clinica.atendimento.entity.Prontuario;
import br.com.clinica.atendimento.repository.ProntuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProntuarioService {

    private final ProntuarioRepository prontuarioRepository;

    public Prontuario buscarOuCriarPorPacienteId(Long pacienteId) {
        return prontuarioRepository.findByPacienteId(pacienteId)
                .orElseGet(() -> criarProntuario(pacienteId));
    }

    private Prontuario criarProntuario(Long pacienteId) {
        Prontuario prontuario = Prontuario.builder()
                .pacienteId(pacienteId)
                .build();

        return prontuarioRepository.save(prontuario);
    }
}