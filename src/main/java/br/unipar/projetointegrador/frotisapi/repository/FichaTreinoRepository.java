package br.unipar.projetointegrador.frotisapi.repository;

import br.unipar.projetointegrador.frotisapi.model.FichaTreino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FichaTreinoRepository extends JpaRepository<FichaTreino, Long> {
    // Busca as fichas de um aluno específico
    List<FichaTreino> findByAlunoId(Long alunoId);

    List<FichaTreino> findByInstrutorId(Long instrutorId);

    @Modifying // Diz ao Spring que isso altera dados
    @Query("UPDATE FichaTreino f SET f.ativa = false WHERE f.aluno.id = :alunoId")
    void desativarTodasDoAluno(@Param("alunoId") Long alunoId);


}