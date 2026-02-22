import apiClient from './client';
import { Document } from '../types/document';

export const documentsApi = {
  upload: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return apiClient.post<Document>('/documents/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then((r) => r.data);
  },

  list: () =>
    apiClient.get<Document[]>('/documents').then((r) => r.data),

  get: (id: number) =>
    apiClient.get<Document>(`/documents/${id}`).then((r) => r.data),

  delete: (id: number) =>
    apiClient.delete(`/documents/${id}`),
};
