package pierro.lmsPlatform.service.learnning.Student;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pierro.lmsPlatform.dto.response.Learning.CourseDetailDto;
import pierro.lmsPlatform.dto.response.Learning.CourseSummaryDto;
import pierro.lmsPlatform.dto.response.Learning.LessonDetailDto;
import pierro.lmsPlatform.entity.auth.User;
import pierro.lmsPlatform.entity.learning.Course;
import pierro.lmsPlatform.entity.learning.Lesson;
import pierro.lmsPlatform.entity.learning.LessonProgress;
import pierro.lmsPlatform.mapper.CourseMapper;
import pierro.lmsPlatform.mapper.LessonMapper;
import pierro.lmsPlatform.repository.auth.UserRepository;
import pierro.lmsPlatform.repository.learning.CourseRepository;
import pierro.lmsPlatform.repository.learning.LessonProgressRepository;
import pierro.lmsPlatform.repository.learning.LessonRepository;
import pierro.lmsPlatform.security.crypto.IdObfuscator;
import pierro.lmsPlatform.security.exception.ForbiddenException;
import pierro.lmsPlatform.security.exception.ResourceNotFoundException;
import pierro.lmsPlatform.service.learnning.CloudStorageService;
import pierro.lmsPlatform.service.learnning.StorageUrlResolver;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CourseMapper courseMapper;
    private final IdObfuscator idObfuscator;
    private final StorageUrlResolver storageUrlResolver;

    // Lấy danh sách khóa học cho sinh viên (lọc theo nhóm và tính tiến độ)
    @Transactional(readOnly = true)
    public List<CourseSummaryDto> seeAllCourse(Long userId) {
        if (userId == null) {
            return seeAllCourse();
        }

        User user = findUser(userId);
        List<Course> courses = user.getUserGroup() == null
                ? courseRepository.findByUserGroupIsNullOrderByIdDesc()
                : courseRepository.findAvailableForGroup(user.getUserGroup().getId());

        if (courses.isEmpty()) return List.of();

        List<Long> courseIds = courses.stream().map(Course::getId).toList();
        Map<Long, Long> lessonCounts = toCountMap(courseRepository.countLessonsByCourseIds(courseIds));
        Map<Long, Long> completedCounts = toCountMap(lessonProgressRepository.countCompletedByUserForCourses(userId, courseIds));

        return courses.stream().map(c -> {
            int total = lessonCounts.getOrDefault(c.getId(), 0L).intValue();
            int completed = completedCounts.getOrDefault(c.getId(), 0L).intValue();
            double pct = total == 0 ? 0.0 : Math.round(completed * 10000.0 / total) / 100.0;
            return CourseSummaryDto.builder()
                    .id(idObfuscator.encode(c.getId()))
                    .title(c.getTitle())
                    .description(c.getDescription())
                    .userGroup(c.getUserGroup() == null ? null : c.getUserGroup().getName())
                    .totalLessons(total)
                    .completedLessons(completed)
                    .progressPercent(pct)
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<CourseSummaryDto> seeAllCourse() {
        return courseMapper.toSummaries(courseRepository.findAllByOrderByIdDesc());
    }

    // Xem chi tiết khóa học kèm trạng thái hoàn thành từng bài học
    @Transactional(readOnly = true)
    public CourseDetailDto seeCourseDetail(Long userId, String courseIdH) {
        Course course = findCourse(idObfuscator.decode(courseIdH));
        if (userId != null) {
            assertCanAccess(course, findUser(userId));
        }
        Set<Long> completedIds = userId == null ? Set.of()
                : new HashSet<>(lessonProgressRepository.findCompletedLessonIdsByUserIdAndCourseId(userId, course.getId()));
        List<LessonDetailDto> lessons = course.getLessons().stream()
                .sorted(Comparator.comparing(Lesson::getId))
                .map(l -> LessonDetailDto.builder()
                        .id(idObfuscator.encode(l.getId()))
                        .title(l.getTitle())
                        .content(l.getContent())
                        .video_url(storageUrlResolver.resolve(l.getVideo_url()))              // <-- Đã resolve URL đầy đủ
                        .attachment_urls(storageUrlResolver.resolveList(l.getAttachment_urls())) // <-- Đã resolve URLs tệp đính kèm
                        .completed(completedIds.contains(l.getId()))
                        .build())
                .toList();
        int total = lessons.size();
        int completed = (int) lessons.stream().filter(LessonDetailDto::isCompleted).count();
        double pct = total == 0 ? 0.0 : Math.round(completed * 10000.0 / total) / 100.0;
        return CourseDetailDto.builder()
                .id(idObfuscator.encode(course.getId()))
                .title(course.getTitle())
                .description(course.getDescription())
                .userGroup(course.getUserGroup() == null ? null : course.getUserGroup().getName())
                .totalLessons(total)
                .completedLessons(completed)
                .progressPercent(pct)
                .lessons(lessons)
                .build();
    }
    // Hàm phụ gọi khi không truyền userId: chỉ việc gọi hàm chính
    @Transactional(readOnly = true)
    public CourseDetailDto seeCourseDetail(String courseIdH) {
        return seeCourseDetail(null, courseIdH);
    }

    // Xem chi tiết một bài học (Hàm chính)
    @Transactional(readOnly = true)
    public LessonDetailDto seeLessonDetail(Long userId, String lessonIdH) {
        Lesson lesson = findLesson(idObfuscator.decode(lessonIdH));
        // Chặn nếu bài học chưa được đưa vào khóa học nào
        if (lesson.getCourse() == null) {
            throw new ResourceNotFoundException("Bài học chưa thuộc khóa học nào");
        }
        if (userId != null) {
            assertCanAccess(lesson.getCourse(), findUser(userId));
        }
        Optional<LessonProgress> lp = userId == null ? Optional.empty()
                : lessonProgressRepository.findByUserIdAndLessonId(userId, lesson.getId());
        boolean isCompleted = lp.map(LessonProgress::isCompleted).orElse(false);
        LocalDateTime completedAt = lp.map(LessonProgress::getCompletedAt).orElse(null);
        return LessonDetailDto.builder()
                .id(idObfuscator.encode(lesson.getId()))
                .title(lesson.getTitle())
                .content(lesson.getContent())
                .video_url(storageUrlResolver.resolve(lesson.getVideo_url()))              // <-- Đã resolve URL đầy đủ
                .attachment_urls(storageUrlResolver.resolveList(lesson.getAttachment_urls())) // <-- Đã resolve URLs tệp đính kèm
                .completed(isCompleted)
                .completedAt(completedAt)
                .build();
    }
    // Hàm phụ gọi khi không truyền userId: chỉ việc gọi hàm chính
    @Transactional(readOnly = true)
    public LessonDetailDto seeLessonDetail(String lessonIdH) {
        return seeLessonDetail(null, lessonIdH);
    }

    // Đánh dấu bài học đã hoàn thành
    @Transactional
    public void completeLesson(Long userId, String lessonIdH) {
        Lesson lesson = findLesson(idObfuscator.decode(lessonIdH));
        if (lesson.getCourse() != null) {
            assertCanAccess(lesson.getCourse(), findUser(userId));
        }

        LessonProgress progress = lessonProgressRepository.findByUserIdAndLessonId(userId, lesson.getId())
                .orElseGet(() -> {
                    LessonProgress p = new LessonProgress();
                    p.setUser(userRepository.getReferenceById(userId));
                    p.setLesson(lesson);
                    return p;
                });

        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        lessonProgressRepository.save(progress);
    }

    // Hủy đánh dấu hoàn thành bài học
    @Transactional
    public void unCompleteLesson(Long userId, String lessonIdH) {
        Long lessonId = idObfuscator.decode(lessonIdH);
        lessonProgressRepository.findByUserIdAndLessonId(userId, lessonId)
                .ifPresent(p -> {
                    p.setCompleted(false);
                    p.setCompletedAt(null);
                    lessonProgressRepository.save(p);
                });
    }

    private void assertCanAccess(Course course, User user) {
        if (course.getUserGroup() == null) return;
        if (user.getUserGroup() == null || !course.getUserGroup().getId().equals(user.getUserGroup().getId())) {
            throw new ForbiddenException("Khóa học này không dành cho nhóm của bạn");
        }
    }

    private Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoá học"));
    }

    private Lesson findLesson(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học"));
    }

    private User findUser(Long id) {
        return userRepository.findWithRoleAndGroupById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }

    private Map<Long, Long> toCountMap(List<Object[]> rows) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] r : rows) {
            map.put((Long) r[0], ((Number) r[1]).longValue());
        }
        return map;
    }

}
