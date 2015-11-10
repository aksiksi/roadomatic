// Imports
var path = require('path');
var express = require('express');
var jade = require('jade');
var MongoClient = require('mongodb').MongoClient;

// Create an Express app
var app = express();

// Load configuration
var config = require('../config.js');

// Set static directory
app.use(express.static(path.join(__dirname, config.static_dir)));

// Use Jade for templating
app.set('views', path.join(__dirname, config.views_dir));
app.set('view engine', 'jade');

// Setup body-parser, needed for POST param extraction
var bodyParser = require('body-parser');
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

var roads = {
  r1: 'Khalifa Bin Zayed Street',
  r2: 'Test Street'
};

// Index route
app.get('/', (req, res) => {
  res.render('index', {title: 'Hello!', roads: roads});
});

// Extract one or more Polygons from GeoJSON string
var extractShapes = (geo, callback) => {
  var shapes = [];

  try {
    const parsed = JSON.parse(geo);

    // Find all Polygons
    parsed.features.forEach((val) => {
      shapes.push(val.geometry);
    });

    callback(null, shapes);
  } catch (e) {
    console.log(e);
    callback(e, null);
  }
};

app.post('/segment', (req, res) => {
  const road = {};

  try {
    road.name = roads[req.body.name];
    road.geo = req.body.segment;
    road.speed = parseInt(req.body.speed);
  } catch (e) {
    res.render('error', {error: e});
  }

  extractShapes(road.geo, (err, shapes) => {
    if (err) {
      res.render('error', {error: 'Segment not formatted correctly.'});
    } else if (shapes.length == 0) {
      res.render('error', {error: 'No shapes found!'});
    } else {
      // Create an array of documents
      const docs = shapes.map((shape) => {
        return {name: road.name, shape: shape, speed: road.speed};
      });

      writeSegments(docs, (err) => {
        if (err) {
          res.render('error', {error: err});
        } else {
          res.render('success');
        }
      });
    }
  });
});

// Write one or more road segments to DB
var writeSegments = (data, callback) => {
  MongoClient.connect(config.db_url, (err, db) => {
    // Database offline
    if (err) {
      console.log(err);
      callback(err);
    } else {
      // Get road_id
      db.collection(config.roads).findOne({name: data[0].name}, (err, road) => {
        if (err || !road) {
          console.log(err);
          db.close();
          callback(err);
        } else {
          // Add road_id to each document
          const docs = data.map((d) => {
            return {speed: d.speed, road_id: road._id, shape: d.shape};
          });

          // Write documents to DB
          db.collection(config.segments).insert(docs, (err, res) => {
            db.close();

            if (err || !road) {
              console.log(err);
              callback(err);
            } else {
              callback();
            }
          });
        }
      });
    }
  });
};

// Start the Express server
var startServer = () => {
  var server = app.listen(config.frontend_port, () => {
    var host = config.frontend_host;
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
