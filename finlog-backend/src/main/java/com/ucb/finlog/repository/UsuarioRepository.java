package com.ucb.finlog.repository;

import com.ucb.finlog.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Aqui poderemos criar buscas por e-mail para o UC01 (Login) futuramente
}