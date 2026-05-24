package br.com.clinica.agendamento.service;

import br.com.clinica.agendamento.dto.CancelamentoRequest;
import br.com.clinica.agendamento.dto.ConsultaRequest;
import br.com.clinica.agendamento.entity.Cancelamento;
import br.com.clinica.agendamento.entity.Consulta;
import br.com.clinica.agendamento.entity.StatusConsulta;
import br.com.clinica.agendamento.entity.TipoConsulta;
import br.com.clinica.agendamento.repository.CancelamentoRepository;
import br.com.clinica.agendamento.repository.ConsultaRepository;
import br.com.clinica.commons.exception.RecursoNaoEncontradoException;
import br.com.clinica.commons.exception.RegraDeNegocioException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private CancelamentoRepository cancelamentoRepository;

    @InjectMocks
    private AgendamentoService agendamentoService;

    private Consulta consultaPadrao;
    private LocalDateTime dataFutura;

    @BeforeEach
    void setUp() {
        dataFutura = LocalDateTime.now().plusDays(2);
        consultaPadrao = new Consulta();
        consultaPadrao.setId(1L);
        consultaPadrao.setPacienteId(10L);
        consultaPadrao.setMedicoId(20L);
        consultaPadrao.setDataHora(dataFutura);
        consultaPadrao.setStatus(StatusConsulta.AGENDADA);
        consultaPadrao.setTipo(TipoConsulta.MEDICA);
    }

    @Test
    @DisplayName("Deve agendar uma consulta com sucesso quando o horário estiver livre")
    void deveAgendarConsultaComSucesso() {
        ConsultaRequest request = new ConsultaRequest(10L, 20L, dataFutura, "MEDICA", null);

        Mockito.when(consultaRepository.existsByMedicoIdAndDataHoraAndStatusNot(
                20L, dataFutura, StatusConsulta.CANCELADA)).thenReturn(false);

        Mockito.when(consultaRepository.save(any(Consulta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Consulta resultado = agendamentoService.agendarConsulta(request);

        assertNotNull(resultado);
        assertEquals(StatusConsulta.AGENDADA, resultado.getStatus());
        assertEquals(TipoConsulta.MEDICA, resultado.getTipo());
    }

    @Test
    @DisplayName("Deve lançar exceção ao agendar consulta se o médico já tiver horário ocupado")
    void deveLancarExcecaoQuandoHorarioOcupado() {
        ConsultaRequest request = new ConsultaRequest(10L, 20L, dataFutura, "MEDICA", null);

        Mockito.when(consultaRepository.existsByMedicoIdAndDataHoraAndStatusNot(
                20L, dataFutura, StatusConsulta.CANCELADA)).thenReturn(true);

        assertThrows(RegraDeNegocioException.class, () -> agendamentoService.agendarConsulta(request));
    }

    @Test
    @DisplayName("Regra 10: Deve remarcar consulta com sucesso alterando a original para CANCELADA e criando uma nova")
    void deveRemarcarConsultaComSucesso() {
        LocalDateTime novaData = dataFutura.plusDays(1);
        ConsultaRequest request = new ConsultaRequest(null, null, null, null, novaData);

        Mockito.when(consultaRepository.findById(1L)).thenReturn(Optional.of(consultaPadrao));
        Mockito.when(consultaRepository.save(any(Consulta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Consulta novaConsulta = agendamentoService.remarcarConsulta(1L, request);

        // Valida que a consulta original foi cancelada (Imutabilidade da linha)
        assertEquals(StatusConsulta.CANCELADA, consultaPadrao.getStatus());

        // Valida os dados da nova linha gerada no banco de dados
        assertNotNull(novaConsulta);
        assertEquals(StatusConsulta.REMARCADA, novaConsulta.getStatus());
        assertEquals(novaData, novaConsulta.getDataHora());
        assertEquals(1L, novaConsulta.getConsultaOriginalId()); // Vínculo ID
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar remarcar uma consulta que já está cancelada")
    void deveLancarExcecaoAoRemarcarConsultaJaCancelada() {
        consultaPadrao.setStatus(StatusConsulta.CANCELADA);
        ConsultaRequest request = new ConsultaRequest(null, null, null, null, dataFutura);

        Mockito.when(consultaRepository.findById(1L)).thenReturn(Optional.of(consultaPadrao));

        assertThrows(RegraDeNegocioException.class, () -> agendamentoService.remarcarConsulta(1L, request));
    }

    @Test
    @DisplayName("Deve cancelar uma consulta ativa com sucesso registrando o motivo e autor")
    void deveCancelarConsultaComSucesso() {
        CancelamentoRequest request = new CancelamentoRequest("Paciente desistiu", "PACIENTE");

        Mockito.when(consultaRepository.findById(1L)).thenReturn(Optional.of(consultaPadrao));

        agendamentoService.cancelarConsulta(1L, request);

        assertEquals(StatusConsulta.CANCELADA, consultaPadrao.getStatus());
        Mockito.verify(cancelamentoRepository, Mockito.times(1)).save(any(Cancelamento.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cancelar consulta inexistente")
    void deveLancarExcecaoAoCancelarInexistente() {
        CancelamentoRequest request = new CancelamentoRequest("Motivo", "MEDICO");

        Mockito.when(consultaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> agendamentoService.cancelarConsulta(99L, request));
    }
}