package at.ac.sbc.carfactory.xvms.util;

public class CarFactoryException extends Exception {

	private static final long serialVersionUID = -5926408904014959508L;
	
	public CarFactoryException() {
		super();
	}
	
	public CarFactoryException(String errorMessage) {
		super(errorMessage);
	}

}
