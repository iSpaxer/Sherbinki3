package ru.stm.shcherbinki3.model.redis;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfo {

    private Long id;
    private String userAgent;
    private String device;
    private Instant createdAt;

}
