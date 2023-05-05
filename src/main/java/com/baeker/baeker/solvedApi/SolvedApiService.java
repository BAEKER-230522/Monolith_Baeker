package com.baeker.baeker.solvedApi;

import com.baeker.baeker.base.event.BaekJoonEvent;
import com.baeker.baeker.member.Member;
import com.baeker.baeker.member.MemberService;
import com.baeker.baeker.member.embed.BaekJoonDto;
import com.baeker.baeker.member.snapshot.MemberSnapshot;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SolvedApiService {

    private final SolvedApiManager solvedApiManager;
    private final MemberService memberService;
    private final ApplicationEventPublisher publisher;

    /**
     * 난이도별 체크 후 문제풀이 수 리턴
     */
    public Integer getSolvedCount(Member member, Integer min, Integer max) throws IOException, ParseException {
        JSONArray test = this.solvedApiManager.getProblemCount(member);
        Integer solvedCount = 0;
        for (Object o : test) {
            JSONObject jsonObject = (JSONObject) o;
            if ((int)jsonObject.get("level") >= min && (int) jsonObject.get("level") < max) {
                solvedCount += (Integer) jsonObject.get("solved");
            }
        }

        return solvedCount;
    }

    /**
     * 수동으로 본인 기록 업데이트
     */
    public void getSolvedCount(Member member) throws IOException, ParseException {

        int Bronze = getSolvedCount(member, 1, 6) - member.getBronze();

        int Silver = getSolvedCount(member, 6, 11) - member.getSliver();

        int Gold = getSolvedCount(member, 11, 16) - member.getGold();

        int Platinum = getSolvedCount(member, 16, 21) - member.getPlatinum();

        int Diamond = getSolvedCount(member, 21, 26) - member.getDiamond();

        int Ruby = getSolvedCount(member, 26, 31) - member.getRuby();

        BaekJoonDto dto = new BaekJoonDto(Bronze, Silver, Gold, Platinum, Diamond, Ruby);
        publisher.publishEvent(new BaekJoonEvent(this, member, dto));
    }

    /**
     * 회원가입시 사용자 체크
     */
    public boolean findUser(String studyId) throws IOException, ParseException {
        try {
            String check = solvedApiManager.findUser(studyId);
        } catch (HttpClientErrorException e) {
            return false;
        }
        return true;
    }
}
