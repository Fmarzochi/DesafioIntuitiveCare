package com.intuitivecare.desafio.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "demonstracoes_contabeis")
public class DemonstracaoContabil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_competencia")
    private String dataCompetencia;

    @Column(name = "reg_ans")
    private String regAns;

    @Column(name = "cd_conta_contabil")
    private String cdContaContabil;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "vl_saldo_inicial")
    private BigDecimal vlSaldoInicial;

    @Column(name = "vl_saldo_final")
    private BigDecimal vlSaldoFinal;
}