package br.unipar.projetointegrador.frotisapi.service;

import br.unipar.projetointegrador.frotisapi.dto.DiaTreinoRequestDTO;
import br.unipar.projetointegrador.frotisapi.dto.FichaCompletaRequestDTO;
import br.unipar.projetointegrador.frotisapi.dto.ItemTreinoRequestDTO;
import br.unipar.projetointegrador.frotisapi.model.*;
import br.unipar.projetointegrador.frotisapi.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class FichaTreinoService {

    @Autowired private FichaTreinoRepository fichaRepository;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private InstrutorRepository instrutorRepository;
    @Autowired private ExercicioRepository exercicioRepository;
    @Autowired private TreinoRepository treinoRepository;
    @Autowired private br.unipar.projetointegrador.frotisapi.repository.UsuarioRepository usuarioRepository; // Injete isso


    public List<FichaTreino> findAll() {
        return fichaRepository.findAll(); }

    public FichaTreino findById(Long id) {
        return fichaRepository.findById(id).orElse(null); }

    public void deleteById(Long id) {
        fichaRepository.deleteById(id); }

    public FichaTreino save(FichaTreino f) {
        return fichaRepository.save(f); }

    @Autowired
    private FichaTreinoRepository fichaTreinoRepository;


    public FichaTreino criarFicha(Long alunoId, FichaTreino ficha) {
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));

        ficha.setAluno(aluno);

        // Vínculo reverso para o Hibernate salvar tudo de uma vez
        if (ficha.getTreinos() != null) {
            for (Treino treino : ficha.getTreinos()) {
                treino.setFichaTreino(ficha); // Pai do Treino é esta Ficha

                if (treino.getExercicios() != null) {
                    for (Exercicio exercicio : treino.getExercicios()) {
                        exercicio.setTreino(treino); // Pai do Exercicio é este Treino
                    }
                }
            }
        }
        return fichaTreinoRepository.save(ficha);
    }

    @Transactional
    public FichaTreino salvarFichaCompleta(FichaCompletaRequestDTO dto) {

        if (dto.getAlunoId() != null) {
            fichaRepository.desativarTodasDoAluno(dto.getAlunoId());
        }

        List<FichaTreino> fichasAntigas = fichaTreinoRepository.findByAlunoId(dto.getAlunoId());
        for (FichaTreino f : fichasAntigas) {
            // Se a ficha estiver ativa, desativa ela
            if (Boolean.TRUE.equals(f.getAtiva())) {
                f.setAtiva(false);
                fichaTreinoRepository.save(f);
            }
        }

        FichaTreino ficha = new FichaTreino();
        ficha.setDescricao(dto.getDescricao());


        if (dto.getDataInicio() != null) {
            Date inicio = Date.from(dto.getDataInicio().atStartOfDay(ZoneId.systemDefault()).toInstant());
            ficha.setDataInicio(inicio);
        }

        if (dto.getDataFim() != null) {
            Date fim = Date.from(dto.getDataFim().atStartOfDay(ZoneId.systemDefault()).toInstant());
            ficha.setDataFim(fim);
        }


        ficha.setAtiva(true);


        Aluno aluno = alunoRepository.findById(dto.getAlunoId())
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        Instrutor instrutor = instrutorRepository.findById(dto.getInstrutorId())
                .orElseThrow(() -> new RuntimeException("Instrutor não encontrado"));

        ficha.setAluno(aluno);
        ficha.setInstrutor(instrutor);

        // Salva a ficha primeiro para ter ID
        FichaTreino fichaSalva = fichaRepository.save(ficha);

        // Processar Dias de Treino
        if (dto.getDias() != null) {
            for (DiaTreinoRequestDTO diaDTO : dto.getDias()) {
                Treino treino = new Treino();
                treino.setFichaTreino(fichaSalva);
                treino.setDiaSemana(diaDTO.getDiaSemana());
                treino.setNome(diaDTO.getNomeTreino());

                // Lista de exercícios para este treino
                List<Exercicio> exerciciosDoTreino = new ArrayList<>();

                // Processar Exercícios do Dia
                if (diaDTO.getExercicios() != null) {
                    for (ItemTreinoRequestDTO itemDTO : diaDTO.getExercicios()) {
                        // Busca o exercício original no catálogo
                        Exercicio catalogo = exercicioRepository.findById(itemDTO.getExercicioId()).orElse(null);

                        if (catalogo != null) {
                            // Cria uma cópia vinculada ao treino específico
                            Exercicio exercicioReal = new Exercicio();
                            exercicioReal.setNome(catalogo.getNome()); // Copia o nome
                            exercicioReal.setSeries(itemDTO.getSeries());
                            exercicioReal.setRepeticoes(itemDTO.getRepeticoes());
                            exercicioReal.setTreino(treino); // Vincula ao treino atual

                            exerciciosDoTreino.add(exercicioReal);
                        }
                    }
                }

                treino.setExercicios(exerciciosDoTreino);
                treinoRepository.save(treino);
            }
        }

        return fichaSalva;
    }

    public List<FichaTreino> buscarPorAluno(Long alunoId) {
        return fichaTreinoRepository.findByAlunoId(alunoId);
    }

    public List<FichaTreino> buscarPorInstrutor(Long instrutorId) {
        return fichaTreinoRepository.findByInstrutorId(instrutorId);
    }


}