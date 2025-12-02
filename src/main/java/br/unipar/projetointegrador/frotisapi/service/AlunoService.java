package br.unipar.projetointegrador.frotisapi.service;

import br.unipar.projetointegrador.frotisapi.dto.AlunoCompletoDTO;
import br.unipar.projetointegrador.frotisapi.dto.AlunoUpdateDTO;
import br.unipar.projetointegrador.frotisapi.dto.MudarSenhaDTO;
import br.unipar.projetointegrador.frotisapi.model.*;
import br.unipar.projetointegrador.frotisapi.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Adicionado import
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final PasswordEncoder passwordEncoder; // Adicionado o codificador de senha
    private final EnderecoRepository enderecoRepository;
    @Autowired private MatriculaRepository matriculaRepository; // Adicione isso lá em cima
    @Autowired private PlanoRepository planoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private br.unipar.projetointegrador.frotisapi.repository.InstrutorRepository instrutorRepository;
    @Autowired
    public AlunoService(AlunoRepository alunoRepository,
                        PasswordEncoder passwordEncoder,
                        EnderecoRepository enderecoRepository,
                        MatriculaRepository matriculaRepository,
                        PlanoRepository planoRepository) {
        this.alunoRepository = alunoRepository;
        this.passwordEncoder = passwordEncoder;
        this.enderecoRepository = enderecoRepository;
        this.matriculaRepository = matriculaRepository;
        this.planoRepository = planoRepository;
    }


    @Transactional
    public Aluno salvar(Aluno aluno) {
        // CHAMA A VALIDAÇÃO ANTES DE TUDO
        validarDuplicidade(aluno.getCpf(), aluno.getEmail(), aluno.getTelefone(), aluno.getId());

        // ... (resto do código igual: hash senha, save, criarUsuario) ...
        String senhaPura = aluno.getSenha();
        if (senhaPura != null) aluno.setSenha(passwordEncoder.encode(senhaPura));
        Aluno salvo = alunoRepository.save(aluno);
        criarUsuarioParaAluno(salvo, senhaPura);
        return salvo;
    }

    public Aluno buscarPorId(Long id) {
        return alunoRepository.findById(id).orElse(null);
    }

    public void deletar(Long id) {
        alunoRepository.deleteById(id);
    }

    public List<Aluno> listarTodos() {
        return alunoRepository.findAll();
    }

    public Aluno atualizar(Long id, Aluno alunoAtualizado) {
        Aluno alunoExistente = buscarPorId(id);

        if (alunoExistente != null) {
            // atualiza os campos do aluno existente com os valores do aluno atualizado
            alunoExistente.setNome(alunoAtualizado.getNome());
            alunoExistente.setEmail(alunoAtualizado.getEmail());
            alunoExistente.setTelefone(alunoAtualizado.getTelefone());
            // Nota: Se permitir atualizar a senha, a lógica de codificação também deve ser aplicada aqui.
            return alunoRepository.save(alunoExistente);
        } else {
            return null; // ou lance uma exceção, dependendo da sua lógica de negócios
        }
    }

    public Aluno buscarPorCpf(String cpf) {
        // O .orElse(null) retorna o aluno se encontrado, ou null se não for.
        return alunoRepository.findByCpf(cpf).orElse(null);
    }

    public Aluno atualizarPerfil(String cpf, AlunoUpdateDTO dto) {
        Aluno aluno = alunoRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado."));

        // Atualiza dados pessoais
        if (dto.getNome() != null) aluno.setNome(dto.getNome());
        if (dto.getEmail() != null) aluno.setEmail(dto.getEmail());
        if (dto.getTelefone() != null) aluno.setTelefone(dto.getTelefone());
        if (dto.getDataNascimento() != null) aluno.setDataNascimento(dto.getDataNascimento());

        // Atualiza ou cria Endereço
        Endereco endereco = aluno.getEndereco();
        if (endereco == null) {
            endereco = new Endereco();
        }

        if (dto.getRua() != null) endereco.setRua(dto.getRua());
        if (dto.getCidade() != null) endereco.setCidade(dto.getCidade());
        if (dto.getEstado() != null) endereco.setEstado(dto.getEstado());
        if (dto.getCep() != null) endereco.setCep(dto.getCep());

        // Salva endereço e vincula
        enderecoRepository.save(endereco);
        aluno.setEndereco(endereco);

        return alunoRepository.save(aluno);
    }

    public void alterarSenha(String cpf, MudarSenhaDTO dto) throws Exception {
        // 1. Busca o aluno
        Aluno aluno = alunoRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado."));

        // 2. Validações básicas de segurança
        if (dto.getSenhaAtual() == null || dto.getNovaSenha() == null) {
            throw new Exception("Dados da senha incompletos.");
        }

        // 3. Verifica se a senha atual do banco confere com a enviada
        if (!passwordEncoder.matches(dto.getSenhaAtual(), aluno.getSenha())) {
            throw new Exception("A senha atual está incorreta.");
        }

        // 4. Criptografa e salva a nova senha
        // (Não precisamos comparar com 'confirmarSenha' aqui, o App já fez isso)
        aluno.setSenha(passwordEncoder.encode(dto.getNovaSenha()));
        alunoRepository.save(aluno);

        System.out.println("SUCESSO: Senha alterada para o CPF " + cpf);
    }

    public void atualizarPeso(Long id, Double novoPeso) {
        Aluno aluno = alunoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));

        aluno.setPeso(novoPeso.floatValue()); // Convertendo Double para float
        alunoRepository.save(aluno);
    }

    public void atualizarAltura(Long id, Double novaAltura) {
        Aluno aluno = alunoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));

        // Se a altura vier em cm (ex: 175), converte ou mantém.
        // O modelo usa float.
        aluno.setAltura(novaAltura.floatValue());
        alunoRepository.save(aluno);
    }

    @Transactional
    public Aluno salvarCompleto(AlunoCompletoDTO dto, Long idExistente) {
        // valida os dados de cpf | email e telefone
        validarDuplicidade(dto.getCpf(), dto.getEmail(), dto.getTelefone(), idExistente);
        Aluno aluno;

        if (idExistente != null) {
            aluno = alunoRepository.findById(idExistente)
                    .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        } else {
            aluno = new Aluno();
            aluno.setCpf(dto.getCpf());
            if (dto.getSenha() != null) {
                aluno.setSenha(passwordEncoder.encode(dto.getSenha()));
            }
        }

        // 1. Dados Básicos
        aluno.setNome(dto.getNome());
        aluno.setEmail(dto.getEmail());
        aluno.setTelefone(dto.getTelefone());
        aluno.setDataNascimento(dto.getDataNascimento());
        aluno.setSexo(dto.getSexo());
        if(dto.getAltura() != null) aluno.setAltura(dto.getAltura());
        if(dto.getPeso() != null) aluno.setPeso(dto.getPeso());

        // 2. Endereço
        Endereco endereco = aluno.getEndereco();
        if (endereco == null) endereco = new Endereco();

        endereco.setRua(dto.getRua());
        endereco.setNumero(dto.getNumero());
        endereco.setBairro(dto.getBairro());
        endereco.setCidade(dto.getCidade());
        endereco.setEstado(dto.getEstado());
        endereco.setCep(dto.getCep());

        enderecoRepository.save(endereco);
        aluno.setEndereco(endereco);

        if (dto.getInstrutorId() != null) {
            // Busca o instrutor pelo ID que veio do formulário
            br.unipar.projetointegrador.frotisapi.model.Instrutor instrutorResponsavel =
                    instrutorRepository.findById(dto.getInstrutorId()).orElse(null);

            aluno.setInstrutor(instrutorResponsavel);
        }

        // Salva o Aluno
        Aluno alunoSalvo = alunoRepository.save(aluno);

        if (dto.getSenha() != null && !dto.getSenha().isEmpty()) {
            criarUsuarioParaAluno(alunoSalvo, dto.getSenha());
        } else if (idExistente == null) {

        }

        // 3. MATRÍCULA (VINCULAR PLANO)
        if (dto.getPlanoId() != null) {
            Plano plano = planoRepository.findById(dto.getPlanoId()).orElse(null);

            if (plano != null) {
                // Procura matrícula existente ou cria nova
                // Como não temos findByAluno no repo ainda, vamos iterar na lista do aluno (se tiver)
                // ou simplesmente criar uma nova e o banco decide.

                Matricula matricula = new Matricula();
                // Se o aluno já tiver matrículas, o ideal seria atualizar a vigente.
                // Aqui vamos criar uma nova para garantir que o vínculo seja feito.

                matricula.setAluno(alunoSalvo);
                matricula.setPlano(plano);
                matricula.setDiaVencimento(dto.getDiaVencimento()); // Certifique-se que adicionou este campo em Matricula.java
                // matricula.setAtiva(true); // Se tiver campo ativa

                matriculaRepository.save(matricula);
            }
        }

        return alunoSalvo;
    }

    private void validarDuplicidade(String cpf, String email, String telefone, Long idExistente) {
        // Valida CPF
        if (cpf != null) {
            alunoRepository.findByCpf(cpf).ifPresent(a -> {
                if (idExistente == null || !a.getId().equals(idExistente)) {
                    throw new RuntimeException("Já existe um aluno cadastrado com este CPF: " + cpf);
                }
            });
        }

        // Valida Email
        if (email != null) {
            alunoRepository.findByEmail(email).ifPresent(a -> {
                if (idExistente == null || !a.getId().equals(idExistente)) {
                    throw new RuntimeException("Já existe um aluno cadastrado com este E-mail: " + email);
                }
            });
        }

        // Valida Telefone
        if (telefone != null) {
            alunoRepository.findByTelefone(telefone).ifPresent(a -> {
                if (idExistente == null || !a.getId().equals(idExistente)) {
                    throw new RuntimeException("Já existe um aluno com este Telefone: " + telefone);
                }
            });
        }
    }

    private void criarUsuarioParaAluno(Aluno aluno, String senhaPura) {
        if (senhaPura == null || senhaPura.isEmpty()) return;

        //Usar EMAIL como login
        String login = aluno.getEmail();


        if (login == null || login.isEmpty()) {
            login = aluno.getCpf(); // Tenta CPF se não tiver email
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByLogin(login);
        Usuario usuario = usuarioOpt.orElse(new Usuario());

        usuario.setLogin(login); // Grava o EMAIL no banco

        // Criptografia da senha
        if (!senhaPura.startsWith("$2a$")) {
            usuario.setSenha(passwordEncoder.encode(senhaPura));
        } else {
            usuario.setSenha(senhaPura);
        }

        usuario.setRole(br.unipar.projetointegrador.frotisapi.model.enums.RoleEnum.ROLE_ALUNO);
        usuario.setAluno(aluno);

        usuarioRepository.save(usuario);
    }

    public List<Aluno> listarPorInstrutor(Long instrutorId) {
        return alunoRepository.findByInstrutorId(instrutorId);
    }


}