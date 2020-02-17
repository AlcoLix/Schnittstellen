package Jira.utils;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyListener;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class GuiUtils {

	public static void resizeColumnWidth(JTable table) {
	    final TableColumnModel columnModel = table.getColumnModel();
	    for (int column = 0; column < table.getColumnCount(); column++) {
	        int width = 15; // Min width
	        for (int row = 0; row < table.getRowCount(); row++) {
	            TableCellRenderer renderer = table.getCellRenderer(row, column);
	            Component comp = table.prepareRenderer(renderer, row, column);
	            width = Math.max(comp.getPreferredSize().width +1 , width);
	        }
	        if(width > 300)
	            width=300;
	        columnModel.getColumn(column).setPreferredWidth(width);
	    }
	}
	public static void maximizeDialogOnOwner(Dialog d, int smallerByPixels){
		Window w = d.getOwner();
		d.setSize(w.getWidth()-smallerByPixels, w.getHeight()-smallerByPixels);
		centerDialogOnWindow(d, w);
	}
	public static void centerDialogOnWindow(Dialog d, Window w){
		Dimension masterDim = w.getSize();
		centerWindowOn(d, masterDim, new Point(0,0));
	}
	public static void centerWindowOnWindow(Window master, Window sub){
		Point p = master.getLocation();
		Dimension masterDim = master.getSize();
		centerWindowOn(sub, masterDim, p);
	}
	public static void centerWindowOnScreen(Window w){
		centerWindowOn(w, Toolkit.getDefaultToolkit().getScreenSize(), new Point(0,0));
	}
	private static void centerWindowOn(Window w, Dimension dim, Point p){
		Dimension subDim = w.getSize();
		Point target = new Point();
		target.x = p.x+(dim.width/2)-(subDim.width/2);
		target.y = p.y+(dim.height/2)-(subDim.height/2);
		w.setLocation(target);
	}
	public static void assignWindowBelowWindow(Window toAssign, Window stationary){
		int h = stationary.getHeight();
		h+= stationary.getY();
		toAssign.setLocation(stationary.getX(), h);
	}
	public static void assignWindowAdjacentWindow(Window toAssign, Window stationary){
		int w = stationary.getWidth();
		w+= stationary.getX();
		toAssign.setLocation(w, stationary.getY());
	}
	public static Window getTopLevelElement(Component c){
		Component comp = getTopLevelElementRecursive(c);
		if(comp instanceof Window){
			return (Window)comp; 
		}
		return null;
	}
	private static Component getTopLevelElementRecursive(Component c){
		Component comp = c.getParent();
		if(comp == null){
			return c;
		}
		return getTopLevelElementRecursive(comp);
	}
	public static Dialog getParentDialogElement(Component c) {
		Component comp = getParentDialogElementRecursive(c);
		if(comp instanceof Dialog){
			return (Dialog)comp; 
		}
		return null;
	}

	private static Component getParentDialogElementRecursive(Component c){
		Component comp = c.getParent();
		if(comp == null) {
			return null;
		}
		if(comp instanceof Dialog){
			return comp;
		}
		return getParentDialogElementRecursive(comp);
	}
	
	public static void setOpaqueRecursive(Component c, boolean b){
		if(c instanceof JComponent){
			((JComponent)c).setOpaque(b);
		}
		if(c instanceof Container){
			for (Component comp : ((Container)c).getComponents()) {
				setOpaqueRecursive(comp, b);
			}
		}
	}
	public static void addKeyListenerToWholeWindow(Component c, KeyListener listener){
		Window w = getTopLevelElement(c);
		addKeyListenerRecursive(w, listener);
	}
	private static void addKeyListenerRecursive(Component c, KeyListener listener){
		c.addKeyListener(listener);
		if(c instanceof Container){
			for (Component comp : ((Container)c).getComponents()) {
				addKeyListenerRecursive(comp, listener);
			}
		}
	}
	public static void removeKeyListenerFromWholeWindow(Component c, KeyListener listener){
		Window w = getTopLevelElement(c);
		removeKeyListenerRecursive(w, listener);
	}
	private static void removeKeyListenerRecursive(Component c, KeyListener listener){
		c.removeKeyListener(listener);
		if(c instanceof Container){
			for (Component comp : ((Container)c).getComponents()) {
				removeKeyListenerRecursive(comp, listener);
			}
		}
	}
}
