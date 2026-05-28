package com.ucb.finlog.service;

import com.ucb.finlog.dto.MovimentacaoRequest;
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

    public MovimentacaoService(
            MovimentacaoRepository repository,
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository
    ) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Movimentacao> listarTodas(String emailUsuario) {
        return repository.findByUsuarioEmail(emailUsuario);
    }

    public Movimentacao salvar(MovimentacaoRequest request, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setUsuario(usuario);
        aplicarDados(movimentacao, request, usuario, emailUsuario, false);

        return repository.save(movimentacao);
    }

    public Movimentacao buscarPorId(Long id, String emailUsuario) {
        return repository.findByIdAndUsuarioEmail(id, emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movimentacao nao encontrada"));
    }

    public Movimentacao atualizar(Long id, MovimentacaoRequest request, String emailUsuario) {
        Movimentacao movimentacao = repository.findByIdAndUsuarioEmail(id, emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movimentacao nao encontrada"));

        aplicarDados(movimentacao, request, movimentacao.getUsuario(), emailUsuario, true);

        return repository.save(movimentacao);
    }

    public void deletar(Long id, String emailUsuario) {
        Movimentacao movimentacao = repository.findByIdAndUsuarioEmail(id, emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movimentacao nao encontrada"));
        repository.delete(movimentacao);
    }

    private void aplicarDados(
            Movimentacao movimentacao,
            MovimentacaoRequest request,
            Usuario usuario,
            String emailUsuario,
            boolean preservarCategoriaAtual
    ) {
        movimentacao.setDescricao(request.descricao());
        movimentacao.setValor(request.valor());
        movimentacao.setData(request.data());
        movimentacao.setTipo(request.tipo());
        movimentacao.setCategoria(resolverCategoria(request, usuario, emailUsuario, movimentacao.getCategoria(), preservarCategoriaAtual));
    }

    private Categoria resolverCategoria(
            MovimentacaoRequest request,
            Usuario usuario,
            String emailUsuario,
            Categoria categoriaAtual,
            boolean preservarCategoriaAtual
    ) {
        if (request.categoriaId() != null) {
            return categoriaRepository.findByIdAndUsuarioEmail(request.categoriaId(), emailUsuario)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria nao encontrada"));
        }

        String nomeCategoriaRequest = request.categoriaNomeEfetivo();
        if (nomeCategoriaRequest == null) {
            return preservarCategoriaAtual ? categoriaAtual : null;
        }

        String nomeCategoria = nomeCategoriaRequest.trim();
        if (nomeCategoria.isEmpty()) {
            return null;
        }

        return categoriaRepository.findByNomeIgnoreCaseAndUsuarioEmail(nomeCategoria, emailUsuario)
                .orElseGet(() -> {
                    Categoria nova = new Categoria();
                    nova.setNome(nomeCategoria);
                    nova.setUsuario(usuario);
                    return categoriaRepository.save(nova);
                });
    }
}
