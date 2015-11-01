package main

import (
  "fmt"
  "net"
  "time"
  "bytes"
  "math/rand"
  "encoding/json"
  "gopkg.in/mgo.v2"
  "gopkg.in/mgo.v2/bson"
)

// Server config
const PORT int = 5151
const HOST string = "127.0.0.1"
const ENCRYPT bool = false

// Database config
const MONGO_HOST string = "localhost"
const MONGO_PORT int = 27017
const MONGO_DBNAME string = "roadomatic"
const MONGO_ROADS string = "roads"
const MONGO_SEGMENTS string = "segments"

type Request struct {
  Lat float64 `json:"lat"`
  Lng float64 `json:"lng"`
}

type Response struct {
  Online uint8  `json:"o"`
  Found  uint8  `json:"f"`
  Road   string `json:"r"`
  Speed  int    `json:"s"`
}

func (r *Response) String() string {
  return fmt.Sprintf("Response<o=%v,f=%v,s=%v>", r.Online,
                     r.Found, string(r.Speed))
}

type Road struct {
  Name    string `bson:"name"`
  City    string `bson:"city"`
  Country string `bson:"country"`
}

type Segment struct {
  Speed  int    `bson:"speed"`
  RoadId string `bson:"road_id"`
}

/**
  Given an encrypted request []byte, returns decrypted []byte
*/
func decrypt(req []byte) []byte {
  if !ENCRYPT {
    return req
  }

  l := len(req)-1
  decrypted := req[:l]
  key := req[l]

  for i := 0; i < l; i++ {
    decrypted[i] = req[i] ^ key
  }

  return decrypted
}

/**
  Given a unencrypted response []byte, returns encrypted []byte
*/
func encrypt(resp []byte) []byte {
  if !ENCRYPT {
    return resp
  }

  l := len(resp)
  encrypted := make([]byte, l+1)

  key := byte(rand.Intn(255)+1) // 1-255
  encrypted[l-1] = key

  for i, val := range resp {
    encrypted[i] = val ^ key
  }

  return encrypted
}

/**
  Handles a single UDP connection as a goroutine
*/
func udpHandler(buf []byte, b int, n int, conn *net.UDPConn, addr *net.UDPAddr) {
  defer fmt.Printf("Datagram %d sent!\n", n)

  // Slice buffer depending on read bytes, trim spaces
  clean := bytes.TrimSpace(buf[:b])

  // Decrypt incoming request []byte
  clean = decrypt(clean)

  // Parse the received JSON
  r := Request{}
  err := json.Unmarshal(clean, &r)
  if err != nil {
    fmt.Println(err)
    return
  }

  resp := findRoad(&r)

  // Convert to JSON, send over the wire
  s, err := json.Marshal(&resp)
  if err != nil {
    panic(err)
  }

  // Encrypt response []byte
  s = encrypt(s)

  conn.WriteToUDP(s, addr)
}

/**
  Given a Request (lat, lng), queries the DB to get the speed limit
*/
func findRoad(req *Request) Response  {
  r := Response{Online: 1}

  lat, lng := req.Lat, req.Lng
  loc := bson.M{
    "type": "Point",
    "coordinates": []float64{lng, lat},
  }

  // Query database, retrieve road_id
  session, err := mgo.Dial(fmt.Sprintf("mongodb://%v:%d", MONGO_HOST, MONGO_PORT))
  if err != nil {
    r.Online = 0
    fmt.Println(err)
    return r
  }
  defer session.Close()

  segments := session.DB(MONGO_DBNAME).C(MONGO_SEGMENTS)
  s := Segment{}

  filter := bson.M{"_id": 0, "speed": 1, "road_id": 1}
  query := bson.M{
    "shape": bson.M{
      "$geoIntersects": bson.M{
        "$geometry": loc,
      },
    },
  }

  err = segments.Find(query).Select(filter).One(&s)
  if err != nil {
    fmt.Println(err)
    return r
  }

  roads := session.DB(MONGO_DBNAME).C(MONGO_ROADS)
  road := Road{}

  err = roads.Find(bson.M{"_id": s.RoadId}).One(&road)
  if err != nil {
    r.Online = 0
    fmt.Println(err)
    return r
  }

  r.Found = 1
  r.Speed = s.Speed
  r.Road = road.Name

  return r
}

func main() {
  addr := net.UDPAddr{
    Port: PORT,
    IP: net.ParseIP(HOST),
  }

  conn, err := net.ListenUDP("udp", &addr)
  if (err != nil) {
    panic(err)
  }
  defer conn.Close()

  fmt.Printf("Listening on %d...\n", PORT)

  // Connection number
  n := 0

  // Setup a new Seed for this run
  rand.Seed(time.Now().UTC().UnixNano())

  // Spawn a goroutine for each incoming UDP datagram
  for {
    // Create a buffer for each request
    buf := make([]byte, 1024)

    // Read bytes into buffer
    b, addr, err := conn.ReadFromUDP(buf)
    if (err != nil) {
      panic(err)
    }

    n += 1
    fmt.Printf("Datagram %d received from %v!\n", n, addr)

    go udpHandler(buf, b, n, conn, addr)
  }
}
