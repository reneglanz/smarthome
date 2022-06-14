package de.shd.device;

public interface PowerMeter extends Sensor<Integer> {

	public int currentPower();
	public float today();
	public float yesterday();
	public float total();
}
