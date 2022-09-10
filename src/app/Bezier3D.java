package app;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Bezier3D extends JPanel{
	private final int POINTSIZE = 20;
	private int x = 0;
	private int y = 0;
	private final int FIRST = 0;
	private final int LAST = 1;
	private boolean touched = false;
	private ArrayList<Point3D> plist;
	private MouseListener currentML;
	private boolean isSelect = true;
	private Combination combination = new Combination(0); // for combination calculation when checking the Bezier calculation
	
	
	public Bezier3D() {
		this.plist = new ArrayList<Point3D>();
		this.currentML = new MouseAdapter(){
			public void mousePressed(MouseEvent e) {
				touched = true;
				x = e.getX();
				y = e.getY();
				if(plist.size() > 0) {
					combination.increment();
				}
				plist.add(new Point3D(x,y,0));
				repaint(x- POINTSIZE/2,y- POINTSIZE/2,POINTSIZE,POINTSIZE);
				
			}
			
			};
			this.addMouseListener(this.currentML);
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		if(this.plist.size() == 0) {
			this.showDialog("set the first and the last control points");
		}
		if(isSelect) {
			// it is in the selection stage
			if(touched) {
				
				if(this.plist.size() < 3) {
					g2.setColor(Color.RED);
					g2.fillOval(this.x- POINTSIZE/2,this.y- POINTSIZE/2,POINTSIZE,POINTSIZE);
					if(this.plist.size() == 2) {
						this.showDialog("set other control points, press the finish button when you finish");
					}
				}else {
					g2.setColor(Color.BLUE);
					g2.fillOval(this.x - POINTSIZE/2,this.y- POINTSIZE/2,POINTSIZE,POINTSIZE);
				}
				touched = false;
			}
		}
		
	}
	
	private void showDialog(String message) {
		JDialog d =new JDialog(SwingUtilities.getWindowAncestor(this),"Tips");
		d.setLocationRelativeTo(null);
		JLabel l = new JLabel(message);
		d.add(l);
		d.pack();
		d.setVisible(true);
	}
	
	private Point3D bezierCal(double u, ArrayList<Point3D> ps) {
		double res_x = Math.pow(1-u, ps.size() - 1) * ps.get(FIRST).x;
		double res_y = Math.pow(1-u, ps.size() - 1) * ps.get(FIRST).y;
		double res_z = Math.pow(1-u, ps.size() - 1) * ps.get(FIRST).z;
		res_x += Math.pow(u, ps.size() - 1) * ps.get(LAST).x;
		res_y += Math.pow(u, ps.size() - 1) * ps.get(LAST).y;
		res_z += Math.pow(u, ps.size() - 1) * ps.get(LAST).z;
		
		for(int i = 2; i < this.plist.size(); i++) {
			res_x += this.combination.calculate(i - 1) * Math.pow(1-u, ps.size() -i) * Math.pow(u, i -1) * ps.get(i).x;
			res_y += this.combination.calculate(i - 1) * Math.pow(1-u, ps.size() -i) * Math.pow(u, i -1) * ps.get(i).y;
			res_z += this.combination.calculate(i - 1) * Math.pow(1-u, ps.size() -i) * Math.pow(u, i -1) * ps.get(i).z;
		}
		return new Point3D(res_x,res_y,res_z);
		
	}
}

class Point3D{
	double x;
	double y;
	double z;
	
	public Point3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
}