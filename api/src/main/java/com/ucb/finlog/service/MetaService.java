package com.ucb.finlog.service;

import com.ucb.finlog.dto.MetaRequest;
import com.ucb.finlog.model.Meta;
import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.repository.MetaRepository;
import com.ucb.finlog.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class MetaService {
    private final MetaRepository repository;
    private final UsuarioRepository usuarioRepository;

    public MetaService(MetaRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Meta> listar(String emailUsuario) {
        return repository.findByUsuarioEmailOrderByIdAsc(emailUsuario);
    }

    public Meta criar(MetaRequest request, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));

        Meta meta = new Meta();
        meta.setUsuario(usuario);
        aplicarDados(meta, request);
        return repository.save(meta);
    }

    public Meta atualizar(Long id, MetaRequest request, String emailUsuario) {
        Meta meta = buscarDoUsuario(id, emailUsuario);
        aplicarDados(meta, request);
        return repository.save(meta);
    }

    public void deletar(Long id, String emailUsuario) {
        repository.delete(buscarDoUsuario(id, emailUsuario));
    }

    private Meta buscarDoUsuario(Long id, String emailUsuario) {
        return repository.findByIdAndUsuarioEmail(id, emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meta nao encontrada"));
    }

    private void aplicarDados(Meta meta, MetaRequest request) {
        meta.setDescricao(request.descricao().trim());
        meta.setValor(request.valor());
    }
}
