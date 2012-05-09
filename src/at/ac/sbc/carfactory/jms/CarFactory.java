/**
 * 
 */
package at.ac.sbc.carfactory.jms;

import at.ac.sbc.carfactory.ui.CarFactoryUI;
import at.ac.sbc.carfactory.ui.util.Model;
import at.ac.sbc.carfactory.ui.util.View;
import at.ac.sbc.carfactory.util.Controller;
import at.ac.sbc.carfactory.util.DomainListener;
import at.ac.sbc.carfactory.util.ICarFactoryManager;
import at.ac.sbc.carfactory.util.LogListener;
import at.ac.sbc.carfactory.jms.application.CarFactoryManager;
import at.ac.sbc.carfactory.jms.server.JMSServer;
import at.ac.sbc.carfactory.jms.server.JobManagementListener;

/**
 * @author exas
 *
 */
public class CarFactory {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Model model = new CarFactoryManager();
		View view = new CarFactoryUI(model);
		
		model.addLogListener((LogListener) view);
		model.addDomainListener((DomainListener)view);
		
		@SuppressWarnings("unused")
		Controller controller = new Controller(model,view);
		
		view.setVisible(true);
	}

}
