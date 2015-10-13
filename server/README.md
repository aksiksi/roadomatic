## Server

**Note:** Run `npm install` first to fetch dependencies.

#### Configuration

The server, database, and frontend options can be configured by modifying the contents of `config.js`.

#### Protocol Specification

Request format:

```json
// Latitude (float), Longitude (float)
{"lat": 24.211, "lng": 55.443}
```

Response format:

```json
// Server online, Road found, Speed, (Road) Name
{"o": [0,1], "f": [0,1], "s": 80, "n": "James Road"}
```

### Server Structure

The server for the `roadomatic` system is built from two parts.

#### frontend/admin.js

An Express app consisting of a static HTML page (`frontend/index.html`). The page displays a simple form used to submit new roads to the database via HTTP POST.

#### server.js

Runs a UDP server that waits for socket connections from clients. When a request is received (lat, lng position), the server queries the MongoDB database to determine road information. It then sends a response over the wire to the client.
