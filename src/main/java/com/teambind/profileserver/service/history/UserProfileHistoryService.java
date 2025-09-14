package com.teambind.profileserver.service.history;


import com.teambind.profileserver.dto.request.HistoryUpdateRequest;
import com.teambind.profileserver.entity.History;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileHistoryService {
    private final HistoryRepository historyRepository;

    public boolean saveHistory(UserInfo userInfo,HistoryUpdateRequest req) {
        History history = History.builder()
                .userInfo(userInfo)
                .fieldName(req.getColumnName())
                .oldVal(req.getOldValue())
                .newVal(req.getNewValue())
                .updatedAt(LocalDateTime.now())
                .build();
        historyRepository.save(history);
        return true;
    }

    public boolean saveAllHistory(UserInfo userInfo, HistoryUpdateRequest[] reqs)
        throws Exception {
        List<History> histories = Arrays.stream(reqs).collect(Collectors.toSet())
                .stream().map(req -> History.builder()
                        .userInfo(userInfo)
                        .fieldName(req.getColumnName())
                        .oldVal(req.getOldValue())
                        .newVal(req.getNewValue())
                        .updatedAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
        historyRepository.saveAll(histories);
        return true;
    }

}
