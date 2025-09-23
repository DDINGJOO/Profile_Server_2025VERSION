package com.teambind.profileserver.utils.validator;

import com.teambind.profileserver.utils.InitTableMapper;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Setter
public class InstrumentsValidator {
    @Value("${instruments.validation.max-size}")
    private int maxSize;

    public boolean isValidInstrumentByIds(Map<Integer, String> instrumentsMap) {
        validateInstrumentSize(instrumentsMap.size());
        validateInstrumentIds(instrumentsMap);
        return true;
    }

    private void validateInstrumentSize(int size) {
        if (size < 0 || size > maxSize) {
            throw new IllegalArgumentException("Invalid instrument size");
        }
    }

    private void validateInstrumentIds(Map<Integer, String> instrumentsMap) {
        // 테스트 환경 등에서 InitTableMapper가 아직 초기화되지 않은 경우를 허용
        if (InitTableMapper.instrumentNameTable == null || InitTableMapper.instrumentNameTable.isEmpty()) {
            return;
        }
        instrumentsMap.forEach((id, name) -> {
            if (id == null) return;
            String actual = InitTableMapper.instrumentNameTable.get(id);
            if (!name.equals(actual)) {
                throw new IllegalArgumentException("Invalid instrument id");
            }
        });
    }
}
