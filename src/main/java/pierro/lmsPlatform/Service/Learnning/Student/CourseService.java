package pierro.lmsPlatform.Service.Learnning.Student;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.DTO.Response.Learning.CourseDetailDto;
import pierro.lmsPlatform.DTO.Response.Learning.CourseSummaryDto;
import pierro.lmsPlatform.DTO.Response.Learning.LessonDetailDto;
import pierro.lmsPlatform.Entity.Learning.Course;
import pierro.lmsPlatform.Entity.Learning.Lesson;
import pierro.lmsPlatform.Mapper.CourseMapper;
import pierro.lmsPlatform.Mapper.LessonMapper;
import pierro.lmsPlatform.Repository.Learning.CourseRepository;
import pierro.lmsPlatform.Repository.Learning.LessonRepository;
import pierro.lmsPlatform.Security.Crypto.IdObfuscator;
import pierro.lmsPlatform.Security.Exception.ResourceNotFoundException;

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