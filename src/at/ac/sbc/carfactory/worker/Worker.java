package at.ac.sbc.carfactory.worker;

public abstract class Worker {

	private long id;
	
	public Worker(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
