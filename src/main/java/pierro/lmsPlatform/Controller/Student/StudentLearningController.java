package pierro.lmsPlatform.Controller.Student;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pierro.lmsPlatform.DTO.Response.ResponseObject;
import pierro.lmsPlatform.Service.Learnning.Student.CourseService;

@RestController
@RequestMapping("/api/student/learning")
@RequiredArgsConstructor
public class StudentLearningController {
    private final CourseService learningService;

    @GetMapping("/courses")
    public ResponseEntity<ResponseObject> getAllCourses() {
        return ResponseObject.ok("Lấy danh sách khóa học thành công", learningService.seeAllCourse());
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<ResponseObject> getCourseDetail(@PathVariable String courseId) {
        return ResponseObject.ok("Lấy chi tiết khóa học thành công", learningService.seeCourseDetail(courseId));
    }

    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<ResponseObject> getLessonDetail(@PathVariable String lessonId) {
        return ResponseObject.ok("Lấy chi tiết bài học thành công", learningService.seeLessonDetail(lessonId));
    }
}
