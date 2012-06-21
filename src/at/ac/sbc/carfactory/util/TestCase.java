package at.ac.sbc.carfactory.util;

import java.util.List;

import at.ac.sbc.carfactory.domain.CarTire;

import at.ac.sbc.carfactory.domain.CarPart;

import at.ac.sbc.carfactory.jms.dto.CarPartDTO;

import at.ac.sbc.carfactory.domain.Car;

import at.ac.sbc.carfactory.jms.dto.CarDTO;

public class TestCase {

	private TestCaseType testCaseType;

	private boolean isTestOK;

	public TestCase(TestCaseType testCaseType) {
		this.setTestCaseType(testCaseType);
	}

	public TestCaseType getTestCaseType() {
		return testCaseType;
	}

	public void setTestCaseType(TestCaseType testCaseType) {
		this.testCaseType = testCaseType;
	}

	public boolean isTestOK() {
		return isTestOK;
	}

	public void setIsTestOK(boolean isTestOK) {
		this.isTestOK = isTestOK;
	}

	public boolean test(CarDTO carDTO) {
		// check first for which type of Test this TestCase is
		if (this.testCaseType.equals(TestCaseType.CHECK_ALL_PARTS)) {
			// check if all parts are here
			if (carDTO.carBody != null
					&& carDTO.carMotor != null
					&& carDTO.carTires.size() == 4
					&& carDTO.carBody.painterId != null
					&& carDTO.carBody.bodyColor != null) {
				isTestOK = true;
			} else {
				isTestOK = false;
			}
		} else if (this.testCaseType.equals(TestCaseType.CHECK_DEFECT_PARTS)) {
			// check if all parts are not defect
			if (!carDTO.carBody.isDefect && !carDTO.carMotor.isDefect
					&& !checkTiresOnDefect(carDTO.carTires)) {
				// all parts must be checked and if all are not defect test
				// succeeded
				isTestOK = true;
			} else {
				isTestOK = false;
			}
		}

		return isTestOK;
	}

	//true if test is OK and FALSE if testfailed!
	public boolean test(Car car) {
		// check first for which type of Test this TestCase is
		if (this.testCaseType.equals(TestCaseType.CHECK_ALL_PARTS)) {
			// check if all parts are here
			if (car.getBody() != null && car.getMotor() != null
					&& car.getTires().size() == 4 && car.getBody().getColor() != null && car.getPainterWorkerId() != null) {
				isTestOK = true;
			} else {
				isTestOK = false;
			}
		} else if (this.testCaseType.equals(TestCaseType.CHECK_DEFECT_PARTS)) {
			// check if all parts are not defect
			if (!car.getBody().isDefect() && !car.getMotor().isDefect()
					&& !checkTiresOnDefect(car.getTires())) {
				// all parts must be checked and if all are not defect test
				// succeeded
				isTestOK = true;
			} else {
				isTestOK = false;
			}
		}

		return isTestOK;
	}

	//for both carTireDTO and carTire objects
	private boolean checkTiresOnDefect(List<?> carTires) {
		boolean isDefect = false;

		for (Object object : carTires) {
			if(object instanceof CarTire) {
				if (((CarPart) object).isDefect()) {
					isDefect = true;
					break;
				}
			} else if(object instanceof CarPartDTO) {
				if (((CarPartDTO) object).isDefect()) {
					isDefect = true;
					break;
				}
			}
		}
		return isDefect;
	}
}
