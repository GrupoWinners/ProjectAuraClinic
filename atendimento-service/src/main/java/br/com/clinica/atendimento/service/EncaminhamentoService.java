package br.com.clinica.atendimento.service;

import br.com.clinica.atendimento.client.AdminServiceClient;
import br.com.clinica.atendimento.client.dto.MedicoClientResposta;
import br.com.clinica.atendimento.dto.requisicao.AtualizarStatusEncaminhamentoRequisicao;
import br.com.clinica.atendimento.dto.requisicao.EncaminhamentoRequisicao;
import br.com.clinica.atendimento.dto.resposta.EncaminhamentoResposta;
import br.com.clinica.atendimento.entity.Atendimento;
import br.com.clinica.atendimento.entity.Encaminhamento;
import br.com.clinica.atendimento.mapper.EncaminhamentoMapper;
import br.com.clinica.atendimento.repository.EncaminhamentoRepository;
import br.com.clinica.commons.exception.IntegracaoException;
import br.com.clinica.commons.exception.RecursoNaoEncontradoException;
import br.com.clinica.commons.exception.RegraDeNegocioException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EncaminhamentoService {

    private final EncaminhamentoRepository encaminhamentoRepository;
    private final AtendimentoService atendimentoService;
    private final AdminServiceClient adminServiceClient;

    public EncaminhamentoResposta criar(EncaminhamentoRequisicao requisicao) {
        Atendimento atendimento = atendimentoService.buscarEntidadePorId(requisicao.getAtendimentoId());

        MedicoClientResposta medicoDestino = buscarMedicoDestinoNoAdmin(requisicao.getMedicoDestinoId());
        validarMedicoDestino(medicoDestino, requisicao.getMedicoDestinoId());

        Encaminhamento encaminhamento = EncaminhamentoMapper.paraEntidade(requisicao, atendimento);
        Encaminhamento encaminhamentoSalvo = encaminhamentoRepository.save(encaminhamento);

        log.info("Encaminhamento criado com sucesso. ID={}, atendimentoId={}, medicoDestinoId={}",
                encaminhamentoSalvo.getId(),
                atendimento.getId(),
                encaminhamentoSalvo.getMedicoDestinoId());

        return EncaminhamentoMapper.paraResposta(encaminhamentoSalvo);
    }

    public EncaminhamentoResposta buscarPorId(Long id) {
        Encaminhamento encaminhamento = buscarEntidadePorId(id);
        return EncaminhamentoMapper.paraResposta(encaminhamento);
    }

    public List<EncaminhamentoResposta> listarPorMedicoDestino(Long medicoId) {
        return encaminhamentoRepository.findByMedicoDestinoId(medicoId)
                .stream()
                .map(EncaminhamentoMapper::paraResposta)
                .toList();
    }

    public EncaminhamentoResposta atualizarStatus(Long id, AtualizarStatusEncaminhamentoRequisicao requisicao) {
        Encaminhamento encaminhamento = buscarEntidadePorId(id);
        encaminhamento.setStatus(requisicao.getStatus());

        Encaminhamento encaminhamentoAtualizado = encaminhamentoRepository.save(encaminhamento);

        log.info("Status do encaminhamento atualizado. ID={}, status={}",
                encaminhamentoAtualizado.getId(),
                encaminhamentoAtualizado.getStatus());

        return EncaminhamentoMapper.paraResposta(encaminhamentoAtualizado);
    }

    private Encaminhamento buscarEntidadePorId(Long id) {
        return encaminhamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Encaminhamento com ID " + id + " não encontrado"));
    }

    private MedicoClientResposta buscarMedicoDestinoNoAdmin(Long medicoDestinoId) {
        try {
            return adminServiceClient.buscarMedicoPorId(medicoDestinoId);
        } catch (FeignException.NotFound ex) {
            throw new RecursoNaoEncontradoException("Médico destino com ID " + medicoDestinoId + " não encontrado");
        } catch (FeignException ex) {
            throw new IntegracaoException("Erro ao buscar médico destino no admin-service");
        }
    }

    private void validarMedicoDestino(MedicoClientResposta medicoDestino, Long medicoDestinoId) {
        if (medicoDestino == null || Boolean.FALSE.equals(medicoDestino.getAtivo())) {
            throw new RegraDeNegocioException("Médico destino com ID " + medicoDestinoId + " está inativo ou inválido");
        }
    }
}