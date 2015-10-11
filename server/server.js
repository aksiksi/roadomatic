// Imports
var dgram = require('dgram');
var http = require('http');
var util = require('util');
var MongoClient = require('mongodb').MongoClient;

// Database configuration
const DB_URL = 'mongodb://localhost:27017/testing';
const ROADS = 'roads';
const SEGMENTS = 'segments';

// UDP server configuration
const HOSTNAME = '0.0.0.0';
const PORT = 8080;

// Default Response object
const RESPONSE = {online: 1, found: 0, speed: -1, name: ""};
var newResponse = () => JSON.parse(JSON.stringify(RESPONSE));

var findRoad = (parsed, db, callback) => {
  const resp = newResponse();

  // GeoJSON coordinate representation
  const loc = {
    type: 'Point',
    coordinates: [parsed.lng, parsed.lat]
  };

  var segments = db.collection(SEGMENTS);

  // Perform query on SEGMENTS collection
  // Find the road segment that contains Point `loc`
  const q = {
    shape: {
      $geoIntersects: {
        $geometry: loc
      }
    }
  };

  segments.findOne(q, {speed: 1, road_id: 1, _id: 0}, (err, segment) => {
    if (err != null) {
      // Collection read error
      console.log(err);
      resp.online = 0;
      callback(resp);
    } else if (segment == null) {
      // No segment match!
      callback(resp);
    } else {
      // Segment match!
      var roads = db.collection(ROADS);

      // Find road name using road_id
      roads.findOne({_id: segment.road_id}, (err, road) => {
        if (err != null || road == null) {
          // Read error, return what we have
          console.log(err);
          resp.name = "";
        } else {
          resp.found = 1;
          resp.speed = segment.speed;
          resp.name = road.name;
        }

        callback(resp);
      });
    }
  });
};

// UDP message handler
var processRequest = (req, remote, socket) => {
  var parsed;
  var valid = true;

  var sendResponse = resp => {
    // Shorten response keys to save 16 bytes
    var short = {
      o: resp.online,
      f: resp.found,
      s: resp.speed,
      n: resp.name
    };

    // TODO: Perhaps add a single byte XOR key for basic encryption?
    const b = new Buffer(JSON.stringify(short));
    socket.send(b, 0, b.length, remote.port, remote.address);
  };

  try {
    parsed = JSON.parse(req);

    // Check for correct params
    if (!parsed.lat || !parsed.lng) {
      throw Error();
    }
  } catch (e) {
    console.log(e.message);
    valid = false;
  }

  if (valid) {
    MongoClient.connect(DB_URL, (err, db) => {
      if (err != null) {
        // Database offline
        var resp = newResponse();
        resp.online = 0;
        sendResponse(resp);
        db.close();
      } else {
        findRoad(parsed, db, resp => {
          sendResponse(resp);
          db.close();
        });
      }
    });
  }
};

// UDP server
var socket = dgram.createSocket('udp4');
var c = 0;

socket.bind(PORT, HOSTNAME);

socket.on('listening', () => {
  console.log('Listening on %s:%d...', HOSTNAME, PORT);
});

socket.on('message', (req, remote) => {
  console.log('Message #%d received!', (++c));

  processRequest(req, remote, socket);
});

socket.on('error', (error) => {
  console.log(error.stack);
  socket.close();
});
