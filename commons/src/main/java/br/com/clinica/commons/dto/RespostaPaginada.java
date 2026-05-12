package br.com.clinica.commons.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class RespostaPaginada<T> {
    private List<T> conteudo;
    private int paginaAtual;
    private int totalPaginas;
    private long totalElementos;
}