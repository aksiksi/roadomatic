# TODO: write a small web app that displays a form
# On form submission, write GeoJSON data to db

// Imports
var express = require('express');
var app = express();

// Configuration
const PORT = 8080;

app.get('/', (req, res) => {
  res.send('<h1>Hello world!</h1>');
});

var server = app.listen(PORT, () => {
  var host = server.address().address;
  var port = server.address().port;

  console.log('http://%s:%s', host, port);
});
