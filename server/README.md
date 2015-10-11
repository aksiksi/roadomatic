## Server

**Note:** Run `npm install` first to fetch dependencies.

The server for the `roadomatic` system is built from two parts.

### frontend/admin.js

An Express app consisting of a static HTML page (`frontend/index.html`). The page displays a simple form used to submit new roads to the database via HTTP POST.

### server.js

Runs a UDP server that waits for socket connections from clients. When a request is received (lat, lng position), the server queries the MongoDB database to determine road information. It then sends a response over the wire to the client.

Request format:

```json
// Latitude, Longitude
{"lat": [float], "lng": [float]}
```

Response format:

```json
// Server online, Road found, Speed, (Road) Name
{"o": [0|1], "f": [0|1], "s": [int], "n": [string]}
```

The server and database options can be configured by modifying the constants at the top of the file.

* `DB_URL`: points to your MongoDB instance, string, **must change**
* `ROADS`: name of roads collection, string
* `SEGMENTS`: name of road segments collection, string
* `HOSTNAME`: server hostname, string
* `PORT`: server port, int
