package vn.audiostore.ai;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="ai_logs")
public class AiLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="user_id") private Long userId;
    @Column(nullable=false,length=40) private String feature;
    @Column(nullable=false,length=20) private String status;
    @Column(name="duration_ms",nullable=false) private int durationMs;
    @Column(name="created_at",nullable=false) private Instant createdAt=Instant.now();
    protected AiLog(){}
    public AiLog(Long userId,String feature,String status,int durationMs){this.userId=userId;this.feature=feature;this.status=status;this.durationMs=durationMs;}
    public Long getId(){return id;} public Long getUserId(){return userId;} public String getFeature(){return feature;} public String getStatus(){return status;} public int getDurationMs(){return durationMs;} public Instant getCreatedAt(){return createdAt;}
}
