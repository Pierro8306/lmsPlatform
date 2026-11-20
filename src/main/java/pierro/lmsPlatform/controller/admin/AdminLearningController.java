package pierro.lmsPlatform.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pierro.lmsPlatform.dto.request.Learning.CreateCourseDto;
import pierro.lmsPlatform.dto.request.Learning.CreateLessonDto;
import pierro.lmsPlatform.dto.response.ResponseObject;
import pierro.lmsPlatform.security.exception.BadRequestException;
import pierro.lmsPlatform.service.learnning.Admin.AdminCourseService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/learning")
@RequiredArgsConstructor
public class AdminLearningController {
    private static final int MAX_ATTACHMENTS_PER_REQUEST = 10;

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

    /**
     * Tải video bài giảng lên cloud storage (multipart, field "file").
     * Lưu đường dẫn object vào Lesson.video_url, trả về {path, url, contentType, size}.
     */
    @PostMapping(value = "/lessons/{lessonId}/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseObject> uploadLessonVideo(@PathVariable String lessonId,
                                                            @RequestParam("file") MultipartFile file) {
        return ResponseObject.ok("Tải video lên thành công", learningService.uploadLessonVideo(lessonId, file));
    }

    /**
     * Tải tệp đính kèm (PDF, ảnh, tài liệu...) lên cloud storage (multipart, field "files", nhiều tệp).
     * Nối đường dẫn vào Lesson.attachment_urls, trả về danh sách {path, url, contentType, size}.
     */
    @PostMapping(value = "/lessons/{lessonId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseObject> uploadLessonAttachments(@PathVariable String lessonId,
                                                                  @RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BadRequestException("Chưa chọn tệp đính kèm nào");
        }
        if (files.size() > MAX_ATTACHMENTS_PER_REQUEST) {
            throw new BadRequestException("Mỗi lần chỉ được tải tối đa " + MAX_ATTACHMENTS_PER_REQUEST + " tệp");
        }
        return ResponseObject.ok("Tải tệp đính kèm lên thành công",
                learningService.uploadLessonAttachments(lessonId, files));
    }
    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<ResponseObject> getLessonDetail(@PathVariable String lessonId) {
        return ResponseObject.ok("Lấy chi tiết bài học thành công", learningService.getLessonDetail(lessonId));
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

