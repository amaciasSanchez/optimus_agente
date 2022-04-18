package claro.edx.optimus.processs.util;

import java.util.concurrent.TimeUnit;

public class Stopwatch {

    
	private long start = 0;

    public void start() {
        start = System.nanoTime();
    }
    public double elapsedTimeMiliseconds() {
        long now = System.nanoTime();
        return (now - start) / 1000000.0;
    }
    
    public Stopwatch() {
        start = System.nanoTime();
    }
    public double elapsedTime() {
        long now = System.nanoTime();
        return (now - start) / 1000000.0;
    }
    @SuppressWarnings("unused")
	public static void main(String[] args) {
    	try {
    		//--------SAMPLE -----------------
			Stopwatch timerGlobal = new Stopwatch();
			TimeUnit.SECONDS.sleep(1);
			TimeUnit.MILLISECONDS.sleep(7);
		} catch (InterruptedException e) {
		}
	}
}
