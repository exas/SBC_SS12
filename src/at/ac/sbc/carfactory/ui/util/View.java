package at.ac.sbc.carfactory.ui.util;

import java.awt.event.ActionListener;

import javax.swing.JFrame;

import at.ac.sbc.carfactory.ui.ProducerPanel;

public abstract class View extends JFrame{

	private static final long serialVersionUID = 1L;
	
	public abstract void updateProducerPanel(Long id);

	public abstract void closeView();
	
	public abstract void addCreateProducerListener(ActionListener al);
	public abstract void addShowStatisticsListener(ActionListener al);
	public abstract void addCloseAppListener(ActionListener al);
	
	public abstract ProducerPanel getProducerPanel();

	public abstract Model getModel();
	
}
