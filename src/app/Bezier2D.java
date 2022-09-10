package app;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;




public class Bezier2D extends JPanel{
	private final int FIRST = 0;
	private final int LAST = 1;
	private final int POINTSIZE = 20;
	private int x = 0;
	private int y = 0;
	
	private ArrayList<Point2D> plist;
	private ArrayList<Point2D> convexhull;
	private boolean touched = false;
	private boolean isSelect = true;
	private MouseListener currentML;

	// this if for the perturb function
	private HashMap<Integer, PerturbPoint> perturbPoints;
	
	private Combination combination = new Combination(0); // for combination calculation when checking the Bezier calculation
	
	public Bezier2D() {
		this.plist = new ArrayList<Point2D>();
		this.perturbPoints = new HashMap<Integer,PerturbPoint>();
		convexhull = new  ArrayList<Point2D>();
		this.currentML = new MouseAdapter(){
			public void mousePressed(MouseEvent e) {
				touched = true;
				x = e.getX();
				y = e.getY();
				if(plist.size() > 0) {
					combination.increment();
				}
				plist.add(new Point(x,y));
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
		}else {
			for(int i = 0; i < this.plist.size(); i++) {
					
				// first two point in the list is the starting point and the end point
				if(i < 2) {
					g.setColor(Color.RED);
					g.fillOval((int)this.plist.get(i).getX()- POINTSIZE/2,(int)this.plist.get(i).getY()- POINTSIZE/2,POINTSIZE,POINTSIZE);
				}else {
					g.setColor(Color.BLUE);
					g.fillOval((int)this.plist.get(i).getX()- POINTSIZE/2,(int)this.plist.get(i).getY()- POINTSIZE/2,POINTSIZE,POINTSIZE);
				}
			}
			// the drawing stage
			// draw the convex hull
			for(int i = 0 ; i < this.convexhull.size() -1; i ++) {
				g2.setColor(Color.BLACK);
				g2.draw(new Line2D.Double(this.convexhull.get(i).getX(),this.convexhull.get(i).getY(),this.convexhull.get(i+1).getX(),this.convexhull.get(i+1).getY()));
			}
				
				// enclose the convex hull
			g2.draw(new Line2D.Double(this.convexhull.get(0).getX(),this.convexhull.get(0).getY(),this.convexhull.get(this.convexhull.size() -1).getX(),this.convexhull.get(this.convexhull.size() -1 ).getY()));
				
				
			ArrayList<Point2D> bezierPoints = new ArrayList<Point2D>();
			g2.setColor(Color.GREEN);
			for(int i = 1; i < 101; i++) {
				bezierPoints.add(this.bezierCal(i/100.,this.plist));
			}
				
			for(int i = 0 ;i < bezierPoints.size() - 1; i++) {
				g2.draw(new Line2D.Double(bezierPoints.get(i), bezierPoints.get(i + 1)));
			}
				
				// if there is touch, plot the possible perturbing bezier curve
			if(touched) {
				// take account into the adjust level
				ArrayList<Point2D> adjust_p = new ArrayList<Point2D>();
				adjust_p.add(this.plist.get(this.FIRST));
				adjust_p.add(this.plist.get(this.LAST));
					
				// if the point is perturb, adjust it coordinate and put into new list
				for(int i = 2; i<this.plist.size();i++) {
					adjust_p.add(this.perturbPoints.get(i).perturb());
				}
				
					
				// plot bezier curve
				ArrayList<Point2D> new_bezierPoints = new ArrayList<Point2D>();
				g2.setColor(Color.ORANGE);
				for(int i = 1; i < 101; i++) {
					new_bezierPoints.add(this.bezierCal(i/100.,adjust_p));
				}
					
				for(int i = 0 ;i < new_bezierPoints.size() - 1; i++) {
					g2.draw(new Line2D.Double(new_bezierPoints.get(i), new_bezierPoints.get(i + 1)));
				}
				this.touched = false;
					
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
	
	
	
	// the function to get the convet hull
		private ArrayList<Point2D> getConvexHull(ArrayList<Point2D> points) {
			
			// find the start poin which is the leftmost bottom point first
			Point2D start = this.findLeftmostBottomPoint(points);
			
			// now use the Graham's scan to find the convex hull
			// i cannot find a appropriate name for it , basically it the map sorted by the angle of the vector of they and P
			// with the x axis, the key is the cosine
			// it is descending order, need to use in reverse order
			Point2D[] process = this.secondStep(start, points);
			
			ArrayList<Point2D> res = new ArrayList<Point2D>();
			res.add(start);
			
			// start from the third point, since we need three point
			for(int i = process.length - 1; i > -1 ; i--) {
				
				// if stack size lower than 2, add this one and start next loop
				if(res.size() < 2) {
					res.add(process[i]);
					continue;
				}
				// check p1 p2 p3 is turning left or not
				// if it is,store p3 into the stack
				if(this.isTurnLeft(res.get(res.size() -2), res.get(res.size() - 1), process[i])) {
					res.add(process[i]);
				}else {
					
					// if not, delete the p2 from the stack, but do not store this one
					// add the counter by one, because still need to check this one in next round
					res.remove(res.size() - 1);
					i++;
				}
			}
			
			// now check the final point
			if(!this.isTurnLeft(res.get(res.size() -2), res.get(res.size() - 1), res.get(0))) {
				res.remove(res.size() - 1);
			}
			return res;
		
		}
		
		
		private Point2D findLeftmostBottomPoint(ArrayList<Point2D> points) {
			// we want to find a start point a point that is the bottom leftmost point among all points
			ArrayList<Point2D> bottom_points = new ArrayList<Point2D>();
			int bottom_y = (int) points.get(0).getY();
			bottom_points.add(points.get(0));
			
			// now check the bottom point
			for(int i = 1 ; i < points.size(); i++) {
				if(bottom_y >= (int) points.get(i).getY()) {
					if(bottom_y > (int) points.get(i).getY()) {
						
						// if this point more closer to the bottom, reset the list of the bottom points and reset a temporary value
						bottom_points = new ArrayList<Point2D>();
						bottom_points.add(points.get(i));
						bottom_y = (int) points.get(0).getY();;
					}else {
						
						// or it is the point parallel to another bottom points
						bottom_points.add(points.get(i));
					}
				}
			}
			
			// now find the left most point
			Point2D start_p = bottom_points.get(0);
			int leftmost_x = (int)bottom_points.get(0).getX();
			
			for(Point2D point:bottom_points) {
				if(leftmost_x > point.getX()) {
					leftmost_x = (int)point.getX();
					start_p = point;
				}
			}
			return start_p;
		}
		
		/**
		 * i do not know how to name it
		 * it is the second step of the Graham scan
		 * use p as a reference, find the vector of it with other point and check their angle with x axis
		 * and sort it
		 * it return the point with the angle with the x axis in descending order
		 */
		private Point2D[] secondStep(Point2D p, ArrayList<Point2D> points) {
			// first find all the vector from p to other point 
			Point xaxis = new Point(1,0);
			
			// now find all the angle
			HashMap<Double,Point2D> hashmap = new HashMap<Double,Point2D>();
			for(Point2D point: points) {
				
				// no need to check the point itself
				if(point.equals(p)) {
					continue;
				}
				Point vector = new Point((int)(point.getX()-p.getX()),(int)(point.getY() - p.getY()));
				// get the angle between two vector, represent by cosine
				// round to 5 decimal places
				double cos = (int)(this.getVectorsCos(vector, xaxis)*100000)/100000.;
				
				// if there are two points and p on the same line
				// consider the one with further distance
				if(hashmap.containsKey(cos)) {
					Point2D vector2 = hashmap.get(cos);
					
					// store the one with the further distance
					if(this.getNormal(vector2) < this.getNormal(vector)) {
						hashmap.remove(cos);
						hashmap.put(cos, point);
					}// if not, not store it
				}else {
					hashmap.put(cos, point);
				}	
			}
			
			// then sort it by the angle
			// in the range of 0 - 180, which is the maximum in angle, cos is increase with decreasing angle and vice versa
			TreeMap<Double,Point2D> res = new TreeMap<Double,Point2D>(hashmap);
			
			return res.values().toArray(new Point2D[res.size()]);
			
		}
		
		// get the cosine between two vectors
		private double getVectorsCos(Point2D u, Point2D v) {
			return (u.getX() * v.getX() + u.getY()*v.getY())/ (this.getNormal(u) * this.getNormal(v));
		}
		
		private double getNormal(Point2D v) {
			return Math.sqrt(Math.pow(v.getX(),2)+Math.pow(v.getY(),2));
		}
		
		// part of the Graham scan, check if it turn left or right
		private boolean isTurnLeft(Point2D p1, Point2D p2, Point2D p3) {
			if(((p2.getX() - p1.getX()) * (p3.getY() - p1.getY()) - (p2.getY() - p1.getY()) * (p3.getX() - p1.getX()))>= 0) {
				return true;
			}
			return false;
		}
		
		
		// change the state from the changing control point to perturb the line
		public void finishSelect() {
			this.isSelect = false;

			this.convexhull = this.getConvexHull(plist);
			
			
			// the adjust_plist set to unadjust state
			this.perturbPoints.put(1, new PerturbPoint(this.plist.get(this.FIRST)));
			this.perturbPoints.put(this.plist.size(), new PerturbPoint(this.plist.get(this.LAST)));
			
			for(int i = 2; i < plist.size(); i++) {
				this.perturbPoints.put(i, new PerturbPoint(this.plist.get(i)));
			}
			
			
			// change the response the mouse action
			this.removeMouseListener(this.currentML);
			
			// again reset the mouse listener
			this.currentML = new MouseAdapter(){
				public void mousePressed(MouseEvent e) {
					
					int inputx = e.getX();
					int inputy = e.getY();
					
					for(int i = 2; i < plist.size(); i++) {
						// if touched perturb it, and quit
						// because only once
						if(isHit(plist.get(i), inputx, inputy)) {
							perturbPoints.put(i,perturb(perturbPoints.get(i - 1), perturbPoints.get(i),perturbPoints.get(i+1)) );
							touched = true;
							repaint();
							break;
						}
					}
				}
			};
			
			this.addMouseListener(this.currentML);
		}
		
		
		private Point2D bezierCal(double u, ArrayList<Point2D> ps) {
			double res_x = Math.pow(1-u, ps.size() - 1) * ps.get(FIRST).getX();
			double res_y = Math.pow(1-u, ps.size() - 1) * ps.get(FIRST).getY();
			res_x += Math.pow(u, ps.size() - 1) * ps.get(LAST).getX();
			res_y += Math.pow(u, ps.size() - 1) * ps.get(LAST).getY();
			
			for(int i = 2; i < this.plist.size(); i++) {
				res_x += this.combination.calculate(i - 1) * Math.pow(1-u, ps.size() -i) * Math.pow(u, i -1) * ps.get(i).getX();
				res_y += this.combination.calculate(i - 1) * Math.pow(1-u, ps.size() -i) * Math.pow(u, i -1) * ps.get(i).getY();
			}
			return new Point2D.Double(res_x,res_y);
			
		}
		
		/**
		 * the idea of the perturb is to reduce the degree
		 * for adjacent point A B C
		 * reduce B's distance to AC straight line
		 */
		private PerturbPoint perturb(PerturbPoint a, PerturbPoint b, PerturbPoint c) {
			if(!b.exist) {
				// find the line direction vector first
				Point2D ac = (Point2D)new Point2D.Double(c.point.getX() - a.point.getX(), c.point.getY() - a.point.getY());
				Point2D ba = (Point2D)new Point2D.Double(a.point.getX() - b.point.getX(), a.point.getY() - b.point.getY());
				Point2D bc = (Point2D)new Point2D.Double(c.point.getX() - b.point.getX(), c.point.getY() - b.point.getY());
		
				// then find the unit vector of each direction vector
				Point2D unit_ba = this.getUnitVector(ba);
				Point2D unit_bc = this.getUnitVector(bc);
			
				// get a point on ab name d, a point on bc name e
				// bde is isosceles triangle
				Point2D d = (Point2D)new Point2D.Double(b.point.getX()+unit_ba.getX(),b.point.getY()+unit_ba.getY());
				Point2D e = (Point2D)new Point2D.Double(b.point.getX()+unit_bc.getX(),b.point.getY()+unit_bc.getY());
			
				// get the middle point on de, name it f
				Point2D de = (Point2D)new Point2D.Double(e.getX() - d.getX(), e.getY() - d.getY());
				Point2D f = (Point2D)new Point2D.Double(d.getX() + 1/2* de.getX(), d.getY() + 1/2 * de.getY());
			
				// based on the property of the isosceles triangle, bf is the bisecting line
				Point2D bf = (Point2D)new Point2D.Double(f.getX() - b.point.getX(), f.getY() - b.point.getY());
				
				b.exist = true;
				
				// now find the distance from b to ac through the line bf
				double d_ba = this.getNorm(ba);
				double cos_bac = (-ba.getX() * ac.getX() - ba.getY()*ac.getY())/(d_ba*this.getNorm(ac));
				double sin_bac = Math.sqrt(1- Math.pow(cos_bac, 2));
						
				// bg is the perpendicular distance from b to ac
				double bg = sin_bac * d_ba;
				
				// h is the point which bf and ac intersect
				double cos_bhc = (-bf.getX() * ac.getX() - bf.getY()*ac.getY())/(d_ba*this.getNorm(ac));
				double sin_bhc = Math.sqrt(1- Math.pow(cos_bhc, 2));
				double l_bh = bg / sin_bhc; // finally we got this
				
				b.perturbVector = new Point2D.Double(bf.getX() * l_bh, bf.getY() * l_bh);
				}
			// finally............perturb the point
			b.increase();
			return b;
		}
		
		
		/**
		 * to check if this point is hit by the touching point
		 * @param p
		 * @return
		 */
		private boolean isHit(Point2D p , double x, double y) {
			return this.inRange(p.getX() + POINTSIZE, p.getX(), x) && this.inRange(p.getY() + this.POINTSIZE, p.getY(), y );
		}
		
		// small utility function like is top <= n <= bottom
		private boolean inRange(double top, double bottom, double n) {
			return top >= n && bottom <= n;
		}
		
		private Point2D getUnitVector(Point2D v) {
			double norm = this.getNorm(v);
			return ((Point2D)new Point2D.Double((1/norm)*v.getX(),(1/norm)*v.getY()));
		}
		
		private double getNorm(Point2D p) {
			return Math.sqrt(Math.pow(p.getX(), 2)+Math.pow(p.getY(), 2));
		}
}

class PerturbPoint{
	Point2D perturbVector;
	Point2D point;
	boolean exist;
	double coefficient;
	final double INCREASE = .1;
	
	public PerturbPoint(Point2D point) {
		this.coefficient = 0.;
		this.exist = false;
		this.point = point;
	}
	
	public void increase() {
		if(this.coefficient < 1) {
			this.coefficient += this.INCREASE;
		}
	}
	
	public Point2D perturb() {

		return exist? (Point2D)new Point2D.Double(
				point.getX()+ perturbVector.getX() * coefficient,
				point.getY()+ perturbVector.getY() * coefficient
				) :point;
	}
}

