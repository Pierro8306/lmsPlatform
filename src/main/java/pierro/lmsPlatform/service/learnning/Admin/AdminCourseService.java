package pierro.lmsPlatform.service.learnning.Admin;
import pierro.lmsPlatform.security.exception.ConflictException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.dto.request.Learning.CreateCourseDto;
import pierro.lmsPlatform.dto.request.Learning.CreateLessonDto;
import pierro.lmsPlatform.dto.response.Learning.AdminLessonSummaryDto;
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
        Long courseId = idObfuscator.decode(courseIdH);
        findCourse(courseId);                               // 404 nếu không tồn tại
        lessonRepository.detachFromCourse(courseId);        // bài học trở về trạng thái "chưa gắn khoá", không bị xoá
        courseRepository.deleteById(courseId);
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
        Course current = lesson.getCourse();
        if (current != null) {
            if (current.getId().equals(course.getId())) return;   // đã thuộc khoá này
            throw new ConflictException("Bài học đang thuộc khoá học khác, hãy gỡ khỏi khoá đó trước");
        }
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

