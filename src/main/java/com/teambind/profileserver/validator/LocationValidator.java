package com.teambind.profileserver.validator;

import com.teambind.profileserver.exceptions.ProfileErrorCode;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.utils.InitTableMapper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LocationValidator implements ConstraintValidator<Location, String> {
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return isValidLocation(value);
		
	}
	
	private boolean isValidLocation(String location) {
		// null은 허용 (PATCH 요청에서 null은 변경하지 않음을 의미)
		if (location == null) {
			return true;
		}
		if(InitTableMapper.locationNamesTable.containsKey(location)){
			return true;
		};
		throw new ProfileException(ProfileErrorCode.NOT_ALLOWED_LOCATION_ID_AND_NAME);
	}
}
