"use server";

import { createBeneficiary } from "@/lib/api";
import type { Beneficiary } from "@/lib/types";

export interface CreateState {
  status: "idle" | "success" | "error";
  message: string;
  beneficiary?: Beneficiary;
}

/** Server action de cadastro de beneficiário (REQ-BEN-001..004). */
export async function createBeneficiaryAction(
  _prev: CreateState,
  formData: FormData,
): Promise<CreateState> {
  const cpf = String(formData.get("cpf") ?? "").trim();
  const nisRaw = String(formData.get("nis") ?? "").trim();
  const name = String(formData.get("name") ?? "").trim();
  const birthDate = String(formData.get("birthDate") ?? "").trim();
  const sex = String(formData.get("sex") ?? "").trim();

  if (!cpf || !name || !birthDate || !sex) {
    return { status: "error", message: "Preencha todos os campos obrigatórios." };
  }

  const result = await createBeneficiary({
    cpf,
    nis: nisRaw === "" ? null : nisRaw,
    name,
    birthDate,
    sex,
  });

  if (result.ok && result.beneficiary) {
    return {
      status: "success",
      message: `Beneficiário cadastrado com status ${result.beneficiary.status}.`,
      beneficiary: result.beneficiary,
    };
  }

  return { status: "error", message: result.message ?? "Erro ao cadastrar." };
}
