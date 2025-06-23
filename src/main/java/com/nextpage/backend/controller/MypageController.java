package com.nextpage.backend.controller;

import com.nextpage.backend.dto.response.BookmarkResponseDTO;
import com.nextpage.backend.dto.response.StoryListResponseDTO;
import com.nextpage.backend.result.ResultResponse;
import com.nextpage.backend.service.MypageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.nextpage.backend.result.ResultCode.*;

@Tag(name = "Mypage", description = "Mypage 관리")
@RestController
@RequestMapping("/api/v2/mypage") // 공통 api
public class MypageController {

    private final MypageService mypageService;

    public MypageController(MypageService mypageService) {
        this.mypageService = mypageService;
    }

    @Operation(summary = "내가 쓴 스토리 조회", description = "본인이 작성한 스토리를 조회합니다.")
    @GetMapping("/mystories")
    public ResponseEntity<ResultResponse> getStoriesByNickname(HttpServletRequest request) {
        List<StoryListResponseDTO> storiesByNickname = mypageService.getStoriesByNickname(request);
        return ResponseEntity.ok(ResultResponse.of(MYPAGE_MYSTORY_LIST_SUCCESS, storiesByNickname));
    }

    @Operation(summary = "북마크 목록 조회", description = "본인의 북마크 목록을 조회합니다.")
    @GetMapping("/bookmarks")
    public ResponseEntity<ResultResponse> getBookmarks(HttpServletRequest request) {
        List<BookmarkResponseDTO> bookmarks = mypageService.getBookmarks(request);
        return ResponseEntity.ok(ResultResponse.of(MYPAGE_BOOKMARK_LIST_SUCCESS, bookmarks));
    }

    @Operation(summary = "북마크 추가", description = "스토리를 북마크에 추가합니다.")
    @PostMapping("/bookmarks/{storyId}")
    public ResponseEntity<ResultResponse> addBookmark(HttpServletRequest request, @PathVariable Long storyId) {
        mypageService.addBookmark(request, storyId);
        return ResponseEntity.ok(ResultResponse.of(MYPAGE_BOOKMARK_ADD_SUCCESS));
    }

    @Operation(summary = "북마크 삭제", description = "스토리를 북마크에서 삭제합니다.")
    @DeleteMapping("/bookmarks/{storyId}")
    public ResponseEntity<ResultResponse> deleteBookmark(HttpServletRequest request, @PathVariable Long storyId) {
        mypageService.deleteBookmark(request, storyId);
        return ResponseEntity.ok(ResultResponse.of(MYPAGE_BOOKMARK_DELETE_SUCCESS));
    }
}
