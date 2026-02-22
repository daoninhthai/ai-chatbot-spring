import { useState } from 'react';
import AppLayout from '../components/layout/AppLayout';
import Sidebar from '../components/layout/Sidebar';
import ChatWindow from '../components/chat/ChatWindow';

export default function ChatPage() {
  const [activeConversationId, setActiveConversationId] = useState<number>(0);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const handleMessageSent = () => {
    setRefreshTrigger((t) => t + 1);
  };

  return (
    <AppLayout>
      <Sidebar
        onSelectConversation={setActiveConversationId}
        activeConversationId={activeConversationId}
        refreshTrigger={refreshTrigger}
      />
      <ChatWindow
        conversationId={activeConversationId}
        onMessageSent={handleMessageSent}
      />
    </AppLayout>
  );
}
