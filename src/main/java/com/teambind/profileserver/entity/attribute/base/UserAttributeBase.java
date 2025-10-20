package com.teambind.profileserver.entity.attribute.base;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class UserAttributeBase<K, T> {

	@Version
	@Column(name = "version")
	private int version;

	/**
	 * 복합키를 반환하는 추상 메서드
	 */
	public abstract K getId();

	/**
	 * 복합키를 설정하는 추상 메서드
	 */
	public abstract void setId(K id);

	/**
	 * 속성 값을 반환하는 추상 메서드 (장르 또는 악기)
	 */
	public abstract T getAttribute();

	/**
	 * 속성 값을 설정하는 추상 메서드
	 */
	public abstract void setAttribute(T attribute);
}
