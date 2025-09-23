package com.teambind.profileserver.utils.validator;

import com.teambind.profileserver.exceptions.ErrorCode;
import com.teambind.profileserver.exceptions.ProfileException;
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

    public boolean isValidInstrumentByIds(Map<Integer, String> instrumentsMap) throws ProfileException {
        validateInstrumentSize(instrumentsMap.size());
        validateInstrumentIds(instrumentsMap);
        return true;
    }

    private void validateInstrumentSize(int size) throws ProfileException {
        if (size < 0 || size > maxSize) {
            throw new ProfileException(ErrorCode.INSTRUMENT_SIZE_INVALID);
        }
    }

    private void validateInstrumentIds(Map<Integer, String> instrumentsMap) throws ProfileException {
        // 테스트 환경 등에서 InitTableMapper가 아직 초기화되지 않은 경우를 허용
        if (InitTableMapper.instrumentNameTable == null || InitTableMapper.instrumentNameTable.isEmpty()) {
            return;
        }
        for (Map.Entry<Integer, String> entry : instrumentsMap.entrySet()) {
            Integer id = entry.getKey();
            String name = entry.getValue();
            if (id == null) continue;
            String actual = InitTableMapper.instrumentNameTable.get(id);
            if (!name.equals(actual)) {
                throw new ProfileException(ErrorCode.NOT_ALLOWED_INSTRUMENTS_ID_AND_NAME);
            }
        }
    }
}
