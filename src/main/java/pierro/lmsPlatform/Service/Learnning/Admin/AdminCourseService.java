package pierro.lmsPlatform.Service.Learnning.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.DTO.Request.Learning.CreateCourseDto;
import pierro.lmsPlatform.DTO.Request.Learning.CreateLessonDto;
import pierro.lmsPlatform.DTO.Response.Learning.AdminLessonSummaryDto;
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
public class AdminCourseService {
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final CourseMapper courseMapper;
    private final LessonMapper lessonMapper;
    private final IdObfuscator idObfuscator;

    @Transactional
    public CourseSummaryDto createCourse(CreateCourseDto dto) {
        Course saved = courseRepository.save(courseMapper.toEntity(dto));
        return courseMapper.toSummary(saved);
    }

    @Transactional(readOnly = true)
    public List<CourseSummaryDto> listCourses() {
        return courseMapper.toSummaries(courseRepository.findAll());
    }

    @Transactional
    public void updateCourse(String courseIdH, CreateCourseDto dto) {
        courseMapper.updateEntity(dto, findCourse(idObfuscator.decode(courseIdH)));
    }

    @Transactional
    public void deleteCourse(String courseIdH) {
        courseRepository.delete(findCourse(idObfuscator.decode(courseIdH)));
    }

    @Transactional
    public LessonDetailDto createLesson(CreateLessonDto dto) {
        Lesson saved = lessonRepository.save(lessonMapper.toEntity(dto));
        return lessonMapper.entityToDTO(saved);
    }

    /** @param unassignedOnly true = chỉ lấy bài học chưa gắn khoá học nào. */
    @Transactional(readOnly = true)
    public List<AdminLessonSummaryDto> listLessons(boolean unassignedOnly) {
        List<Lesson> lessons = unassignedOnly
                ? lessonRepository.findByCourseIsNullOrderByIdDesc()
                : lessonRepository.findAllByOrderByIdDesc();
        return lessons.stream().map(this::toSummary).toList();
    }

    @Transactional
    public void updateLesson(String lessonIdH, CreateLessonDto dto) {
        lessonMapper.updateEntity(dto, findLesson(idObfuscator.decode(lessonIdH)));
    }

    @Transactional
    public void deleteLesson(String lessonIdH) {
        lessonRepository.delete(findLesson(idObfuscator.decode(lessonIdH)));
    }

    @Transactional
    public void addLesson(String courseIdH, String lessonIdH) {
        Course course = findCourse(idObfuscator.decode(courseIdH));
        Lesson lesson = findLesson(idObfuscator.decode(lessonIdH));
        lesson.setCourse(course);
    }

    @Transactional
    public void removeLesson(String courseIdH, String lessonIdH) {
        Long courseId = idObfuscator.decode(courseIdH);
        Lesson lesson = findLesson(idObfuscator.decode(lessonIdH));
        if (lesson.getCourse() == null || !lesson.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Bài học không thuộc khoá học này");
        }
        lesson.setCourse(null);
    }

    private AdminLessonSummaryDto toSummary(Lesson lesson) {
        Course course = lesson.getCourse();
        return new AdminLessonSummaryDto(
                idObfuscator.encode(lesson.getId()),
                lesson.getTitle(),
                course == null ? null : idObfuscator.encode(course.getId()),
                course == null ? null : course.getTitle());
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
