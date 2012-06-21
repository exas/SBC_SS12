package at.ac.sbc.carfactory.util;

public enum TestCaseType {
	CHECK_ALL_PARTS, CHECK_DEFECT_PARTS;

	public TestCaseType getNext() {
		return values()[(ordinal() + 1) % values().length];
	}
}
