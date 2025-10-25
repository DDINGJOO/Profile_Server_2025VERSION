package com.teambind.profileserver.utils.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.teambind.profileserver.dto.request.ProfileUpdateRequest;
import com.teambind.profileserver.dto.response.UserResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * JsonUtilWithObjectMapper 단위 테스트
 *
 * <p>테스트 전략: 1. 단위 테스트로 JSON 직렬화/역직렬화 검증 2. 다양한 DTO 타입 테스트 3. 예외 상황 테스트 4. null 처리 테스트
 */
@DisplayName("JsonUtilWithObjectMapper 단위 테스트")
class JsonUtilWithObjectMapperTest {

  private JsonUtilWithObjectMapper jsonUtil;

  @BeforeEach
  void setUp() {
    jsonUtil = new JsonUtilWithObjectMapper();
  }

  @Nested
  @DisplayName("toJson() - 객체를 JSON 문자열로 변환")
  class ToJsonTests {

    @Test
    @DisplayName("성공 - ProfileUpdateRequest를 JSON으로 변환")
    void toJson_ProfileUpdateRequest_Success() {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("testuser")
              .city("서울")
              .introduction("안녕하세요")
              .chattable(true)
              .publicProfile(false)
              .sex('M')
              .genres(List.of(1, 2, 3))
              .instruments(List.of(1, 2))
              .build();

      // when
      String json = jsonUtil.toJson(request);

      // then
      assertThat(json).isNotNull();
      assertThat(json).contains("testuser");
      assertThat(json).contains("서울");
      assertThat(json).contains("안녕하세요");
      assertThat(json).contains("\"chattable\":true");
      assertThat(json).contains("\"publicProfile\":false");
    }

    @Test
    @DisplayName("성공 - UserResponse를 JSON으로 변환")
    void toJson_UserResponse_Success() {
      // given
      UserResponse response =
          UserResponse.builder()
              .userId("user123")
              .nickname("testnick")
              .city("부산")
              .introduction("테스트")
              .sex('F')
              .isChattable(true)
              .isPublic(true)
              .profileImageUrl("https://example.com/image.jpg")
              .genres(List.of("Rock", "Jazz"))
              .instruments(List.of("Guitar", "Piano"))
              .build();

      // when
      String json = jsonUtil.toJson(response);

      // then
      assertThat(json).isNotNull();
      assertThat(json).contains("user123");
      assertThat(json).contains("testnick");
      assertThat(json).contains("부산");
      assertThat(json).contains("Rock");
      assertThat(json).contains("Guitar");
    }

    @Test
    @DisplayName("성공 - 빈 객체를 JSON으로 변환")
    void toJson_EmptyObject_Success() {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder().chattable(false).publicProfile(false).build();

      // when
      String json = jsonUtil.toJson(request);

      // then
      assertThat(json).isNotNull();
      assertThat(json).contains("\"chattable\":false");
      assertThat(json).contains("\"publicProfile\":false");
    }

    @Test
    @DisplayName("성공 - List를 JSON으로 변환")
    void toJson_List_Success() {
      // given
      List<String> list = List.of("item1", "item2", "item3");

      // when
      String json = jsonUtil.toJson(list);

      // then
      assertThat(json).isNotNull();
      assertThat(json).contains("item1");
      assertThat(json).contains("item2");
      assertThat(json).contains("item3");
      assertThat(json).startsWith("[");
      assertThat(json).endsWith("]");
    }

    @Test
    @DisplayName("성공 - null 필드를 가진 객체를 JSON으로 변환")
    void toJson_ObjectWithNullFields_Success() {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("testuser")
              .city(null)
              .introduction(null)
              .chattable(false)
              .publicProfile(false)
              .sex(null)
              .genres(null)
              .instruments(null)
              .build();

      // when
      String json = jsonUtil.toJson(request);

      // then
      assertThat(json).isNotNull();
      assertThat(json).contains("testuser");
      assertThat(json).contains("null");
    }

    @Test
    @DisplayName("성공 - 중첩 객체를 JSON으로 변환")
    void toJson_NestedObject_Success() {
      // given
      UserResponse response =
          UserResponse.builder()
              .userId("user123")
              .genres(List.of("Rock", "Jazz", "Classical"))
              .instruments(List.of("Guitar"))
              .isChattable(true)
              .isPublic(true)
              .build();

      // when
      String json = jsonUtil.toJson(response);

      // then
      assertThat(json).isNotNull();
      assertThat(json).contains("genres");
      assertThat(json).contains("Rock");
      assertThat(json).contains("instruments");
      assertThat(json).contains("Guitar");
    }
  }

  @Nested
  @DisplayName("fromJson() - JSON 문자열을 객체로 변환")
  class FromJsonTests {

    @Test
    @DisplayName("성공 - JSON을 ProfileUpdateRequest로 변환")
    void fromJson_ToProfileUpdateRequest_Success() {
      // given
      String json =
          """
                {
                    "nickname": "testuser",
                    "city": "서울",
                    "introduction": "안녕하세요",
                    "chattable": true,
                    "publicProfile": false,
                    "sex": "M",
                    "genres": [1, 2, 3],
                    "instruments": [1, 2]
                }
                """;

      // when
      ProfileUpdateRequest request = jsonUtil.fromJson(json, ProfileUpdateRequest.class);

      // then
      assertThat(request).isNotNull();
      assertThat(request.getNickname()).isEqualTo("testuser");
      assertThat(request.getCity()).isEqualTo("서울");
      assertThat(request.getIntroduction()).isEqualTo("안녕하세요");
      assertThat(request.isChattable()).isTrue();
      assertThat(request.isPublicProfile()).isFalse();
      assertThat(request.getSex()).isEqualTo('M');
      assertThat(request.getGenres()).containsExactly(1, 2, 3);
      assertThat(request.getInstruments()).containsExactly(1, 2);
    }

    @Test
    @DisplayName("성공 - JSON을 UserResponse로 변환")
    void fromJson_ToUserResponse_Success() {
      // given
      String json =
          """
                {
                    "userId": "user123",
                    "nickname": "testnick",
                    "city": "부산",
                    "introduction": "테스트",
                    "sex": "F",
                    "isChattable": true,
                    "isPublic": true,
                    "profileImageUrl": "https://example.com/image.jpg",
                    "genres": ["Rock", "Jazz"],
                    "instruments": ["Guitar", "Piano"]
                }
                """;

      // when
      UserResponse response = jsonUtil.fromJson(json, UserResponse.class);

      // then
      assertThat(response).isNotNull();
      assertThat(response.getUserId()).isEqualTo("user123");
      assertThat(response.getNickname()).isEqualTo("testnick");
      assertThat(response.getCity()).isEqualTo("부산");
      assertThat(response.getIntroduction()).isEqualTo("테스트");
      assertThat(response.getSex()).isEqualTo('F');
      assertThat(response.getIsChattable()).isTrue();
      assertThat(response.getIsPublic()).isTrue();
      assertThat(response.getProfileImageUrl()).isEqualTo("https://example.com/image.jpg");
      assertThat(response.getGenres()).containsExactly("Rock", "Jazz");
      assertThat(response.getInstruments()).containsExactly("Guitar", "Piano");
    }

    @Test
    @DisplayName("성공 - null 필드가 있는 JSON을 객체로 변환")
    void fromJson_WithNullFields_Success() {
      // given
      String json =
          """
                {
                    "nickname": "testuser",
                    "city": null,
                    "introduction": null,
                    "chattable": false,
                    "publicProfile": false,
                    "sex": null,
                    "genres": null,
                    "instruments": null
                }
                """;

      // when
      ProfileUpdateRequest request = jsonUtil.fromJson(json, ProfileUpdateRequest.class);

      // then
      assertThat(request).isNotNull();
      assertThat(request.getNickname()).isEqualTo("testuser");
      assertThat(request.getCity()).isNull();
      assertThat(request.getIntroduction()).isNull();
      assertThat(request.isChattable()).isFalse();
      assertThat(request.isPublicProfile()).isFalse();
      assertThat(request.getSex()).isNull();
      assertThat(request.getGenres()).isNull();
      assertThat(request.getInstruments()).isNull();
    }

    @Test
    @DisplayName("성공 - 최소 필드만 있는 JSON을 객체로 변환")
    void fromJson_MinimalFields_Success() {
      // given
      String json =
          """
                {
                    "chattable": false,
                    "publicProfile": false
                }
                """;

      // when
      ProfileUpdateRequest request = jsonUtil.fromJson(json, ProfileUpdateRequest.class);

      // then
      assertThat(request).isNotNull();
      assertThat(request.isChattable()).isFalse();
      assertThat(request.isPublicProfile()).isFalse();
    }

    @Test
    @DisplayName("실패 - 잘못된 JSON 형식")
    void fromJson_InvalidJson_ThrowsException() {
      // given
      String invalidJson = "{ invalid json }";

      // when & then
      assertThatThrownBy(() -> jsonUtil.fromJson(invalidJson, ProfileUpdateRequest.class))
          .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("실패 - 빈 문자열")
    void fromJson_EmptyString_ThrowsException() {
      // given
      String emptyJson = "";

      // when & then
      assertThatThrownBy(() -> jsonUtil.fromJson(emptyJson, ProfileUpdateRequest.class))
          .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("실패 - null JSON 문자열")
    void fromJson_NullString_ThrowsException() {
      // given
      String nullJson = null;

      // when & then
      assertThatThrownBy(() -> jsonUtil.fromJson(nullJson, ProfileUpdateRequest.class))
          .isInstanceOf(RuntimeException.class);
    }
  }

  @Nested
  @DisplayName("직렬화/역직렬화 왕복 테스트")
  class RoundTripTests {

    @Test
    @DisplayName("성공 - ProfileUpdateRequest 왕복 변환")
    void roundTrip_ProfileUpdateRequest_Success() {
      // given
      ProfileUpdateRequest original =
          ProfileUpdateRequest.builder()
              .nickname("testuser")
              .city("서울")
              .introduction("안녕하세요")
              .chattable(true)
              .publicProfile(false)
              .sex('M')
              .genres(List.of(1, 2, 3))
              .instruments(List.of(1, 2))
              .build();

      // when
      String json = jsonUtil.toJson(original);
      ProfileUpdateRequest result = jsonUtil.fromJson(json, ProfileUpdateRequest.class);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getNickname()).isEqualTo(original.getNickname());
      assertThat(result.getCity()).isEqualTo(original.getCity());
      assertThat(result.getIntroduction()).isEqualTo(original.getIntroduction());
      assertThat(result.isChattable()).isEqualTo(original.isChattable());
      assertThat(result.isPublicProfile()).isEqualTo(original.isPublicProfile());
      assertThat(result.getSex()).isEqualTo(original.getSex());
      assertThat(result.getGenres()).isEqualTo(original.getGenres());
      assertThat(result.getInstruments()).isEqualTo(original.getInstruments());
    }

    @Test
    @DisplayName("성공 - UserResponse 왕복 변환")
    void roundTrip_UserResponse_Success() {
      // given
      UserResponse original =
          UserResponse.builder()
              .userId("user123")
              .nickname("testnick")
              .city("부산")
              .introduction("테스트")
              .sex('F')
              .isChattable(true)
              .isPublic(true)
              .profileImageUrl("https://example.com/image.jpg")
              .genres(List.of("Rock", "Jazz"))
              .instruments(List.of("Guitar", "Piano"))
              .build();

      // when
      String json = jsonUtil.toJson(original);
      UserResponse result = jsonUtil.fromJson(json, UserResponse.class);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getUserId()).isEqualTo(original.getUserId());
      assertThat(result.getNickname()).isEqualTo(original.getNickname());
      assertThat(result.getCity()).isEqualTo(original.getCity());
      assertThat(result.getIntroduction()).isEqualTo(original.getIntroduction());
      assertThat(result.getSex()).isEqualTo(original.getSex());
      assertThat(result.getIsChattable()).isEqualTo(original.getIsChattable());
      assertThat(result.getIsPublic()).isEqualTo(original.getIsPublic());
      assertThat(result.getProfileImageUrl()).isEqualTo(original.getProfileImageUrl());
      assertThat(result.getGenres()).isEqualTo(original.getGenres());
      assertThat(result.getInstruments()).isEqualTo(original.getInstruments());
    }
  }
}
