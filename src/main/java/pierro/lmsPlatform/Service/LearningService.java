package pierro.lmsPlatform.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.DTO.Request.Learning.CreateCourseDto;
import pierro.lmsPlatform.DTO.Request.Learning.CreateLessonDto;
import pierro.lmsPlatform.DTO.Response.Learning.CourseDetailDto;
import pierro.lmsPlatform.DTO.Response.Learning.CourseSummaryDto;
import pierro.lmsPlatform.DTO.Response.Learning.LessonDetailDto;
import pierro.lmsPlatform.Entity.Learning.Course;
import pierro.lmsPlatform.Entity.Learning.Lesson;
import pierro.lmsPlatform.Mapper.LessonMapper;
import pierro.lmsPlatform.Repository.Learning.CourseRepository;
import pierro.lmsPlatform.Repository.Learning.LessonRepository;
import pierro.lmsPlatform.Security.Crypto.IdObfuscator;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LearningService {
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final IdObfuscator idObfuscator;

    public void createLesson(CreateLessonDto createLessonDto) {
        Lesson lesson = new Lesson();
        lesson.setTitle(createLessonDto.getTitle());
        lesson.setContent(createLessonDto.getContent());
        lesson.setAttachment_urls(createLessonDto.getAttachment_urls());
        lesson.setVideo_url(createLessonDto.getVideo_url());
        lessonRepository.save(lesson);
    }

    public void addLesson(String courseIdH, String lessonIdH) {
        Long courseId = idObfuscator.decode(courseIdH);
        Long lessonId = idObfuscator.decode(lessonIdH);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        lesson.setCourse(course);
        lessonRepository.save(lesson);
    }

    // Sửa chữ C viết hoa thành c thường
    public void createCourse(CreateCourseDto createCourseDto) {
        Course course = new Course();
        course.setTitle(createCourseDto.getTitle());
        course.setDescription(createCourseDto.getDescription());
        courseRepository.save(course);
    }

    public void deleteCourse(String courseIdH) {
        Long courseId = idObfuscator.decode(courseIdH);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        lessonRepository.deleteByCourseId(courseId);
        courseRepository.delete(course);
    }

    public void deleteLesson(String lessonIdH) {
        Long lessonId = idObfuscator.decode(lessonIdH);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        lessonRepository.deleteById(lessonId);
    }

    public List<CourseSummaryDto> seeAllCourse() {
        List<Course> courseList = courseRepository.findAll();
        List<CourseSummaryDto> courseSummaryDtoList = new ArrayList<>();
        for (Course course : courseList) {
            CourseSummaryDto courseSummaryDto = new CourseSummaryDto();
            courseSummaryDto.setId(idObfuscator.encode(course.getId()));
            courseSummaryDto.setTitle(course.getTitle());
            courseSummaryDto.setDescription(course.getDescription());
            courseSummaryDtoList.add(courseSummaryDto);
        }
        return courseSummaryDtoList;
    }

    public CourseDetailDto seeCourseDetail(String courseIdH) {
        Long courseId = idObfuscator.decode(courseIdH);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        CourseDetailDto courseDetailDto = new CourseDetailDto();
        courseDetailDto.setId(idObfuscator.encode(courseId));
        courseDetailDto.setTitle(course.getTitle());
        courseDetailDto.setDescription(course.getDescription());
        courseDetailDto.setLessons(lessonMapper.entityToDTO(new ArrayList<>(course.getLessons())));
        return courseDetailDto;
    }

    public LessonDetailDto seeLessonDetail(String lessonIdH) {
        Long lessonId = idObfuscator.decode(lessonIdH);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        LessonDetailDto lessonDetailDto = new LessonDetailDto();
        lessonDetailDto.setTitle(lesson.getTitle());
        lessonDetailDto.setContent(lesson.getContent());
        lessonDetailDto.setAttachment_urls(lesson.getAttachment_urls());
        lessonDetailDto.setVideo_url(lesson.getVideo_url());
        return lessonDetailDto;
    }
}
