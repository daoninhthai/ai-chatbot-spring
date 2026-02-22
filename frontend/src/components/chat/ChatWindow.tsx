import { useState, useEffect, useRef } from 'react';
import { Bot, MessageSquarePlus } from 'lucide-react';
import { chatApi } from '../../api/chat';
import { ChatMessage } from '../../types/chat';
import MessageBubble from './MessageBubble';
import StreamingMessage from './StreamingMessage';
import TypingIndicator from './TypingIndicator';
import MessageInput from './MessageInput';

interface ChatWindowProps {
  conversationId: number;
  onMessageSent?: () => void;
}

export default function ChatWindow({ conversationId, onMessageSent }: ChatWindowProps) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [streamingContent, setStreamingContent] = useState('');
  const [isStreaming, setIsStreaming] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (conversationId) {
      loadMessages();
    }
  }, [conversationId]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, streamingContent]);

  const loadMessages = async () => {
    try {
      const data = await chatApi.getMessages(conversationId);
      setMessages(data);
    } catch (err) {
      console.error('Failed to load messages:', err);
    }
  };

  const handleSend = async (content: string) => {
    // Add user message optimistically
    const userMsg: ChatMessage = {
      id: Date.now(),
      role: 'USER',
      content,
      createdAt: new Date().toISOString(),
    };
    setMessages((prev) => [...prev, userMsg]);
    setIsStreaming(true);
    setStreamingContent('');

    try {
      const { fetchPromise } = chatApi.streamMessage(conversationId, content);
      const response = await fetchPromise;

      if (!response.ok) {
        throw new Error('Stream request failed');
      }

      const reader = response.body?.getReader();
      const decoder = new TextDecoder();
      let fullContent = '';

      if (reader) {
        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          const text = decoder.decode(value, { stream: true });
          // Parse SSE data lines
          const lines = text.split('\n');
          for (const line of lines) {
            if (line.startsWith('data:')) {
              const data = line.slice(5);
              if (data.trim() === '[DONE]') continue;
              fullContent += data;
              setStreamingContent(fullContent);
            } else if (line.trim() && !line.startsWith(':')) {
              // Raw text (non-SSE format)
              fullContent += line;
              setStreamingContent(fullContent);
            }
          }
        }
      }

      // Add the complete assistant message
      const assistantMsg: ChatMessage = {
        id: Date.now() + 1,
        role: 'ASSISTANT',
        content: fullContent,
        model: 'gpt-4o-mini',
        createdAt: new Date().toISOString(),
      };
      setMessages((prev) => [...prev, assistantMsg]);
      onMessageSent?.();
    } catch (err) {
      console.error('Streaming error:', err);
      const errorMsg: ChatMessage = {
        id: Date.now() + 1,
        role: 'ASSISTANT',
        content: 'Sorry, something went wrong. Please try again.',
        createdAt: new Date().toISOString(),
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setIsStreaming(false);
      setStreamingContent('');
    }
  };

  if (!conversationId) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center text-gray-400 dark:text-gray-500">
        <MessageSquarePlus size={48} className="mb-4 opacity-40" />
        <p className="text-lg">Select a conversation or start a new chat</p>
      </div>
    );
  }

  return (
    <div className="flex-1 flex flex-col min-w-0">
      <div className="flex-1 overflow-y-auto px-4 py-6 space-y-4">
        {messages.length === 0 && !isStreaming && (
          <div className="flex flex-col items-center justify-center h-full text-gray-400 dark:text-gray-500">
            <Bot size={40} className="mb-3 opacity-40" />
            <p>Send a message to start the conversation</p>
          </div>
        )}

        {messages.map((msg) => (
          <MessageBubble key={msg.id} message={msg} />
        ))}

        {isStreaming && streamingContent && (
          <StreamingMessage content={streamingContent} isStreaming={true} />
        )}

        {isStreaming && !streamingContent && <TypingIndicator />}

        <div ref={messagesEndRef} />
      </div>

      <MessageInput onSend={handleSend} disabled={isStreaming} />
    </div>
  );
}
