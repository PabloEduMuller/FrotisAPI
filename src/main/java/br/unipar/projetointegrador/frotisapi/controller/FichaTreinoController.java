package br.unipar.projetointegrador.frotisapi.controller;

import br.unipar.projetointegrador.frotisapi.dto.FichaCompletaRequestDTO;
import br.unipar.projetointegrador.frotisapi.model.FichaTreino;
import br.unipar.projetointegrador.frotisapi.repository.FichaTreinoRepository;
import br.unipar.projetointegrador.frotisapi.service.FichaTreinoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/fichas", "/ficha-treino"})
@CrossOrigin(origins = "*")
public class FichaTreinoController {

    @Autowired
    private FichaTreinoService fichaTreinoService;
    private final FichaTreinoRepository fichaTreinoRepository;

    @Autowired
    public FichaTreinoController(FichaTreinoRepository fichaTreinoRepository) {
        this.fichaTreinoRepository = fichaTreinoRepository;
    }

    @Autowired
    private br.unipar.projetointegrador.frotisapi.repository.UsuarioRepository usuarioRepository;


    @GetMapping("/listar")
    public ResponseEntity<List<FichaTreino>> listarTodas() {
        // 1. Identifica o usuário logado
        String login = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();

        br.unipar.projetointegrador.frotisapi.model.Usuario usuario =
                usuarioRepository.findByLoginOrEmail(login).orElse(null);

        if (usuario == null) return ResponseEntity.status(401).build();

        List<FichaTreino> fichas;

        // 2. Filtra os dados conforme a Role
        if (usuario.getRole() == br.unipar.projetointegrador.frotisapi.model.enums.RoleEnum.ROLE_GERENCIADOR) {
            // GERENTE: Vê tudo
            fichas = fichaTreinoService.findAll();

        } else if (usuario.getRole() == br.unipar.projetointegrador.frotisapi.model.enums.RoleEnum.ROLE_INSTRUTOR) {
            // INSTRUTOR: Vê apenas as fichas que ele criou/supervisiona
            if (usuario.getInstrutor() != null) {
                fichas = fichaTreinoService.buscarPorInstrutor(usuario.getInstrutor().getId());
            } else {
                fichas = java.util.Collections.emptyList();
            }

        } else if (usuario.getRole() == br.unipar.projetointegrador.frotisapi.model.enums.RoleEnum.ROLE_ALUNO) {
            // ALUNO: Vê apenas as suas fichas
            if (usuario.getAluno() != null) {
                fichas = fichaTreinoService.buscarPorAluno(usuario.getAluno().getId());
            } else {
                fichas = java.util.Collections.emptyList();
            }

        } else {
            return ResponseEntity.status(403).build();
        }

        //Retorna (204 se vazio, 200 se tiver dados)
        if (fichas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(fichas);
    }

    // POST: Cria uma ficha COMPLETA (Ficha -> Treinos -> Exercícios) para um aluno
    @PostMapping("/aluno/{alunoId}")
    public ResponseEntity<FichaTreino> criarFicha(@PathVariable Long alunoId, @RequestBody FichaTreino ficha) {
        FichaTreino novaFicha = fichaTreinoService.criarFicha(alunoId, ficha);
        return ResponseEntity.ok(novaFicha);
    }

    // GET: Busca todas as fichas de um aluno
    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<FichaTreino>> listarPorAluno(@PathVariable Long alunoId) {
        return ResponseEntity.ok(fichaTreinoService.buscarPorAluno(alunoId));
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<FichaTreino> buscarPorId(@PathVariable Long id) {
        return fichaTreinoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/salvar")
    public ResponseEntity<FichaTreino> salvar(@RequestBody FichaTreino ficha) {
        return ResponseEntity.ok(fichaTreinoRepository.save(ficha));
    }

    @PostMapping("/salvar-completa")
    public ResponseEntity<FichaTreino> salvarCompleta(@RequestBody FichaCompletaRequestDTO dto) {
        try {
            FichaTreino novaFicha = fichaTreinoService.salvarFichaCompleta(dto);
            return ResponseEntity.status(201).body(novaFicha);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }


}