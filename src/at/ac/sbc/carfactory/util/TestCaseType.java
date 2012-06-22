package at.ac.sbc.carfactory.util;

public enum TestCaseType {
	CHECK_ALL_PARTS(0),
	CHECK_DEFECT_PARTS(1);

	public TestCaseType getNext() {
		return values()[(ordinal() + 1) % values().length];
	}

	private int value;

	private TestCaseType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static TestCaseType getEnumByValue(int value) {
		for(TestCaseType testCaseType : TestCaseType.values()) {
			if(testCaseType.getValue() == value) {
				return testCaseType;
			}
		}
		return null;
	}
}
