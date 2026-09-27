package pierro.lmsPlatform.Controller.User;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pierro.lmsPlatform.DTO.Response.ResponseObject;
import pierro.lmsPlatform.Service.LearningService;

@RestController
@RequestMapping("/api/user/learning")
@RequiredArgsConstructor
public class UserLearningController {
    private final LearningService learningService;

    @GetMapping("/courses")
    public ResponseEntity<ResponseObject> getAllCourses() {
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Lấy danh sách khóa học thành công", learningService.seeAllCourse()));
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<ResponseObject> getCourseDetail(@PathVariable String courseId) {
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Lấy chi tiết khóa học thành công", learningService.seeCourseDetail(courseId)));
    }

    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<ResponseObject> getLessonDetail(@PathVariable String lessonId) {
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Lấy chi tiết bài học thành công", learningService.seeLessonDetail(lessonId)));
    }
}