package com.intuitivecare.desafio.repository;

import com.intuitivecare.desafio.model.Operadora;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OperadoraRepository extends JpaRepository<Operadora, String> {

    @Query("SELECT o FROM Operadora o WHERE LOWER(o.razaoSocial) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Operadora> searchByRazaoSocial(@Param("search") String search, Pageable pageable);
}