import { Bot, User } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { ChatMessage } from '../../types/chat';

interface MessageBubbleProps {
  message: ChatMessage;
}

export default function MessageBubble({ message }: MessageBubbleProps) {
  const isUser = message.role === 'USER';

  return (
    <div className={`flex gap-3 ${isUser ? 'flex-row-reverse' : ''}`}>
      <div className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 ${
        isUser
          ? 'bg-indigo-100 dark:bg-indigo-900/30'
          : 'bg-emerald-100 dark:bg-emerald-900/30'
      }`}>
        {isUser ? (
          <User size={16} className="text-indigo-600 dark:text-indigo-400" />
        ) : (
          <Bot size={16} className="text-emerald-600 dark:text-emerald-400" />
        )}
      </div>

      <div className={`max-w-[75%] rounded-2xl px-4 py-2.5 ${
        isUser
          ? 'bg-indigo-600 text-white'
          : 'bg-gray-100 dark:bg-slate-800 text-gray-900 dark:text-gray-100'
      }`}>
        {isUser ? (
          <p className="text-sm whitespace-pre-wrap">{message.content}</p>
        ) : (
          <div className="prose prose-sm dark:prose-invert max-w-none [&>*:first-child]:mt-0 [&>*:last-child]:mb-0">
            <ReactMarkdown remarkPlugins={[remarkGfm]}>
              {message.content}
            </ReactMarkdown>
          </div>
        )}
      </div>
    </div>
  );
}
