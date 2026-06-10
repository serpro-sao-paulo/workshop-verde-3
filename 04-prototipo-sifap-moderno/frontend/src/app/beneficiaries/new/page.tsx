import { BeneficiaryForm } from "./beneficiary-form";

export default function NewBeneficiaryPage() {
  return (
    <section className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Cadastrar Beneficiário</h1>
        <p className="mt-1 text-sm text-slate-600">
          CPF é validado pelo módulo 11; maiores de 75 anos entram como SUSPENDED.
        </p>
      </div>
      <BeneficiaryForm />
    </section>
  );
}
