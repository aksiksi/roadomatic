// Script that sets up a fresh DB and creates required collections
var MongoClient = require('mongodb').MongoClient;
var config = require('./config.js');

MongoClient.connect(config.db_url, (err, db) => {
  if (err) {
    console.log(err);
  } else {
    // Create roads collection
    db.createCollection(config.roads, (err, collection) => {
      if (err) {
        console.log('%s collection already created!', config.roads);
      } else {
        // Create segments collection
        db.createCollection(config.segments, (err, collection) => {
          if (err) {
            console.log('%s collection already created!', config.segments);
          }

          // Ensure index is present for GeoJSON objects
          db.ensureIndex({shape: '2dsphere'}, (err, idx) => {
            if (err) {
              console.log('Index is present already.');
            }
            db.close();
          });
        });
      }
    });
  });
});
