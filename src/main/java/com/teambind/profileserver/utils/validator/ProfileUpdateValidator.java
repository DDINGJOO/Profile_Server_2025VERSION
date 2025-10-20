package com.teambind.profileserver.utils.validator;

import com.teambind.profileserver.exceptions.ProfileException;

import java.util.List;

public interface ProfileUpdateValidator
{
    void validateProfileUpdateRequest(String Nickname, List<Integer> genreIds, List<Integer> instrumentIds) throws ProfileException;
    boolean NicknameValidation(String nickname);
    boolean isGenreValidByIds(List<Integer> genreIds) throws ProfileException;
    void isInstrumentValidByIds(List<Integer> instrumentIds) throws ProfileException;
}
