package com.spring.service.repository;

import com.spring.data.domain.BaseTerminal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.stream.Stream;

public interface TerminalRepository extends ElasticsearchRepository<BaseTerminal, String>, TerminalRepositoryCustom {

    List<BaseTerminal> findByLineNoBetweenOrderByUpdateTimeDesc(Integer minLineNo, Integer maxLineNo);

    @Query("{\"bool\":{\"must\":[{\"match\":{\"terminalCode\":\"?0\"}},{\"match\":{\"terminalName\":\"?1\"}}],\"must_not\":{\"match\":{\"lineNo\":?2}}}}")
    Page<BaseTerminal> findByTerminalCodeAndTerminalNameAndNotLineNo(String terminalCode, String terminalName, Integer lineNo, Pageable pageable);

    Page<BaseTerminal> findByTerminalCode(String terminalCode, Pageable pageable);

    Stream<BaseTerminal> findByLineNo(Integer lineNo);

    @Query("{\"bool\":{\"must\":[{\"match\":{\"terminalName\":\"?0\"}},{\"script\":{\"script\":{\"inline\":\"doc.lineNo.value * 2 <?1\"}}}]}}")
    List<BaseTerminal> findByCustom(String terminalName, Integer lineNo);
}
