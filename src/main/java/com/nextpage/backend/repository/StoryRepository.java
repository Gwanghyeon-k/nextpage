package com.nextpage.backend.repository;

import com.nextpage.backend.entity.Story;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryRepository extends Neo4jRepository<Story,Long> {

    // 부모 관계가 없는 스토리(루트 스토리)를 가져오기
    @Query("MATCH (s:Story) WHERE NOT (s)<-[:PARENT_OF]-() RETURN s")
    List<Story> findRootStories();

    // 해당 스토리의 모든 후손을 가져오기
    @Query("MATCH p=(root:Story)-[:PARENT_OF*0..]->(child:Story) WHERE ID(root) = $rootId RETURN nodes(p)")
    List<Story> findAllChildrenByRootId(Long rootId);

    // 해당 스토리의 부모를 가져오기
    @Query("MATCH (child:Story)-[:PARENT_OF]->(parent:Story) WHERE ID(parent) = $childId RETURN child")
    Optional<Story> findParentByChildId(Long childId);

    // 해당 스토리의 모든 자식을 가져오기
    @Query("MATCH (parent:Story)-[:PARENT_OF]->(child:Story) WHERE ID(parent) = $parentId RETURN child")
    List<Story> findChildByParentId(Long parentId);

    // 특정 분기 조회를 위한 해당 스토리의 부모 재귀적으로 가져오기
    @Query("MATCH p=(child:Story)<-[:PARENT_OF*0..]-(parent:Story) WHERE ID(child) = $storyId RETURN nodes(p)")
    List<Story> findRecursivelyByLeafId(Long storyId);

    @Query("MATCH (s:Story {userNickname: $nickname}) RETURN s")
    List<Story> findStoriesByNickname(String nickname);
}
