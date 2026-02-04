package com.intuitivecare.desafio.repository;

import com.intuitivecare.desafio.model.Despesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {

    List<Despesa> findByOperadora_RegistroAns(String registroAns);

    @Query("SELECT SUM(d.valor) FROM Despesa d")
    Double somarTodasDespesas();
}