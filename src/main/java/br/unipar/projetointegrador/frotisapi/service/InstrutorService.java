package br.unipar.projetointegrador.frotisapi.service;

import br.unipar.projetointegrador.frotisapi.model.Instrutor;
import br.unipar.projetointegrador.frotisapi.model.Usuario;
import br.unipar.projetointegrador.frotisapi.model.enums.RoleEnum;
import br.unipar.projetointegrador.frotisapi.repository.InstrutorRepository;
import br.unipar.projetointegrador.frotisapi.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InstrutorService {

    private final InstrutorRepository instrutorRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired
    public InstrutorService(InstrutorRepository instrutorRepository) {
        this.instrutorRepository = instrutorRepository;
    }



    public List<Instrutor> listarTodos() {
        return instrutorRepository.findAll();
    }

    @Transactional // Garante que salva os dois ou nenhum
    public Instrutor salvar(Instrutor instrutor) {
        // 1. Salva o Instrutor
        Instrutor instrutorSalvo = instrutorRepository.save(instrutor);

        // 2. Cria ou Atualiza o Usuário de Login
        if (instrutor.getSenha() != null && !instrutor.getSenha().isEmpty()) {
            // Verifica se já existe usuário para este instrutor (pelo login/email)
            Optional<Usuario> usuarioExistente = usuarioRepository.findByLogin(instrutor.getEmail());

            Usuario usuario = usuarioExistente.orElse(new Usuario());

            usuario.setLogin(instrutor.getEmail()); // Login é o Email
            usuario.setSenha(passwordEncoder.encode(instrutor.getSenha()));
            usuario.setRole(RoleEnum.ROLE_INSTRUTOR);
            usuario.setInstrutor(instrutorSalvo); // Vincula o ID

            usuarioRepository.save(usuario);
        }

        return instrutorSalvo;
    }

    public Instrutor buscarPorId(Long id) {
        return instrutorRepository.findById(id).orElse(null);
    }

    public void deletar(Long id) {
        instrutorRepository.deleteById(id);
    }

    public Instrutor atualizar(Long id, Instrutor instrutorAtualizado) {
        Instrutor instrutorExistente = buscarPorId(id);

        if (instrutorExistente != null) {
            // Atualiza dados básicos
            instrutorExistente.setNome(instrutorAtualizado.getNome());
            instrutorExistente.setEmail(instrutorAtualizado.getEmail());
            instrutorExistente.setTelefone(instrutorAtualizado.getTelefone());
            instrutorExistente.setSexo(instrutorAtualizado.getSexo());
            instrutorExistente.setDataNascimento(instrutorAtualizado.getDataNascimento());
            // CPF geralmente não se muda, mas se quiser permitir, descomente:
            // instrutorExistente.setCPF(instrutorAtualizado.getCPF());

            // Atualiza Endereço
            if (instrutorExistente.getEndereco() != null && instrutorAtualizado.getEndereco() != null) {
                br.unipar.projetointegrador.frotisapi.model.Endereco endExistente = instrutorExistente.getEndereco();
                br.unipar.projetointegrador.frotisapi.model.Endereco endNovo = instrutorAtualizado.getEndereco();

                endExistente.setRua(endNovo.getRua());
                endExistente.setNumero(endNovo.getNumero());
                endExistente.setBairro(endNovo.getBairro());
                endExistente.setCidade(endNovo.getCidade());
                endExistente.setEstado(endNovo.getEstado());
                endExistente.setCep(endNovo.getCep());
                // O JPA salvará o endereço automaticamente por causa do CascadeType.ALL
            } else if (instrutorAtualizado.getEndereco() != null) {
                // Se não tinha endereço e agora tem
                instrutorExistente.setEndereco(instrutorAtualizado.getEndereco());
            }

            return instrutorRepository.save(instrutorExistente);
        }
        return null;
    }

    private void validarDuplicidade(String email, String cpf, Long idExistente) {
        // Valida Login na tabela Usuario (O login do instrutor é o email)
        usuarioRepository.findByLoginOrEmail(email).ifPresent(u -> {
            // Se achou usuário e não é atualização do mesmo instrutor
            if (idExistente == null || (u.getInstrutor() != null && !u.getInstrutor().getId().equals(idExistente))) {
                throw new RuntimeException("Este Email já está cadastrado como login.");
            }
        });

        // Se tiver busca por CPF no InstrutorRepository, use aqui também
    }
}