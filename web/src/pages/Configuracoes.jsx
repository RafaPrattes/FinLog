import { PageHeader } from '../components/PageHeader'

/* Não há tela de Configurações no protótipo: mostra os dados da conta.
   A saída da conta fica no cartão da pessoa, no menu lateral. */
function Configuracoes({ user }) {
  return (
    <>
      <PageHeader title="Configurações" subtitle="Consulte os dados da sua conta." />

      <section className="card settings-card" aria-labelledby="conta-title">
        <h2 className="card-title card-head" id="conta-title">Dados da conta</h2>
        <div className="m-grid">
          <div className="m-field">
            <label className="m-lbl" htmlFor="conf-nome">Nome</label>
            <input id="conf-nome" className="m-inp" type="text" value={user?.nome || ''} readOnly />
          </div>
          <div className="m-field">
            <label className="m-lbl" htmlFor="conf-email">E-mail</label>
            <input id="conf-email" className="m-inp" type="email" value={user?.email || ''} readOnly />
          </div>
        </div>
      </section>
    </>
  )
}

export default Configuracoes
