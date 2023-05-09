package com.baeker.baeker.rule.api;

import com.baeker.baeker.rule.Rule;
import com.baeker.baeker.rule.RuleForm;
import com.baeker.baeker.rule.RuleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class RuleApiController {

    private final RuleService ruleService;

    /**
     * 생성
     */
    @PostMapping("/api/v1/rule")
    public CreateRuleResponse createRule(@RequestBody @Valid CreateRuleRequest request) {
        RuleForm ruleForm = new RuleForm(request.getName(), request.getAbout(), request.getXp().toString(),request.getCount().toString(), request.getProvider(), request.getDifficulty());
        Rule rule = ruleService.create(ruleForm).getData();

        return new CreateRuleResponse(rule.getId());
    }

    /**
     * 수정
     */
    @PutMapping("/api/v1/rule/{id}")
    public ModifyRuleResponse modifyRule(@PathVariable("id") Long id,
                                         @RequestBody @Valid ModifyRuleRequest request) {
        RuleForm ruleForm = new RuleForm(request.getName(), request.getAbout(), request.getXp().toString(),request.getCount().toString(), request.getProvider(), request.getDifficulty());
        ruleService.modify(id, ruleForm);
        Rule rule = ruleService.getRule(id).getData();

        return new ModifyRuleResponse(rule.getName(), rule.getAbout(), rule.getXp(),rule.getProvider() ,rule.getDifficulty());
    }

    /**
     * 조회
     */
    @GetMapping("/api/v1/rule/search")
    public Result searchRule() {
        List<Rule> rules = ruleService.getRuleList();
        List<RuleDto> collect = rules.stream()
                .map(m -> new RuleDto(m.getName(), m.getAbout(), m.getXp(), m.getProvider(), m.getDifficulty()))
                .toList();
        return new Result(collect);
    }

    /**
     * DTO Request
     */
    //== 생성 ==//
    @Data
    static class CreateRuleRequest {
        @NotEmpty
        private String name;

        private String about;

        private Integer xp;

        private Integer count;
        private String provider;

        private String difficulty;
    }

    //== 수정 ==//
    @Data
    static class ModifyRuleRequest {
        private String name;
        private String about;
        private Integer xp;

        private Integer count;
        private String provider;
        private String difficulty;
    }


    /**
     * DTO Response
     */

    //== 생성 ==//
    @Data
    @AllArgsConstructor
    static class CreateRuleResponse {
        private Long id;
    }

    //== 수정 ==//
    @Data
    @AllArgsConstructor
    static class ModifyRuleResponse {
        private String name;
        private String about;
        private Integer xp;
        private String provider;
        private String difficulty;
    }

    //== 조회 ==//
    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }

    /**
     * DTO
     */
    @Data
    @AllArgsConstructor
    static class RuleDto {
        private String name;
        private String about;
        private Integer xp;
        private String provider;
        private String difficulty;
    }
}
