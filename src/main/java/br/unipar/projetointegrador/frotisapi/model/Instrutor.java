package br.unipar.projetointegrador.frotisapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
public class Instrutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String CPF; // Recomendo manter padrão camelCase 'cpf', mas mantive CPF para compatibilidade

    @Temporal(TemporalType.DATE)
    private Date dataNascimento;

    private String telefone;
    private String email;
    private String sexo;
    private Boolean ativo = true; // Campo útil para soft-delete

    @Transient
    private String senha;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "endereco_id")
    private Endereco endereco;


    @OneToMany(mappedBy = "instrutor", fetch = FetchType.LAZY)
    @JsonIgnore // Importante: Evita que ao buscar um instrutor, a API tente baixar todas as fichas de todos os alunos dele (Ciclo infinito/Lentidão)
    private List<FichaTreino> fichasSupervisionadas = new ArrayList<>();
}