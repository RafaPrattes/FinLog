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

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    // ════════════════════════════════════════
    //  CATEGORIAS — CRUD
    // ════════════════════════════════════════

    /** Lista todas as categorias cadastradas. */
    public List<CategoriaDTO> listarTodas() {
        List<Movimentacao> todasMovs = movimentacaoRepository.findAll();
        return categoriaRepository.findAll()
                .stream()
                .map(cat -> toDTO(cat, todasMovs))
                .collect(Collectors.toList());
    }

    /** Busca uma categoria pelo ID. Retorna null se não encontrada. */
    public CategoriaDTO buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .map(cat -> toDTO(cat, movimentacaoRepository.findAll()))
                .orElse(null);
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
        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new IllegalArgumentException("Já existe uma categoria com este nome: " + nome);
        }

        Categoria cat = new Categoria();
        cat.setNome(nome);
        cat.setDescricao(dto.getDescricao());

        return toDTO(categoriaRepository.save(cat), movimentacaoRepository.findAll());
    }

    /**
     * Atualiza nome e/ou descrição de uma categoria existente.
     * Lança IllegalArgumentException se não encontrada ou se o novo nome já existir em outra.
     */
    public CategoriaDTO atualizar(Long id, CategoriaDTO dto) {
        Categoria cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada: id=" + id));

        String novoNome = dto.getNome() == null ? "" : dto.getNome().trim();
        if (novoNome.isEmpty()) {
            throw new IllegalArgumentException("O nome da categoria é obrigatório.");
        }

        boolean nomeRepetido = categoriaRepository.findAll().stream()
                .anyMatch(c -> !c.getId().equals(id) && c.getNome().equalsIgnoreCase(novoNome));
        if (nomeRepetido) {
            throw new IllegalArgumentException("Já existe outra categoria com este nome: " + novoNome);
        }

        cat.setNome(novoNome);
        cat.setDescricao(dto.getDescricao());
        return toDTO(categoriaRepository.save(cat), movimentacaoRepository.findAll());
    }

    /**
     * Remove uma categoria.
     * Lança IllegalStateException se houver movimentações vinculadas a ela.
     */
    public void deletar(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new IllegalArgumentException("Categoria não encontrada: id=" + id);
        }

        boolean temMovimentacoes = movimentacaoRepository.findAll().stream()
                .anyMatch(m -> m.getCategoria() != null && id.equals(m.getCategoria().getId()));
        if (temMovimentacoes) {
            throw new IllegalStateException(
                "Não é possível excluir: existem movimentações vinculadas a esta categoria."
            );
        }

        categoriaRepository.deleteById(id);
    }

    // ════════════════════════════════════════
    //  RESUMO POR CATEGORIA
    // ════════════════════════════════════════

    /**
     * Retorna o resumo financeiro agrupado por categoria:
     * total de movimentações, total de receitas, total de despesas e saldo.
     */
    public List<CategoriaResumoDTO> resumo() {
        List<Movimentacao> todasMovs = movimentacaoRepository.findAll();

        return categoriaRepository.findAll().stream()
                .map(cat -> {
                    List<Movimentacao> movs = todasMovs.stream()
                            .filter(m -> m.getCategoria() != null
                                    && cat.getId().equals(m.getCategoria().getId()))
                            .collect(Collectors.toList());
                    return calcularResumo(cat, movs);
                })
                .sorted(Comparator.comparing(CategoriaResumoDTO::getTotalDespesas).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Resumo de uma categoria específica.
     */
    public CategoriaResumoDTO resumoPorId(Long id) {
        Categoria cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada: id=" + id));

        List<Movimentacao> movs = movimentacaoRepository.findAll().stream()
                .filter(m -> m.getCategoria() != null && id.equals(m.getCategoria().getId()))
                .collect(Collectors.toList());

        return calcularResumo(cat, movs);
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

    private CategoriaDTO toDTO(Categoria cat, List<Movimentacao> todasMovs) {
        List<Movimentacao> movsDaCategoria = todasMovs.stream()
                .filter(m -> m.getCategoria() != null && cat.getId().equals(m.getCategoria().getId()))
                .collect(Collectors.toList());

        BigDecimal total = movsDaCategoria.stream()
                .map(Movimentacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CategoriaDTO dto = new CategoriaDTO();
        dto.setId(cat.getId());
        dto.setNome(cat.getNome());
        dto.setDescricao(cat.getDescricao());
        dto.setTotalMovimentacoes(movsDaCategoria.size());
        dto.setTotalValor(total);
        return dto;
    }
}