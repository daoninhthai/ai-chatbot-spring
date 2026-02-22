import { useState, useEffect } from 'react';
import { ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import AppLayout from '../components/layout/AppLayout';
import DocumentUpload from '../components/documents/DocumentUpload';
import DocumentList from '../components/documents/DocumentList';
import { documentsApi } from '../api/documents';
import { Document } from '../types/document';

export default function DocumentsPage() {
  const [documents, setDocuments] = useState<Document[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    loadDocuments();
  }, []);

  const loadDocuments = async () => {
    try {
      const data = await documentsApi.list();
      setDocuments(data);
    } catch (err) {
      console.error('Failed to load documents:', err);
    }
  };

  return (
    <AppLayout>
      <div className="flex-1 overflow-y-auto">
        <div className="max-w-3xl mx-auto px-6 py-8">
          <div className="flex items-center gap-3 mb-6">
            <button
              onClick={() => navigate('/')}
              className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-slate-800 text-gray-500 dark:text-gray-400 transition"
            >
              <ArrowLeft size={20} />
            </button>
            <div>
              <h1 className="text-xl font-bold text-gray-900 dark:text-white">Knowledge Base</h1>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Upload documents to enhance AI responses with your data
              </p>
            </div>
          </div>

          <div className="space-y-6">
            <DocumentUpload onUploadComplete={loadDocuments} />
            <DocumentList documents={documents} onDelete={loadDocuments} />
          </div>
        </div>
      </div>
    </AppLayout>
  );
}
