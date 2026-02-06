package com.intuitivecare.desafio.repository;

import com.intuitivecare.desafio.model.Operadora;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OperadoraRepository extends JpaRepository<Operadora, String> {

    // Método antigo (mantido por segurança, mas não usado na busca principal)
    @Query("SELECT o FROM Operadora o WHERE LOWER(o.razaoSocial) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Operadora> searchByRazaoSocial(@Param("search") String search, Pageable pageable);

    // NOVO MÉTODO: Busca Inteligente (Requisito 4.3)
    // Verifica se o texto digitado existe no Nome, no Registro ANS ou no CNPJ
    @Query("SELECT o FROM Operadora o WHERE " +
            "LOWER(o.razaoSocial) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
            "o.registroAns LIKE CONCAT('%', :texto, '%') OR " +
            "o.cnpj LIKE CONCAT('%', :texto, '%')")
    Page<Operadora> buscarPorTexto(@Param("texto") String texto, Pageable pageable);
}