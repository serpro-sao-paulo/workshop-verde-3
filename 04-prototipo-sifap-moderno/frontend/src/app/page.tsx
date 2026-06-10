import Link from "next/link";

export default function HomePage() {
  return (
    <section className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Cadastro de Beneficiários</h1>
        <p className="mt-2 text-slate-600">
          Módulo de beneficiários do SIFAP 2.0 — inclusão e consulta com as regras
          preservadas do sistema legado.
        </p>
      </div>
      <div className="grid gap-4 sm:grid-cols-2">
        <Link
          href="/beneficiaries/new"
          className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm transition hover:border-emerald-400"
        >
          <h2 className="text-lg font-semibold text-slate-900">Cadastrar</h2>
          <p className="mt-1 text-sm text-slate-600">
            Incluir um novo beneficiário (REQ-BEN-001 a 004).
          </p>
        </Link>
        <Link
          href="/beneficiaries"
          className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm transition hover:border-emerald-400"
        >
          <h2 className="text-lg font-semibold text-slate-900">Consultar</h2>
          <p className="mt-1 text-sm text-slate-600">
            Buscar por CPF ou NIS, com CPF mascarado (REQ-BEN-006).
          </p>
        </Link>
      </div>
    </section>
  );
}
