package com.ha.gp;

import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;



public class RoadomaticRequest implements Runnable {




    public TextView mSpeed ; // we use it to make the variable global
    public TextView mName ; // we use it to make the variable global
    public String request = "{\"lat\":24.200470,\"lng\": 55.669236}"; // Get The coordinates from the GPS ;
    public DatagramSocket s;
    public String IP = "188.166.68.166";




    public void run () {
        // TODO Auto-generated method stub
        try {


send();


recieve();





        }

        catch (Exception e) {

        }



    }




    public void send() throws IOException {


        // prepare a Socket and send it
        s = new DatagramSocket();

        int server_port = 5151;
        InetAddress local = InetAddress.getByName(IP);
        int msg_length = request.length();
        byte[] message = request.getBytes();
        DatagramPacket p = new DatagramPacket(message, msg_length, local, server_port);
        Log.d("testing1", "yes");
        s.send(p);



    }



    public JSONObject recieve() throws IOException, JSONException {


        // The recieved socket
        byte[] m = new byte[512]; // the recived packet length
        DatagramPacket p1 = new DatagramPacket(m, m.length);
        s.receive(p1);

        String str = new String(p1.getData(), 0, p1.getLength());

        Log.d("testing" , str);


        // Convert it to JSON
        JSONObject obj = new JSONObject(str); // to change the string that recieved to json.


        String O = obj.getString("o"); // to extract the value of o from JSON
        String F = obj.getString("f"); // to extract the value of f from JSON
        String S = obj.getString("s"); // to extract the value of s from JSON
        String N = obj.getString("n"); // to extract the value of n from JSON


        // User interface

        if (O.equals("1") && F.equals("1") )
        {


            mSpeed.setText(""+ S); // to set the speed text

            mName.setText("" + N);// to set the Streetname text



        }
        if (O.equals("1") && F.equals("0") )
        {


            mSpeed.setText("None" ); // to set the speed text
            mName.setText("Not Found");// to set the Streetname text


        }
return(obj);

    }




}
