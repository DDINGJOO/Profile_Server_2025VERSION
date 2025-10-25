package com.teambind.profileserver.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * UserGenres, UserInstruments 등 사용자 속성 Repository의 공통 인터페이스
 *
 * @param <T> UserAttributeBase를 상속한 엔티티 타입
 * @param <K> 복합키 타입
 */
@NoRepositoryBean
public interface UserAttributeRepository<T, K> {

  /**
   * 특정 사용자의 속성 ID 목록 조회
   *
   * @param userId 사용자 ID
   * @return 속성 ID 목록
   */
  List<Integer> findAttributeIdsByUserId(String userId);

  /**
   * 특정 사용자의 특정 속성들 삭제
   *
   * @param userId 사용자 ID
   * @param attributeIds 삭제할 속성 ID 목록
   * @return 삭제된 레코드 수
   */
  int deleteByUserIdAndAttributeIdsIn(String userId, Collection<Integer> attributeIds);

  /**
   * 특정 사용자의 모든 속성 삭제
   *
   * @param userId 사용자 ID
   * @return 삭제된 레코드 수
   */
  int deleteByUserId(String userId);
}
