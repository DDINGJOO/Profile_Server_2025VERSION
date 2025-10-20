package com.teambind.profileserver.utils.validator.impl;

import com.teambind.profileserver.exceptions.ErrorCode;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.utils.validator.GenreValidator;
import com.teambind.profileserver.utils.validator.InstrumentsValidator;
import com.teambind.profileserver.utils.validator.NickNameValidator;
import com.teambind.profileserver.utils.validator.ProfileUpdateValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfileUpdateValidatorImpl implements ProfileUpdateValidator {
  private final GenreValidator genreValidator;
  private final InstrumentsValidator instrumentValidator;
  private final NickNameValidator nickNameValidator;

  @Override
  public void validateProfileUpdateRequest(
      String Nickname, List<Integer> genreIds, List<Integer> instrumentIds) {
    if (!NicknameValidation(Nickname)) throw new ProfileException(ErrorCode.NICKNAME_INVALID);
    if (!isGenreValidByIds(genreIds)) throw new ProfileException(ErrorCode.GENRE_INVALID);
    isInstrumentValidByIds(instrumentIds);
  }

  @Override
  public boolean NicknameValidation(String nickname) {
    return nickNameValidator.isValidNickName(nickname);
  }

  @Override
  public boolean isGenreValidByIds(List<Integer> genreIds) {
    return genreValidator.isValidGenreByIds(genreIds);
  }

  @Override
  public void isInstrumentValidByIds(List<Integer> instrumentIds) {
    instrumentValidator.isValidInstrumentByIds(instrumentIds);
  }
}
