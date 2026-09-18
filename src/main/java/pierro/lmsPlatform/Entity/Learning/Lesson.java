package pierro.lmsPlatform.Entity.Learning;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pierro.lmsPlatform.Entity.Quiz.Quiz;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
@Table(name = "Lessons")
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    private String video_url;
    private String attachment_urls;
    @ManyToOne
    @JoinColumn(name = "id_course")
    private Course course;
}
