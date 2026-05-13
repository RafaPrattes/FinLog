function Dashboard() {
  return (
    <div
      id="screen-dashboard"
      className="screen active"
    >
      <aside className="sidebar">
        <div className="sidebar-logo">
          FinLog
        </div>

        <div className="sidebar-user">
          <div className="avatar">
            SG
          </div>

          <div>
            <div className="user-name">
              Serena Gilmore
            </div>

            <div className="logout-label">
              Clique para sair
            </div>
          </div>
        </div>
      </aside>

      <main className="main">
        <h2 className="page-title">
          Página Inicial
        </h2>

        <div className="row row-top">

          <div className="saldo-card">
            <div className="saldo-arrow">
              ↗
            </div>

            <div className="saldo-lbl">
              Seu Saldo
            </div>

            <div className="saldo-val">
              R$ 0,00
            </div>

            <div className="saldo-btns">
              <button className="saldo-btn">
                Entrada +
              </button>

              <button className="saldo-btn">
                Saída –
              </button>
            </div>
          </div>

          <div className="summary-card">
            <div className="summary-nums">
              <div className="s-row">

                <div className="s-item">
                  <label>
                    Entradas
                  </label>

                  <div className="amt">
                    R$ 0,00
                  </div>
                </div>

                <div className="s-item">
                  <label>
                    Saídas
                  </label>

                  <div className="amt">
                    R$ 0,00
                  </div>
                </div>

              </div>
            </div>
          </div>

        </div>

<div className="row row-bottom">

  <div className="card">

    <div className="card-head">

      <div className="card-head-title">
        Metas
      </div>

      <button className="icon-btn">
        +
      </button>

    </div>

    <div className="metas-scroll">

      <div className="meta-card">

        <div className="meta-d">
          Meta de emergência
        </div>

        <div className="meta-v">
          R$ 5.000
        </div>

      </div>

      <div className="meta-card">

        <div className="meta-d">
          Viagem
        </div>

        <div className="meta-v">
          R$ 2.000
        </div>

      </div>

    </div>

  </div>

  <div className="card">

    <div className="card-head">

      <div className="card-head-title">
        Extrato
      </div>

    </div>

    <div className="extrato-scroll">

      <div className="txn-row">

        <div className="txn-left">

          <div className="dot entrada"></div>

          <div>
            <div className="txn-name">
              Salário
            </div>

            <div className="txn-sub">
              Entrada
            </div>
          </div>

        </div>

        <div className="txn-right">

          <div className="txn-val">
            + R$ 3.000
          </div>

        </div>

      </div>

      <div className="txn-row">

        <div className="txn-left">

          <div className="dot saida"></div>

          <div>
            <div className="txn-name">
              Mercado
            </div>

            <div className="txn-sub">
              Saída
            </div>
          </div>

        </div>

        <div className="txn-right">

          <div className="txn-val">
            - R$ 250
          </div>

        </div>

      </div>

    </div>

  </div>

</div>

</main>
    </div>
  )
}

export default Dashboard