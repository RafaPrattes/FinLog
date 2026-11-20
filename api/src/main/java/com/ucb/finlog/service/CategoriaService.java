package com.ucb.finlog.service;

import com.ucb.finlog.dto.CategoriaDTO;
import com.ucb.finlog.dto.CategoriaRequest;
import com.ucb.finlog.dto.CategoriaResumoDTO;
import com.ucb.finlog.model.Categoria;
import com.ucb.finlog.model.Movimentacao;
import com.ucb.finlog.model.TipoMovimentacao;
import com.ucb.finlog.model.Usuario;
import com.ucb.finlog.repository.CategoriaRepository;
import com.ucb.finlog.repository.MovimentacaoRepository;
import com.ucb.finlog.repository.TotalPorCategoria;
import com.ucb.finlog.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
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
        Map<Long, Totais> totais = totaisPorCategoria(emailUsuario);
        return categoriaRepository.findByUsuarioEmailOrderByNomeAsc(emailUsuario).stream()
                .map(categoria -> CategoriaDTO.from(categoria, totaisDa(totais, categoria).total()))
                .toList();
    }

    public CategoriaDTO buscarPorId(Long id, String emailUsuario) {
        Categoria categoria = buscarDoUsuario(id, emailUsuario);
        return CategoriaDTO.from(categoria, totalDaCategoria(categoria.getId(), emailUsuario));
    }

    public CategoriaDTO criar(CategoriaRequest request, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));

        String nome = normalizarNome(request.nome());
        if (categoriaRepository.existsByNomeIgnoreCaseAndUsuarioEmail(nome, emailUsuario)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Categoria ja cadastrada");
        }

        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        categoria.setDescricao(request.descricao());
        categoria.setUsuario(usuario);

        // Uma categoria recem-criada ainda nao tem movimentacoes.
        return CategoriaDTO.from(categoriaRepository.save(categoria), BigDecimal.ZERO);
    }

    public CategoriaDTO atualizar(Long id, CategoriaRequest request, String emailUsuario) {
        Categoria categoria = buscarDoUsuario(id, emailUsuario);
        String nome = normalizarNome(request.nome());

        categoriaRepository.findByNomeIgnoreCaseAndUsuarioEmail(nome, emailUsuario)
                .filter(existente -> !existente.getId().equals(id))
                .ifPresent(existente -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Categoria ja cadastrada");
                });

        categoria.setNome(nome);
        categoria.setDescricao(request.descricao());

        Categoria salva = categoriaRepository.save(categoria);
        return CategoriaDTO.from(salva, totalDaCategoria(salva.getId(), emailUsuario));
    }

    public List<CategoriaResumoDTO> resumo(String emailUsuario) {
        Map<Long, Totais> totais = totaisPorCategoria(emailUsuario);
        return categoriaRepository.findByUsuarioEmailOrderByNomeAsc(emailUsuario).stream()
                .map(categoria -> resumoDaCategoria(categoria, totaisDa(totais, categoria)))
                .toList();
    }

    public CategoriaResumoDTO resumoPorId(Long id, String emailUsuario) {
        Categoria categoria = buscarDoUsuario(id, emailUsuario);
        List<Movimentacao> movimentacoes = movimentacaoRepository.findByCategoriaIdAndUsuarioEmail(id, emailUsuario);
        Totais totais = new Totais(
                totalPorTipo(movimentacoes, TipoMovimentacao.RECEITA),
                totalPorTipo(movimentacoes, TipoMovimentacao.DESPESA)
        );
        return resumoDaCategoria(categoria, totais);
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

    private CategoriaResumoDTO resumoDaCategoria(Categoria categoria, Totais totais) {
        return new CategoriaResumoDTO(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                totais.receitas(),
                totais.despesas(),
                totais.receitas().subtract(totais.despesas())
        );
    }

    /**
     * Soma os valores de todas as categorias do usuario em uma unica consulta,
     * evitando uma consulta de movimentacoes por categoria (N+1).
     */
    private Map<Long, Totais> totaisPorCategoria(String emailUsuario) {
        Map<Long, Totais> totais = new HashMap<>();
        for (TotalPorCategoria linha : movimentacaoRepository.somarPorCategoriaETipo(emailUsuario)) {
            BigDecimal valor = linha.total() == null ? BigDecimal.ZERO : linha.total();
            Totais parcial = linha.tipo() == TipoMovimentacao.RECEITA
                    ? new Totais(valor, BigDecimal.ZERO)
                    : new Totais(BigDecimal.ZERO, valor);
            totais.merge(linha.categoriaId(), parcial, Totais::somar);
        }
        return totais;
    }

    private Totais totaisDa(Map<Long, Totais> totais, Categoria categoria) {
        return totais.getOrDefault(categoria.getId(), Totais.VAZIO);
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

    private record Totais(BigDecimal receitas, BigDecimal despesas) {
        static final Totais VAZIO = new Totais(BigDecimal.ZERO, BigDecimal.ZERO);

        Totais somar(Totais outro) {
            return new Totais(receitas.add(outro.receitas), despesas.add(outro.despesas));
        }

        BigDecimal total() {
            return receitas.add(despesas);
        }
    }
}
