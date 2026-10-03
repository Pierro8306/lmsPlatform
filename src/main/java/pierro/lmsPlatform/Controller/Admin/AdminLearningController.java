package pierro.lmsPlatform.Controller.Admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pierro.lmsPlatform.DTO.Request.Learning.CreateCourseDto;
import pierro.lmsPlatform.DTO.Request.Learning.CreateLessonDto;
import pierro.lmsPlatform.DTO.Response.ResponseObject;
import pierro.lmsPlatform.Service.Learnning.Admin.AdminCourseService;

@RestController
@RequestMapping("/api/admin/learning")
@RequiredArgsConstructor
public class AdminLearningController {
    private final AdminCourseService learningService;

    @GetMapping("/courses")
    public ResponseEntity<ResponseObject> listCourses() {
        return ResponseObject.ok("Lấy danh sách khóa học thành công", learningService.listCourses());
    }

    /** Trả về khoá học vừa tạo (có id mã hoá). */
    @PostMapping("/courses")
    public ResponseEntity<ResponseObject> createCourse(@Valid @RequestBody CreateCourseDto createCourseDto) {
        return ResponseObject.ok("Tạo khóa học thành công", learningService.createCourse(createCourseDto));
    }

    @PutMapping("/courses/{courseId}")
    public ResponseEntity<ResponseObject> updateCourse(@PathVariable String courseId,
                                                       @Valid @RequestBody CreateCourseDto dto) {
        learningService.updateCourse(courseId, dto);
        return ResponseObject.ok("Cập nhật khóa học thành công");
    }

    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<ResponseObject> deleteCourse(@PathVariable String courseId) {
        learningService.deleteCourse(courseId);
        return ResponseObject.ok("Xóa khóa học thành công");
    }

    /** ?unassigned=true: chỉ bài học chưa gắn khoá học (để lấy id gọi addLessonToCourse). */
    @GetMapping("/lessons")
    public ResponseEntity<ResponseObject> listLessons(@RequestParam(defaultValue = "false") boolean unassigned) {
        return ResponseObject.ok("Lấy danh sách bài học thành công", learningService.listLessons(unassigned));
    }

    /** Trả về bài học vừa tạo (có id mã hoá). */
    @PostMapping("/lessons")
    public ResponseEntity<ResponseObject> createLesson(@Valid @RequestBody CreateLessonDto createLessonDto) {
        return ResponseObject.ok("Tạo bài học thành công", learningService.createLesson(createLessonDto));
    }

    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<ResponseObject> updateLesson(@PathVariable String lessonId,
                                                       @Valid @RequestBody CreateLessonDto dto) {
        learningService.updateLesson(lessonId, dto);
        return ResponseObject.ok("Cập nhật bài học thành công");
    }

    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<ResponseObject> deleteLesson(@PathVariable String lessonId) {
        learningService.deleteLesson(lessonId);
        return ResponseObject.ok("Xóa bài học thành công");
    }

    @PostMapping("/courses/{courseId}/lessons/{lessonId}")
    public ResponseEntity<ResponseObject> addLessonToCourse(@PathVariable String courseId, @PathVariable String lessonId) {
        learningService.addLesson(courseId, lessonId);
        return ResponseObject.ok("Thêm bài học vào khóa học thành công");
    }

    @DeleteMapping("/courses/{courseId}/lessons/{lessonId}")
    public ResponseEntity<ResponseObject> removeLessonFromCourse(@PathVariable String courseId,
                                                                 @PathVariable String lessonId) {
        learningService.removeLesson(courseId, lessonId);
        return ResponseObject.ok("Gỡ bài học khỏi khóa học thành công");
    }
}
