package com.ucb.finlog.service;

import com.ucb.finlog.dto.CategoriaDTO;
import com.ucb.finlog.dto.CategoriaResumoDTO;
import com.ucb.finlog.model.Categoria;
import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.model.TipoMovimentacao;
import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.repository.CategoriaRepository;
import com.ucb.finlog.repository.MovimentacaoRepository;
import com.ucb.finlog.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public CategoriaService(
            CategoriaRepository categoriaRepository,
            MovimentacaoRepository movimentacaoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.categoriaRepository = categoriaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<CategoriaDTO> listarTodas(String emailUsuario) {
        return categoriaRepository.findByUsuarioEmailOrderByNomeAsc(emailUsuario).stream()
                .map(categoria -> CategoriaDTO.from(categoria, totalDaCategoria(categoria.getId(), emailUsuario)))
                .toList();
    }

    public CategoriaDTO buscarPorId(Long id, String emailUsuario) {
        Categoria categoria = buscarDoUsuario(id, emailUsuario);
        return CategoriaDTO.from(categoria, totalDaCategoria(categoria.getId(), emailUsuario));
    }

    public CategoriaDTO criar(CategoriaDTO dto, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));

        String nome = normalizarNome(dto.getNome());
        if (categoriaRepository.existsByNomeIgnoreCaseAndUsuarioEmail(nome, emailUsuario)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Categoria ja cadastrada");
        }

        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setDescricao(dto.getDescricao());
        categoria.setUsuario(usuario);

        Categoria salva = categoriaRepository.save(categoria);
        return CategoriaDTO.from(salva, totalDaCategoria(salva.getId(), emailUsuario));
    }

    public CategoriaDTO atualizar(Long id, CategoriaDTO dto, String emailUsuario) {
        Categoria categoria = buscarDoUsuario(id, emailUsuario);
        String nome = normalizarNome(dto.getNome());

        categoriaRepository.findByNomeIgnoreCaseAndUsuarioEmail(nome, emailUsuario)
                .filter(existente -> !existente.getId().equals(id))
                .ifPresent(existente -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Categoria ja cadastrada");
                });

        categoria.setNome(nome);
        categoria.setDescricao(dto.getDescricao());

        Categoria salva = categoriaRepository.save(categoria);
        return CategoriaDTO.from(salva, totalDaCategoria(salva.getId(), emailUsuario));
    }

    public List<CategoriaResumoDTO> resumo(String emailUsuario) {
        return categoriaRepository.findByUsuarioEmailOrderByNomeAsc(emailUsuario).stream()
                .map(categoria -> resumoDaCategoria(categoria, emailUsuario))
                .toList();
    }

    public CategoriaResumoDTO resumoPorId(Long id, String emailUsuario) {
        return resumoDaCategoria(buscarDoUsuario(id, emailUsuario), emailUsuario);
    }

    public void deletar(Long id, String emailUsuario) {
        Categoria categoria = buscarDoUsuario(id, emailUsuario);

        if (movimentacaoRepository.existsByCategoriaIdAndUsuarioEmail(id, emailUsuario)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Categoria possui movimentacoes");
        }

        categoriaRepository.delete(categoria);
    }

    private Categoria buscarDoUsuario(Long id, String emailUsuario) {
        return categoriaRepository.findByIdAndUsuarioEmail(id, emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria nao encontrada"));
    }

    private CategoriaResumoDTO resumoDaCategoria(Categoria categoria, String emailUsuario) {
        List<Movimentacao> movimentacoes = movimentacaoRepository.findByCategoriaIdAndUsuarioEmail(
                categoria.getId(),
                emailUsuario
        );

        BigDecimal totalReceitas = totalPorTipo(movimentacoes, TipoMovimentacao.RECEITA);
        BigDecimal totalDespesas = totalPorTipo(movimentacoes, TipoMovimentacao.DESPESA);

        return new CategoriaResumoDTO(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                totalReceitas,
                totalDespesas,
                totalReceitas.subtract(totalDespesas)
        );
    }

    private BigDecimal totalDaCategoria(Long categoriaId, String emailUsuario) {
        return movimentacaoRepository.findByCategoriaIdAndUsuarioEmail(categoriaId, emailUsuario).stream()
                .map(Movimentacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalPorTipo(List<Movimentacao> movimentacoes, TipoMovimentacao tipo) {
        return movimentacoes.stream()
                .filter(movimentacao -> tipo == movimentacao.getTipo())
                .map(Movimentacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String normalizarNome(String nome) {
        String nomeNormalizado = nome == null ? "" : nome.trim();
        if (nomeNormalizado.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome da categoria e obrigatorio");
        }
        return nomeNormalizado;
    }
}
