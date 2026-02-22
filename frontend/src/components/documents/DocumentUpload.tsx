import { useState, useRef } from 'react';
import { Upload, File, X } from 'lucide-react';
import { documentsApi } from '../../api/documents';

interface DocumentUploadProps {
  onUploadComplete: () => void;
}

export default function DocumentUpload({ onUploadComplete }: DocumentUploadProps) {
  const [dragActive, setDragActive] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);

  const handleFiles = async (files: FileList | null) => {
    if (!files || files.length === 0) return;
    setError('');
    setUploading(true);

    try {
      for (const file of Array.from(files)) {
        await documentsApi.upload(file);
      }
      onUploadComplete();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div>
      <div
        onDragOver={(e) => { e.preventDefault(); setDragActive(true); }}
        onDragLeave={() => setDragActive(false)}
        onDrop={(e) => { e.preventDefault(); setDragActive(false); handleFiles(e.dataTransfer.files); }}
        onClick={() => inputRef.current?.click()}
        className={`border-2 border-dashed rounded-xl p-8 text-center cursor-pointer transition ${
          dragActive
            ? 'border-indigo-500 bg-indigo-50 dark:bg-indigo-900/10'
            : 'border-gray-300 dark:border-slate-600 hover:border-indigo-400 dark:hover:border-indigo-500'
        }`}
      >
        <input
          ref={inputRef}
          type="file"
          className="hidden"
          accept=".pdf,.docx,.txt,.md,.csv"
          multiple
          onChange={(e) => handleFiles(e.target.files)}
        />
        <Upload className="w-10 h-10 mx-auto text-gray-400 dark:text-gray-500 mb-3" />
        <p className="text-sm text-gray-600 dark:text-gray-300 font-medium">
          {uploading ? 'Uploading...' : 'Drop files here or click to upload'}
        </p>
        <p className="text-xs text-gray-400 dark:text-gray-500 mt-1">
          PDF, DOCX, TXT, MD, CSV (max 20MB)
        </p>
      </div>

      {error && (
        <div className="mt-3 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 px-4 py-2 rounded-lg text-sm flex items-center gap-2">
          <X size={16} />
          {error}
        </div>
      )}
    </div>
  );
}
