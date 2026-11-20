package pierro.lmsPlatform.controller.student;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pierro.lmsPlatform.dto.response.ResponseObject;
import pierro.lmsPlatform.service.learnning.Student.CourseService;

@RestController
@RequestMapping("/api/student/learning")
@RequiredArgsConstructor
public class StudentLearningController {
    private final CourseService learningService;

    @GetMapping("/courses")
    public ResponseEntity<ResponseObject> getAllCourses(@AuthenticationPrincipal Long userId) {
        return ResponseObject.ok("Lấy danh sách khóa học thành công", learningService.seeAllCourse(userId));
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<ResponseObject> getCourseDetail(@AuthenticationPrincipal Long userId,
                                                          @PathVariable String courseId) {
        return ResponseObject.ok("Lấy chi tiết khóa học thành công", learningService.seeCourseDetail(userId, courseId));
    }

    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<ResponseObject> getLessonDetail(@AuthenticationPrincipal Long userId,
                                                          @PathVariable String lessonId) {
        return ResponseObject.ok("Lấy chi tiết bài học thành công", learningService.seeLessonDetail(userId, lessonId));
    }

    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<ResponseObject> completeLesson(@AuthenticationPrincipal Long userId,
                                                         @PathVariable String lessonId) {
        learningService.completeLesson(userId, lessonId);
        return ResponseObject.ok("Đánh dấu hoàn thành bài học thành công");
    }

    @DeleteMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<ResponseObject> uncompleteLesson(@AuthenticationPrincipal Long userId,
                                                           @PathVariable String lessonId) {
        learningService.uncompleteLesson(userId, lessonId);
        return ResponseObject.ok("Hủy đánh dấu hoàn thành bài học thành công");
    }
}
