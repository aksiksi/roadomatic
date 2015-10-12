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
    shape: JSON.parse(req.body.segment),
    speed: parseInt(req.body.speed)
  };

  console.log(data);

  writeSegment(data, (err) => {
    if (err) {
      res.redirect('/');
    }
    else {
      res.render('success');
    }
  });
});

// Write a Segment to DB
var writeSegment = (data, callback) => {
  MongoClient.connect(config.db_url, (err, db) => {
    // Database offline
    if (err) {
      console.log(err);
      callback(err);
    } else {
      // Get road_id
      db.collection(config.roads).findOne({name: data.name}, (err, road) => {
        if (err || !road) {
          db.close();
          callback(err);
        } else {
          const segment = {
            speed: data.speed,
            road_id: road._id,
            shape: data.shape
          };

          db.collection(config.segments).insertOne(segment, (err, res) => {
            if (err || !road) {
              console.log(err);
              callback(err);
            }

            else {
              callback();
            }

            db.close();
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
