package com.teambind.profileserver.repository.dsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teambind.profileserver.entity.QUserGenres;
import com.teambind.profileserver.entity.QUserInfo;
import com.teambind.profileserver.entity.QUserInstruments;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.repository.ProfileSearchRepository;
import com.teambind.profileserver.repository.search.ProfileSearchCriteria;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ProfileSearchRepositoryImpl implements ProfileSearchRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    private static final QUserInfo ui = QUserInfo.userInfo;
    private static final QUserGenres ug = QUserGenres.userGenres;
    private static final QUserInstruments uins = QUserInstruments.userInstruments;

    @Override
    public UserInfo search(String userId) {
        UserInfo userInfo = queryFactory
                .selectFrom(ui)
                .where(ui.userId.eq(userId))
                .fetchOne();

        if (userInfo != null) {
            batchInitializeCollections(List.of(userInfo));
        }
        return userInfo;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserInfo> search(ProfileSearchCriteria criteria, Pageable pageable) {
        BooleanBuilder where = buildWhere(criteria);

        // 기본 페이징 조회 (카테시안 곱을 피하기 위해 컬렉션 fetch 조인을 생략)
        List<UserInfo> content = queryFactory
                .selectFrom(ui)
                .where(where)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(ui.createdAt.desc().nullsLast())
                .fetch();

        Long totalLong = queryFactory
                .select(ui.count())
                .from(ui)
                .where(where)
                .fetchOne();
        long total = totalLong == null ? 0L : totalLong;

        if (!content.isEmpty()) {
            batchInitializeCollections(content);
        }

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<UserInfo> searchByCursor(ProfileSearchCriteria criteria, String cursor, int size) {
        BooleanBuilder where = buildWhere(criteria);
        if (cursor != null && !cursor.isBlank()) {
            // userId 내림차순 정렬이므로 다음 페이지를 위해 userId < cursor 조건을 적용
            where.and(ui.userId.lt(cursor));
        }

        List<UserInfo> fetched = queryFactory
                .selectFrom(ui)
                .where(where)
                .orderBy(ui.userId.desc())
                .limit(size + 1L)
                .fetch();

        boolean hasNext = fetched.size() > size;
        List<UserInfo> content = hasNext ? fetched.subList(0, size) : fetched;

        if (!content.isEmpty()) {
            batchInitializeCollections(content);
        }

        return new SliceImpl<>(content, PageRequest.of(0, size), hasNext);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserInfo> searchByUserIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();
        // 단순 요약 필드만 필요하므로 컬렉션 초기화는 생략
        return queryFactory
                .selectFrom(ui)
                .where(ui.userId.in(userIds))
                .fetch();
    }

    private BooleanBuilder buildWhere(ProfileSearchCriteria criteria) {
        BooleanBuilder where = new BooleanBuilder();
        if (criteria == null) return where;

        if (criteria.getCity() != null && !criteria.getCity().isBlank()) {
            where.and(ui.city.eq(criteria.getCity()));
        }
        if (criteria.getSex() != null) {
            where.and(ui.sex.eq(criteria.getSex()));
        }
        if (criteria.getNickName() != null && !criteria.getNickName().isBlank()) {
            where.and(ui.nickname.containsIgnoreCase(criteria.getNickName()));
        }
        if (criteria.getGenres() != null && !criteria.getGenres().isEmpty()) {
            where.and(
                    JPAExpressions.selectOne()
                            .from(ug)
                            .where(ug.userInfo.eq(ui)
                                    .and(ug.genre.genreId.in(criteria.getGenres())))
                            .exists()
            );
        }
        if (criteria.getInstruments() != null && !criteria.getInstruments().isEmpty()) {
            where.and(
                    JPAExpressions.selectOne()
                            .from(uins)
                            .where(uins.userInfo.eq(ui)
                                    .and(uins.instrument.instrumentId.in(criteria.getInstruments())))
                            .exists()
            );
        }

        return where;
    }

    private void batchInitializeCollections(List<UserInfo> content) {
        List<String> userIds = content.stream().map(UserInfo::getUserId).collect(Collectors.toList());
        // 장르 컬렉션 초기화
        queryFactory
                .selectFrom(ui)
                .leftJoin(ui.userGenres, ug).fetchJoin()
                .leftJoin(ug.genre).fetchJoin()
                .where(ui.userId.in(userIds))
                .fetch();

        // 악기 컬렉션 초기화
        queryFactory
                .selectFrom(ui)
                .leftJoin(ui.userInstruments, uins).fetchJoin()
                .leftJoin(uins.instrument).fetchJoin()
                .where(ui.userId.in(userIds))
                .fetch();
    }
}
