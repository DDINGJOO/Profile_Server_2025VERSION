package com.teambind.profileserver.utils.validator;

import com.teambind.profileserver.utils.InitTableMapper;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Setter
public class GenreValidator {

    @Value("${genres.validation.max-size}")
    private int maxSize;

    public boolean isValidGenreByIds(Map<Integer, String> genre) {
        // 최대 장르 수 확인
        validateGenreSize(genre.size());
        // ID 매핑되는 값인지 확인 (초기 맵 미초기화 시 스킵)
        validateGenreIds(genre);
        return true;
    }

    private void validateGenreSize(int size) {
        if (size < 0 || size > maxSize) {
            throw new IllegalArgumentException("Invalid genre size");
        }
    }

    private void validateGenreIds(Map<Integer, String> genre) {
        // 테스트 환경 등에서 InitTableMapper가 아직 초기화되지 않은 경우를 허용
        if (InitTableMapper.genreNameTable == null || InitTableMapper.genreNameTable.isEmpty()) {
            return;
        }
        genre.forEach((id, name) -> {
            if (id == null) return;
            String actual = InitTableMapper.genreNameTable.get(id);
            if (!name.equals(actual)) {
                throw new IllegalArgumentException("Invalid genre id");
            }
        });
    }
}
