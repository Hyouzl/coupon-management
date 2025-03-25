package com.traffic.couponconsumer.component;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traffic.couponcore.repository.redis.RedisRepository;
import com.traffic.couponcore.repository.redis.dto.CouponIssueRequest;
import com.traffic.couponcore.service.CouponIssueService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import static com.traffic.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

@RequiredArgsConstructor
@EnableScheduling
@Component
public class CouponIssueListener {

    private final RedisRepository redisRepository;
    private final CouponIssueService couponIssueService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String issueReuqestQueueKey = getIssueRequestQueueKey();

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Scheduled(fixedDelay = 1000L)
    public void issue() throws JsonProcessingException {
        log.info("listen....");
        while (existCouponIssueTartget()) {
            CouponIssueRequest couponIssueRequest = getIssueTarget();
            log.info("발급 시작 %s".formatted(couponIssueRequest));
            couponIssueService.issue(couponIssueRequest.couponId(), couponIssueRequest.userId());
            log.info("발급 완료 %s".formatted(couponIssueRequest));
            removeIssuedTarget();
        }
    }

    private boolean existCouponIssueTartget() {
        return redisRepository.lSize(issueReuqestQueueKey) > 0;
    }

    private CouponIssueRequest getIssueTarget() throws JsonProcessingException {
        return objectMapper.readValue(redisRepository.lIndex(issueReuqestQueueKey, 0) ,CouponIssueRequest.class);
    }

    private void removeIssuedTarget() {
        redisRepository.lPop(issueReuqestQueueKey);
    }

}
