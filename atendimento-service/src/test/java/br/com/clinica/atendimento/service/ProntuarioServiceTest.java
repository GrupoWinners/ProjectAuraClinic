package br.com.clinica.atendimento.service;

import br.com.clinica.atendimento.entity.Prontuario;
import br.com.clinica.atendimento.repository.ProntuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProntuarioServiceTest {

    @Mock
    private ProntuarioRepository prontuarioRepository;

    @InjectMocks
    private ProntuarioService prontuarioService;

    @Test
    void deveRetornarProntuarioExistenteQuandoPacienteJaPossuiProntuario(){
        Long pacienteId = 1L;

        Prontuario prontuarioExistente = Prontuario.builder()
                .id(10L)
                .pacienteId(pacienteId)
                .build();

        when(prontuarioRepository.findByPacienteId(pacienteId))
                .thenReturn(Optional.of(prontuarioExistente));

        Prontuario resultado = prontuarioService.buscarOuCriarPorPacienteId(pacienteId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(10L);
        assertThat(resultado.getPacienteId()).isEqualTo(pacienteId);

        verify(prontuarioRepository).findByPacienteId(pacienteId);
        verify(prontuarioRepository, never()).save(any());
    }
    //Se já existe prontuário, retorna o existente

    @Test
    void deveCriarProntuarioQuandoPacienteNaoPossuirProntuario(){
        Long pacienteId = 1L;

        Prontuario prontuarioSalvo = Prontuario.builder()
                .id(10L)
                .pacienteId(pacienteId)
                .build();

        when(prontuarioRepository.findByPacienteId(pacienteId))
                .thenReturn(Optional.empty());

        when(prontuarioRepository.save(any(Prontuario.class)))
                .thenReturn(prontuarioSalvo);

        Prontuario resultado = prontuarioService.buscarOuCriarPorPacienteId(pacienteId);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(10L);
        assertThat(resultado.getPacienteId()).isEqualTo(pacienteId);

        verify(prontuarioRepository).findByPacienteId(pacienteId);
        verify(prontuarioRepository).save(any(Prontuario.class));
    }
    //Se não existe, cria um novo
}

