package com.ucb.finlog.repository;

import com.ucb.finlog.model.Meta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetaRepository extends JpaRepository<Meta, Long> {
    List<Meta> findByUsuarioEmailOrderByIdAsc(String email);

    Optional<Meta> findByIdAndUsuarioEmail(Long id, String email);
}
