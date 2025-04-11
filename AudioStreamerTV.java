import android.app.Activity;
import android.os.Bundle;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioStreamerTV extends Activity {
    private AudioRecord recorder;
    private int bufferSize;
    private static final int SAMPLE_RATE = 44100;
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private byte[] audioBuffer;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_streamer_tv);

        // Ses kaydına başlamak için gerekli buffer boyutunu al
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        recorder = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        // Ses kaydını başlat
        startRecording();
    }

    private void startRecording() {
        if (recorder != null && recorder.getState() == AudioRecord.STATE_INITIALIZED) {
            recorder.startRecording();
            Log.d("AudioStreamerTV", "Recording started");

            // Ses verisini almak ve kaydetmek için buffer hazırla
            audioBuffer = new byte[bufferSize];
            
            // Veriyi almak için yeni bir iş parçacığı başlat
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (recorder.read(audioBuffer, 0, audioBuffer.length) > 0) {
                        writeDataToFile(audioBuffer); // Ses verisini dosyaya yaz
                    }
                }
            }).start();
        }
    }

    private void writeDataToFile(byte[] audioData) {
        try {
            // Dosyayı uygulama özel dizininde oluşturuyoruz
            String filePath = getFilesDir() + "/audio_recording.wav"; // Uygulamanın kendi dizininde kaydediyoruz
            FileOutputStream fos = new FileOutputStream(filePath, true); // 'true' ile dosyaya ekleme yapar
            fos.write(audioData);
            fos.close();
            Log.d("AudioStreamerTV", "Data saved to file at: " + filePath);
        } catch (IOException e) {
            Log.e("AudioStreamerTV", "Error saving data", e);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            Log.d("AudioStreamerTV", "Recording stopped");
        }
    }
}
