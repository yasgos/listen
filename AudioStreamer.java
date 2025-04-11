import android.app.Activity;
import android.os.Bundle;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioStreamer extends Activity {
    private AudioRecord recorder;
    private int bufferSize;
    private static final int SAMPLE_RATE = 44100;
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private byte[] audioBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_streamer);

        // Minimum buffer boyutunu al
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        // AudioRecord nesnesini başlat
        recorder = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        // Ses kaydına başla
        startRecording();
    }

    private void startRecording() {
        if (recorder != null && recorder.getState() == AudioRecord.STATE_INITIALIZED) {
            recorder.startRecording(); // Kayda başla
            Log.d("AudioStreamer", "Recording started");

            // Ses verilerini almak ve kaydetmek için buffer hazırla
            audioBuffer = new byte[bufferSize];
            while (recorder.read(audioBuffer, 0, audioBuffer.length) > 0) {
                writeDataToFile(audioBuffer); // Veriyi dosyaya yaz
            }
        }
    }

    private void writeDataToFile(byte[] audioData) {
        try {
            // Dosyayı belirli bir yolda açıyoruz (burada internal storage kullanıyoruz)
            FileOutputStream fos = new FileOutputStream(getFilesDir() + "/audio_recording.wav", true); // true eklersek dosyaya ekleme yapar
            fos.write(audioData); // Ses verisini dosyaya yaz
            fos.close();
            Log.d("AudioStreamer", "Data saved to file");
        } catch (IOException e) {
            Log.e("AudioStreamer", "Error saving data", e);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.stop(); // Kaydı durdur
            recorder.release(); // Kaydı serbest bırak
            Log.d("AudioStreamer", "Recording stopped");
        }
    }
}