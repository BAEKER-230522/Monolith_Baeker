package com.baeker.baeker.rule;

import com.baeker.baeker.base.request.RsData;
import com.baeker.baeker.studyRule.StudyRule;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RuleService {

    private final RuleRepository ruleRepository;


    /**
     * 생성
     */
    public RsData<Rule> create(RuleForm ruleForm) {
        Rule rule = Rule.builder()
                .name(ruleForm.getName())
                .about(ruleForm.getAbout())
                .xp(Integer.parseInt(ruleForm.getXp()))
                .provider(ruleForm.getProvider())
                .difficulty(ruleForm.getDifficulty())
                .build();
        ruleRepository.save(rule);
        return RsData.of("S-1", "Rule 생성 완료", rule);
    }

    /**
     * 수정
     */

    public void modify(Rule rule, RuleForm ruleForm) {
        Rule rule1 = rule.toBuilder()
                .name(ruleForm.getName())
                .about(ruleForm.getAbout())
                .provider(ruleForm.getProvider())
                .xp(Integer.parseInt(ruleForm.getXp()))
                .difficulty(ruleForm.getDifficulty())
                .build();
        ruleRepository.save(rule1);
        RsData.of("S-1", "규칙이 수정 되었습니다.", rule1);
    }
    public void setModify(Rule rule, RuleForm ruleForm) {
        ruleForm.setName(rule.getName());
        ruleForm.setAbout(rule.getAbout());
        ruleForm.setProvider(rule.getProvider());
        ruleForm.setDifficulty(rule.getDifficulty());
    }

    /**
     * 조회
     */

    public RsData<Rule> getRule(Long id) {
        Optional<Rule> rs = ruleRepository.findById(id);
        return rs.map(rule -> RsData.of("S-1", "Rule 조회 성공", rule))
                .orElseGet(() -> RsData.of("F-1", "Rule 조회 실패"));
    }

    public RsData<Rule> getRule(String name) {
        Optional<Rule> rs = ruleRepository.findByName(name);
        return rs.map(rule -> RsData.of("S-1", "Rule 조회 성공" ,rule))
                .orElseGet(() -> RsData.of("F-1", "Rule 조회 실패"));
    }

    public List<Rule> getRuleList() {
        return ruleRepository.findAll();
    }

    public Page<Rule> getList(int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return ruleRepository.findAll(pageable);
    }

    /**
     * 삭제
     */
    public void delete(Rule rule) {
        this.ruleRepository.delete(rule);
        RsData.of("S-1", "규칙이 삭제 되었습니다.");
    }


}
