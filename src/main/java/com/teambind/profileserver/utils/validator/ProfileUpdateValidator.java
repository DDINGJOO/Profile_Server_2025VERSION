package com.teambind.profileserver.utils.validator;

import com.teambind.profileserver.exceptions.ProfileException;

import java.util.Map;

public interface ProfileUpdateValidator
{
    void validateProfileUpdateRequest(String Nickname, Map<Integer,String> genreMap, Map<Integer, String> instrumentMap) throws ProfileException;
    boolean NicknameValidation(String nickname);
    boolean isGenreValidByIds(Map<Integer,String> genreMap) throws ProfileException;
    void isInstrumentValidByIds(Map<Integer, String> instrumentMap) throws ProfileException;
}
