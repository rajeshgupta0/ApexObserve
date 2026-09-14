import { useState, useEffect, useCallback, useRef } from "react";

export function usePolling<T>(
  fetchFn: () => Promise<T>,
  intervalMs = 5000,
  enabled = true
) {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<Error | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);
  
  const savedFetchFn = useRef(fetchFn);

  useEffect(() => {
    savedFetchFn.current = fetchFn;
  }, [fetchFn]);

  const execute = useCallback(async () => {
    try {
      const result = await savedFetchFn.current();
      setData(result);
      setError(null);
      setLastUpdated(new Date());
    } catch (e: any) {
      setError(e instanceof Error ? e : new Error(e?.message || "Unknown error"));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!enabled) return;

    // Initial fetch
    execute();

    // Setup polling
    const intervalId = setInterval(execute, intervalMs);

    return () => clearInterval(intervalId);
  }, [execute, intervalMs, enabled]);

  return { data, loading, error, lastUpdated, refetch: execute };
}
