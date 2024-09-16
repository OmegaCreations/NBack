import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File; /**
 * Class representing audio player
 */
public class AudioPlayer {

    /**
     * Plays audio file
     * @param filePath path to audio file
     */
    public static void playSound(String filePath) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new File(filePath)));
            clip.start();
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }
}
