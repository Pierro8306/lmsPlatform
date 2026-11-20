package pierro.lmsPlatform.entity.cache;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity 
@Getter 
@Setter 
@NoArgsConstructor
@Table(name = "analysis_cache")
public class AnalysisCache {
    @Id @Column(name = "cache_key", length = 64)
    private String cacheKey;         
    @Column(columnDefinition = "TEXT", nullable = false) 
    private String payload;
    @Column(name = "hit_count") 
    private int hitCount;
    @CreationTimestamp @Column(name = "created_at", updatable = false) 
    private LocalDateTime createdAt;
    @Column(name = "last_hit_at") 
    private LocalDateTime lastHitAt;
}