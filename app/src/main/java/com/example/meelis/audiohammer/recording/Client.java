package com.example.meelis.audiohammer.recording;

import android.media.AudioFormat;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Meelis on 28/04/2017.
 */

public class Client {
    private AudioFormat audioFormat;
    private String username;
    private Socket servSocket;
    private DataOutputStream servOutputStream;
    private DataInputStream servInputStream;
    private BlockingQueue<String> recordingInfo = new ArrayBlockingQueue<String>(5);
    private boolean isRecording;
    Thread captureThread;


    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    public boolean isRecording(){
        return isRecording;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        System.out.println("sain username");
    }
    private void sendFormat() throws IOException {
        servOutputStream.writeFloat(44100);
        servOutputStream.writeInt(16);
        servOutputStream.writeInt(1);
        servOutputStream.writeBoolean(true);
        servOutputStream.writeBoolean(false);
    }
    public void createConnection() throws IOException {
        this.servSocket = new Socket("172.19.30.117", 1337);
        this.servOutputStream = new DataOutputStream(servSocket.getOutputStream());
        this.servInputStream = new DataInputStream(servSocket.getInputStream());
        System.out.println("Connection made");
    }

    public void closeConnection() throws IOException {
        this.servSocket.close();
    }

    public void sendCommand(String command) throws IOException {
        servOutputStream.writeUTF(command);
    }

    public void startRecording() throws IOException {
        sendFormat();
        servOutputStream.writeBoolean(false);
        recordingInfo.add("start");
        AudioCaptureThread audioCaptureThread = new AudioCaptureThread(servOutputStream,recordingInfo,new ByteArrayOutputStream());
        audioCaptureThread.setBufferedMode(false);
        this.captureThread = new Thread(audioCaptureThread);
        captureThread.start();
        System.out.println("hakkas lindistama");
    }
    public void startBufferedRecording(int minutes)throws IOException{
        sendFormat();
        servOutputStream.writeBoolean(true);
        servOutputStream.writeInt(minutes);
        recordingInfo.add("start");
        AudioCaptureThread audioCaptureThread = new AudioCaptureThread(servOutputStream,recordingInfo,new ByteArrayOutputStream());
        audioCaptureThread.setBufferedMode(true);
        this.captureThread = new Thread(audioCaptureThread);
        captureThread.start();
    }
    public void saveBuffer() throws IOException {
        recordingInfo.add("buffer");
    }

    public void pauseRecording() {
        recordingInfo.add("pause");
        System.out.println("recording paused");
    }

    public void resumeRecording() {
        recordingInfo.add("resume");
        System.out.println("resumed");
    }

    public void stopRecording() throws IOException {
        recordingInfo.add("stop");
        System.out.println("stopped");
        try {
            captureThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("finished recording");
    }
    public boolean sendUsername(String username, String password) throws IOException {
        servOutputStream.writeUTF("username"); // Indicate incoming user info
        servOutputStream.writeUTF(username);
        servOutputStream.writeUTF(password);
        return servInputStream.readBoolean();
    }
}
