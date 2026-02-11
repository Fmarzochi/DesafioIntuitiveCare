package com.intuitivecare.desafio.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "dados_agregados")
public class DadosAgregados {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razao_social")
    private String razaoSocial;

    private String uf;

    @Column(name = "total_despesas")
    private Double totalDespesas;

    @Column(name = "media_trimestral")
    private Double mediaTrimestral;

    @Column(name = "desvio_padrao")
    private Double desvioPadrao;

    @Column(name = "qtd_registros")
    private Integer qtdRegistros;
}