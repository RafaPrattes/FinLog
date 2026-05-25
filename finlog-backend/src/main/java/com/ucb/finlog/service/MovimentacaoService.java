package com.ucb.finlog.service;

import com.ucb.finlog.model.Categoria;
import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.repository.CategoriaRepository;
import com.ucb.finlog.repository.MovimentacaoRepository;
import com.ucb.finlog.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MovimentacaoService {
    private final MovimentacaoRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;

    public MovimentacaoService(MovimentacaoRepository repository, UsuarioRepository usuarioRepository, CategoriaRepository categoriaRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Movimentacao> listarTodas(String emailUsuario) {
        return repository.findByUsuarioEmail(emailUsuario);
    }

    public Movimentacao salvar(Movimentacao movimentacao, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        movimentacao.setUsuario(usuario);

        if (movimentacao.getCategoria() != null && movimentacao.getCategoria().getNome() != null) {
            String nomeCategoria = movimentacao.getCategoria().getNome().trim();
            Categoria categoria = categoriaRepository.findByNomeIgnoreCase(nomeCategoria)
                    .orElseGet(() -> {
                        Categoria nova = new Categoria();
                        nova.setNome(nomeCategoria);
                        return categoriaRepository.save(nova);
                    });
            movimentacao.setCategoria(categoria);
        }

        return repository.save(movimentacao);
    }

    public Movimentacao buscarPorId(Long id, String emailUsuario) {
        return repository.findByIdAndUsuarioEmail(id, emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movimentação não encontrada"));
    }

    public void deletar(Long id, String emailUsuario) {
        Movimentacao movimentacao = repository.findByIdAndUsuarioEmail(id, emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movimentação não encontrada"));
        repository.delete(movimentacao);
    }

    public Movimentacao atualizar(Long id, Movimentacao movimentacao, String emailUsuario) {
    Movimentacao existente = repository.findByIdAndUsuarioEmail(id, emailUsuario)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movimentação não encontrada"));

    existente.setDescricao(movimentacao.getDescricao());
    existente.setValor(movimentacao.getValor());
    existente.setData(movimentacao.getData());
    existente.setTipo(movimentacao.getTipo());

    if (movimentacao.getCategoria() != null && movimentacao.getCategoria().getNome() != null) {
        String nomeCategoria = movimentacao.getCategoria().getNome().trim();
        Categoria categoria = categoriaRepository.findByNomeIgnoreCase(nomeCategoria)
                .orElseGet(() -> {
                    Categoria nova = new Categoria();
                    nova.setNome(nomeCategoria);
                    return categoriaRepository.save(nova);
                });
        existente.setCategoria(categoria);
    }

    return repository.save(existente);
    }   
}