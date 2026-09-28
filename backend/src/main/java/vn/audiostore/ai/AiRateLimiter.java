package vn.audiostore.ai;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import vn.audiostore.common.TooManyRequestsException;

@Component
public class AiRateLimiter {
    private static final long WINDOW=60_000; private static final int LIMIT=10;
    private final ConcurrentHashMap<String,Bucket> buckets=new ConcurrentHashMap<>();
    public void check(String identity){
        long now=System.currentTimeMillis();
        buckets.compute(identity,(key,bucket)->{
            if(bucket==null||now-bucket.startedAt>=WINDOW)return new Bucket(now,1);
            if(bucket.count>=LIMIT)throw new TooManyRequestsException("Bạn đã gửi nhiều yêu cầu AI trong thời gian ngắn. Hãy thử lại sau một phút.");
            bucket.count++;return bucket;
        });
        if(buckets.size()>3000)buckets.entrySet().removeIf(e->now-e.getValue().startedAt>=WINDOW);
    }
    private static final class Bucket{final long startedAt;int count;Bucket(long start,int count){this.startedAt=start;this.count=count;}}
}
