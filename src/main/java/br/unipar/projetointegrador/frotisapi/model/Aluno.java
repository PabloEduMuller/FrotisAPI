package br.unipar.projetointegrador.frotisapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Entity
@Getter
@Setter
public class Aluno implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String cpf;
    private String senha;
    private String email;

    @Temporal(TemporalType.DATE)
    private Date dataNascimento;

    private String telefone;
    private String sexo;
    private float altura;
    private float peso;
    private Boolean ativo = true;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dataCadastro = new Date();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "endereco_id")
    private Endereco endereco;


    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL)
    private List<FichaTreino> fichasTreino = new ArrayList<>();

    // ... (Mantenha os métodos UserDetails como estavam) ...
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return Collections.emptyList(); }
    @Override
    public String getPassword() { return this.senha; }
    @Override
    public String getUsername() { return this.cpf; }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() {
        // Evita NullPointerException se 'ativo' for null no banco
        return this.ativo != null && this.ativo;
    }

    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonManagedReference("aluno-matriculas") // Permite que a matrícula apareça no JSON do aluno
    private List<Matricula> matriculaList = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "instrutor_id")
    @com.fasterxml.jackson.annotation.JsonIgnore // Evita loop infinito e dados desnecessários na listagem
    private Instrutor instrutor;

    public List<Matricula> getMatriculaList() {
        return matriculaList;
    }

    public void setMatriculaList(List<Matricula> matriculaList) {
        this.matriculaList = matriculaList;
    }
}