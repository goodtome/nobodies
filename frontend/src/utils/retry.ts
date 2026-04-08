interface RetryOptions {
  shouldContinue?: () => boolean;
}

export async function retryWithBackoff<T>(
  task: () => Promise<T>,
  retries = 3,
  baseDelayMs = 500,
  options?: RetryOptions,
): Promise<T> {
  let lastError: unknown;
  for (let attempt = 0; attempt < retries; attempt += 1) {
    if (options?.shouldContinue && !options.shouldContinue()) {
      throw lastError ?? new Error('retry aborted');
    }
    try {
      return await task();
    } catch (error) {
      lastError = error;
      if (attempt === retries - 1) {
        break;
      }
      if (options?.shouldContinue && !options.shouldContinue()) {
        break;
      }
      const delay = baseDelayMs * 2 ** attempt;
      const jitter = Math.floor(Math.random() * 300);
      await new Promise((resolve) => window.setTimeout(resolve, delay + jitter));
    }
  }
  throw lastError;
}
