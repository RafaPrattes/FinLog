import Icon from './Icon'
import { iniciais } from '../utils/format'

const NAV_ITEMS = [
  { view: 'home',    label: 'Página Inicial',    icon: 'home' },
  { view: 'extrato', label: 'Extrato Detalhado', icon: 'file-text' },
  { view: 'metas',   label: 'Metas',             icon: 'target' },
  { view: 'chat',    label: 'IA Conselheira',    icon: 'message-circle' },
  { view: 'config',  label: 'Configurações',     icon: 'settings' },
]

function Sidebar({ view, onNavigate, user, onUserClick }) {
  return (
    <aside className="sidebar">
      <div className="sidebar-top">
        <div className="sidebar-logo">FinLog</div>
        <nav className="sidebar-nav" aria-label="Menu principal">
          {NAV_ITEMS.map(item => (
            <button
              key={item.view}
              type="button"
              className={`nav-item${view === item.view ? ' active' : ''}`}
              aria-current={view === item.view ? 'page' : undefined}
              onClick={() => onNavigate(item.view)}
            >
              <Icon name={item.icon} size={24} />
              {item.label}
            </button>
          ))}
        </nav>
      </div>

      <button type="button" className="sidebar-user" onClick={onUserClick} title="Sair da conta" aria-label={`${user?.nome || 'Usuário'} — sair da conta`}>
        <span className="avatar" aria-hidden="true">{iniciais(user?.nome)}</span>
        <span className="user-meta">
          <span className="user-name">{user?.nome || 'Usuário'}</span>
          <span className="user-sub">{user?.email || 'Clique para sair'}</span>
        </span>
      </button>
    </aside>
  )
}

export default Sidebar
