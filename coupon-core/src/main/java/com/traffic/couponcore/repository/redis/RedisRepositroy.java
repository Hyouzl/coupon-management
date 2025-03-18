package com.traffic.couponcore.repository.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.traffic.couponcore.exception.CouponIssueException;
import com.traffic.couponcore.exception.ErrorCode;
import com.traffic.couponcore.repository.redis.dto.CouponIssueRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.traffic.couponcore.repository.redis.CouponIssueRequest.checkRequestResult;
import static com.traffic.couponcore.repository.redis.CouponIssueRequest.find;
import static com.traffic.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static com.traffic.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

@RequiredArgsConstructor
@Repository
public class RedisRepositroy {

    private final RedisTemplate<String, String> redisTemplate;
    private RedisScript<String> issueScript = issueRequestScript();
    private final String issueRequestQueuKey = getIssueRequestQueueKey();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Boolean zAdd(String key, String value, Long scroe) {
        // 데이터가 없는 경우만 !! 커맨드 처리.
        return redisTemplate.opsForZSet().addIfAbsent(key, value, scroe);
    }
    public Long sAdd(String key, String value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    public Long sCard(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    public Boolean sIsMember(String key, String value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Long rPush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }


    // 레디스를 활용한 비동기
    public void issueRequest(Long couponId, Long userId, int totalIssueQuantity) {
        String issueRequestKey = getIssueRequestKey(couponId);
        CouponIssueRequest couponIssueRequest = new CouponIssueRequest(couponId, userId);

        try {
            String code = redisTemplate.execute(
                    issueScript,
                    List.of(issueRequestKey, issueRequestQueuKey),
                    String.valueOf(userId),
                    String.valueOf(totalIssueQuantity),
                    objectMapper.writeValueAsString(couponIssueRequest)
            );
            checkRequestResult(find(code));
        } catch (JsonProcessingException e) {
            throw new CouponIssueException(ErrorCode.FAIL_COUPON_ISSUE_REQUEST, "input %s".formatted(couponIssueRequest));
        }

    }

    private RedisScript<String> issueRequestScript() {
        String script = """
                if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then
                    return '2'
                end
                
                if tonumber(ARGV[2]) > redis.call('SCARD', KEYS[1]) then
                    redis.call('SADD', KEYS[1], ARGV[1])
                    redis.call('RPUSH', KEYS[2], ARGV[3])
                    return '1'
                end
                
                return '3'
                """;

        return RedisScript.of(script, String.class);
    }
}
