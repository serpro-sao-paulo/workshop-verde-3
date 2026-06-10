-- V1__init_beneficiary_module.sql
-- Schema do módulo Beneficiary — mapeado do DDM BENEFICIARIO (FNR 150).
-- Migração: ADR-002 (expand-contract). PE DEPENDENTES → tabela filha normalizada.

CREATE SCHEMA IF NOT EXISTS beneficiary;

-- REQ-BEN-003/004: beneficiário com status e datas de cadastro/atualização.
CREATE TABLE beneficiary.beneficiary (
    id          UUID PRIMARY KEY,
    cpf         VARCHAR(11)  NOT NULL UNIQUE,       -- BR-019/BR-020 (validado na app)
    nis         VARCHAR(11),                        -- BR-047 (consulta por NIS)
    name        VARCHAR(120) NOT NULL,              -- BR-021/BR-051
    birth_date  DATE         NOT NULL,              -- BR-022
    sex         VARCHAR(1)   NOT NULL CHECK (sex IN ('M', 'F')),  -- BR-023
    status      VARCHAR(10)  NOT NULL CHECK (status IN ('ACTIVE','SUSPENDED','CANCELLED','INACTIVE','DISCHARGED')),  -- BR-048
    created_at  DATE         NOT NULL,
    updated_at  DATE         NOT NULL
);

CREATE INDEX idx_beneficiary_nis ON beneficiary.beneficiary (nis);

-- REQ-BEN-005: dependentes (PE DEPENDENTES, máx. 5) normalizados em tabela filha.
CREATE TABLE beneficiary.beneficiary_dependent (
    id              UUID PRIMARY KEY,
    beneficiary_id  UUID NOT NULL REFERENCES beneficiary.beneficiary (id) ON DELETE CASCADE,
    cpf             VARCHAR(11)  NOT NULL,
    name            VARCHAR(120) NOT NULL,
    parentesco      VARCHAR(10)  NOT NULL CHECK (parentesco IN ('FILHO','CONJUGE','IRMAO','OUTRO')),  -- BR-033
    CONSTRAINT uq_dependent_cpf_per_holder UNIQUE (beneficiary_id, cpf)  -- BR-034 (dedup por titular)
);

CREATE INDEX idx_dependent_beneficiary ON beneficiary.beneficiary_dependent (beneficiary_id);
