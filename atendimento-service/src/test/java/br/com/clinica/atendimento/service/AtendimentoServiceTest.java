package br.com.clinica.atendimento.service;

import br.com.clinica.atendimento.client.AdminServiceClient;
import br.com.clinica.atendimento.client.AgendamentoServiceClient;
import br.com.clinica.atendimento.client.dto.ConsultaClientResposta;
import br.com.clinica.atendimento.client.dto.MedicoClientResposta;
import br.com.clinica.atendimento.client.dto.PacienteClientResposta;
import br.com.clinica.atendimento.dto.requisicao.AtendimentoRequisicao;
import br.com.clinica.atendimento.dto.requisicao.AtualizarUrgenciaRequisicao;
import br.com.clinica.atendimento.dto.requisicao.ReceitaRequisicao;
import br.com.clinica.atendimento.dto.requisicao.SolicitacaoExameRequisicao;
import br.com.clinica.atendimento.dto.resposta.AtendimentoResposta;
import br.com.clinica.atendimento.entity.Atendimento;
import br.com.clinica.atendimento.entity.Prontuario;
import br.com.clinica.atendimento.enums.NivelUrgencia;
import br.com.clinica.atendimento.enums.UrgenciaExame;
import br.com.clinica.atendimento.repository.AtendimentoRepository;
import br.com.clinica.commons.exception.RecursoNaoEncontradoException;
import br.com.clinica.commons.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AtendimentoServiceTest {

    @Mock
    private AtendimentoRepository atendimentoRepository;

    @Mock
    private ProntuarioService prontuarioService;

    @Mock
    private AdminServiceClient adminServiceClient;

    @Mock
    private AgendamentoServiceClient agendamentoServiceClient;

    @InjectMocks
    private AtendimentoService atendimentoService;

    @Test
    void deveRegistrarAtendimentoComSucesso() {
        AtendimentoRequisicao requisicao = criarAtendimentoRequisicao();

        PacienteClientResposta paciente = PacienteClientResposta.builder()
                .id(1L)
                .nomeCompleto("Henrique Vieira Rocha")
                .ativo(true)
                .build();

        MedicoClientResposta medico = MedicoClientResposta.builder()
                .id(1L)
                .nomeCompleto("Miguel Nader")
                .crm("3456312")
                .ativo(true)
                .build();

        ConsultaClientResposta consulta = ConsultaClientResposta.builder()
                .id(1L)
                .pacienteId(1L)
                .medicoId(1L)
                .dataHora(LocalDateTime.now().plusDays(1))
                .status("AGENDADA")
                .tipo("MEDICA")
                .build();

        Prontuario prontuario = Prontuario.builder()
                .id(1L)
                .pacienteId(1L)
                .build();

        Atendimento atendimentoSalvo = Atendimento.builder()
                .id(1L)
                .prontuario(prontuario)
                .consultaId(1L)
                .medicoId(1L)
                .dataAtendimento(LocalDateTime.now())
                .sintomas("Dor de cabeça")
                .diagnostico("Avaliação inicial")
                .escopoMedico("Consulta clínica")
                .observacoes("Paciente orientado")
                .nivelUrgencia(NivelUrgencia.AMARELO)
                .receitas(List.of())
                .solicitacoesExame(List.of())
                .build();

        when(adminServiceClient.buscarPacientePorId(1L)).thenReturn(paciente);
        when(adminServiceClient.buscarMedicoPorId(1L)).thenReturn(medico);
        when(agendamentoServiceClient.buscarConsultaPorId(1L)).thenReturn(consulta);
        when(prontuarioService.buscarOuCriarPorPacienteId(1L)).thenReturn(prontuario);
        when(atendimentoRepository.save(any(Atendimento.class))).thenReturn(atendimentoSalvo);

        AtendimentoResposta resposta = atendimentoService.registrar(requisicao);

        assertThat(resposta).isNotNull();
        assertThat(resposta.getId()).isEqualTo(1L);
        assertThat(resposta.getConsultaId()).isEqualTo(1L);
        assertThat(resposta.getMedicoId()).isEqualTo(1L);
        assertThat(resposta.getPacienteId()).isEqualTo(1L);
        assertThat(resposta.getNivelUrgencia()).isEqualTo(NivelUrgencia.AMARELO);

        verify(adminServiceClient).buscarPacientePorId(1L);
        verify(adminServiceClient).buscarMedicoPorId(1L);
        verify(agendamentoServiceClient).buscarConsultaPorId(1L);
        verify(prontuarioService).buscarOuCriarPorPacienteId(1L);
        verify(atendimentoRepository).save(any(Atendimento.class));
    }

    @Test
    void deveLancarErroQuandoPacienteEstiverInativo() {
        AtendimentoRequisicao requisicao = criarAtendimentoRequisicao();

        PacienteClientResposta pacienteInativo = PacienteClientResposta.builder()
                .id(1L)
                .ativo(false)
                .build();

        when(adminServiceClient.buscarPacientePorId(1L)).thenReturn(pacienteInativo);

        assertThatThrownBy(() -> atendimentoService.registrar(requisicao))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Paciente com ID 1 está inativo ou inválido");

        verify(adminServiceClient).buscarPacientePorId(1L);
        verifyNoInteractions(agendamentoServiceClient);
        verify(atendimentoRepository, never()).save(any());
    }

    @Test
    void deveLancarErroQuandoMedicoEstiverInativo() {
        AtendimentoRequisicao requisicao = criarAtendimentoRequisicao();

        PacienteClientResposta paciente = PacienteClientResposta.builder()
                .id(1L)
                .ativo(true)
                .build();

        MedicoClientResposta medicoInativo = MedicoClientResposta.builder()
                .id(1L)
                .ativo(false)
                .build();

        when(adminServiceClient.buscarPacientePorId(1L)).thenReturn(paciente);
        when(adminServiceClient.buscarMedicoPorId(1L)).thenReturn(medicoInativo);

        assertThatThrownBy(() -> atendimentoService.registrar(requisicao))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Médico com ID 1 está inativo ou inválido");

        verify(adminServiceClient).buscarPacientePorId(1L);
        verify(adminServiceClient).buscarMedicoPorId(1L);
        verifyNoInteractions(agendamentoServiceClient);
        verify(atendimentoRepository, never()).save(any());
    }

    @Test
    void deveLancarErroQuandoPacienteNaoCorresponderAoPacienteDaConsulta() {
        AtendimentoRequisicao requisicao = criarAtendimentoRequisicao();

        PacienteClientResposta paciente = PacienteClientResposta.builder()
                .id(1L)
                .ativo(true)
                .build();

        MedicoClientResposta medico = MedicoClientResposta.builder()
                .id(1L)
                .ativo(true)
                .build();

        ConsultaClientResposta consulta = ConsultaClientResposta.builder()
                .id(1L)
                .pacienteId(99L)
                .medicoId(1L)
                .status("AGENDADA")
                .tipo("MEDICA")
                .build();

        when(adminServiceClient.buscarPacientePorId(1L)).thenReturn(paciente);
        when(adminServiceClient.buscarMedicoPorId(1L)).thenReturn(medico);
        when(agendamentoServiceClient.buscarConsultaPorId(1L)).thenReturn(consulta);

        assertThatThrownBy(() -> atendimentoService.registrar(requisicao))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Paciente informado não corresponde ao paciente da consulta");

        verify(atendimentoRepository, never()).save(any());
    }

    @Test
    void deveLancarErroQuandoMedicoNaoCorresponderAoMedicoDaConsulta() {
        AtendimentoRequisicao requisicao = criarAtendimentoRequisicao();

        PacienteClientResposta paciente = PacienteClientResposta.builder()
                .id(1L)
                .ativo(true)
                .build();

        MedicoClientResposta medico = MedicoClientResposta.builder()
                .id(1L)
                .ativo(true)
                .build();

        ConsultaClientResposta consulta = ConsultaClientResposta.builder()
                .id(1L)
                .pacienteId(1L)
                .medicoId(99L)
                .status("AGENDADA")
                .tipo("MEDICA")
                .build();

        when(adminServiceClient.buscarPacientePorId(1L)).thenReturn(paciente);
        when(adminServiceClient.buscarMedicoPorId(1L)).thenReturn(medico);
        when(agendamentoServiceClient.buscarConsultaPorId(1L)).thenReturn(consulta);

        assertThatThrownBy(() -> atendimentoService.registrar(requisicao))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Médico informado não corresponde ao médico da consulta");

        verify(atendimentoRepository, never()).save(any());
    }

    @Test
    void deveLancarErroQuandoConsultaNaoEstiverAgendada() {
        AtendimentoRequisicao requisicao = criarAtendimentoRequisicao();

        PacienteClientResposta paciente = PacienteClientResposta.builder()
                .id(1L)
                .ativo(true)
                .build();

        MedicoClientResposta medico = MedicoClientResposta.builder()
                .id(1L)
                .ativo(true)
                .build();

        ConsultaClientResposta consulta = ConsultaClientResposta.builder()
                .id(1L)
                .pacienteId(1L)
                .medicoId(1L)
                .status("CANCELADA")
                .tipo("MEDICA")
                .build();

        when(adminServiceClient.buscarPacientePorId(1L)).thenReturn(paciente);
        when(adminServiceClient.buscarMedicoPorId(1L)).thenReturn(medico);
        when(agendamentoServiceClient.buscarConsultaPorId(1L)).thenReturn(consulta);

        assertThatThrownBy(() -> atendimentoService.registrar(requisicao))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Consulta precisa estar com status AGENDADA para registrar atendimento");

        verify(atendimentoRepository, never()).save(any());
    }

    @Test
    void deveBuscarAtendimentoPorIdComSucesso() {
        Prontuario prontuario = Prontuario.builder()
                .id(1L)
                .pacienteId(1L)
                .build();

        Atendimento atendimento = Atendimento.builder()
                .id(1L)
                .prontuario(prontuario)
                .consultaId(1L)
                .medicoId(1L)
                .nivelUrgencia(NivelUrgencia.VERDE)
                .receitas(List.of())
                .solicitacoesExame(List.of())
                .build();

        when(atendimentoRepository.findById(1L)).thenReturn(Optional.of(atendimento));

        AtendimentoResposta resposta = atendimentoService.buscarPorId(1L);

        assertThat(resposta).isNotNull();
        assertThat(resposta.getId()).isEqualTo(1L);
        assertThat(resposta.getPacienteId()).isEqualTo(1L);
    }

    @Test
    void deveLancarErroQuandoAtendimentoNaoForEncontrado() {
        when(atendimentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> atendimentoService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Atendimento com ID 99 não encontrado");
    }

    @Test
    void deveAtualizarUrgenciaDoAtendimento() {
        Prontuario prontuario = Prontuario.builder()
                .id(1L)
                .pacienteId(1L)
                .build();

        Atendimento atendimento = Atendimento.builder()
                .id(1L)
                .prontuario(prontuario)
                .consultaId(1L)
                .medicoId(1L)
                .nivelUrgencia(NivelUrgencia.VERDE)
                .receitas(List.of())
                .solicitacoesExame(List.of())
                .build();

        AtualizarUrgenciaRequisicao requisicao = AtualizarUrgenciaRequisicao.builder()
                .nivelUrgencia(NivelUrgencia.VERMELHO)
                .build();

        when(atendimentoRepository.findById(1L)).thenReturn(Optional.of(atendimento));
        when(atendimentoRepository.save(any(Atendimento.class))).thenReturn(atendimento);

        AtendimentoResposta resposta = atendimentoService.atualizarUrgencia(1L, requisicao);

        assertThat(resposta).isNotNull();
        assertThat(resposta.getNivelUrgencia()).isEqualTo(NivelUrgencia.VERMELHO);

        verify(atendimentoRepository).save(atendimento);
    }

    private AtendimentoRequisicao criarAtendimentoRequisicao() {
        ReceitaRequisicao receita = ReceitaRequisicao.builder()
                .medicamento("Dipirona")
                .dosagem("500mg")
                .frequencia("A cada 8 horas")
                .duracao("3 dias")
                .observacoes("Tomar após alimentação")
                .build();

        SolicitacaoExameRequisicao exame = SolicitacaoExameRequisicao.builder()
                .tipoExame("Hemograma")
                .descricao("Hemograma completo")
                .urgencia(UrgenciaExame.NORMAL)
                .build();

        return AtendimentoRequisicao.builder()
                .consultaId(1L)
                .medicoId(1L)
                .pacienteId(1L)
                .sintomas("Dor de cabeça")
                .diagnostico("Avaliação inicial")
                .escopoMedico("Consulta clínica")
                .observacoes("Paciente orientado")
                .nivelUrgencia(NivelUrgencia.AMARELO)
                .receitas(List.of(receita))
                .solicitacoesExame(List.of(exame))
                .build();
    }

}
