package com.intuitivecare.desafio.repository;

import com.intuitivecare.desafio.model.Operadora;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OperadoraRepository extends JpaRepository<Operadora, String> {

    // --- BUSCA INTELIGENTE (CNPJ, NOME E ANS) ---
    // :termo -> O texto exatamente como o usuário digitou (para buscar nomes)
    // :termoNumerico -> Apenas os números (para buscar CNPJ mesmo se tiver pontos na busca)
    @Query("SELECT o FROM Operadora o WHERE " +
            "LOWER(o.razaoSocial) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "o.registroAns LIKE CONCAT('%', :termo, '%') OR " +
            "o.cnpj LIKE CONCAT('%', :termoNumerico, '%')")
    Page<Operadora> buscarGeral(@Param("termo") String termo,
                                @Param("termoNumerico") String termoNumerico,
                                Pageable pageable);
}