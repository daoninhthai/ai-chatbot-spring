export interface ChatMessage {
  id: number;
  role: 'USER' | 'ASSISTANT' | 'SYSTEM';
  content: string;
  model?: string;
  tokenCount?: number;
  createdAt: string;
}

export interface ChatMessageRequest {
  content: string;
  useRag?: boolean;
}
