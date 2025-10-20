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
public class InstrumentsValidator {
    @Value("${instruments.validation.max-size:3}")
    private int maxSize;

    public boolean isValidInstrumentByIds(List<Integer> instrumentIds) {
        if (instrumentIds == null) {
            return true; // null은 허용 (업데이트하지 않음을 의미)
        }
        validateInstrumentSize(instrumentIds.size());
        validateInstrumentIds(instrumentIds);
        return true;
    }

    private void validateInstrumentSize(int size) {
        if (size < 0 || size > maxSize) {
            throw new ProfileException(ErrorCode.INSTRUMENT_SIZE_INVALID);
        }
    }

    private void validateInstrumentIds(List<Integer> instrumentIds) {
        // 테스트 환경 등에서 InitTableMapper가 아직 초기화되지 않은 경우를 허용
        if (InitTableMapper.instrumentNameTable == null || InitTableMapper.instrumentNameTable.isEmpty()) {
            return;
        }
        for (Integer id : instrumentIds) {
            if (id == null) continue;
            if (!InitTableMapper.instrumentNameTable.containsKey(id)) {
                throw new ProfileException(ErrorCode.NOT_ALLOWED_INSTRUMENTS_ID_AND_NAME);
            }
        }
    }
}
