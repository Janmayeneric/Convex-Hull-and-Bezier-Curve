package app;

import javax.swing.SwingUtilities;

import java.awt.CardLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MainFrame {
	
	static final int DRAWER  = 1 ;

	public static void main(String[] args) {
		
		// frame, the outer container setting
		JFrame frame = new JFrame("Menu");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000,1000);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		
		
		/**
		 * this is the main program
		 * just identify it as the thread
		 */
		CardLayout cards = new CardLayout();// manage the transition between the different panel
		JPanel p = new JPanel(cards);// this the main panel of the whole program
		
		/**
		 * it is the setting of the menu, it has the button lead to the different drawer
		 */
		JPanel menu = new JPanel();// menu panel , select 2D or 3D
		menu.setLayout(new BoxLayout(menu,BoxLayout.Y_AXIS)); // layout for the menu
		
		// button to 2D drawer
		JButton b_draw2d = new JButton("2DDrawer"); 
		b_draw2d.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cards.show(p,"controlPoint"); // select the drawer
				frame.setTitle("Bezier Curve 2D");
				//frame.repaint(); // every time of update need for repainting 
			}
		});
		
		// button to 2D drawer
		JButton b_draw3d = new JButton("3DDrawer"); 
		b_draw3d.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cards.show(p,"3d"); // select the drawer
				frame.setTitle("Bezier Curve 3D");
				//frame.repaint(); // every time of update need for repainting 
			}
		});
		menu.add(b_draw2d);
		menu.add(b_draw3d); // add that button to the menu
		p.add("menu",menu); // add the menu to the program
		
		
		
		
		/**
		 * this is the waiting page during the transition
		 */
		JPanel wait = new JPanel();
		JLabel wait_l = new JLabel("please wait .......");
		wait.add(wait_l);
		
		
		/**
		 * it is the drawer of the Bezier 2D graph
		 */
		JPanel controlPoint = new JPanel();
		// now the drawing panel for plotting the control points
		Bezier2D drawer = new Bezier2D();
		controlPoint.setLayout(new BorderLayout());
		// this is the top select bar
		JPanel select = new JPanel();
		select.setLayout(new BoxLayout(select,BoxLayout.X_AXIS)); // layout for the menu
		
		// button back to the menu
		JButton back = new JButton("Back to Menu");
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cards.show(p,"menu"); // select the drawer
				frame.setTitle("Menu");
				
				// if quit, remove the drawer and reset new one
				controlPoint.remove(DRAWER);
				controlPoint.add(new Bezier2D(), BorderLayout.CENTER);

				//frame.repaint(); // every time of update need for repainting 
			}
		});
		
		
		
		// when finish get the index from the drawpanel and get another panel to give the result
		JButton next = new JButton("draw");
		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cards.show(p,"wait");
				frame.setTitle("Loading");
				
				Bezier2D d = (Bezier2D)controlPoint.getComponent(DRAWER);
				controlPoint.remove(DRAWER);
				d.finishSelect();
				controlPoint.add(d, BorderLayout.CENTER);
				cards.show(p,"controlPoint");
			}
		});
		select.add(back); // add back button to the tool bar
		select.add(next);
		
		controlPoint.add(select, BorderLayout.NORTH); // add tool bar on the top of the control point interface
		controlPoint.add(new Bezier2D(), BorderLayout.CENTER); // the drawer is the main part of the program
		
		
		JPanel draw3d = new JPanel();
		draw3d.setLayout(new BorderLayout());
		Bezier3D drawer3d = new Bezier3D();
		JPanel select3d = new JPanel();
		select3d.setLayout(new BoxLayout(select3d,BoxLayout.X_AXIS)); // layout for the menu
		JButton back3d = new JButton("Back to Menu");
		back3d.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cards.show(p,"menu"); // select the drawer
				frame.setTitle("Menu");
				
				// if quit, remove the drawer and reset new one
				draw3d.remove(DRAWER);
				draw3d.add(new Bezier3D(), BorderLayout.CENTER);

				//frame.repaint(); // every time of update need for repainting 
			}
		});
		// when finish get the index from the drawpanel and get another panel to give the result
		JButton next3d = new JButton("next");
		next3d.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cards.show(p,"wait");
				frame.setTitle("Loading");
				
				Bezier3D d = (Bezier3D)draw3d.getComponent(DRAWER);
				draw3d.remove(DRAWER);
				//d.finishSelect();
				draw3d.add(d, BorderLayout.CENTER);
				cards.show(p,"3d");
			}
		});
		select3d.add(back3d); // add back button to the tool bar
		select3d.add(next3d);
		
		draw3d.add(select3d, BorderLayout.NORTH); // add tool bar on the top of the control point interface
		draw3d.add(new Bezier3D(), BorderLayout.CENTER); // the drawer is the main part of the program
		
		
		p.add("controlPoint",controlPoint); // add the control point interface to the program
		p.add("wait",wait);
		p.add("3d",draw3d);
		frame.add(p);
	}
	
}
