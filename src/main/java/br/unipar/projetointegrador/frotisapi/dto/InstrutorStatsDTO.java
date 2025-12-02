package br.unipar.projetointegrador.frotisapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InstrutorStatsDTO {
    private long totalAlunos;
    private long fichasAtivas;
}