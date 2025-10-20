package com.teambind.profileserver.service.update;


import com.teambind.profileserver.dto.request.HistoryUpdateRequest;
import com.teambind.profileserver.dto.request.ProfileUpdateRequest;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.exceptions.ErrorCode;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.repository.GenreNameTableRepository;
import com.teambind.profileserver.repository.InstrumentNameTableRepository;
import com.teambind.profileserver.repository.UserInfoRepository;
import com.teambind.profileserver.service.history.UserProfileHistoryService;
import com.teambind.profileserver.utils.InitTableMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileUpdateService {
    private final UserInfoRepository userInfoRepository;
    private final InstrumentNameTableRepository instrumentNameTableRepository;
    private final GenreNameTableRepository genreNameTableRepository;
    private final UserProfileHistoryService historyService;
	private final InitTableMapper initTableMapper;
	
	
	@Transactional
    public void UserProfileImageUpdate(String userId, String imageUrl)
    {
		UserInfo userInfo = getUserInfo(userId);
        userInfo.setProfileImageUrl(imageUrl);
    }
    /**
     * 프로필을 부분 업데이트합니다. (PATCH 방식)
     * request에서 null이 아닌 필드만 업데이트합니다.
     *
     * @param userId 사용자 ID
     * @param request 업데이트할 프로필 정보
     * @return 업데이트된 UserInfo
     */
    @Transactional
    public UserInfo updateProfile(String userId, ProfileUpdateRequest request) {
	    UserInfo userInfo = getUserInfo(userId);
		setNickname(userInfo, request.getNickname());
		setCommonAttribute(userInfo,request);
		setGenres(userInfo, request.getGenres());
		setInstruments(userInfo, request.getInstruments());
		userInfoRepository.save(userInfo);

        // 추가 조회와 불필요한 지연 로딩을 피하기 위해 영속 엔티티 반환
        return userInfo;
    }
	
    @Transactional
    public UserInfo updateProfileImage(String userId, String imageUrl)  {
	    UserInfo userInfo = getUserInfo(userId);
        userInfo.setProfileImageUrl(imageUrl);
        
		historyService.saveAllHistory(userInfo, new HistoryUpdateRequest[]{
                HistoryUpdateRequest.builder()
                        .columnName("profileImageUrl")
                        .oldValue(userInfo.getProfileImageUrl())
                        .newValue(imageUrl)
                        .build()
        });

        return userInfo;
    }
	
	
	
	private UserInfo getUserInfo(String userId){
		
		return userInfoRepository.findById(userId).orElseThrow(
				() -> new ProfileException(ErrorCode.USER_NOT_FOUND)
		);
	}
	private void setNickname(UserInfo userInfo, String nickname) {
		if (nickname != null && !nickname.equals(userInfo.getNickname())) {
			if (userInfoRepository.existsByNickname(nickname)) {
				throw new ProfileException(ErrorCode.NICKNAME_ALREADY_EXISTS);
			}
			
			historyService.saveAllHistory(userInfo, new HistoryUpdateRequest[]{
					HistoryUpdateRequest.builder()
							.columnName("nickname")
							.oldValue(userInfo.getNickname())
							.newValue(nickname)
							.build()
			});
			userInfo.setNickname(nickname);
		}
	}
	private void setCommonAttribute(UserInfo userInfo,ProfileUpdateRequest request) {
		// 기타 필드 업데이트 (null이 아닌 경우)
		if (request.getIntroduction() != null) {
			userInfo.setIntroduction(request.getIntroduction());
		}
		if (request.getCity() != null) {
			userInfo.setCity(request.getCity());
		}
		if (request.getSex() != null) {
			userInfo.setSex(request.getSex());
		}
		userInfo.setIsChatable(request.isChattable());
		userInfo.setIsPublic(request.isPublicProfile());
	}
	
	private void setGenres(UserInfo userInfo, List<Integer> genreIds) {
		if (genreIds == null) return;
		userInfo.clearGenres();
		for(Integer id : genreIds) {
			userInfo.addGenre(InitTableMapper.genreNameTable.get(id));
		}
	}
	private void setInstruments(UserInfo userInfo, List<Integer> instrumentIds) {
		if (instrumentIds == null) return;
		userInfo.clearInstruments();
		for(Integer id : instrumentIds) {
			userInfo.addInstrument(InitTableMapper.instrumentNameTable.get(id));
		}
	}
}
