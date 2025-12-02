package br.unipar.projetointegrador.frotisapi.controller;

import br.unipar.projetointegrador.frotisapi.dto.*;
import br.unipar.projetointegrador.frotisapi.model.Aluno;
import br.unipar.projetointegrador.frotisapi.model.Usuario;
import br.unipar.projetointegrador.frotisapi.model.enums.RoleEnum;
import br.unipar.projetointegrador.frotisapi.repository.UsuarioRepository;
import br.unipar.projetointegrador.frotisapi.service.AlunoService;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aluno")
public class AlunoController {

    @Autowired
    private AlunoService alunoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // --- MÉTODO LISTAR INTELIGENTE (CORRIGIDO) ---
    @GetMapping("/listar")
    public ResponseEntity<List<Aluno>> listarAlunos() {
        // 1. Identifica quem está logado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth.getName();

        // 2. Busca o usuário no banco
        Usuario usuario = usuarioRepository.findByLoginOrEmail(login).orElse(null);

        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }

        List<Aluno> alunos;

        // 3. Lógica de Segurança: Quem vê o quê?
        if (usuario.getRole() == RoleEnum.ROLE_GERENCIADOR) {
            // GERENTE: Vê tudo
            alunos = alunoService.listarTodos();
        } else if (usuario.getRole() == RoleEnum.ROLE_INSTRUTOR) {
            // INSTRUTOR: Vê apenas os seus alunos
            if (usuario.getInstrutor() != null) {
                // Chama o método que criamos no service
                alunos = alunoService.listarPorInstrutor(usuario.getInstrutor().getId());
            } else {
                alunos = List.of(); // Instrutor sem cadastro completo não vê nada
            }
        } else {
            // ALUNO: Não deveria acessar essa lista
            return ResponseEntity.status(403).build();
        }

        if (alunos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(alunos);
    }
    // ---------------------------------------------

    @PostMapping("/salvar")
    public ResponseEntity<?> salvarAluno(@RequestBody Aluno aluno) {
        try {
            Aluno alunoSalvo = alunoService.salvar(aluno);
            return ResponseEntity.status(Response.SC_CREATED).body(alunoSalvo);
        } catch (RuntimeException e) {
            // Retorna erro 400 com a mensagem "Já existe..."
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/salvar-completo")
    public ResponseEntity<?> salvarAlunoCompleto(@RequestBody AlunoCompletoDTO dto) {
        try {
            return ResponseEntity.ok(alunoService.salvarCompleto(dto, null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/atualizar-completo/{id}")
    public ResponseEntity<?> atualizarAlunoCompleto(@PathVariable Long id, @RequestBody AlunoCompletoDTO dto) {
        try {
            return ResponseEntity.ok(alunoService.salvarCompleto(dto, id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/buscar/{id}")
    public ResponseEntity<Aluno> buscarAlunoPorID(@PathVariable Long id) {
        Aluno aluno = alunoService.buscarPorId(id);
        return (aluno != null) ? ResponseEntity.ok(aluno) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletarAluno(@PathVariable Long id) {
        alunoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<Aluno> atualizarAluno(@PathVariable Long id, @RequestBody Aluno alunoAtualizado) {
        Aluno aluno = alunoService.atualizar(id, alunoAtualizado);
        return (aluno != null) ? ResponseEntity.ok(aluno) : ResponseEntity.notFound().build();
    }

    @GetMapping("/perfil")
    public ResponseEntity<AlunoResponseDTO> getPerfil() {
        String cpf = SecurityContextHolder.getContext().getAuthentication().getName();
        Aluno aluno = alunoService.buscarPorCpf(cpf);
        return (aluno != null) ? ResponseEntity.ok(new AlunoResponseDTO(aluno)) : ResponseEntity.notFound().build();
    }

    @PutMapping("/perfil")
    public ResponseEntity<AlunoResponseDTO> atualizarPerfil(@RequestBody AlunoUpdateDTO dto) {
        try {
            String cpf = SecurityContextHolder.getContext().getAuthentication().getName();
            Aluno alunoAtualizado = alunoService.atualizarPerfil(cpf, dto);
            return ResponseEntity.ok(new AlunoResponseDTO(alunoAtualizado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/mudar-senha")
    public ResponseEntity<String> mudarSenha(@RequestBody MudarSenhaDTO dto) {
        try {
            String cpf = SecurityContextHolder.getContext().getAuthentication().getName();
            alunoService.alterarSenha(cpf, dto);
            return ResponseEntity.ok("Senha alterada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AlunoResponseDTO> getMe() {
        return getPerfil(); // Reutiliza a lógica
    }

    @GetMapping("/estatisticas")
    public ResponseEntity<DashboardStatsDTO> getEstatisticas() {
        long total = alunoService.listarTodos().size();
        long ativos = alunoService.listarTodos().stream().filter(a -> Boolean.TRUE.equals(a.getAtivo())).count();
        return ResponseEntity.ok(new DashboardStatsDTO(ativos, total - ativos, 0));
    }

    @GetMapping("/estatisticas/instrutor/{id}")
    public ResponseEntity<DashboardStatsDTO> getEstatisticasInstrutor(@PathVariable Long id) {
        // Aqui você pode implementar a lógica real de contar alunos DO instrutor
        // Por enquanto, retorna dados dummy ou totais
        return getEstatisticas();
    }

    @PutMapping("/atualizar-peso/{id}")
    public ResponseEntity<Void> atualizarPeso(@PathVariable Long id, @RequestBody AtualizarPesoRequestDTO dto) {
        alunoService.atualizarPeso(id, dto.getPeso());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/atualizar-altura/{id}")
    public ResponseEntity<Void> atualizarAltura(@PathVariable Long id, @RequestBody AtualizarAlturaRequestDTO dto) {
        alunoService.atualizarAltura(id, dto.getAltura());
        return ResponseEntity.ok().build();
    }
}