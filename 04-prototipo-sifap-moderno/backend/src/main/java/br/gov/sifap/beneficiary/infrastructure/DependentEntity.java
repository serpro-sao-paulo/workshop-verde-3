package br.gov.sifap.beneficiary.infrastructure;

import br.gov.sifap.beneficiary.domain.Parentesco;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Entidade JPA do dependente. Mapeada do grupo periódico (PE) DEPENDENTES do
 * DDM BENEFICIARIO — normalizada em tabela filha (ADR-002).
 */
@Entity
@Table(name = "beneficiary_dependent", schema = "beneficiary")
public class DependentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private BeneficiaryEntity beneficiary;

    @Column(name = "cpf", nullable = false, length = 11)
    private String cpf;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "parentesco", nullable = false, length = 10)
    private Parentesco parentesco;

    protected DependentEntity() {
    }

    public DependentEntity(BeneficiaryEntity beneficiary, String cpf, String name, Parentesco parentesco) {
        this.beneficiary = beneficiary;
        this.cpf = cpf;
        this.name = name;
        this.parentesco = parentesco;
    }

    public UUID getId() {
        return id;
    }

    public String getCpf() {
        return cpf;
    }

    public String getName() {
        return name;
    }

    public Parentesco getParentesco() {
        return parentesco;
    }
}
