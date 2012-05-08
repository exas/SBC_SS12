package at.ac.sbc.carfactory.xvms;

import at.ac.sbc.carfactory.ui.CarFactoryUI;
import at.ac.sbc.carfactory.ui.util.Model;
import at.ac.sbc.carfactory.ui.util.View;
import at.ac.sbc.carfactory.util.Controller;
import at.ac.sbc.carfactory.util.DomainListener;
import at.ac.sbc.carfactory.util.ICarFactoryManager;
import at.ac.sbc.carfactory.util.LogListener;
import at.ac.sbc.carfactory.xvms.application.CarFactoryManager;

public class CarFactory {

	public static void main(String[] args) {

		// TODO: start UI and initialize logic layer
		Model model = new CarFactoryManager();
		View view = new CarFactoryUI(model);
		
		((ICarFactoryManager)model).addLogListener((LogListener)view);
		((ICarFactoryManager)model).addDomainListener((DomainListener)view);
		
		
		@SuppressWarnings("unused")
		Controller controller = new Controller(model,view);
		
		view.setVisible(true);
		
		
		
	}
}
