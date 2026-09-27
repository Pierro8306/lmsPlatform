package pierro.lmsPlatform.Controller.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pierro.lmsPlatform.DTO.Request.Learning.CreateCourseDto;
import pierro.lmsPlatform.DTO.Request.Learning.CreateLessonDto;
import pierro.lmsPlatform.DTO.Response.ResponseObject;
import pierro.lmsPlatform.Service.LearningService;

@RestController
@RequestMapping("/api/admin/learning")
@RequiredArgsConstructor
public class AdminLearningController {
    private final LearningService learningService;

    @PostMapping("/courses")
    public ResponseEntity<ResponseObject> createCourse(@RequestBody CreateCourseDto createCourseDto) {
        learningService.createCourse(createCourseDto);
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Tạo khóa học thành công", null));
    }

    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<ResponseObject> deleteCourse(@PathVariable String courseId) {
        learningService.deleteCourse(courseId);
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Xóa khóa học thành công", null));
    }

    @PostMapping("/lessons")
    public ResponseEntity<ResponseObject> createLesson(@RequestBody CreateLessonDto createLessonDto) {
        learningService.createLesson(createLessonDto);
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Tạo bài học thành công", null));
    }

    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<ResponseObject> deleteLesson(@PathVariable String lessonId) {
        learningService.deleteLesson(lessonId);
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Xóa bài học thành công", null));
    }

    @PostMapping("/courses/{courseId}/lessons/{lessonId}")
    public ResponseEntity<ResponseObject> addLessonToCourse(@PathVariable String courseId, @PathVariable String lessonId) {
        learningService.addLesson(courseId, lessonId);
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Thêm bài học vào khóa học thành công", null));
    }
}