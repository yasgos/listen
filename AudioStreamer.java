import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.camera2.*;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int AUDIO_REQUEST_CODE = 101;

    private AudioRecord recorder;
    private int bufferSize;
    private static final int SAMPLE_RATE = 44100;
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private byte[] audioBuffer;
    
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = findViewById(R.id.surfaceView);

        // Kamera ve mikrofon izinlerini kontrol et
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            startCamera();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_REQUEST_CODE);
        } else {
            startRecording();
        }
    }

    // Kamera izni sonucu gelen yanıtı kontrol et
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Log.e("MainActivity", "Camera permission denied");
            }
        }

        if (requestCode == AUDIO_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Log.e("MainActivity", "Audio permission denied");
            }
        }
    }

    // Kamera başlatma işlemi
    private void startCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);

            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    cameraDevice = camera;
                    startPreview(camera);
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                    cameraDevice.close();
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    cameraDevice.close();
                }
            }, new Handler(Looper.getMainLooper()));
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview(CameraDevice camera) {
        try {
            Surface surface = surfaceView.getHolder().getSurface();
            CaptureRequest.Builder captureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            camera.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    captureSession = session;
                    try {
                        CaptureRequest captureRequest = captureRequestBuilder.build();
                        captureSession.setRepeatingRequest(captureRequest, null, new Handler(Looper.getMainLooper()));
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.e("MainActivity", "Configuration failed");
                }
            }, new Handler(Looper.getMainLooper()));
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // Ses kaydını başlat
    private void startRecording() {
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        recorder = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        if (recorder != null && recorder.getState() == AudioRecord.STATE_INITIALIZED) {
            recorder.startRecording();
            Log.d("MainActivity", "Recording started");

            audioBuffer = new byte[bufferSize];
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (recorder.read(audioBuffer, 0, audioBuffer.length) > 0) {
                        writeDataToFile(audioBuffer);
                    }
                }
            }).start();
        }
    }

    // Ses verisini dosyaya yaz
    private void writeDataToFile(byte[] audioData) {
        try {
            FileOutputStream fos = new FileOutputStream(getFilesDir() + "/audio_recording.wav", true);
            fos.write(audioData);
            fos.close();
            Log.d("MainActivity", "Data saved to file");
        } catch (IOException e) {
            Log.e("MainActivity", "Error saving data", e);
        }
    }

    // Kaydı durdur
    @Override
    protected void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            Log.d("MainActivity", "Recording stopped");
        }

        if (cameraDevice != null) {
            cameraDevice.close();
        }
    }
}
