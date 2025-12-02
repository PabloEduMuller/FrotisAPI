package br.unipar.projetointegrador.frotisapi.repository;

import br.unipar.projetointegrador.frotisapi.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca Exata (Padrão)
    Optional<Usuario> findByLogin(String login);

    @Query("SELECT u FROM Usuario u " +
            "LEFT JOIN u.aluno a " +
            "LEFT JOIN u.instrutor i " +
            "WHERE LOWER(TRIM(u.login)) = LOWER(TRIM(:identificador)) " + // Tenta pelo campo Login
            "OR LOWER(TRIM(a.email)) = LOWER(TRIM(:identificador)) " +    // Tenta pelo Email do Aluno
            "OR a.cpf = :identificador " +                                // Tenta pelo CPF do Aluno
            "OR LOWER(TRIM(i.email)) = LOWER(TRIM(:identificador))")      // Tenta pelo Email do Instrutor
    Optional<Usuario> findByLoginOrEmail(@Param("identificador") String identificador);
}