package com.mreze.podcastappserver.bullshit;

import com.mreze.podcastappserver.AudioRecorder;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
import java.util.UUID;

@Data
@Log4j2
public class StreamingServer implements ActionListener{
    DatagramSocket rtpSocket; //socket to be used to send and receive UDP packets
    DatagramPacket rtpPacket; //UDP packet containing the video frames

    InetAddress clientAddress;   //Client IP address
    int rtpDestinationPort = 0;      //destination port for RTP packets  (given by the RTSP Client)
    int rtspDestinationPort = 0;

    //Video variables:
    //----------------
    int imagenb = 0; //image nb of the image currently transmitted
    AudioRecorder audioRecorder; //VideoStream object used to access video frames
    static int MJPEG_TYPE = 26; //RTP payload type for MJPEG video
    static int FRAME_PERIOD = 100; //Frame period of the video to stream, in ms
    static int VIDEO_LENGTH = 500; //length of the video in frames

    Timer timer;    //timer used to send the images at the video frame rate
    byte[] buf;     //buffer used to store the images to send to the client
    int sendDelay;  //the delay to send images over the wire. Ideally should be
    //equal to the frame rate of the video file, but may be
    //adjusted when congestion is detected.


    Socket rtspSocket; //socket used to send/receive RTSP messages
    //input and output stream filters
    static BufferedReader RTSPBufferedReader;
    static BufferedWriter RTSPBufferedWriter;
    static String VideoFileName; //video file requested from the client
    static String RTSPid = UUID.randomUUID().toString(); //ID of the RTSP session
    int RTSPSeqNb = 0; //Sequence number of RTSP messages within the session


    //RTCP variables
    //----------------
    static int RTCP_RCV_PORT = 19001; //port where the client will receive the RTP packets
    static int RTCP_PERIOD = 400;     //How often to check for control events
    DatagramSocket rtcpSocket;
    RtcpReceiver rtcpReceiver;
    int congestionLevel;

    //Performance optimization and Congestion control
    CongestionController cc;

    final static String CRLF = "\r\n";

    public StreamingServer() throws IOException {

        //init RTP sending Timer
        sendDelay = FRAME_PERIOD;
        timer = new Timer(sendDelay, null);
        timer.setInitialDelay(0);
        timer.setCoalesce(true);

        //init congestion controller
        cc = new CongestionController(600);

        //allocate memory for the sending buffer
        buf = new byte[20000];

//        //stop the timer and exit
//        timer.stop();
//        rtcpReceiver.stopRcv();

        this.rtspDestinationPort = 8081;

        ServerSocket listenSocket = new ServerSocket(rtspDestinationPort);
        this.rtspSocket = listenSocket.accept();
        listenSocket.close();

        this.clientAddress = this.rtspSocket.getInetAddress();

        RTSPBufferedReader = new BufferedReader(new InputStreamReader(this.rtspSocket.getInputStream()));
        RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(this.rtspSocket.getOutputStream()));

        //init the RTCP packet receiver
        rtcpReceiver = new RtcpReceiver(RTCP_PERIOD);
    }

    //------------------------
    //Handler for timer
    //------------------------
    public void actionPerformed(ActionEvent e) {
//        byte[] frame;
//
//        //if the current image nb is less than the length of the video
//        if (imagenb < VIDEO_LENGTH) {
//            //update current imagenb
//            imagenb++;
//
//            try {
//                if (congestionLevel > 0) {
//
//                }
//
//                //Builds an RTPpacket object containing the frame
//                RTPpacket rtp_packet = new RTPpacket(MJPEG_TYPE, imagenb, imagenb*FRAME_PERIOD, buf, image_length);
//
//                //get to total length of the full rtp packet to send
//                int packet_length = rtp_packet.getlength();
//
//                //retrieve the packet bitstream and store it in an array of bytes
//                byte[] packet_bits = new byte[packet_length];
//                rtp_packet.getpacket(packet_bits);
//
//                //send the packet as a DatagramPacket over the UDP socket
//                rtpPacket = new DatagramPacket(packet_bits, packet_length, clientAddress, rtpDestinationPort);
//                rtpSocket.send(rtpPacket);
//
//                System.out.println("Send frame #" + imagenb + ", Frame size: " + image_length + " (" + buf.length + ")");
//                //print the header bitstream
//                rtp_packet.printheader();
//            }
//            catch(Exception ex) {
//                log.error("Exception, e: ", ex);
//                System.exit(0);
//            }
//        }
//        else {
//            //if we have reached the end of the video file, stop the timer
//            timer.stop();
//            rtcpReceiver.stopRcv();
//        }
    }

    //------------------------
    //Controls RTP sending rate based on traffic
    //------------------------
    class CongestionController implements ActionListener {
        private Timer ccTimer;
        int interval;   //interval to check traffic stats
        int prevLevel;  //previously sampled congestion level

        public CongestionController(int interval) {
            this.interval = interval;
            ccTimer = new Timer(interval, this);
            ccTimer.start();
        }

        public void actionPerformed(ActionEvent e) {

            //adjust the send rate
            if (prevLevel != congestionLevel) {
                sendDelay = FRAME_PERIOD + congestionLevel * (int)(FRAME_PERIOD * 0.1);
                timer.setDelay(sendDelay);
                prevLevel = congestionLevel;
                System.out.println("Send delay changed to: " + sendDelay);
            }
        }
    }

    class RtcpReceiver implements ActionListener {
        private Timer rtcpTimer;
        private byte[] rtcpBuf;
        int interval;

        public RtcpReceiver(int interval) {
            //set timer with interval for receiving packets
            this.interval = interval;
            rtcpTimer = new Timer(interval, this);
            rtcpTimer.setInitialDelay(0);
            rtcpTimer.setCoalesce(true);

            //allocate buffer for receiving RTCP packets
            rtcpBuf = new byte[512];
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            //Construct a DatagramPacket to receive data from the UDP socket
            DatagramPacket dp = new DatagramPacket(rtcpBuf, rtcpBuf.length);
            float fractionLost;

            try {
                rtcpSocket.receive(dp);   // Blocking
                RTCPpacket rtcpPkt = new RTCPpacket(dp.getData(), dp.getLength());
                System.out.println("[RTCP] " + rtcpPkt);

                //set congestion level between 0 to 4
                fractionLost = rtcpPkt.getFractionLost();
                if (fractionLost >= 0 && fractionLost <= 0.01) {
                    congestionLevel = 0;    //less than 0.01 assume negligible
                }
                else if (fractionLost > 0.01 && fractionLost <= 0.25) {
                    congestionLevel = 1;
                }
                else if (fractionLost > 0.25 && fractionLost <= 0.5) {
                    congestionLevel = 2;
                }
                else if (fractionLost > 0.5 && fractionLost <= 0.75) {
                    congestionLevel = 3;
                }
                else {
                    congestionLevel = 4;
                }
            }
            catch (InterruptedIOException iioe) {
                log.error("Nothign to read, e: ", iioe);
            } catch (IOException ioe) {
                log.error("Exception, e: ", ioe);
            }
        }

        public void startRcv() {
            rtcpTimer.start();
        }

        public void stopRcv() {
            rtcpTimer.stop();
        }
    }

    public RequestType parseRequest() {
        RequestType request_type = RequestType.UNKNOWN;
        try {
            //parse request line and extract the request_type:
            String requestLine = RTSPBufferedReader.readLine();
            System.out.println("RTSP Server - Received from Client:");
            System.out.println(requestLine);

            StringTokenizer tokens = new StringTokenizer(requestLine);
            String request_type_string = tokens.nextToken();

            //convert to request_type structure:
            if ((new String(request_type_string)).compareTo("SETUP") == 0)
                request_type = RequestType.SETUP;
            else if ((new String(request_type_string)).compareTo("PLAY") == 0)
                request_type = RequestType.PLAY;
            else if ((new String(request_type_string)).compareTo("PAUSE") == 0)
                request_type = RequestType.PAUSE;
            else if ((new String(request_type_string)).compareTo("TEARDOWN") == 0)
                request_type = RequestType.TEARDOWN;
            else if ((new String(request_type_string)).compareTo("DESCRIBE") == 0)
                request_type = RequestType.DESCRIBE;

            if (request_type == RequestType.SETUP) {
                //extract VideoFileName from requestLine
                VideoFileName = tokens.nextToken();
            }

            //parse the SeqNumLine and extract CSeq field
            String SeqNumLine = RTSPBufferedReader.readLine();
            System.out.println(SeqNumLine);
            tokens = new StringTokenizer(SeqNumLine);
            tokens.nextToken();
            RTSPSeqNb = Integer.parseInt(tokens.nextToken());

            //get LastLine
            String LastLine = RTSPBufferedReader.readLine();
            System.out.println(LastLine);

            tokens = new StringTokenizer(LastLine);
            if (request_type == RequestType.SETUP) {
                //extract RTP_dest_port from LastLine
                for (int i=0; i<3; i++)
                    tokens.nextToken(); //skip unused stuff
                rtpDestinationPort = Integer.parseInt(tokens.nextToken());
            }
            else if (request_type == RequestType.DESCRIBE) {
                tokens.nextToken();
                String describeDataType = tokens.nextToken();
            }
            else {
                //otherwise LastLine will be the SessionId line
                tokens.nextToken(); //skip Session:
                RTSPid = tokens.nextToken();
            }
        } catch(Exception ex) {
            log.error("Exception, e: ", ex);
            System.exit(0);
        }

        return request_type;
    }

    // Creates a DESCRIBE response string in SDP format for current media
    private String describe() {
        StringWriter writer1 = new StringWriter();
        StringWriter writer2 = new StringWriter();

        // Write the body first so we can get the size later
        writer2.write("v=0" + CRLF);
        writer2.write("m=video " + rtspDestinationPort + " RTP/AVP " + MJPEG_TYPE + CRLF);
        writer2.write("a=control:streamid=" + RTSPid + CRLF);
        writer2.write("a=mimetype:string;\"video/MJPEG\"" + CRLF);
        String body = writer2.toString();

        writer1.write("Content-Base: " + VideoFileName + CRLF);
        writer1.write("Content-Type: " + "application/sdp" + CRLF);
        writer1.write("Content-Length: " + body.length() + CRLF);
        writer1.write(body);

        return writer1.toString();
    }

    //------------------------------------
    //Send RTSP Response
    //------------------------------------
    void sendResponse() {
        try {
            RTSPBufferedWriter.write("RTSP/1.0 200 OK"+CRLF);
            RTSPBufferedWriter.write("CSeq: "+RTSPSeqNb+CRLF);
            RTSPBufferedWriter.write("Session: "+RTSPid+CRLF);
            RTSPBufferedWriter.flush();
            System.out.println("RTSP Server - Sent response to Client.");
        } catch(Exception ex) {
            log.error("Exception, e: ", ex);
            System.exit(0);
        }
    }

    void sendDescribe() {
        String des = describe();
        try {
            RTSPBufferedWriter.write("RTSP/1.0 200 OK"+CRLF);
            RTSPBufferedWriter.write("CSeq: "+RTSPSeqNb+CRLF);
            RTSPBufferedWriter.write(des);
            RTSPBufferedWriter.flush();
            System.out.println("RTSP Server - Sent response to Client.");
        } catch(Exception ex) {
            log.error("Exception, e: ", ex);
            System.exit(0);
        }
    }
}
