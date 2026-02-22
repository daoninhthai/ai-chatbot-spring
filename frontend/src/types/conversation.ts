import { ChatMessage } from './chat';

export interface Conversation {
  id: number;
  title: string;
  systemPrompt?: string;
  ragEnabled: boolean;
  messageCount: number;
  createdAt: string;
  updatedAt: string;
  messages?: ChatMessage[];
}

export interface CreateConversationRequest {
  title: string;
  systemPrompt?: string;
  ragEnabled?: boolean;
}
