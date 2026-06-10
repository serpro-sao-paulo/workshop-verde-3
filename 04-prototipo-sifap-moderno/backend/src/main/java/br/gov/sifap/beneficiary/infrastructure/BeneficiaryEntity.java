package br.gov.sifap.beneficiary.infrastructure;

import br.gov.sifap.beneficiary.domain.BeneficiaryStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Entidade JPA do beneficiário. Mapeada do DDM BENEFICIARIO (FNR 150). */
@Entity
@Table(name = "beneficiary", schema = "beneficiary")
public class BeneficiaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(name = "nis", length = 11)
    private String nis;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "sex", nullable = false, length = 1)
    private String sex;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private BeneficiaryStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;

    @OneToMany(mappedBy = "beneficiary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DependentEntity> dependents = new ArrayList<>();

    protected BeneficiaryEntity() {
    }

    public BeneficiaryEntity(String cpf, String nis, String name, LocalDate birthDate,
                             String sex, BeneficiaryStatus status, LocalDate today) {
        this.cpf = cpf;
        this.nis = nis;
        this.name = name;
        this.birthDate = birthDate;
        this.sex = sex;
        this.status = status;
        this.createdAt = today;
        this.updatedAt = today;
    }

    public UUID getId() {
        return id;
    }

    public String getCpf() {
        return cpf;
    }

    public String getNis() {
        return nis;
    }

    public String getName() {
        return name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getSex() {
        return sex;
    }

    public BeneficiaryStatus getStatus() {
        return status;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public List<DependentEntity> getDependents() {
        return dependents;
    }
}
