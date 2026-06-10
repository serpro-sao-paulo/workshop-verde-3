import type { ApiError, Beneficiary } from "@/lib/types";

/**
 * URL interna do backend. No Docker Compose, o frontend (server-side) alcança o
 * backend pelo nome do serviço; localmente, por localhost.
 */
const BASE_URL = process.env.BACKEND_INTERNAL_URL ?? "http://localhost:8080";

export interface SearchResult {
  ok: boolean;
  beneficiary?: Beneficiary;
  message?: string;
}

/** Consulta um beneficiário por CPF ou NIS (REQ-BEN-006). */
export async function searchBeneficiary(params: {
  cpf?: string;
  nis?: string;
}): Promise<SearchResult> {
  const query = new URLSearchParams();
  if (params.cpf) {
    query.set("cpf", params.cpf);
  }
  if (params.nis) {
    query.set("nis", params.nis);
  }

  let response: Response;
  try {
    response = await fetch(`${BASE_URL}/api/v1/beneficiaries?${query.toString()}`, {
      cache: "no-store",
    });
  } catch {
    return { ok: false, message: "Não foi possível conectar ao backend." };
  }

  if (response.status === 404) {
    return { ok: false, message: "Beneficiário não encontrado." };
  }
  if (!response.ok) {
    const error = (await response.json().catch(() => null)) as ApiError | null;
    return { ok: false, message: error?.message ?? "Erro na consulta." };
  }

  const beneficiary = (await response.json()) as Beneficiary;
  return { ok: true, beneficiary };
}

export interface CreatePayload {
  cpf: string;
  nis: string | null;
  name: string;
  birthDate: string;
  sex: string;
}

export interface CreateResult {
  ok: boolean;
  beneficiary?: Beneficiary;
  message?: string;
}

/** Cadastra um beneficiário (REQ-BEN-001..004). */
export async function createBeneficiary(payload: CreatePayload): Promise<CreateResult> {
  let response: Response;
  try {
    response = await fetch(`${BASE_URL}/api/v1/beneficiaries`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
      cache: "no-store",
    });
  } catch {
    return { ok: false, message: "Não foi possível conectar ao backend." };
  }

  if (response.status === 201) {
    const beneficiary = (await response.json()) as Beneficiary;
    return { ok: true, beneficiary };
  }

  const error = (await response.json().catch(() => null)) as ApiError | null;
  return { ok: false, message: error?.message ?? "Erro ao cadastrar." };
}
