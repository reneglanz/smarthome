package javazoom.jl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.ISupportMixer;
import javazoom.jl.player.Player;

public class Test {

	public static void main(String[] args) throws JavaLayerException, FileNotFoundException {
		if(args[0].equals("list")) {
			Info[] mixers = AudioSystem.getMixerInfo();
			for(Info info:mixers) {
				System.out.println(info.getName());
			}
		}
		
		AudioDevice device=FactoryRegistry.systemRegistry().createAudioDevice();
		if(device instanceof ISupportMixer) {
			Info[] mixers = AudioSystem.getMixerInfo();
			Mixer mixer=null;
			for(Info info:mixers) {
				if(info.getName().equals(args[0])) {
					mixer=AudioSystem.getMixer(info);
				}
			}
			if(mixer!=null) {
				System.out.println("found mixer");
				((ISupportMixer) device).setMixer(mixer);
			}
			
			Player player=new Player(new FileInputStream(args[1]),device);
			player.play();
		}
	}
}
