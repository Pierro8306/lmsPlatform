package pierro.lmsPlatform.repository.cache;

import org.springframework.data.jpa.repository.JpaRepository;

import pierro.lmsPlatform.entity.cache.AnalysisCache;

public interface AnalysisCacheRepository  extends JpaRepository<AnalysisCache,String>{
    
}
