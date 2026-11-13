export default defineEventHandler(async event => {
  const operation = getRouterParam(event, 'operation')
  const response = await event.context.forwardAuth(operation, {
    headers: {
      cookie: getHeader(event, 'cookie') || '',
      'X-XSRF-TOKEN': getHeader(event, 'X-XSRF-TOKEN') || '',
      'Content-Type': getHeader(event, 'content-type') || 'application/json',
      Accept: getHeader(event, 'accept') || '*/*'
    },
    body: operation === 'login' ? await readRawBody(event) : undefined
  })
  return sendWebResponse(event, response)
})
