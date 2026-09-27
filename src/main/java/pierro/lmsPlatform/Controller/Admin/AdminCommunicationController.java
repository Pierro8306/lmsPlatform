package pierro.lmsPlatform.Controller.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pierro.lmsPlatform.DTO.Response.ResponseObject;
import pierro.lmsPlatform.Service.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/communications")
@RequiredArgsConstructor
public class AdminCommunicationController {
    private final CommunicationService communicationService;

    @PostMapping("/notifications")
    public ResponseEntity<ResponseObject> createNotification(@RequestParam String title, @RequestParam String description) {
        communicationService.createNotification(title, description);
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Tạo thông báo thành công", null));
    }

    @PostMapping("/events")
    public ResponseEntity<ResponseObject> createEvent(@RequestParam String title, @RequestParam String description, @RequestParam LocalDateTime date) {
        communicationService.createEvent(title, description, date.toLocalDate());
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Tạo sự kiện thành công", null));
    }

    @GetMapping("/events/{eventId}/members")
    public ResponseEntity<ResponseObject> getMembersByEvent(@PathVariable String eventId) {
        return ResponseEntity.ok(new ResponseObject("SUCCESS", "Lấy danh sách thành viên tham gia sự kiện thành công", communicationService.getMembersByEvent(eventId)));
    }
}