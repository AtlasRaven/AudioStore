package vn.audiostore.ai;

import vn.audiostore.auth.UserRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AiService {
    private final GeminiClient gemini; private final AiLogRepository logs; private final UserRepository users;
    public AiService(GeminiClient gemini,AiLogRepository logs,UserRepository users){this.gemini=gemini;this.logs=logs;this.users=users;}
    public Result generate(String feature,String prompt,String email){
        long start=System.nanoTime();String status="OK";String text=null;
        try{text=gemini.generate(prompt);}catch(RuntimeException ex){status="UNAVAILABLE";}
        Long userId=email==null?null:users.findByEmailIgnoreCase(email).map(u->u.getId()).orElse(null);
        try{logs.save(new AiLog(userId,feature,status,(int)((System.nanoTime()-start)/1_000_000)));}catch(RuntimeException ignored){}
        return new Result(text,status.equals("OK"));
    }
    public Page<AiLog> logs(int page,int size){return logs.findAll(PageRequest.of(Math.max(0,page),Math.min(100,Math.max(1,size)),Sort.by(Sort.Direction.DESC,"createdAt")));}
    public record Result(String text,boolean available){}
}
