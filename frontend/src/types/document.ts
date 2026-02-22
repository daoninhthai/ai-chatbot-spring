export interface Document {
  id: number;
  fileName: string;
  contentType: string;
  fileSize: number;
  chunkCount?: number;
  status: 'PROCESSING' | 'INDEXED' | 'FAILED';
  errorMessage?: string;
  createdAt: string;
}
