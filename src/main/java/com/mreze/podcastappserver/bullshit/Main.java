package com.mreze.podcastappserver.bullshit;

import com.mreze.podcastappserver.AudioRecorder;

import java.net.DatagramSocket;

public class Main {

    static int state;

    final static int INIT = 0;
    final static int READY = 1;
    final static int PLAYING = 2;

    static int RTCP_RCV_PORT = 19001; //port where the client will receive the RTP packets
    static int RTCP_PERIOD = 400;

    public static void main(String argv[]) throws Exception {
        StreamingServer server = new StreamingServer();

        // rtsp port

        state = INIT;


        //Wait for the SETUP message from the client
        RequestType request_type;
        boolean done = false;
        while (!done) {
            request_type = server.parseRequest(); //blocking

            if (request_type == RequestType.SETUP) {
                done = true;

                //update RTSP state
                state = READY;
                System.out.println("New RTSP state: READY");

                //Send response
                server.sendResponse();

                //init the VideoStream object:
                server.audioRecorder = new AudioRecorder();

                //init RTP and RTCP sockets
                server.rtpSocket = new DatagramSocket();
                server.rtcpSocket = new DatagramSocket(RTCP_RCV_PORT);
            }
        }

        while (true) {
            request_type = server.parseRequest(); //blocking

            if ((request_type == RequestType.PLAY) && (state == READY)) {
                //send back response
                server.sendResponse();
                //start timer
                server.timer.start();
                server.rtcpReceiver.startRcv();
                //update state
                state = PLAYING;
                System.out.println("New RTSP state: PLAYING");
            } else if ((request_type == RequestType.PAUSE) && (state == PLAYING)) {
                //send back response
                server.sendResponse();
                //stop timer
                server.timer.stop();
                server.rtcpReceiver.stopRcv();
                //update state
                state = READY;
                System.out.println("New RTSP state: READY");
            } else if (request_type == RequestType.TEARDOWN) {
                //send back response
                server.sendResponse();
                //stop timer
                server.timer.stop();
                server.rtcpReceiver.stopRcv();
                //close sockets
                server.rtspSocket.close();
                server.rtpSocket.close();

                System.exit(0);
            } else if (request_type == RequestType.DESCRIBE) {
                System.out.println("Received DESCRIBE request");
                server.sendDescribe();
            }
        }
    }

}
