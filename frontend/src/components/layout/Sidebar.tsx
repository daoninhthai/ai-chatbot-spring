import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Plus, MessageSquare, FileText, Trash2 } from 'lucide-react';
import { conversationsApi } from '../../api/conversations';
import { Conversation } from '../../types/conversation';

interface SidebarProps {
  onSelectConversation: (id: number) => void;
  activeConversationId?: number;
  refreshTrigger?: number;
}

export default function Sidebar({ onSelectConversation, activeConversationId, refreshTrigger }: SidebarProps) {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    loadConversations();
  }, [refreshTrigger]);

  const loadConversations = async () => {
    try {
      const data = await conversationsApi.list();
      setConversations(data);
    } catch (err) {
      console.error('Failed to load conversations:', err);
    }
  };

  const handleNewChat = async () => {
    try {
      const conv = await conversationsApi.create({
        title: 'New Chat',
      });
      setConversations((prev) => [conv, ...prev]);
      onSelectConversation(conv.id);
    } catch (err) {
      console.error('Failed to create conversation:', err);
    }
  };

  const handleDelete = async (e: React.MouseEvent, id: number) => {
    e.stopPropagation();
    try {
      await conversationsApi.delete(id);
      setConversations((prev) => prev.filter((c) => c.id !== id));
      if (activeConversationId === id) {
        onSelectConversation(0);
      }
    } catch (err) {
      console.error('Failed to delete conversation:', err);
    }
  };

  return (
    <aside className="w-64 border-r border-gray-200 dark:border-slate-700 bg-gray-50 dark:bg-slate-900 flex flex-col h-full shrink-0">
      <div className="p-3">
        <button
          onClick={handleNewChat}
          className="w-full flex items-center gap-2 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg transition text-sm font-medium"
        >
          <Plus size={18} />
          New Chat
        </button>
      </div>

      <nav className="flex-1 overflow-y-auto px-2 space-y-0.5">
        {conversations.map((conv) => (
          <button
            key={conv.id}
            onClick={() => onSelectConversation(conv.id)}
            className={`w-full flex items-center gap-2 px-3 py-2 rounded-lg text-left text-sm transition group ${
              activeConversationId === conv.id
                ? 'bg-indigo-50 dark:bg-indigo-900/20 text-indigo-700 dark:text-indigo-300'
                : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-slate-800'
            }`}
          >
            <MessageSquare size={16} className="shrink-0 opacity-60" />
            <span className="truncate flex-1">{conv.title}</span>
            <button
              onClick={(e) => handleDelete(e, conv.id)}
              className="opacity-0 group-hover:opacity-100 p-1 hover:bg-red-100 dark:hover:bg-red-900/30 rounded text-gray-400 hover:text-red-500 transition"
            >
              <Trash2 size={14} />
            </button>
          </button>
        ))}

        {conversations.length === 0 && (
          <p className="text-center text-sm text-gray-400 dark:text-gray-500 mt-8 px-4">
            No conversations yet. Start a new chat!
          </p>
        )}
      </nav>

      <div className="p-3 border-t border-gray-200 dark:border-slate-700">
        <button
          onClick={() => navigate('/documents')}
          className={`w-full flex items-center gap-2 px-3 py-2 rounded-lg text-sm transition ${
            location.pathname === '/documents'
              ? 'bg-indigo-50 dark:bg-indigo-900/20 text-indigo-700 dark:text-indigo-300'
              : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-slate-800'
          }`}
        >
          <FileText size={16} />
          Knowledge Base
        </button>
      </div>
    </aside>
  );
}
