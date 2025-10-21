package com.teambind.profileserver.service.update;

import com.teambind.profileserver.dto.request.ProfileUpdateRequest;
import com.teambind.profileserver.entity.History;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.events.event.UserNickNameChangedEvent;
import com.teambind.profileserver.events.publisher.EventPublisher;
import com.teambind.profileserver.exceptions.ErrorCode;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.repository.UserInfoRepository;
import com.teambind.profileserver.utils.InitTableMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileUpdateService {
  private final UserInfoRepository userInfoRepository;
  private final EventPublisher eventPublisher;

  @Transactional
  public void UserProfileImageUpdate(String userId, String imageUrl) {
    UserInfo userInfo = getUserInfo(userId);
    userInfo.setProfileImageUrl(imageUrl);
  }

  /**
   * 프로필을 부분 업데이트합니다. (PATCH 방식) request에서 null이 아닌 필드만 업데이트합니다.
   *
   * @param userId 사용자 ID
   * @param request 업데이트할 프로필 정보
   * @return 업데이트된 UserInfo
   */
  @Transactional
  public void updateProfile(String userId, ProfileUpdateRequest request) {
    UserInfo userInfo = getUserInfo(userId);

    applyPatch(userInfo, request);

	//명시적 표기
    userInfoRepository.save(userInfo);
  }

  @Transactional
  public void updateProfileImage(String userId, String imageUrl) {
    UserInfo userInfo = getUserInfo(userId);
    userInfo.setProfileImageUrl(imageUrl);
    userInfo.addHistory(new History("profileImageUrl", userInfo.getProfileImageUrl(), imageUrl));
  }

  public boolean isNickNameExist(String nickname) {
    return userInfoRepository.existsByNickname(nickname);
  }

  private UserInfo getUserInfo(String userId) {

    return userInfoRepository
        .findById(userId)
        .orElseThrow(() -> new ProfileException(ErrorCode.USER_NOT_FOUND));
  }

  private void setNickname(UserInfo userInfo, String nickname) {
    if (nickname != null && !nickname.equals(userInfo.getNickname())) {
      if (userInfoRepository.existsByNickname(nickname)) {
        throw new ProfileException(ErrorCode.NICKNAME_ALREADY_EXISTS);
      }

      userInfo.getUserHistory().add(new History("nickname", userInfo.getNickname(), nickname));
      userInfo.setNickname(nickname);
	    eventPublisher.publish(
          new UserNickNameChangedEvent(userInfo.getUserId(), userInfo.getNickname()));
    }
  }

  private void applyPatch(UserInfo userInfo, ProfileUpdateRequest req) {
    // 닉네임
    setNickname(userInfo, req.getNickname());

    // 공통 스칼라 속성
    if (req.getIntroduction() != null) userInfo.setIntroduction(req.getIntroduction());
    if (req.getCity() != null) userInfo.setCity(req.getCity());
    if (req.getSex() != null) userInfo.setSex(req.getSex());
    userInfo.setIsChatable(req.isChattable());
    userInfo.setIsPublic(req.isPublicProfile());

    // 컬렉션 속성
    if (req.getGenres() != null) {
      userInfo.clearGenres();
      for (Integer id : req.getGenres()) {
        userInfo.addGenre(InitTableMapper.genreNameTable.get(id));
      }
    }
    if (req.getInstruments() != null) {
      userInfo.clearInstruments();
      for (Integer id : req.getInstruments()) {
        userInfo.addInstrument(InitTableMapper.instrumentNameTable.get(id));
      }
    }
  }
}
