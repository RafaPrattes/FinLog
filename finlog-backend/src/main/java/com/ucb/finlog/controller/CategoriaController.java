package com.ucb.finlog.controller;

import com.ucb.finlog.dto.CategoriaDTO;
import com.ucb.finlog.dto.CategoriaResumoDTO;
import com.ucb.finlog.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService service;

    // ────────────────────────────────────────
    //  CRUD DE CATEGORIAS
    // ────────────────────────────────────────

    /**
     * GET /api/categorias
     * Lista todas as categorias com contagem e total de movimentações.
     */
    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> listar() {
        return ResponseEntity.ok(service.listarTodas());
    }

    /**
     * GET /api/categorias/{id}
     * Retorna uma categoria específica.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> buscar(@PathVariable Long id) {
        CategoriaDTO dto = service.buscarPorId(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    /**
     * POST /api/categorias
     * Cria uma nova categoria.
     * Body: { "nome": "Alimentação", "descricao": "Gastos com comida" }
     */
    @PostMapping
    public ResponseEntity<?> criar(@RequestBody CategoriaDTO dto) {
        try {
            CategoriaDTO criada = service.criar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(criada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PUT /api/categorias/{id}
     * Atualiza nome e/ou descrição de uma categoria.
     * Body: { "nome": "Lazer", "descricao": "Entretenimento e diversão" }
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody CategoriaDTO dto) {
        try {
            CategoriaDTO atualizada = service.atualizar(id, dto);
            return ResponseEntity.ok(atualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * DELETE /api/categorias/{id}
     * Remove uma categoria (somente se não houver movimentações vinculadas).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // ────────────────────────────────────────
    //  RESUMO FINANCEIRO POR CATEGORIA
    // ────────────────────────────────────────

    /**
     * GET /api/categorias/resumo
     * Retorna o resumo de receitas, despesas e saldo agrupado por categoria.
     * Ordenado por maior despesa.
     */
    @GetMapping("/resumo")
    public ResponseEntity<List<CategoriaResumoDTO>> resumo() {
        return ResponseEntity.ok(service.resumo());
    }

    /**
     * GET /api/categorias/{id}/resumo
     * Retorna o resumo financeiro de uma categoria específica.
     */
    @GetMapping("/{id}/resumo")
    public ResponseEntity<?> resumoPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.resumoPorId(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}