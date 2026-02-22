import apiClient from './client';
import { Conversation, CreateConversationRequest } from '../types/conversation';

export const conversationsApi = {
  list: () =>
    apiClient.get<Conversation[]>('/conversations').then((r) => r.data),

  get: (id: number) =>
    apiClient.get<Conversation>(`/conversations/${id}`).then((r) => r.data),

  create: (data: CreateConversationRequest) =>
    apiClient.post<Conversation>('/conversations', data).then((r) => r.data),

  update: (id: number, data: CreateConversationRequest) =>
    apiClient.put<Conversation>(`/conversations/${id}`, data).then((r) => r.data),

  delete: (id: number) =>
    apiClient.delete(`/conversations/${id}`),
};
