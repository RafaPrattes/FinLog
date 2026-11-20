package com.ucb.finlog.repository;

import com.ucb.finlog.model.ChatMensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMensagemRepository extends JpaRepository<ChatMensagem, Long> {
    List<ChatMensagem> findByUsuarioEmailOrderByIdAsc(String email);

    List<ChatMensagem> findTop10ByUsuarioEmailOrderByIdDesc(String email);

    void deleteByUsuarioId(Long usuarioId);
}
