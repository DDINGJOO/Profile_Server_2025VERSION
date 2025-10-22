package com.teambind.profileserver.validator;

import com.teambind.profileserver.utils.InitTableMapper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LocationValidator implements ConstraintValidator<Location, String> {
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return isValidLocation(value);
		
	}
	
	private boolean isValidLocation(String location) {
		return InitTableMapper.locationNamesTable.containsKey(location);
	}
}
