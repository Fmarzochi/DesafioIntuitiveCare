package com.intuitivecare.desafio.repository;

import com.intuitivecare.desafio.model.Despesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DespesaRepository extends JpaRepository<Despesa, Long> {
    // Busca as despesas pelo Registro ANS da operadora
    List<Despesa> findByOperadora_RegistroAns(String registroAns);
}