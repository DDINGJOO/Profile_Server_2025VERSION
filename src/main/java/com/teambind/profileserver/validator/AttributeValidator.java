package com.teambind.profileserver.validator;


import com.teambind.profileserver.exceptions.ProfileErrorCode;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.utils.InitTableMapper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;

public class AttributeValidator implements ConstraintValidator<Attribute, List<Integer>> {
	
	@Value("${genres.validation.max-size:3}")
	private int maxSize;
	
	private String flag; // @Attribute의 value를 담을 필드
	
	@Override
	public void initialize(Attribute annotation) {
		// 어노테이션에 지정한 value 값을 여기서 가져올 수 있음
		this.flag = annotation.value();
	}
	
	@Override
	public boolean isValid(List<Integer> value, ConstraintValidatorContext context) {
		if (value == null) return true; // 필요시 null 처리 정책
		// flag 값에 따라 분기 처리
		switch (flag) {
			case "GENRE":
				return isLessThanMaxSize(value) && isValidGenreIds(value);
			case "INTEREST":
				return isLessThanMaxSize(value) &&isValidInterestIds(value);
			default:
				return false;
		}
	}
	
	private boolean isLessThanMaxSize(List<Integer> attributeIds) {
		return attributeIds.size() <= maxSize;
	}
	
	private boolean isValidGenreIds(List<Integer> attributeIds) {
		if(attributeIds.isEmpty()) return true;
		for (Integer id : attributeIds) {
			if (id == null) continue;
			if (!InitTableMapper.genreNameTable.containsKey(id)) {
				throw new ProfileException(ProfileErrorCode.NOT_ALLOWED_INSTRUMENTS_ID_AND_NAME);
			}
		}
		return true;
	}
	
	private boolean isValidInterestIds(List<Integer> attributeIds) {
		if(attributeIds.isEmpty()) return true;
		for (Integer id : attributeIds) {
			if (id == null) continue;
			if (!InitTableMapper.instrumentNameTable.containsKey(id)) {
				throw new ProfileException(ProfileErrorCode.NOT_ALLOWED_INSTRUMENTS_ID_AND_NAME);
			}
		}
		return true;
	}
}
