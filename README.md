# Keycloak HTTP Forwarder

This is an event listener that forwards events to any http endpoint.

## Configuration

The configuration is done via system properties:

- `events.http.timeout`: defines the timeout in seconds for the http requests (defaults to `2`)
- `events.http.url`: defines the endpoint url for sending the events (defaults to `http://localhost:8000`)
- `events.http.header.NAME`: sets a header `NAME` for each HTTP request