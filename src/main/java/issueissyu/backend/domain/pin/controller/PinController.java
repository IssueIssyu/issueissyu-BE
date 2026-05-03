package issueissyu.backend.domain.pin.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pins")
@RequiredArgsConstructor
@Tag(name = "Pin", description = "핀 상세/댓글")
public class PinController {
}
