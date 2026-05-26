package br.com.clinica.agendamento.service;

import br.com.clinica.agendamento.client.AdminServiceClient;
import br.com.clinica.agendamento.dto.CancelamentoRequisicao;
import br.com.clinica.agendamento.dto.ConsultaRequisicao;
import br.com.clinica.agendamento.dto.ConsultaResposta;
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

    @Mock
    private AdminServiceClient adminServiceClient;

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
    @DisplayName("Deve agendar uma consulta com sucesso ao validar clientes externos ativos e horários livres")
    void deveAgendarConsultaComSucesso() {
        ConsultaRequisicao requisicao = new ConsultaRequisicao(10L, 20L, dataFutura, "MEDICA", null, 30L);

        Mockito.when(adminServiceClient.validarPacienteAtivo(10L)).thenReturn(true);
        Mockito.when(adminServiceClient.validarMedicoAtivo(20L)).thenReturn(true);
        Mockito.when(adminServiceClient.validarConvenioAtivo(30L)).thenReturn(true);

        Mockito.when(consultaRepository.existsByMedicoIdAndDataHoraAndStatusNot(20L, dataFutura, StatusConsulta.CANCELADA)).thenReturn(false);
        Mockito.when(consultaRepository.existsByPacienteIdAndDataHoraAndStatusNot(10L, dataFutura, StatusConsulta.CANCELADA)).thenReturn(false);
        Mockito.when(consultaRepository.save(any(Consulta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConsultaResposta resultado = agendamentoService.agendarConsulta(requisicao);

        assertNotNull(resultado);
        assertEquals("AGENDADA", resultado.getStatus());
    }

    @Test
    @DisplayName("Deve lançar exceção se o paciente não estiver ativo no microsserviço admin")
    void deveLancarExcecaoQuandoPacienteInativo() {
        ConsultaRequisicao requisicao = new ConsultaRequisicao(10L, 20L, dataFutura, "MEDICA", null, null);

        Mockito.when(adminServiceClient.validarPacienteAtivo(10L)).thenReturn(false);

        assertThrows(RegraDeNegocioException.class, () -> agendamentoService.agendarConsulta(requisicao));
    }

    @Test
    @DisplayName("Deve lançar exceção se o convênio informado estiver inativo no microsserviço admin")
    void deveLancarExcecaoQuandoConvenioInativo() {
        ConsultaRequisicao requisicao = new ConsultaRequisicao(10L, 20L, dataFutura, "MEDICA", null, 30L);

        Mockito.when(adminServiceClient.validarPacienteAtivo(10L)).thenReturn(true);
        Mockito.when(adminServiceClient.validarMedicoAtivo(20L)).thenReturn(true);
        Mockito.when(adminServiceClient.validarConvenioAtivo(30L)).thenReturn(false);

        assertThrows(RegraDeNegocioException.class, () -> agendamentoService.agendarConsulta(requisicao));
    }

    @Test
    @DisplayName("Deve remarcar consulta com sucesso aplicando as regras de validação de horário na nova data")
    void deveRemarcarConsultaComSucesso() {
        LocalDateTime novaData = dataFutura.plusDays(1);
        ConsultaRequisicao requisicao = new ConsultaRequisicao(null, null, null, null, novaData, null);

        Mockito.when(consultaRepository.findById(1L)).thenReturn(Optional.of(consultaPadrao));
        Mockito.when(consultaRepository.existsByMedicoIdAndDataHoraAndStatusNot(20L, novaData, StatusConsulta.CANCELADA)).thenReturn(false);
        Mockito.when(consultaRepository.existsByPacienteIdAndDataHoraAndStatusNot(10L, novaData, StatusConsulta.CANCELADA)).thenReturn(false);
        Mockito.when(consultaRepository.save(any(Consulta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConsultaResposta resultado = agendamentoService.remarcarConsulta(1L, requisicao);

        assertEquals(StatusConsulta.CANCELADA, consultaPadrao.getStatus());
        assertNotNull(resultado);
        assertEquals("REMARCADA", resultado.getStatus());
        assertEquals(1L, resultado.getConsultaOriginalId());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cancelar consulta inexistente")
    void deveLancarExcecaoAoCancelarInexistente() {
        CancelamentoRequisicao requisicao = new CancelamentoRequisicao("Motivo", "MEDICO");
        Mockito.when(consultaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> agendamentoService.cancelarConsulta(99L, requisicao));
    }
}