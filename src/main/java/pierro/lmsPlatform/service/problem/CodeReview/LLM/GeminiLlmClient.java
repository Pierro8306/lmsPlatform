
// package pierro.lmsPlatform.service.problem.CodeReview.LLM;

// import org.springframework.ai.chat.client.ChatClient;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Component;

// @Component
// public class GeminiLlmClient {

//     private final ChatClient chatClient;

//     @Value("${codereview.llm.model}")
//     private String model;

//     public GeminiLlmClient(ChatClient.Builder builder) {
//         this.chatClient = builder.build();           
//     }

//     public String model() {
//         return model;
//     }

//     public String complete(String system, String user) {
//         return chatClient.prompt()
//                 .system(system)
//                 .user(user)
//                 .call()
//                 .content();
//     }
// }


package pierro.lmsPlatform.service.problem.CodeReview.LLM;

import java.util.concurrent.Semaphore;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GeminiLlmClient {

    private final ChatClient chatClient;
    private final String model;

    // Cho phép tối đa 2 request LLM chạy đồng thời
    private final Semaphore semaphore = new Semaphore(2);

    public GeminiLlmClient(
            ChatClient.Builder builder,
            @Value("${codereview.llm.model}") String model) {

        this.chatClient = builder.build();
        this.model = model.trim();

        System.out.println("Gemini model = [" + this.model + "]");
    }

    public String model() {
        return model;
    }

    public String complete(String system, String user) {
        boolean acquired = false;

        try {
            // Chờ đến khi có slot trống
            semaphore.acquire();
            acquired = true;

            return chatClient.prompt()
                    .system(system)
                    .user(user)
                    .call()
                    .content();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("LLM request interrupted", e);

        } finally {
            // Chỉ trả lại slot nếu đã acquire thành công
            if (acquired) {
                semaphore.release();
            }
        }
    }
}


