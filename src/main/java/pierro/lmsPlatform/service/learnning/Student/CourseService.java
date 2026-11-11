package pierro.lmsPlatform.service.learnning.Student;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.dto.response.Learning.CourseDetailDto;
import pierro.lmsPlatform.dto.response.Learning.CourseSummaryDto;
import pierro.lmsPlatform.dto.response.Learning.LessonDetailDto;
import pierro.lmsPlatform.entity.learning.Course;
import pierro.lmsPlatform.entity.learning.Lesson;
import pierro.lmsPlatform.mapper.CourseMapper;
import pierro.lmsPlatform.mapper.LessonMapper;
import pierro.lmsPlatform.repository.learning.CourseRepository;
import pierro.lmsPlatform.repository.learning.LessonRepository;
import pierro.lmsPlatform.security.crypto.IdObfuscator;
import pierro.lmsPlatform.security.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final CourseMapper courseMapper;
    private final LessonMapper lessonMapper;
    private final IdObfuscator idObfuscator;

    @Transactional(readOnly = true)
    public List<CourseSummaryDto> seeAllCourse() {
        return courseMapper.toSummaries(courseRepository.findAll());
    }

    @Transactional(readOnly = true)
    public CourseDetailDto seeCourseDetail(String courseIdH) {
        return courseMapper.toDetail(findCourse(idObfuscator.decode(courseIdH)));
    }

    @Transactional(readOnly = true)
    public LessonDetailDto seeLessonDetail(String lessonIdH) {
        return lessonMapper.entityToDTO(findLesson(idObfuscator.decode(lessonIdH)));
    }

    private Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoá học"));
    }

    private Lesson findLesson(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học"));
    }
}