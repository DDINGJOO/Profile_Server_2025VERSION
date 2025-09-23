package com.teambind.profileserver.utils.validator;

import java.util.Map;

public interface ProfileUpdateValidator
{
    void validateProfileUpdateRequest(String Nickname, Map<Integer,String> genreMap, Map<Integer, String> instrumentMap);
    boolean NicknameValidation(String nickname);
    boolean isGenreValidByIds(Map<Integer,String> genreMap);
    void isInstrumentValidByIds(Map<Integer, String> instrumentMap);
}
