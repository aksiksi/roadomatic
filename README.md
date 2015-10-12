# Roadomatic

Roadomatic is a complete road alert system my team is designing as a senior project in Electrical Engineering at UAE University.

## Outline

The system delivers road information in realtime to drivers. It consists of a client running on the driver's Android smartphone, a UDP server that handles requests from clients, a  MongoDB database that stores road information, and a small Express web app that acts as a CRUD frontend for insertion of new roads into the database.

## Structure

* `client`: houses the Android application code; written in Java
* `server`: written in Node.js
  - `admin.js`: the admin frontend web app
  - `server.js`: the UDP server used to receive packets from clients; requires a running MongoDB instance
