package com.ucb.finlog.service;

import com.ucb.finlog.dto.CategoriaDTO;
import com.ucb.finlog.dto.CategoriaResumoDTO;
import com.ucb.finlog.model.Categoria;
import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.model.TipoMovimentacao;
import com.ucb.finlog.repository.CategoriaRepository;
import com.ucb.finlog.repository.MovimentacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    // ─────────────────────────────────────────────────────────
    // ARMAZENAMENTO EM MEMÓRIA
    // TODO: remover os campos abaixo quando o banco estiver pronto.
    //       O @Autowired de CategoriaRepository e MovimentacaoRepository
    //       já está preparado — basta descomentar e deletar os mapas.
    // ─────────────────────────────────────────────────────────

    private final Map<Long, Categoria>    memoriaCategoria    = new LinkedHashMap<>();
    private final Map<Long, Movimentacao> memoriaMovimentacao = new LinkedHashMap<>();
    private long proximoIdCategoria    = 1L;
    private long proximoIdMovimentacao = 1L;

    // @Autowired
    // private CategoriaRepository categoriaRepository;

    // @Autowired
    // private MovimentacaoRepository movimentacaoRepository;

    // ════════════════════════════════════════
    //  CATEGORIAS — CRUD
    // ════════════════════════════════════════

    /** Lista todas as categorias cadastradas. */
    public List<CategoriaDTO> listarTodas() {
        return memoriaCategoria.values().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        // TODO (banco): return categoriaRepository.findAll()
        //                  .stream().map(this::toDTO).collect(Collectors.toList());
    }

    /** Busca uma categoria pelo ID. Retorna null se não encontrada. */
    public CategoriaDTO buscarPorId(Long id) {
        Categoria cat = memoriaCategoria.get(id);
        if (cat == null) return null;
        return toDTO(cat);

        // TODO (banco): return categoriaRepository.findById(id).map(this::toDTO).orElse(null);
    }

    /**
     * Cria uma nova categoria.
     * Lança IllegalArgumentException se o nome já existir.
     */
    public CategoriaDTO criar(CategoriaDTO dto) {
        String nome = dto.getNome() == null ? "" : dto.getNome().trim();
        if (nome.isEmpty()) {
            throw new IllegalArgumentException("O nome da categoria é obrigatório.");
        }

        boolean nomeRepetido = memoriaCategoria.values().stream()
                .anyMatch(c -> c.getNome().equalsIgnoreCase(nome));
        if (nomeRepetido) {
            throw new IllegalArgumentException("Já existe uma categoria com este nome: " + nome);
        }

        Categoria cat = new Categoria();
        cat.setId(proximoIdCategoria++);
        cat.setNome(nome);
        cat.setDescricao(dto.getDescricao());

        memoriaCategoria.put(cat.getId(), cat);
        return toDTO(cat);

        // TODO (banco):
        // if (categoriaRepository.existsByNomeIgnoreCase(nome))
        //     throw new IllegalArgumentException("Já existe uma categoria com este nome.");
        // Categoria cat = new Categoria();
        // cat.setNome(nome);
        // cat.setDescricao(dto.getDescricao());
        // return toDTO(categoriaRepository.save(cat));
    }

    /**
     * Atualiza nome e/ou descrição de uma categoria existente.
     * Lança IllegalArgumentException se não encontrada ou se o novo nome já existir em outra.
     */
    public CategoriaDTO atualizar(Long id, CategoriaDTO dto) {
        Categoria cat = memoriaCategoria.get(id);
        if (cat == null) {
            throw new IllegalArgumentException("Categoria não encontrada: id=" + id);
        }

        String novoNome = dto.getNome() == null ? "" : dto.getNome().trim();
        if (novoNome.isEmpty()) {
            throw new IllegalArgumentException("O nome da categoria é obrigatório.");
        }

        boolean nomeRepetido = memoriaCategoria.values().stream()
                .anyMatch(c -> !c.getId().equals(id) && c.getNome().equalsIgnoreCase(novoNome));
        if (nomeRepetido) {
            throw new IllegalArgumentException("Já existe outra categoria com este nome: " + novoNome);
        }

        cat.setNome(novoNome);
        cat.setDescricao(dto.getDescricao());
        return toDTO(cat);

        // TODO (banco):
        // Categoria cat = categoriaRepository.findById(id)
        //     .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));
        // cat.setNome(novoNome);
        // cat.setDescricao(dto.getDescricao());
        // return toDTO(categoriaRepository.save(cat));
    }

    /**
     * Remove uma categoria.
     * Lança IllegalStateException se houver movimentações vinculadas a ela.
     */
    public void deletar(Long id) {
        if (!memoriaCategoria.containsKey(id)) {
            throw new IllegalArgumentException("Categoria não encontrada: id=" + id);
        }

        boolean temMovimentacoes = memoriaMovimentacao.values().stream()
                .anyMatch(m -> id.equals(m.getCategoriaId()));
        if (temMovimentacoes) {
            throw new IllegalStateException(
                "Não é possível excluir: existem movimentações vinculadas a esta categoria."
            );
        }

        memoriaCategoria.remove(id);

        // TODO (banco):
        // if (!categoriaRepository.existsById(id))
        //     throw new IllegalArgumentException("Categoria não encontrada.");
        // boolean temMovimentacoes = movimentacaoRepository.findAll().stream()
        //     .anyMatch(m -> id.equals(m.getCategoriaId()));
        // if (temMovimentacoes)
        //     throw new IllegalStateException("Não é possível excluir: existem movimentações vinculadas.");
        // categoriaRepository.deleteById(id);
    }

    // ════════════════════════════════════════
    //  RESUMO POR CATEGORIA
    // ════════════════════════════════════════

    /**
     * Retorna o resumo financeiro agrupado por categoria:
     * total de movimentações, total de receitas, total de despesas e saldo.
     */
    public List<CategoriaResumoDTO> resumo() {
        List<CategoriaResumoDTO> resultado = new ArrayList<>();

        for (Categoria cat : memoriaCategoria.values()) {
            List<Movimentacao> movs = memoriaMovimentacao.values().stream()
                    .filter(m -> cat.getId().equals(m.getCategoriaId()))
                    .collect(Collectors.toList());

            CategoriaResumoDTO r = calcularResumo(cat, movs);
            resultado.add(r);
        }

        // Ordenar por total de despesas (maior primeiro)
        resultado.sort(Comparator.comparing(CategoriaResumoDTO::getTotalDespesas).reversed());

        return resultado;

        // TODO (banco):
        // List<Categoria> cats = categoriaRepository.findAll();
        // List<Movimentacao> todasMovs = movimentacaoRepository.findAll();
        // return cats.stream().map(cat -> {
        //     List<Movimentacao> movs = todasMovs.stream()
        //         .filter(m -> cat.getId().equals(m.getCategoriaId()))
        //         .collect(Collectors.toList());
        //     return calcularResumo(cat, movs);
        // }).sorted(Comparator.comparing(CategoriaResumoDTO::getTotalDespesas).reversed())
        //   .collect(Collectors.toList());
    }

    /**
     * Resumo de uma categoria específica.
     */
    public CategoriaResumoDTO resumoPorId(Long id) {
        Categoria cat = memoriaCategoria.get(id);
        if (cat == null) {
            throw new IllegalArgumentException("Categoria não encontrada: id=" + id);
        }

        List<Movimentacao> movs = memoriaMovimentacao.values().stream()
                .filter(m -> id.equals(m.getCategoriaId()))
                .collect(Collectors.toList());

        return calcularResumo(cat, movs);

        // TODO (banco):
        // Categoria cat = categoriaRepository.findById(id)
        //     .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));
        // List<Movimentacao> movs = movimentacaoRepository.findAll().stream()
        //     .filter(m -> id.equals(m.getCategoriaId())).collect(Collectors.toList());
        // return calcularResumo(cat, movs);
    }

    // ════════════════════════════════════════
    //  MOVIMENTAÇÕES (memória) — para testes
    //  TODO: remover quando o banco estiver pronto
    // ════════════════════════════════════════

    /** Salva uma movimentação em memória (usado nos testes sem banco). */
    public Movimentacao salvarMovimentacao(Movimentacao mov) {
        if (mov.getId() == null) {
            mov.setId(proximoIdMovimentacao++);
        }
        memoriaMovimentacao.put(mov.getId(), mov);
        return mov;
        // TODO (banco): return movimentacaoRepository.save(mov);
    }

    // ════════════════════════════════════════
    //  HELPERS PRIVADOS
    // ════════════════════════════════════════

    private CategoriaResumoDTO calcularResumo(Categoria cat, List<Movimentacao> movs) {
        BigDecimal totalReceitas = movs.stream()
                .filter(m -> TipoMovimentacao.RECEITA.equals(m.getTipo()))
                .map(Movimentacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDespesas = movs.stream()
                .filter(m -> TipoMovimentacao.DESPESA.equals(m.getTipo()))
                .map(Movimentacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CategoriaResumoDTO r = new CategoriaResumoDTO();
        r.setCategoriaId(cat.getId());
        r.setCategoriaNome(cat.getNome());
        r.setTotalMovimentacoes(movs.size());
        r.setTotalReceitas(totalReceitas);
        r.setTotalDespesas(totalDespesas);
        r.setSaldo(totalReceitas.subtract(totalDespesas));
        return r;
    }

    private CategoriaDTO toDTO(Categoria cat) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setId(cat.getId());
        dto.setNome(cat.getNome());
        dto.setDescricao(cat.getDescricao());

        long count = memoriaMovimentacao.values().stream()
                .filter(m -> cat.getId().equals(m.getCategoriaId()))
                .count();
        BigDecimal total = memoriaMovimentacao.values().stream()
                .filter(m -> cat.getId().equals(m.getCategoriaId()))
                .map(Movimentacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        dto.setTotalMovimentacoes((int) count);
        dto.setTotalValor(total);
        return dto;
    }
}
