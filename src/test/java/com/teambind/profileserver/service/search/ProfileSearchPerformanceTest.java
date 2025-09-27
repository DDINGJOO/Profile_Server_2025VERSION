package com.teambind.profileserver.service.search;

import com.teambind.profileserver.dto.response.UserResponse;
import com.teambind.profileserver.factory.UserInfoFactory;
import com.teambind.profileserver.repository.search.ProfileSearchCriteria;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 페이징 기반 vs 커서 기반 검색 성능 비교 테스트
 * - 동일한 조건에서 선두 N건(예: 10,000건)을 조회하는 데 걸리는 시간을 비교합니다.
 * - 로그는 한국어로 출력되며, 절대 시간(ms)과 1천건당 처리 시간(ms/1k)을 함께 표시합니다.
 * - 대량 데이터는 UserInfoFactory.ensureUsersGenerated 로 필요 시에만 추가 생성합니다.
 */
@SpringBootTest(classes = com.teambind.profileserver.ProfileServerApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProfileSearchPerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(ProfileSearchPerformanceTest.class);

    @Autowired private ProfileSearchService profileSearchService;
    @Autowired private UserInfoFactory userInfoFactory;

    @BeforeAll
    void setup() {
        // 성능 비교를 위해 적어도 수만 건의 데이터가 필요합니다. 이미 있으면 건너뜀.
        int total = Integer.getInteger("profile.test.perf-users", 50_000);
        int batch = Integer.getInteger("profile.test.perf-batch", 2_000);
        log.info("[사전준비] 성능 테스트용 데이터 보장: total={}, batch={}", total, batch);
        userInfoFactory.ensureUsersGenerated(total, batch);
    }

    @Test
    @DisplayName("페이징 기반 vs 커서 기반 성능 비교 로그 출력")
    void comparePagingAndCursorPerformance() {
        // given: 필터가 거의 없는 기본 조회 조건 (모든 사용자 대상)
        ProfileSearchCriteria criteria = ProfileSearchCriteria.builder().build();

        int targetCount = Integer.getInteger("profile.test.perf-target", 10_000); // 비교를 위해 읽을 총 레코드 수
        int pageSize = Integer.getInteger("profile.test.perf-page-size", 200);

        log.info("[성능비교] 대상 레코드 수: {}, 페이지 사이즈: {}", targetCount, pageSize);

        // when-1: 페이징 기반 조회
        long startPage = System.nanoTime();
        int collected = 0;
        int pageIndex = 0;
        List<UserResponse> pageCollected = new ArrayList<>(targetCount);
        while (collected < targetCount) {
            Pageable pageable = PageRequest.of(pageIndex++, pageSize);
            Page<UserResponse> page = profileSearchService.searchProfiles(criteria, pageable);
            List<UserResponse> content = page.getContent();
            if (content.isEmpty()) break;
            for (UserResponse u : content) {
                pageCollected.add(u);
                if (++collected >= targetCount) break;
            }
            if (!page.hasNext()) break;
        }
        long endPage = System.nanoTime();
        long pageMs = (endPage - startPage) / 1_000_000L;
        double pagePer1k = targetCount > 0 ? (double) pageMs / (targetCount / 1000.0) : 0.0;
        String pagePer1kStr = String.format("%.2f", pagePer1k);

        log.info("[결과-페이징] 총 소요(ms): {}, 1천건당(ms): {}, 수집: {}", pageMs, pagePer1kStr, pageCollected.size());

        // when-2: 커서 기반 조회 (userId 기준 내림차순, repository 구현과 동일 가정)
        long startCursor = System.nanoTime();
        collected = 0;
        String cursor = null; // 최초 커서는 null
        List<UserResponse> cursorCollected = new ArrayList<>(targetCount);
        while (collected < targetCount) {
            Slice<UserResponse> slice = profileSearchService.searchProfilesByCursor(criteria, cursor, pageSize);
            List<UserResponse> content = slice.getContent();
            if (content.isEmpty()) break;
            for (UserResponse u : content) {
                cursorCollected.add(u);
                if (++collected >= targetCount) break;
            }
            // 다음 페이지 커서: 현재 페이지의 마지막 userId (내림차순 정렬)
            cursor = content.get(content.size() - 1).getUserId();
            if (!slice.hasNext()) break;
        }
        long endCursor = System.nanoTime();
        long cursorMs = (endCursor - startCursor) / 1_000_000L;
        double cursorPer1k = targetCount > 0 ? (double) cursorMs / (targetCount / 1000.0) : 0.0;
        String cursorPer1kStr = String.format("%.2f", cursorPer1k);

        log.info("[결과-커서]  총 소요(ms): {}, 1천건당(ms): {}, 수집: {}", cursorMs, cursorPer1kStr, cursorCollected.size());

        // then: 두 방식 모두 결과가 존재해야 하며, 비교 로그가 출력됨
        assertFalse(pageCollected.isEmpty(), "페이징 방식 결과가 최소 1건 이상이어야 합니다");
        assertFalse(cursorCollected.isEmpty(), "커서 방식 결과가 최소 1건 이상이어야 합니다");

        log.info("[요약] 페이징(ms)={}, 커서(ms)={}, 페이징 1천건당(ms)={}, 커서 1천건당(ms)={}",
                pageMs, cursorMs, pagePer1kStr, cursorPer1kStr);
        if (cursorMs < pageMs) {
            log.info("[관찰] 본 환경에서 커서 기반이 더 빠르게 관찰되었습니다 ({} ms < {} ms)", cursorMs, pageMs);
        } else if (cursorMs > pageMs) {
            log.info("[관찰] 본 환경에서 페이징 기반이 더 빠르게 관찰되었습니다 ({} ms < {} ms)", pageMs, cursorMs);
        } else {
            log.info("[관찰] 두 방식의 소요 시간이 유사하게 관찰되었습니다 ({} ms)", pageMs);
        }
    }
}
