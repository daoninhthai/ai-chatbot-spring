import { Bot } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

interface StreamingMessageProps {
  content: string;
  isStreaming: boolean;
}

export default function StreamingMessage({ content, isStreaming }: StreamingMessageProps) {
  return (
    <div className="flex gap-3">
      <div className="w-8 h-8 rounded-full flex items-center justify-center shrink-0 bg-emerald-100 dark:bg-emerald-900/30">
        <Bot size={16} className="text-emerald-600 dark:text-emerald-400" />
      </div>
      <div className="max-w-[75%] rounded-2xl px-4 py-2.5 bg-gray-100 dark:bg-slate-800 text-gray-900 dark:text-gray-100">
        <div className="prose prose-sm dark:prose-invert max-w-none [&>*:first-child]:mt-0 [&>*:last-child]:mb-0">
          <ReactMarkdown remarkPlugins={[remarkGfm]}>
            {content}
          </ReactMarkdown>
          {isStreaming && (
            <span className="inline-block w-2 h-5 bg-indigo-500 animate-pulse rounded-sm ml-0.5 align-text-bottom" />
          )}
        </div>
      </div>
    </div>
  );
}
