package br.unipar.projetointegrador.frotisapi.service;

import br.unipar.projetointegrador.frotisapi.dto.TreinoDTO;
import br.unipar.projetointegrador.frotisapi.model.Exercicio;
import br.unipar.projetointegrador.frotisapi.model.Treino;
import br.unipar.projetointegrador.frotisapi.repository.AlunoRepository;
import br.unipar.projetointegrador.frotisapi.repository.TreinoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TreinoService {

    private final TreinoRepository treinoRepository;
    @Autowired
    private AlunoRepository alunoRepository;

    public TreinoService(TreinoRepository treinoRepository) {
        this.treinoRepository = treinoRepository;
    }

    public Treino save(Treino treino) {
        // --- CORREÇÃO DO ERRO "StaleObjectStateException" ---

        // 1. Se o ID vier como 0 do Android, força NULL para o Hibernate criar (INSERT)
        if (treino.getId() != null && treino.getId() == 0) {
            treino.setId(null);
        }

        if (treino.getExercicios() != null) {
            for (Exercicio exercicio : treino.getExercicios()) {
                // 2. Garante o vínculo (Pai -> Filho)
                exercicio.setTreino(treino);

                // 3. Mesma correção para os Exercícios: 0 vira NULL
                if (exercicio.getId() != null && exercicio.getId() == 0) {
                    exercicio.setId(null);
                }
            }
        }
        return treinoRepository.save(treino);
    }

    public List<Treino> findAll() {
        return treinoRepository.findAllTreinosCompletos();
    }

    public void deleteById(Long id) {
        treinoRepository.deleteById(id);
    }

    public Treino findById(Long id) {
        return treinoRepository.findTreinoCompletoById(id).orElse(null);
    }

    public TreinoDTO getTreinoCompletoDTO(Long id) {
        Treino treino = treinoRepository.findTreinoCompletoById(id).orElse(null);
        if (treino != null) {
            return new TreinoDTO(treino);
        }
        return null;
    }

    public List<TreinoDTO> buscarTreinosSemFicha() {
        return treinoRepository.findTreinosSemFicha().stream()
                .map(TreinoDTO::new)
                .collect(Collectors.toList());
    }

    private String converterDiaDaSemana(DayOfWeek dia) {
        switch (dia) {
            case MONDAY: return "SEGUNDA"; // Mudado para Maiúsculo
            case TUESDAY: return "TERCA";
            case WEDNESDAY: return "QUARTA";
            case THURSDAY: return "QUINTA";
            case FRIDAY: return "SEXTA";
            case SATURDAY: return "SABADO"; // Adicionado/Ajustado
            case SUNDAY: return "DOMINGO";  // Adicionado/Ajustado
            default: return "";
        }
    }

    public TreinoDTO buscarTreinoDeHoje() {
        String cpf = SecurityContextHolder.getContext().getAuthentication().getName();
        var aluno = alunoRepository.findByCpf(cpf).orElse(null);
        if (aluno == null) return null;

        LocalDate hoje = LocalDate.now();
        DayOfWeek diaDaSemanaEnum = hoje.getDayOfWeek();
        String diaDaSemanaStr = converterDiaDaSemana(diaDaSemanaEnum);

        // CORREÇÃO: Recebe lista e pega o primeiro
        List<Treino> treinos = treinoRepository.findTreinoDeHojePorAluno(diaDaSemanaStr, aluno.getId());

        if (treinos != null && !treinos.isEmpty()) {
            // Pega o primeiro treino encontrado (o mais relevante)
            return new TreinoDTO(treinos.get(0));
        }

        return null;
    }
}