package com.ucb.finlog.controller;

import com.ucb.finlog.dto.CategoriaDTO;
import com.ucb.finlog.dto.CategoriaResumoDTO;
import com.ucb.finlog.model.Movimentacao;
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

    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> listar() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> buscar(@PathVariable Long id) {
        CategoriaDTO dto = service.buscarPorId(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody CategoriaDTO dto) {
        try {
            CategoriaDTO criada = service.criar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(criada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody CategoriaDTO dto) {
        try {
            CategoriaDTO atualizada = service.atualizar(id, dto);
            return ResponseEntity.ok(atualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

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

    @GetMapping("/resumo")
    public ResponseEntity<List<CategoriaResumoDTO>> resumo() {
        return ResponseEntity.ok(service.resumo());
    }

    @GetMapping("/{id}/resumo")
    public ResponseEntity<?> resumoPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.resumoPorId(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/teste/movimentacao")
    public ResponseEntity<Movimentacao> salvarMovimentacaoTeste(@RequestBody Movimentacao mov) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvarMovimentacao(mov));
    }
}
