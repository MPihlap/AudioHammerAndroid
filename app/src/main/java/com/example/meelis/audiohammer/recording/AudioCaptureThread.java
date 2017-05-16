package com.example.meelis.audiohammer.recording;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Meelis on 28/04/2017.
 */

public class AudioCaptureThread implements Runnable {
    private boolean bufferedMode;
    private final DataOutputStream serverStream;
    private final BlockingQueue<String> recordingQueue;
    private final ByteArrayOutputStream byteArrayOutputStream;

    public void setBufferedMode(boolean bufferedMode) {
        this.bufferedMode = bufferedMode;
    }

    public AudioCaptureThread(DataOutputStream serverStream, BlockingQueue<String> recordingQueue, ByteArrayOutputStream byteArrayOutputStream) {

        this.serverStream = serverStream;
        this.recordingQueue = recordingQueue;
        this.byteArrayOutputStream = byteArrayOutputStream;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void run() {
        try {
            AudioRecord recorder = new AudioRecord(1, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, 44100 / 5);
            if (recorder.getState() == AudioRecord.STATE_UNINITIALIZED)
                throw new RuntimeException("Oioi");
            recorder.startRecording();
            byte[] audioByteBuffer = new byte[44100 / 5];
            serverStream.writeInt(1);
            serverStream.writeInt(audioByteBuffer.length);
            while (true) {
                String command = recordingQueue.poll();
                if (command != null) {
                    if (command.equals("stop")) {
                        break;
                    }
                    if (command.equals("pause")) {
                        try {
                            recordingQueue.take();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (command.equals("buffer")) {
                        System.out.println("idk");
                    }
                }
                int numBytesRead = recorder.read(audioByteBuffer, 0, audioByteBuffer.length);
                byteArrayOutputStream.write(audioByteBuffer, 0, numBytesRead);
                serverStream.writeInt(0);
                serverStream.writeInt(numBytesRead);
                serverStream.write(audioByteBuffer, 0, numBytesRead);
            }
            serverStream.writeInt(2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
