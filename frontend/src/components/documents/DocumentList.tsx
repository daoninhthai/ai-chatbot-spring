import { FileText, Trash2, CheckCircle, AlertCircle, Loader } from 'lucide-react';
import { Document } from '../../types/document';
import { documentsApi } from '../../api/documents';

interface DocumentListProps {
  documents: Document[];
  onDelete: () => void;
}

export default function DocumentList({ documents, onDelete }: DocumentListProps) {
  const handleDelete = async (id: number) => {
    try {
      await documentsApi.delete(id);
      onDelete();
    } catch (err) {
      console.error('Failed to delete document:', err);
    }
  };

  const formatSize = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  const statusIcon = (status: Document['status']) => {
    switch (status) {
      case 'INDEXED':
        return <CheckCircle size={16} className="text-emerald-500" />;
      case 'PROCESSING':
        return <Loader size={16} className="text-amber-500 animate-spin" />;
      case 'FAILED':
        return <AlertCircle size={16} className="text-red-500" />;
    }
  };

  if (documents.length === 0) {
    return (
      <div className="text-center py-12 text-gray-400 dark:text-gray-500">
        <FileText size={40} className="mx-auto mb-3 opacity-40" />
        <p>No documents uploaded yet</p>
        <p className="text-sm mt-1">Upload documents to enhance AI responses with your data</p>
      </div>
    );
  }

  return (
    <div className="space-y-2">
      {documents.map((doc) => (
        <div
          key={doc.id}
          className="flex items-center gap-3 p-3 rounded-lg border border-gray-200 dark:border-slate-700 bg-white dark:bg-slate-800 group"
        >
          <FileText size={20} className="text-gray-400 shrink-0" />
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-gray-900 dark:text-white truncate">
              {doc.fileName}
            </p>
            <div className="flex items-center gap-2 text-xs text-gray-400 dark:text-gray-500">
              <span>{formatSize(doc.fileSize)}</span>
              {doc.chunkCount && <span>{doc.chunkCount} chunks</span>}
              <span className="flex items-center gap-1">
                {statusIcon(doc.status)} {doc.status.toLowerCase()}
              </span>
            </div>
            {doc.errorMessage && (
              <p className="text-xs text-red-500 mt-1">{doc.errorMessage}</p>
            )}
          </div>
          <button
            onClick={() => handleDelete(doc.id)}
            className="opacity-0 group-hover:opacity-100 p-1.5 hover:bg-red-100 dark:hover:bg-red-900/30 rounded-lg text-gray-400 hover:text-red-500 transition"
          >
            <Trash2 size={16} />
          </button>
        </div>
      ))}
    </div>
  );
}
