// Imports
var fs = require('fs');
var path = require('path');
var express = require('express');
var jade = require('jade');
var MongoClient = require('mongodb').MongoClient;

var app = express();

// Config variables
const PORT = 8000;
const DB_URL = 'mongodb://localhost:27017/testing';
const ROADS = 'roads';
const SEGMENTS = 'segments';

// Set static directory
app.use(express.static(path.join(__dirname, 'static')));

// Use Jade for templating
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

// Setup body-parser, needed for POST param extraction
var bodyParser = require('body-parser');
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

var roads = {
  r1: 'Khalifa Bin Zayed Street'
};

// Index route
app.get('/', (req, res) => {
  res.render('index', {title: 'Hello!', roads: roads});
});

// Add a road segment to DB
app.post('/segment', (req, res) => {
  const data = {
    name: roads[req.body.name],
    shape: req.body.segment,
    speed: parseInt(req.body.speed)
  };

  console.log(data);

  // Write to DB
  writeSegment(data, () => res.redirect('/'));
});

// Write a Segment to DB
var writeSegment = (data, callback) => {
  MongoClient.connect(DB_URL, (err, db) => {
    // Database offline
    if (err != null) {
      console.log(err);
      callback();
    } else {
      // Get road_id
      db.collection(ROADS).findOne({name: data.name}, (err, road) => {
        if (err != null || road == null) {
          db.close();
          callback();
        } else {
          data.road_id = road._id;

          db.collection(SEGMENTS).insertOne(data, (err, res) => {
            if (err != null || road == null) {
              console.log(err);
            }
            console.log(res);
            db.close();
            callback();
          });
        }
      });
    }
  });
};

// Read a Segment from DB

// Start the Express server
var startServer = () => {
  var server = app.listen(PORT, () => {
    var host = server.address().address;
    var port = server.address().port;

    console.log('http://%s:%s', host, port);
  });
};

// If not main module, export
if (!module.parent) {
  startServer();
} else {
  module.exports.startServer = startServer;
}
