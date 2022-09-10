package app;

import java.awt.Point;
import java.util.TreeMap;

public class test {
	public static void main(String[] args) {
		TreeMap<Integer,Integer> res = new TreeMap<Integer,Integer>();
		res.put(1, 1);
		res.put(2, 2);
		res.put(3,3);
		System.out.println(res.values());
	}
}
