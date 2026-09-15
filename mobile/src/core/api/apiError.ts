import axios from 'axios';

export interface ApiFieldError {
  field: string;
  message: string;
}

interface ApiErrorPayload {
  code?: string;
  message?: string;
  requestId?: string;
  fieldErrors?: ApiFieldError[];
}

export class ApiError extends Error {
  constructor(
    public readonly status: number | null,
    public readonly code: string,
    message: string,
    public readonly requestId: string | null = null,
    public readonly fieldErrors: ApiFieldError[] = [],
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export function toApiError(error: unknown): ApiError {
  if (error instanceof ApiError) {
    return error;
  }

  if (axios.isAxiosError<ApiErrorPayload>(error)) {
    const payload = error.response?.data;
    return new ApiError(
      error.response?.status ?? null,
      payload?.code ?? (error.response ? 'API_ERROR' : 'NETWORK_ERROR'),
      payload?.message ?? (error.response ? 'Request failed' : 'Unable to reach the server'),
      payload?.requestId ?? null,
      payload?.fieldErrors ?? [],
    );
  }

  return new ApiError(null, 'UNKNOWN_ERROR', 'Unexpected error');
}
