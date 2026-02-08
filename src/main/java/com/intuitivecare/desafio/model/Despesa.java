package com.intuitivecare.desafio.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "despesas")
public class Despesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_evento")
    private LocalDate dataEvento;

    @Column(name = "descricao")
    private String descricao;

    private Double valor;

    @ManyToOne
    @JoinColumn(name = "operadora_id", referencedColumnName = "registroAns") // A chave estrangeira
    private Operadora operadora;
}