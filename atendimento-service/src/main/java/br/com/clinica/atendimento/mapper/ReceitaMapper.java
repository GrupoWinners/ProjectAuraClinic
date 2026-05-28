package br.com.clinica.atendimento.mapper;

import br.com.clinica.atendimento.dto.requisicao.ReceitaRequisicao;
import br.com.clinica.atendimento.dto.resposta.ReceitaResposta;
import br.com.clinica.atendimento.entity.Atendimento;
import br.com.clinica.atendimento.entity.Receita;

public class ReceitaMapper {

    private ReceitaMapper(){

    }

    public static Receita paraEntidade(ReceitaRequisicao requisicao, Atendimento atendimento){
        if(requisicao == null) {
            return null;
        }

        return Receita.builder()
                .atendimento(atendimento)
                .medicamento(requisicao.getMedicamento())
                .dosagem(requisicao.getDosagem())
                .frequencia(requisicao.getFrequencia())
                .duracao(requisicao.getDuracao())
                .observacoes(requisicao.getObservacoes())
                .build();
    }

    public static ReceitaResposta paraResposta(Receita receita){
        if (receita == null){
            return null;
        }

        return ReceitaResposta.builder()
                .id(receita.getId())
                .medicamento(receita.getMedicamento())
                .dosagem(receita.getDosagem())
                .frequencia(receita.getFrequencia())
                .duracao(receita.getDuracao())
                .observacoes(receita.getObservacoes())
                .build();
    }
}
