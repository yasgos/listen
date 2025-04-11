import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class AudioStreamer {
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

    private AudioRecord audioRecord;
    private boolean isStreaming = false;
    private String serverIp = "192.168.1.5"; // Telefonun IP adresi
    private int serverPort = 50005;

    public void startStreaming() {
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);

        isStreaming = true;
        audioRecord.startRecording();

        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket();
                byte[] buffer = new byte[BUFFER_SIZE];
                InetAddress address = InetAddress.getByName(serverIp);

                while (isStreaming) {
                    int read = audioRecord.read(buffer, 0, buffer.length);
                    if (read > 0) {
                        DatagramPacket packet = new DatagramPacket(buffer, read, address, serverPort);
                        socket.send(packet);
                    }
                }

                socket.close();
                audioRecord.stop();
                audioRecord.release();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stopStreaming() {
        isStreaming = false;
    }
}