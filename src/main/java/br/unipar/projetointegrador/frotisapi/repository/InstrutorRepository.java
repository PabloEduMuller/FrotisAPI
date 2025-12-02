package br.unipar.projetointegrador.frotisapi.repository;

import br.unipar.projetointegrador.frotisapi.model.Instrutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstrutorRepository extends JpaRepository<Instrutor, Long> {

    Optional<Instrutor> findByEmail(String email); // <--- NOVO
    Optional<Instrutor> findByCPF(String cpf);
}
