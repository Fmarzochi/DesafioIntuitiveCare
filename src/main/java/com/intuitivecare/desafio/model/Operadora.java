package com.intuitivecare.desafio.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "operadoras")
public class Operadora {

    @Id
    private String registroAns; // Usando registroAns como ID.

    private String cnpj;
    private String razaoSocial;
    private String modalidade;
    private String uf;
}