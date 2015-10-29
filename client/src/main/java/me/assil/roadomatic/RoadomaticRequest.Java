package me.assil.roadomatic;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class RoadomaticRequest {
    private DatagramSocket mSocket;
    private InetAddress mHost;
    private boolean mConnected = false;

    private static final int TIMEOUT = 2000;
    private static final int BUFFER_SIZE = 512;

    // Server information
    private static final String SERVER_IP = "188.166.68.166";
    private static final int SERVER_PORT = 5151;
    private static final boolean SERVER_ENCRYPT = false;

    private static final String TAG = "RoadomaticRequest";

    public RoadomaticRequest() {
        try {
            // Open only one socket, set receive timeout
            mSocket = new DatagramSocket();
            mSocket.setSoTimeout(TIMEOUT);

            mHost = InetAddress.getByName(SERVER_IP);

            mConnected = true;
        } catch (IOException e) {
            Log.d(TAG, "Error creating socket, connection problem.");
        }
    }

    public boolean isConnected() {
        return mConnected;
    }

    public boolean send(String request) {
        if (SERVER_ENCRYPT)
            request = XOREncrypt.decrypt(request);

        int l = request.length();
        byte[] message = request.getBytes();
        DatagramPacket p = new DatagramPacket(message, l, mHost, SERVER_PORT);

        boolean success = false;

        try {
            mSocket.send(p);
            success = true;
        } catch (IOException e) {
            Log.d(TAG, "Failed to send packet. No connection?");
        }

        return success;
    }

    public JSONObject receive() {
        JSONObject resp = null;

        // The receive buffer
        byte[] buf = new byte[BUFFER_SIZE];
        DatagramPacket p = new DatagramPacket(buf, buf.length);

        try {
            // Wait for TIMEOUT ms
            mSocket.receive(p);

            // Convert response to JSON, decrypt if needed
            String s = new String(p.getData(), 0, p.getLength());

            if (SERVER_ENCRYPT)
                s = XOREncrypt.decrypt(s);

            resp = new JSONObject(s);
        } catch (SocketTimeoutException e) {
            Log.d(TAG, "Receive timed out!");
        } catch (IOException e) {
            Log.d(TAG, "Failed to receive packet, server offline!");
        } catch (JSONException e) {
            Log.d(TAG, "Error parsing received JSON.");
        }

        // Close socket
        mSocket.close();

        return resp;
    }
}
