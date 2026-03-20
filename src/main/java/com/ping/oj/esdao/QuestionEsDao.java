package com.ping.oj.esdao;

import com.ping.oj.model.dto.post.PostEsDTO;
import com.ping.oj.model.dto.question.QuestionEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 题目 ES 操作
 **/
public interface QuestionEsDao extends ElasticsearchRepository<QuestionEsDTO, Long> {

    List<PostEsDTO> findByUserId(Long userId);
}