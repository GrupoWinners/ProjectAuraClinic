package br.com.clinica.atendimento.service;

import br.com.clinica.atendimento.client.AdminServiceClient;
import br.com.clinica.atendimento.client.dto.MedicoClientResposta;
import br.com.clinica.atendimento.dto.requisicao.AtualizarStatusEncaminhamentoRequisicao;
import br.com.clinica.atendimento.dto.requisicao.EncaminhamentoRequisicao;
import br.com.clinica.atendimento.dto.resposta.EncaminhamentoResposta;
import br.com.clinica.atendimento.entity.Atendimento;
import br.com.clinica.atendimento.entity.Encaminhamento;
import br.com.clinica.atendimento.entity.Prontuario;
import br.com.clinica.atendimento.enums.PrioridadeEncaminhamento;
import br.com.clinica.atendimento.enums.StatusEncaminhamento;
import br.com.clinica.atendimento.repository.EncaminhamentoRepository;
import br.com.clinica.commons.exception.RecursoNaoEncontradoException;
import br.com.clinica.commons.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EncaminhamentoServiceTest {

    @Mock
    private EncaminhamentoRepository encaminhamentoRepository;

    @Mock
    private AtendimentoService atendimentoService;

    @Mock
    private AdminServiceClient adminServiceClient;

    @InjectMocks
    private EncaminhamentoService encaminhamentoService;

    @Test
    void deveCriarEncaminhamentoComSucesso() {
        EncaminhamentoRequisicao requisicao = EncaminhamentoRequisicao.builder()
                .atendimentoId(1L)
                .medicoOrigemId(1L)
                .medicoDestinoId(2L)
                .motivo("Paciente com dores crônicas de cabeça")
                .especialidadeDestino("Neurologista")
                .prioridade(PrioridadeEncaminhamento.BAIXA)
                .build();

        Atendimento atendimento = criarAtendimento();

        MedicoClientResposta medicoDestino = MedicoClientResposta.builder()
                .id(2L)
                .nomeCompleto("Médico Destino")
                .ativo(true)
                .build();

        Encaminhamento encaminhamentoSalvo = Encaminhamento.builder()
                .id(1L)
                .atendimento(atendimento)
                .medicoOrigemId(1L)
                .medicoDestinoId(2L)
                .motivo("Paciente com dores crônicas de cabeça")
                .especialidadeDestino("Neurologista")
                .prioridade(PrioridadeEncaminhamento.BAIXA)
                .status(StatusEncaminhamento.PENDENTE)
                .build();

        when(atendimentoService.buscarEntidadePorId(1L)).thenReturn(atendimento);
        when(adminServiceClient.buscarMedicoPorId(2L)).thenReturn(medicoDestino);
        when(encaminhamentoRepository.save(any(Encaminhamento.class))).thenReturn(encaminhamentoSalvo);

        EncaminhamentoResposta resposta = encaminhamentoService.criar(requisicao);

        assertThat(resposta).isNotNull();
        assertThat(resposta.getId()).isEqualTo(1L);
        assertThat(resposta.getAtendimentoId()).isEqualTo(1L);
        assertThat(resposta.getMedicoDestinoId()).isEqualTo(2L);
        assertThat(resposta.getPrioridade()).isEqualTo(PrioridadeEncaminhamento.BAIXA);
        assertThat(resposta.getStatus()).isEqualTo(StatusEncaminhamento.PENDENTE);

        verify(atendimentoService).buscarEntidadePorId(1L);
        verify(adminServiceClient).buscarMedicoPorId(2L);
        verify(encaminhamentoRepository).save(any(Encaminhamento.class));
    }

    @Test
    void deveLancarErroQuandoMedicoDestinoEstiverInativo() {
        EncaminhamentoRequisicao requisicao = EncaminhamentoRequisicao.builder()
                .atendimentoId(1L)
                .medicoOrigemId(1L)
                .medicoDestinoId(2L)
                .motivo("Encaminhamento")
                .especialidadeDestino("Neurologista")
                .prioridade(PrioridadeEncaminhamento.MEDIA)
                .build();

        MedicoClientResposta medicoDestinoInativo = MedicoClientResposta.builder()
                .id(2L)
                .ativo(false)
                .build();

        when(atendimentoService.buscarEntidadePorId(1L)).thenReturn(criarAtendimento());
        when(adminServiceClient.buscarMedicoPorId(2L)).thenReturn(medicoDestinoInativo);

        assertThatThrownBy(() -> encaminhamentoService.criar(requisicao))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Médico destino com ID 2 está inativo ou inválido");

        verify(encaminhamentoRepository, never()).save(any());
    }

    @Test
    void deveBuscarEncaminhamentoPorIdComSucesso() {
        Encaminhamento encaminhamento = Encaminhamento.builder()
                .id(1L)
                .atendimento(criarAtendimento())
                .medicoOrigemId(1L)
                .medicoDestinoId(2L)
                .motivo("Encaminhamento")
                .especialidadeDestino("Neurologista")
                .prioridade(PrioridadeEncaminhamento.MEDIA)
                .status(StatusEncaminhamento.PENDENTE)
                .build();

        when(encaminhamentoRepository.findById(1L)).thenReturn(Optional.of(encaminhamento));

        EncaminhamentoResposta resposta = encaminhamentoService.buscarPorId(1L);

        assertThat(resposta).isNotNull();
        assertThat(resposta.getId()).isEqualTo(1L);
        assertThat(resposta.getMedicoDestinoId()).isEqualTo(2L);
    }

    @Test
    void deveLancarErroQuandoEncaminhamentoNaoForEncontrado() {
        when(encaminhamentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> encaminhamentoService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Encaminhamento com ID 99 não encontrado");
    }

    @Test
    void deveAtualizarStatusDoEncaminhamento() {
        Encaminhamento encaminhamento = Encaminhamento.builder()
                .id(1L)
                .atendimento(criarAtendimento())
                .medicoOrigemId(1L)
                .medicoDestinoId(2L)
                .motivo("Encaminhamento")
                .especialidadeDestino("Neurologista")
                .prioridade(PrioridadeEncaminhamento.BAIXA)
                .status(StatusEncaminhamento.PENDENTE)
                .build();

        AtualizarStatusEncaminhamentoRequisicao requisicao = AtualizarStatusEncaminhamentoRequisicao.builder()
                .status(StatusEncaminhamento.ACEITO)
                .build();

        when(encaminhamentoRepository.findById(1L)).thenReturn(Optional.of(encaminhamento));
        when(encaminhamentoRepository.save(any(Encaminhamento.class))).thenReturn(encaminhamento);

        EncaminhamentoResposta resposta = encaminhamentoService.atualizarStatus(1L, requisicao);

        assertThat(resposta).isNotNull();
        assertThat(resposta.getStatus()).isEqualTo(StatusEncaminhamento.ACEITO);

        verify(encaminhamentoRepository).save(encaminhamento);
    }

    @Test
    void deveListarEncaminhamentosPorMedicoDestino() {
        Encaminhamento encaminhamento = Encaminhamento.builder()
                .id(1L)
                .atendimento(criarAtendimento())
                .medicoOrigemId(1L)
                .medicoDestinoId(2L)
                .motivo("Encaminhamento")
                .especialidadeDestino("Neurologista")
                .prioridade(PrioridadeEncaminhamento.MEDIA)
                .status(StatusEncaminhamento.PENDENTE)
                .build();

        when(encaminhamentoRepository.findByMedicoDestinoId(2L))
                .thenReturn(List.of(encaminhamento));

        List<EncaminhamentoResposta> resposta = encaminhamentoService.listarPorMedicoDestino(2L);

        assertThat(resposta).hasSize(1);
        assertThat(resposta.get(0).getMedicoDestinoId()).isEqualTo(2L);
    }

    private Atendimento criarAtendimento() {
        Prontuario prontuario = Prontuario.builder()
                .id(1L)
                .pacienteId(1L)
                .build();

        return Atendimento.builder()
                .id(1L)
                .prontuario(prontuario)
                .build();
    }
}

