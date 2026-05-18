package issueissyu.backend.domain.community.service.command;

import issueissyu.backend.domain.community.entity.Community;

public interface CommunityPopularityService {

    /* 특정 커뮤니티 게시글의 popularity를 현재 반응 수 기준으로 갱신한다.
     * 핀 기반 게시글은 Pin의 조회수/공감수/댓글/이모지 반응을 사용하고,
     * 핀이 없는 게시글은 Community의 조회수/공감수를 사용한다. */
    void updatePopularity(Community community);
}
