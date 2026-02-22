import apiClient from './client';
import { ChatMessage, ChatMessageRequest } from '../types/chat';

export const chatApi = {
  sendMessage: (conversationId: number, data: ChatMessageRequest) =>
    apiClient.post<ChatMessage>(`/chat/${conversationId}`, data).then((r) => r.data),

  getMessages: (conversationId: number) =>
    apiClient.get<ChatMessage[]>(`/chat/${conversationId}/messages`).then((r) => r.data),

  /**
   * Stream a message response via SSE.
   * Returns an object with a ReadableStream and an abort controller.
   */
  streamMessage: (conversationId: number, message: string) => {
    const token = localStorage.getItem('accessToken');
    const abortController = new AbortController();

    const fetchPromise = fetch(
      `/api/chat/${conversationId}/stream?message=${encodeURIComponent(message)}&token=${token}`,
      {
        headers: { Accept: 'text/event-stream' },
        signal: abortController.signal,
      }
    );

    return { fetchPromise, abort: () => abortController.abort() };
  },
};
