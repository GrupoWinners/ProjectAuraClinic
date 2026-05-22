package br.com.clinica.agendamento.service;

import br.com.clinica.agendamento.client.dto.AdminClient;
import br.com.clinica.agendamento.client.dto.ConvenioResponse;
import br.com.clinica.agendamento.dto.ConsultaRequest;
import br.com.clinica.agendamento.entity.Consulta;
import br.com.clinica.agendamento.entity.StatusConsulta;
import br.com.clinica.agendamento.repository.CancelamentoRepository;
import br.com.clinica.agendamento.repository.ConsultaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private CancelamentoRepository cancelamentoRepository;

    @Mock
    private AdminClient adminClient;

    @InjectMocks
    private AgendamentoService agendamentoService;

    @Test
    @DisplayName("Deve agendar consulta com sucesso quando dados forem válidos")
    void agendarConsultaCenárioSucesso() {
        // Arrange
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1);
        ConsultaRequest request = new ConsultaRequest(1L, 2L, 10L, dataHora);
        ConvenioResponse convenioAtivo = new ConvenioResponse(10L, "Plano Plus", true);

        when(adminClient.buscarConvenioPorId(10L)).thenReturn(convenioAtivo);
        when(consultaRepository.existsByMedicoIdAndDataHoraAndStatusNot(2L, dataHora, StatusConsulta.CANCELADA)).thenReturn(false);
        when(consultaRepository.existsByPacienteIdAndDataHoraAndStatusNot(1L, dataHora, StatusConsulta.CANCELADA)).thenReturn(false);

        Consulta consultaSalva = Consulta.builder().id(100L).pacienteId(1L).medicoId(2L).dataHora(dataHora).status(StatusConsulta.AGENDADA).build();
        when(consultaRepository.save(any(Consulta.class))).thenReturn(consultaSalva);

        // Act
        Consulta resultado = agendamentoService.agendarConsulta(request);

        // Assert
        assertNotNull(resultado);
        assertEquals(StatusConsulta.AGENDADA, resultado.getStatus());
        verify(consultaRepository, times(1)).save(any(Consulta.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o convênio não estiver ativo")
    void agendarConsultaConvenioInativo() {
        // Arrange
        ConsultaRequest request = new ConsultaRequest(1L, 2L, 10L, LocalDateTime.now());
        ConvenioResponse convenioInativo = new ConvenioResponse(10L, "Plano Antigo", false);

        when(adminClient.buscarConvenioPorId(10L)).thenReturn(convenioInativo);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> agendamentoService.agendarConsulta(request));
        assertTrue(exception.getMessage().contains("O convênio informado não está ativo"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando houver conflito de horário para o médico")
    void agendarConsultaConflitoMedico() {
        // Arrange
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1);
        ConsultaRequest request = new ConsultaRequest(1L, 2L, 10L, dataHora);
        ConvenioResponse convenioAtivo = new ConvenioResponse(10L, "Plano Plus", true);

        when(adminClient.buscarConvenioPorId(10L)).thenReturn(convenioAtivo);
        when(consultaRepository.existsByMedicoIdAndDataHoraAndStatusNot(2L, dataHora, StatusConsulta.CANCELADA)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> agendamentoService.agendarConsulta(request));
        assertTrue(exception.getMessage().contains("O médico já possui uma consulta agendada"));
    }
}