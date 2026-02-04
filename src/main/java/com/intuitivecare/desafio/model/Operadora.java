package com.intuitivecare.desafio.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "operadoras") // O nome exato da tabela no seu banco
public class Operadora {

    @Id
    private String registroAns; // Usando registroAns como ID, confirme se é sua PK

    private String cnpj;
    private String razaoSocial;
    private String modalidade;
    private String uf;
}