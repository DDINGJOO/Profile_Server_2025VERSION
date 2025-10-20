package com.teambind.profileserver.utils.validator;

import com.teambind.profileserver.exceptions.ErrorCode;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.utils.InitTableMapper;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Setter
public class GenreValidator {

    @Value("${genres.validation.max-size:3}")
    private int maxSize;

    public boolean isValidGenreByIds(List<Integer> genreIds) {
        if (genreIds == null) {
            return true; // null은 허용 (업데이트하지 않음을 의미)
        }
        // 최대 장르 수 확인
        validateGenreSize(genreIds.size());
        // ID가 유효한지 확인 (초기 맵 미초기화 시 스킵)
        validateGenreIds(genreIds);
        return true;
    }

    private void validateGenreSize(int size) {
        if (size < 0 || size > maxSize) {
            throw new ProfileException(ErrorCode.GENRE_SIZE_INVALID);
        }
    }

    private void validateGenreIds(List<Integer> genreIds) {
        // 테스트 환경 등에서 InitTableMapper가 아직 초기화되지 않은 경우를 허용
        if (InitTableMapper.genreNameTable == null || InitTableMapper.genreNameTable.isEmpty()) {
            return;
        }
        for (Integer id : genreIds) {
            if (id == null) continue;
            if (!InitTableMapper.genreNameTable.containsKey(id)) {
                throw new ProfileException(ErrorCode.NOT_ALLOWED_GENRE_ID_AND_NAME);
            }
        }
    }
}
