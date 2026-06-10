"use client";

import { useActionState } from "react";
import { createBeneficiaryAction, type CreateState } from "./actions";

const initialState: CreateState = { status: "idle", message: "" };

const inputClass =
  "mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none";

export function BeneficiaryForm() {
  const [state, formAction, pending] = useActionState(createBeneficiaryAction, initialState);

  return (
    <form action={formAction} className="space-y-4 rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
      <div>
        <label htmlFor="cpf" className="text-sm font-medium text-slate-700">
          CPF *
        </label>
        <input id="cpf" name="cpf" required placeholder="000.000.000-00" className={inputClass} />
      </div>

      <div>
        <label htmlFor="name" className="text-sm font-medium text-slate-700">
          Nome completo *
        </label>
        <input id="name" name="name" required placeholder="Nome e sobrenome" className={inputClass} />
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <div>
          <label htmlFor="birthDate" className="text-sm font-medium text-slate-700">
            Data de nascimento *
          </label>
          <input id="birthDate" name="birthDate" type="date" required className={inputClass} />
        </div>
        <div>
          <label htmlFor="sex" className="text-sm font-medium text-slate-700">
            Sexo *
          </label>
          <select id="sex" name="sex" required defaultValue="" className={inputClass}>
            <option value="" disabled>
              Selecione
            </option>
            <option value="F">Feminino</option>
            <option value="M">Masculino</option>
          </select>
        </div>
      </div>

      <div>
        <label htmlFor="nis" className="text-sm font-medium text-slate-700">
          NIS (opcional)
        </label>
        <input id="nis" name="nis" placeholder="00000000000" className={inputClass} />
      </div>

      <button
        type="submit"
        disabled={pending}
        className="rounded-md bg-emerald-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-emerald-700 disabled:opacity-50"
      >
        {pending ? "Cadastrando..." : "Cadastrar"}
      </button>

      {state.status === "success" && (
        <div className="rounded-md border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-800">
          {state.message}
          {state.beneficiary && (
            <span className="block">
              CPF: {state.beneficiary.cpfMasked} · Status: {state.beneficiary.status}
            </span>
          )}
        </div>
      )}

      {state.status === "error" && (
        <div className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-800">
          {state.message}
        </div>
      )}
    </form>
  );
}
