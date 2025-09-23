package com.teambind.profileserver.utils.validator.impl;

import com.teambind.profileserver.utils.validator.GenreValidator;
import com.teambind.profileserver.utils.validator.InstrumentsValidator;
import com.teambind.profileserver.utils.validator.NickNameValidator;
import com.teambind.profileserver.utils.validator.ProfileUpdateValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
@RequiredArgsConstructor
public class ProfileUpdateValidatorImpl implements ProfileUpdateValidator {
    private final GenreValidator genreValidator;
    private final InstrumentsValidator instrumentValidator;
    private final NickNameValidator nickNameValidator;


    @Override
    public void validateProfileUpdateRequest(String Nickname, Map<Integer,String> genreMap, Map<Integer, String> instrumentMap) {
        if(!NicknameValidation(Nickname)) throw new IllegalArgumentException("Invalid nickname");
        if(!isGenreValidByIds(genreMap)) throw new IllegalArgumentException("Invalid genre ids");
        isInstrumentValidByIds(instrumentMap);
    }

    @Override
    public boolean NicknameValidation(String nickname) {
        return nickNameValidator.isValidNickName(nickname);
    }

    @Override
    public boolean isGenreValidByIds(Map<Integer,String> genreMaps) {
        return genreValidator.isValidGenreByIds(genreMaps);
    }

    @Override
    public void isInstrumentValidByIds(Map<Integer,String> instrumentMaps) {
        instrumentValidator.isValidInstrumentByIds(instrumentMaps);
    }
}
