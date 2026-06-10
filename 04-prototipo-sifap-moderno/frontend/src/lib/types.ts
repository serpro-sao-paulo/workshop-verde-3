/** Status do beneficiário (espelha o backend / BR-048). */
export type BeneficiaryStatus =
  | "ACTIVE"
  | "SUSPENDED"
  | "CANCELLED"
  | "INACTIVE"
  | "DISCHARGED";

/** Resposta do backend com CPF mascarado (REQ-BEN-006). */
export interface Beneficiary {
  id: string;
  cpfMasked: string;
  nis: string | null;
  name: string;
  birthDate: string;
  sex: string;
  status: BeneficiaryStatus;
}

/** Erro padronizado retornado pelo backend. */
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
}
