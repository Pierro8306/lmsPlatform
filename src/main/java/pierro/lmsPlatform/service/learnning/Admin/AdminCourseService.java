package pierro.lmsPlatform.service.learnning.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import pierro.lmsPlatform.dto.request.Learning.CreateCourseDto;
import pierro.lmsPlatform.dto.request.Learning.CreateLessonDto;
import pierro.lmsPlatform.dto.request.Learning.UploadedFileDto;
import pierro.lmsPlatform.dto.response.Learning.AdminLessonSummaryDto;
import pierro.lmsPlatform.dto.response.Learning.CourseSummaryDto;
import pierro.lmsPlatform.dto.response.Learning.LessonDetailDto;
import pierro.lmsPlatform.entity.auth.UserGroup;
import pierro.lmsPlatform.entity.learning.Course;
import pierro.lmsPlatform.entity.learning.Lesson;
import pierro.lmsPlatform.mapper.CourseMapper;
import pierro.lmsPlatform.mapper.LessonMapper;
import pierro.lmsPlatform.repository.auth.UserGroupRepository;
import pierro.lmsPlatform.repository.learning.CourseRepository;
import pierro.lmsPlatform.repository.learning.LessonProgressRepository;
import pierro.lmsPlatform.repository.learning.LessonRepository;
import pierro.lmsPlatform.security.crypto.IdObfuscator;
import pierro.lmsPlatform.security.exception.ConflictException;
import pierro.lmsPlatform.security.exception.ResourceNotFoundException;
import pierro.lmsPlatform.service.learnning.CloudStorageService;
import pierro.lmsPlatform.service.learnning.StorageUrlResolver;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCourseService {
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final UserGroupRepository userGroupRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CloudStorageService cloudStorageService;
    private final StorageUrlResolver storageUrlResolver;
    private final CourseMapper courseMapper;
    private final LessonMapper lessonMapper;
    private final IdObfuscator idObfuscator;

    @Transactional
    public CourseSummaryDto createCourse(CreateCourseDto dto) {
        Course course = courseMapper.toEntity(dto);
        course.setUserGroup(findGroup(dto.getUserGroup()));
        Course saved = courseRepository.save(course);
        return courseMapper.toSummary(saved);
    }

    @Transactional(readOnly = true)
    public List<CourseSummaryDto> listCourses() {
        return courseMapper.toSummaries(courseRepository.findAllByOrderByIdDesc());
    }

    @Transactional
    public void updateCourse(String courseIdH, CreateCourseDto dto) {
        Course course = findCourse(idObfuscator.decode(courseIdH));
        courseMapper.updateEntity(dto, course);
        course.setUserGroup(findGroup(dto.getUserGroup()));
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
        Long lessonId = idObfuscator.decode(lessonIdH);
        findLesson(lessonId);
        lessonProgressRepository.deleteByLesson_Id(lessonId);
        lessonRepository.deleteById(lessonId);
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

    private UserGroup findGroup(String name) {
        if (!StringUtils.hasText(name)) return null;
        return userGroupRepository.findByName(name.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm: " + name));
    }



    // Lấy chi tiết bài học cho Admin (đã resolve URL):
    @Transactional(readOnly = true)
    public LessonDetailDto getLessonDetail(String lessonIdH) {
        Lesson lesson = findLesson(idObfuscator.decode(lessonIdH));
        LessonDetailDto dto = lessonMapper.entityToDTO(lesson);
        dto.setVideo_url(storageUrlResolver.resolve(lesson.getVideo_url()));
        dto.setAttachment_urls(storageUrlResolver.resolveList(lesson.getAttachment_urls()));
        return dto;
    }

    // Upload video cho bài học:
    @Transactional
    public UploadedFileDto uploadLessonVideo(String lessonIdH, MultipartFile file) {
        Lesson lesson = findLesson(idObfuscator.decode(lessonIdH));
        CloudStorageService.StoredObject stored = cloudStorageService.upload(file, CloudStorageService.Kind.VIDEO);
        lesson.setVideo_url(stored.key());
        String publicUrl = storageUrlResolver.resolve(stored.key());
        return new UploadedFileDto(stored.key(), publicUrl, stored.contentType(), stored.size());
    }

    // Upload tài liệu đính kèm cho bài học:
    @Transactional
    public List<UploadedFileDto> uploadLessonAttachments(String lessonIdH, List<MultipartFile> files) {
        Lesson lesson = findLesson(idObfuscator.decode(lessonIdH));
        List<UploadedFileDto> results = new ArrayList<>();
        List<String> newKeys = new ArrayList<>();

        for (MultipartFile file : files) {
            CloudStorageService.StoredObject stored = cloudStorageService.upload(file, CloudStorageService.Kind.ATTACHMENT);
            newKeys.add(stored.key());
            results.add(new UploadedFileDto(stored.key(), storageUrlResolver.resolve(stored.key()), stored.contentType(), stored.size()));
        }

        String currentUrls = lesson.getAttachment_urls();
        String joinedNew = String.join("\n", newKeys);
        if (StringUtils.hasText(currentUrls)) {
            lesson.setAttachment_urls(currentUrls.trim() + "\n" + joinedNew);
        } else {
            lesson.setAttachment_urls(joinedNew);
        }
        return results;
    }
}
