import android.app.Activity;
import android.os.Bundle;
import android.media.AudioRecord;
import android.media.MediaRecorder;
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

        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        recorder = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        // Ses kaydetmeye başla
        startRecording();
    }

    private void startRecording() {
        if (recorder != null && recorder.getState() == AudioRecord.STATE_INITIALIZED) {
            recorder.startRecording();
            Log.d("AudioStreamerTV", "Recording started");

            // Ses verisini almak
            audioBuffer = new byte[bufferSize];
            while (recorder.read(audioBuffer, 0, audioBuffer.length) > 0) {
                writeDataToFile(audioBuffer);
            }
        }
    }

    private void writeDataToFile(byte[] audioData) {
        try {
            // Dosyayı belirli bir yolda oluşturuyoruz
            FileOutputStream fos = new FileOutputStream("/path/to/your/file.wav", true); // 'true' eklersek dosyaya ekleme yapar
            fos.write(audioData);
            fos.close();
            Log.d("AudioStreamerTV", "Data saved to file");
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