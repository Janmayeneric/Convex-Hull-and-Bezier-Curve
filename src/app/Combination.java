package app;

import java.util.HashMap;

/**
 * this is a utililty class for the combination
 * for reducing complexity, the map of factorial is prepare before any calculation
 * @author 10130
 *
 */
public class Combination {
	private HashMap<Integer, Integer> map; // storing the factorial
	private int number;

	public Combination(int n) {
		map = new HashMap<Integer, Integer>();
		if(n == 0) {
			this.map.put(0 ,1);
			this.number = 0;
		}else {
			Combination last = new Combination(n - 1);
			this.map.putAll(last.getMap());
			this.map.put(n,n*this.map.get(n - 1));
			this.number = last.getNumber() + 1;
		}
	}
	
	public int calculate(int i) {
		return this.map.get(this.number)/(this.map.get(this.number - i) * this.map.get(i));
	}
	
	public HashMap<Integer, Integer> getMap(){
		return this.map;
	}
	
	public int getNumber() {
		return this.number;
	}
	
	/**
	 * increase by 1
	 */
	public void increment() {
		this.number ++;
		this.map.put(this.number, this.number * this.map.get(this.map.size() - 1));
	}
	
	
}
