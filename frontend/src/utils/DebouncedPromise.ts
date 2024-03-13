
export function debouncedPromise<T>(promise: Promise<T>, wait: number, debounce: number): Promise<T> {
  return new Promise<T>((res, rej) => {
    const startTime = Date.now()
    let timeout = undefined as undefined | number

    promise.then(value => {
      const elapsedTime = Date.now() - startTime;
      if (elapsedTime <= wait)
        return res(value)

      timeout = setTimeout(() => res(value), debounce)
    }).catch(err => {
      clearTimeout(timeout)
      rej(err)
    })
  })
}
