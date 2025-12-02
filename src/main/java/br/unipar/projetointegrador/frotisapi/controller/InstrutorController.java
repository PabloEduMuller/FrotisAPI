package br.unipar.projetointegrador.frotisapi.controller;

import br.unipar.projetointegrador.frotisapi.dto.AlunoUpdateDTO;
import br.unipar.projetointegrador.frotisapi.dto.InstrutorStatsDTO; // Importe o DTO
import br.unipar.projetointegrador.frotisapi.model.Endereco;
import br.unipar.projetointegrador.frotisapi.model.Instrutor;
import br.unipar.projetointegrador.frotisapi.model.Usuario;
import br.unipar.projetointegrador.frotisapi.repository.EnderecoRepository;
import br.unipar.projetointegrador.frotisapi.repository.UsuarioRepository;
import br.unipar.projetointegrador.frotisapi.service.AlunoService; // <--- IMPORT NOVO
import br.unipar.projetointegrador.frotisapi.service.InstrutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/instrutor")
public class InstrutorController {

    private final InstrutorService instrutorService;
    private final UsuarioRepository usuarioRepository;
    private final EnderecoRepository enderecoRepository;
    private final AlunoService alunoService; // <--- DECLARAÇÃO NOVA

    @Autowired
    public InstrutorController(InstrutorService instrutorService,
                               UsuarioRepository usuarioRepository,
                               EnderecoRepository enderecoRepository,
                               AlunoService alunoService) { // <--- INJEÇÃO NO CONSTRUTOR
        this.instrutorService = instrutorService;
        this.usuarioRepository = usuarioRepository;
        this.enderecoRepository = enderecoRepository;
        this.alunoService = alunoService; // <--- ATRIBUIÇÃO
    }

    @GetMapping("/listar")
    public ResponseEntity<List<Instrutor>> listar() {
        return ResponseEntity.ok(instrutorService.listarTodos());
    }

    @PostMapping("/salvar")
    public ResponseEntity<?> salvar(@RequestBody Instrutor instrutor) {
        try {
            return ResponseEntity.ok(instrutorService.salvar(instrutor));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Instrutor instrutor) {
        try {
            Instrutor atualizado = instrutorService.atualizar(id, instrutor);
            if (atualizado != null) return ResponseEntity.ok(atualizado);
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<Instrutor> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(instrutorService.buscarPorId(id));
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        instrutorService.deletar(id);
        return ResponseEntity.noContent().build();
    }


    // --- Endpoint de Atualização do Perfil (Instrutor Logado) ---
    @PutMapping("/me")
    public ResponseEntity<Instrutor> atualizarMe(@RequestBody AlunoUpdateDTO dto) {
        try {
            String login = SecurityContextHolder.getContext().getAuthentication().getName();
            Usuario usuario = usuarioRepository.findByLoginOrEmail(login)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            Instrutor instrutor = usuario.getInstrutor();

            if (instrutor == null) {
                return ResponseEntity.notFound().build();
            }

            if (dto.getNome() != null) instrutor.setNome(dto.getNome());
            if (dto.getEmail() != null) instrutor.setEmail(dto.getEmail());
            if (dto.getTelefone() != null) instrutor.setTelefone(dto.getTelefone());

            Endereco endereco = instrutor.getEndereco();
            if (endereco == null) {
                endereco = new Endereco();
            }

            if (dto.getRua() != null) endereco.setRua(dto.getRua());
            if (dto.getCidade() != null) endereco.setCidade(dto.getCidade());
            if (dto.getEstado() != null) endereco.setEstado(dto.getEstado());
            if (dto.getCep() != null) endereco.setCep(dto.getCep());
            if (dto.getNumero() != null) endereco.setNumero(dto.getNumero());
            if (dto.getBairro() != null) endereco.setBairro(dto.getBairro());

            enderecoRepository.save(endereco);
            instrutor.setEndereco(endereco);

            return ResponseEntity.ok(instrutorService.salvar(instrutor));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // --- Endpoint de Estatísticas do Instrutor ---
    @GetMapping("/estatisticas/me")
    public ResponseEntity<InstrutorStatsDTO> getMinhasEstatisticas() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByLoginOrEmail(login).orElse(null);

        if (usuario == null || usuario.getInstrutor() == null) {
            return ResponseEntity.badRequest().build();
        }

        Long instrutorId = usuario.getInstrutor().getId();

        // Agora o alunoService existe e pode ser usado!
        long totalAlunos = alunoService.listarPorInstrutor(instrutorId).size();

        long fichasAtivas = 0; // Implementar contagem real se desejar

        return ResponseEntity.ok(new InstrutorStatsDTO(totalAlunos, fichasAtivas));
    }
}