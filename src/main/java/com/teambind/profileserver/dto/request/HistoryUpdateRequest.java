package com.teambind.profileserver.dto.request;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoryUpdateRequest {
    private String columnName;
    private String oldValue;
    private String newValue;
}
