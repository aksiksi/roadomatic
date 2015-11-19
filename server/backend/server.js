// Imports
const dgram = require('dgram');
const util = require('util');
const MongoClient = require('mongodb').MongoClient;
const denodeify = require('denodeify');

// Load configuration
const config = require('../config.js');

// Denodeify shortcut
const denodeifyMethod = (object, method) => denodeify(object[method].bind(object));

// Default Response object
const RESPONSE = {online: 1, found: 0, speed: -1, name: ''};
const newResponse = () => JSON.parse(JSON.stringify(RESPONSE));

/*
  Performs a simple XOR encryption on passed Buffer
  Returns encrypted Buffer with key appended
*/
const encrypt = (resp) => {
  if (!config.server_encrypt)
    return resp;

  const key = Math.floor((Math.random()*254)) + 1;
  var encrypted = new Buffer(resp.length+1);

  resp.forEach((val, i) => encrypted[i] = val ^ key);

  return encrypted;
};

/*
  Decrypts passed Buffer using appended XOR key
  Returns the decrypted Buffer
*/
const decrypt = (req) => {
  if (!config.server_encrypt)
    return req;

  var decrypted = new Buffer(req.length-1);

  // Retrieve key
  const key = req[req.length-1];

  for (var i = 0; i < decrypted.length; i++)
    decrypted[i] = req[i] ^ key;

  // Return decrypted Buffer
  return decrypted;
};

/*
  Given a parsed JSON request object:

  1. Queries `segments` collection to find the road the coordinate lies on.
  2. If matched, queries `roads` collection to determine road name.

  Returns a Promise wrapped Response object.
*/
const findRoad = (parsed, db) => {
  // GeoJSON coordinate representation
  const loc = {
    type: 'Point',
    coordinates: [parsed.lng, parsed.lat]
  };

  // Perform query on SEGMENTS collection
  // Find the road segment that contains Point `loc`
  const q = {
    shape: {
      $geoIntersects: {
        $geometry: loc
      }
    }
  };

  const resp = newResponse();
  const segments = db.collection(config.segments);
  const findOneSegment = denodeifyMethod(segments, 'findOne');

  return findOneSegment(q, {speed: 1, road_id: 1, _id: 0})
  .then(segment => {
    if (!segment) {
      return resp;
    } else {
      const roads = db.collection(config.roads);
      const findOneRoad = denodeifyMethod(roads, 'findOne');

      return findOneRoad({_id: segment.road_id})
      .then(road => {
        resp.found = 1;
        resp.speed = segment.speed;
        resp.name = road.name;
        return resp;
      })
      .catch(err => {
        console.log(err);
        resp.name = '';
        return resp;
      });
    }
  })
  .catch(err => {
    console.log(err);
    resp.online = 0;
    return resp;
  });
};

/*
  UDP connection handler.
*/
const processRequest = (req, remote, socket) => {
  var parsed;
  var valid = true;

  try {
    // Decrypt if necessary
    req = decrypt(req);

    parsed = JSON.parse(req);

    // Check for correct params
    if (!parsed.lat || !parsed.lng) {
      throw Error();
    }
  } catch (e) {
    console.log(e.stack);
    valid = false;
  }

  var sendResponse = resp => {
    // Shorten response keys to save 16 bytes
    var short = {
      o: resp.online,
      f: resp.found,
      s: resp.speed,
      n: resp.name
    };

    var b = new Buffer(JSON.stringify(short));

    // Encrypt response
    b = encrypt(b);

    socket.send(b, 0, b.length, remote.port, remote.address);
  };

  if (valid) {
    MongoClient.connect(config.db_url, (err, db) => {
      if (err) {
        // Database offline
        var resp = newResponse();
        resp.online = 0;
        sendResponse(resp);
        db.close();
      } else {
        // Consume the returned Promise
        findRoad(parsed, db)
        .then(resp => {
          sendResponse(resp);
          db.close();
        });
      }
    });
  }
};

// UDP server
const socket = dgram.createSocket('udp4');
var c = 0;

socket.bind(config.server_port, config.server_host);

socket.on('listening', () => {
  console.log('Listening on %s:%d...', config.server_host, config.server_port);
});

socket.on('message', (req, remote) => {
  console.log('Message #%d received!', (++c));

  processRequest(req, remote, socket);
});

socket.on('error', (error) => {
  console.log(error.stack);
  socket.close();
});
