import Link from "next/link";
import { searchBeneficiary } from "@/lib/api";
import type { Beneficiary } from "@/lib/types";

const statusLabels: Record<Beneficiary["status"], string> = {
  ACTIVE: "Ativo",
  SUSPENDED: "Suspenso",
  CANCELLED: "Cancelado",
  INACTIVE: "Inativo",
  DISCHARGED: "Desligado",
};

const inputClass =
  "w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none";

interface PageProps {
  searchParams: Promise<{ cpf?: string; nis?: string }>;
}

export default async function BeneficiariesPage({ searchParams }: PageProps) {
  const params = await searchParams;
  const cpf = params.cpf?.trim();
  const nis = params.nis?.trim();
  const hasQuery = Boolean(cpf || nis);

  const result = hasQuery ? await searchBeneficiary({ cpf, nis }) : null;

  return (
    <section className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Consultar Beneficiário</h1>
        <p className="mt-1 text-sm text-slate-600">Busca por CPF ou NIS (REQ-BEN-006).</p>
      </div>

      <form method="GET" className="flex flex-wrap items-end gap-3 rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <div className="flex-1">
          <label htmlFor="cpf" className="text-sm font-medium text-slate-700">
            CPF
          </label>
          <input id="cpf" name="cpf" defaultValue={cpf ?? ""} placeholder="000.000.000-00" className={inputClass} />
        </div>
        <div className="flex-1">
          <label htmlFor="nis" className="text-sm font-medium text-slate-700">
            NIS
          </label>
          <input id="nis" name="nis" defaultValue={nis ?? ""} placeholder="00000000000" className={inputClass} />
        </div>
        <button
          type="submit"
          className="rounded-md bg-emerald-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-emerald-700"
        >
          Buscar
        </button>
      </form>

      {result && result.ok && result.beneficiary && (
        <article className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <h2 className="text-lg font-semibold text-slate-900">{result.beneficiary.name}</h2>
          <dl className="mt-3 grid gap-2 text-sm sm:grid-cols-2">
            <div>
              <dt className="text-slate-500">CPF</dt>
              <dd className="font-medium">{result.beneficiary.cpfMasked}</dd>
            </div>
            <div>
              <dt className="text-slate-500">NIS</dt>
              <dd className="font-medium">{result.beneficiary.nis ?? "—"}</dd>
            </div>
            <div>
              <dt className="text-slate-500">Nascimento</dt>
              <dd className="font-medium">{result.beneficiary.birthDate}</dd>
            </div>
            <div>
              <dt className="text-slate-500">Status</dt>
              <dd className="font-medium">{statusLabels[result.beneficiary.status]}</dd>
            </div>
          </dl>
        </article>
      )}

      {result && !result.ok && (
        <div className="rounded-md border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800">
          {result.message}{" "}
          <Link href="/beneficiaries/new" className="font-medium underline">
            Cadastrar novo
          </Link>
        </div>
      )}
    </section>
  );
}
