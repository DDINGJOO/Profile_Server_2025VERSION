package com.teambind.profileserver.entity.attribute;

import static org.assertj.core.api.Assertions.assertThat;

import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.entity.attribute.base.UserAttributeBase;
import com.teambind.profileserver.entity.attribute.key.UserInstrumentKey;
import com.teambind.profileserver.entity.attribute.nameTable.InstrumentNameTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserInstrumentsTest {

  @Test
  @DisplayName("UserInstruments는 UserAttributeBase를 상속받는다")
  void userInstruments_extendsUserAttributeBase() {
    UserInstruments userInstruments = new UserInstruments();

    assertThat(userInstruments).isInstanceOf(UserAttributeBase.class);
  }

  @Test
  @DisplayName("getId 메서드는 복합키를 반환한다")
  void getId_returnsCompositeKey() {
    UserInstrumentKey key = new UserInstrumentKey("user123", 1);
    UserInstruments userInstruments = UserInstruments.builder().id(key).build();

    UserInstrumentKey result = userInstruments.getId();

    assertThat(result).isEqualTo(key);
  }

  @Test
  @DisplayName("setId 메서드는 복합키를 설정한다")
  void setId_setsCompositeKey() {
    UserInstruments userInstruments = new UserInstruments();
    UserInstrumentKey key = new UserInstrumentKey("user456", 2);

    userInstruments.setId(key);

    assertThat(userInstruments.getId()).isEqualTo(key);
  }

  @Test
  @DisplayName("getAttribute 메서드는 악기를 반환한다")
  void getAttribute_returnsInstrument() {
    InstrumentNameTable instrument =
        InstrumentNameTable.builder().instrumentId(1).instrumentName("Guitar").build();
    UserInstruments userInstruments = UserInstruments.builder().instrument(instrument).build();

    InstrumentNameTable result = userInstruments.getAttribute();

    assertThat(result).isEqualTo(instrument);
    assertThat(result.getInstrumentName()).isEqualTo("Guitar");
  }

  @Test
  @DisplayName("setAttribute 메서드는 악기를 설정한다")
  void setAttribute_setsInstrument() {
    UserInstruments userInstruments = new UserInstruments();
    InstrumentNameTable instrument =
        InstrumentNameTable.builder().instrumentId(2).instrumentName("Piano").build();

    userInstruments.setAttribute(instrument);

    assertThat(userInstruments.getInstrument()).isEqualTo(instrument);
    assertThat(userInstruments.getAttribute()).isEqualTo(instrument);
  }

  @Test
  @DisplayName("version 필드는 부모 클래스에서 상속된다")
  void version_isInheritedFromParent() {
    UserInstruments userInstruments = new UserInstruments();

    userInstruments.setVersion(5);

    assertThat(userInstruments.getVersion()).isEqualTo(5);
  }

  @Test
  @DisplayName("Builder를 통해 모든 필드를 설정할 수 있다")
  void builder_setsAllFields() {
    UserInstrumentKey key = new UserInstrumentKey("user789", 3);
    UserInfo userInfo = new UserInfo();
    userInfo.setUserId("user789");
    InstrumentNameTable instrument =
        InstrumentNameTable.builder().instrumentId(3).instrumentName("Drums").build();

    UserInstruments userInstruments =
        UserInstruments.builder().id(key).userInfo(userInfo).instrument(instrument).build();

    assertThat(userInstruments.getId()).isEqualTo(key);
    assertThat(userInstruments.getUserInfo()).isEqualTo(userInfo);
    assertThat(userInstruments.getInstrument()).isEqualTo(instrument);
    assertThat(userInstruments.getAttribute()).isEqualTo(instrument);
  }

  @Test
  @DisplayName("NoArgsConstructor를 통해 객체를 생성할 수 있다")
  void noArgsConstructor_createsInstance() {
    UserInstruments userInstruments = new UserInstruments();

    assertThat(userInstruments).isNotNull();
    assertThat(userInstruments.getId()).isNull();
    assertThat(userInstruments.getInstrument()).isNull();
  }

  @Test
  @DisplayName("AllArgsConstructor를 통해 모든 필드를 초기화할 수 있다")
  void allArgsConstructor_initializesAllFields() {
    UserInstrumentKey key = new UserInstrumentKey("user999", 4);
    UserInfo userInfo = new UserInfo();
    userInfo.setUserId("user999");
    InstrumentNameTable instrument =
        InstrumentNameTable.builder().instrumentId(4).instrumentName("Violin").build();

    UserInstruments userInstruments = new UserInstruments(key, userInfo, instrument);

    assertThat(userInstruments.getId()).isEqualTo(key);
    assertThat(userInstruments.getUserInfo()).isEqualTo(userInfo);
    assertThat(userInstruments.getInstrument()).isEqualTo(instrument);
  }
}
