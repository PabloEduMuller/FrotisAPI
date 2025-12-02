package br.unipar.projetointegrador.frotisapi.repository;

import br.unipar.projetointegrador.frotisapi.model.Treino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TreinoRepository extends JpaRepository<Treino, Long> {

    @Query("SELECT DISTINCT t FROM Treino t LEFT JOIN FETCH t.exercicios WHERE t.id = :id")
    Optional<Treino> findTreinoCompletoById(@Param("id") Long id);

    // --- CORREÇÃO AQUI: ADICIONADO 'ORDER BY t.nome ASC' ---
    @Query("SELECT DISTINCT t FROM Treino t LEFT JOIN FETCH t.exercicios ORDER BY t.nome ASC")
    List<Treino> findAllTreinosCompletos();

    @Query("SELECT t FROM Treino t LEFT JOIN FETCH t.exercicios WHERE t.diaSemana = :diaSemana")
    Optional<Treino> findTreinoCompletoByDiaSemana(@Param("diaSemana") String diaSemana);

    @Query("SELECT t FROM Treino t LEFT JOIN FETCH t.exercicios WHERE t.fichaTreino IS NULL")
    List<Treino> findTreinosSemFicha();

    @Query("SELECT t FROM Treino t " +
            "LEFT JOIN FETCH t.exercicios " +
            "WHERE UPPER(t.diaSemana) = UPPER(:diaSemana) " +
            "AND t.fichaTreino.aluno.id = :alunoId " +
            "AND t.fichaTreino.ativa = true")
    List<Treino> findTreinoDeHojePorAluno(@Param("diaSemana") String diaSemana, @Param("alunoId") Long alunoId);

}