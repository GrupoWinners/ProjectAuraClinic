package br.com.clinica.atendimento.service;

import br.com.clinica.atendimento.dto.requisicao.RelatorioClinicoRequisicao;
import br.com.clinica.atendimento.dto.resposta.RelatorioClinicoResposta;
import br.com.clinica.atendimento.entity.Atendimento;
import br.com.clinica.atendimento.entity.Prontuario;
import br.com.clinica.atendimento.entity.RelatorioClinico;
import br.com.clinica.atendimento.repository.RelatorioClinicoRepository;
import br.com.clinica.commons.exception.RecursoNaoEncontradoException;
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
class RelatorioClinicoServiceTest {

    @Mock
    private RelatorioClinicoRepository relatorioClinicoRepository;

    @Mock
    private AtendimentoService atendimentoService;

    @InjectMocks
    private RelatorioClinicoService relatorioClinicoService;

    @Test
    void deveCriarRelatorioClinicoComSucesso() {
        RelatorioClinicoRequisicao requisicao = RelatorioClinicoRequisicao.builder()
                .atendimentoId(1L)
                .conteudo("Paciente apresentou melhora após medicação.")
                .build();

        Atendimento atendimento = criarAtendimento();

        RelatorioClinico relatorioSalvo = RelatorioClinico.builder()
                .id(1L)
                .atendimento(atendimento)
                .conteudo(requisicao.getConteudo())
                .build();

        when(atendimentoService.buscarEntidadePorId(1L)).thenReturn(atendimento);
        when(relatorioClinicoRepository.save(any(RelatorioClinico.class))).thenReturn(relatorioSalvo);

        RelatorioClinicoResposta resposta = relatorioClinicoService.criar(requisicao);

        assertThat(resposta).isNotNull();
        assertThat(resposta.getId()).isEqualTo(1L);
        assertThat(resposta.getAtendimentoId()).isEqualTo(1L);
        assertThat(resposta.getConteudo()).isEqualTo("Paciente apresentou melhora após medicação.");

        verify(atendimentoService).buscarEntidadePorId(1L);
        verify(relatorioClinicoRepository).save(any(RelatorioClinico.class));
    }

    @Test
    void deveBuscarRelatorioClinicoPorIdComSucesso() {
        RelatorioClinico relatorio = RelatorioClinico.builder()
                .id(1L)
                .atendimento(criarAtendimento())
                .conteudo("Relatório clínico")
                .build();

        when(relatorioClinicoRepository.findById(1L)).thenReturn(Optional.of(relatorio));

        RelatorioClinicoResposta resposta = relatorioClinicoService.buscarPorId(1L);

        assertThat(resposta).isNotNull();
        assertThat(resposta.getId()).isEqualTo(1L);
        assertThat(resposta.getConteudo()).isEqualTo("Relatório clínico");
    }

    @Test
    void deveLancarErroQuandoRelatorioNaoForEncontrado() {
        when(relatorioClinicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> relatorioClinicoService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Relatório clínico com ID 99 não encontrado");
    }

    @Test
    void deveAtualizarRelatorioClinicoComSucesso() {
        RelatorioClinicoRequisicao requisicao = RelatorioClinicoRequisicao.builder()
                .atendimentoId(1L)
                .conteudo("Conteúdo atualizado")
                .build();

        RelatorioClinico relatorio = RelatorioClinico.builder()
                .id(1L)
                .atendimento(criarAtendimento())
                .conteudo("Conteúdo antigo")
                .build();

        when(relatorioClinicoRepository.findById(1L)).thenReturn(Optional.of(relatorio));
        when(relatorioClinicoRepository.save(any(RelatorioClinico.class))).thenReturn(relatorio);

        RelatorioClinicoResposta resposta = relatorioClinicoService.atualizar(1L, requisicao);

        assertThat(resposta).isNotNull();
        assertThat(resposta.getConteudo()).isEqualTo("Conteúdo atualizado");

        verify(relatorioClinicoRepository).save(relatorio);
    }

    @Test
    void deveListarRelatoriosPorPaciente() {
        RelatorioClinico relatorio = RelatorioClinico.builder()
                .id(1L)
                .atendimento(criarAtendimento())
                .conteudo("Relatório clínico")
                .build();

        when(relatorioClinicoRepository.findByAtendimentoProntuarioPacienteId(1L))
                .thenReturn(List.of(relatorio));

        List<RelatorioClinicoResposta> resposta = relatorioClinicoService.listarPorPaciente(1L);

        assertThat(resposta).hasSize(1);
        assertThat(resposta.get(0).getId()).isEqualTo(1L);
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
