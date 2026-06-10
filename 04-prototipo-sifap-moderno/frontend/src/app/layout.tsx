import type { Metadata } from "next";
import Link from "next/link";
import "./globals.css";

export const metadata: Metadata = {
  title: "SIFAP 2.0 — Beneficiários",
  description: "Cadastro e consulta de beneficiários",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="pt-BR">
      <body>
        <header className="border-b border-slate-200 bg-white">
          <nav className="mx-auto flex max-w-4xl items-center gap-6 px-6 py-4">
            <Link href="/" className="text-lg font-semibold text-slate-900">
              SIFAP <span className="text-emerald-600">2.0</span>
            </Link>
            <Link href="/beneficiaries" className="text-sm text-slate-600 hover:text-slate-900">
              Consultar
            </Link>
            <Link href="/beneficiaries/new" className="text-sm text-slate-600 hover:text-slate-900">
              Cadastrar
            </Link>
          </nav>
        </header>
        <main className="mx-auto max-w-4xl px-6 py-8">{children}</main>
      </body>
    </html>
  );
}
