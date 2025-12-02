package br.unipar.projetointegrador.frotisapi.repository;

import br.unipar.projetointegrador.frotisapi.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    Optional<Aluno> findByCpf(String cpf);

    List<Aluno> findByInstrutorId(Long instrutorId);

    Optional<Aluno> findByEmail(String email);     // <--- NOVO
    Optional<Aluno> findByTelefone(String telefone);

}
