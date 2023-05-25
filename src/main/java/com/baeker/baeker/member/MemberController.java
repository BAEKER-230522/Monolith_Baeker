package com.baeker.baeker.member;

import com.baeker.baeker.base.request.Rq;
import com.baeker.baeker.base.request.RsData;
import com.baeker.baeker.member.form.*;
import com.baeker.baeker.member.snapshot.MemberSnapshot;

import com.baeker.baeker.myStudy.MyStudy;
import com.baeker.baeker.myStudy.MyStudyService;
import com.baeker.baeker.myStudy.form.MyStudyInviteForm;
import com.baeker.baeker.myStudy.form.MyStudyModfyMsgForm;
import com.baeker.baeker.solvedApi.SolvedApiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MyStudyService myStudyService;
    private final SolvedApiService solvedApiService;
    private final Rq rq;


    //-- login --//
    @GetMapping("/login")
    @PreAuthorize("isAnonymous()")
    public String login(MemberLoginForm form) {
        log.info("login form 요청 확인");
        return "member/login";
    }

    //-- join form -- //
    @GetMapping("/join")
    @PreAuthorize("isAnonymous()")
    public String joinForm(MemberJoinForm form, Model model) {
        log.info("회원가입 폼 요청 확인");

        List<Integer> random = memberService.random();

        model.addAttribute("random", random);
        log.info("회원가입 폼 응답 완료");
        return "member/join";
    }

    //-- join 처리 --//
    @PostMapping("/join")
    @PreAuthorize("isAnonymous()")
    public String join(@Valid MemberJoinForm form) {
        log.info("회원가입 처리 요청 확인 username = {}, name = {}", form.getUsername(), form.getNickName());

        RsData<Member> memberRs = memberService.join(form);

        if (memberRs.isFail()) {
            log.info("회원가입 실패 error = {}", memberRs.getMsg());
            return rq.historyBack(memberRs);
        }

        log.info("회원가입 성공 username = {}", memberRs.getData().getUsername());
        return rq.redirectWithMsg("/member/login", memberRs.getMsg());
    }

    //-- my profile --//
    @GetMapping("/profile/{list}")
    @PreAuthorize("isAuthenticated()")
    public String myProfile(
            @PathVariable String list,
            MyStudyModfyMsgForm form,
            Model model
    ) {
        Member member = rq.getMember();
        log.info("내 프로필 요청 확인 member = {}", member.toString());

        if (member.isNewMember()) {
            log.info("신규 회원 확인, 개인정보 입력으로 redirect");
            return "redirect:/member/info";
        }

        List<MyStudy> myStudies = myStudyService.statusMember(member);
        List<MyStudy> pending = myStudyService.statusNotMember(member);
        List<MemberSnapshot> snapshotList = member.getSnapshotList();

        model.addAttribute("snapshotList", snapshotList);
        model.addAttribute("myStudies", myStudies);
        model.addAttribute("pending", pending);
        model.addAttribute("list", list);
        log.info("내 프로필 응답 완료");
        return "member/profile";
    }

    //-- 내정보 등록 form --//
    @GetMapping("/info")
    @PreAuthorize("isAuthenticated()")
    public String infoForm(
            MemberInfoForm form,
            Model model
    ) {
        Member member = rq.getMember();
        log.info("내정보 폼 요청 확인 id = {}", member.getId());

        form.setNickName(member.getNickName());
        form.setProfileImg(member.getProfileImg());
        List<Integer> random = memberService.random();

        model.addAttribute("random", random);
        log.info("내정보 폼 응답 완료");
        return "member/info";
    }

    //-- 내정보 등록 처리 --//
    @PostMapping("/info")
    @PreAuthorize("isAuthenticated()")
    public String info(
            MemberInfoForm form
    ) {
        Member member = rq.getMember();
        log.info("내정보 등록 처리 요청 확인 member id = {}", member.getId());

        RsData<Member> modifyRs = memberService.modify(member, form);

        if (modifyRs.isFail()) {
            log.info("정보 등록에 실패했습니다. error = {}", modifyRs.getMsg());
            return rq.historyBack(modifyRs.getMsg());
        }

        log.info("내정보 등록 성공");
        return "redirect:/member/connect";
    }

    //-- 백준 연동 폼 --//
    @GetMapping("/connect")
    @PreAuthorize("isAuthenticated()")
    public String connectForm(
            BaekJoonConnectForm form
    ) {
        Member member = rq.getMember();
        log.info("백준 연동 폼 요청 확인 member id = {}", member.getId());

        return "member/connect";
    }

    //-- 백준 연동 처리 --//
    @PostMapping("/connect")
    @PreAuthorize("isAuthenticated()")
    public String connect(
            BaekJoonConnectForm form
    ) throws IOException, ParseException {
        String baekJoonName = form.getBaekJoonName();
        log.info("백준 연동 요청 확인 BaekJoon id = {}", baekJoonName);

        boolean getUser = solvedApiService.findUser(baekJoonName);

        if (!getUser) {
            log.info("존재하지 않는 id 입니다.");
            return rq.historyBack( baekJoonName + "은(는) 존재하지 않는 id 입니다.");
        }
        RsData<Member> connectRs = memberService.connectBaekJoon(rq.getMember(), baekJoonName);
        solvedApiService.getSolvedCount(rq.getMember().getId());

        log.info("백준 연동 성공 BaakJoon id = {}", baekJoonName);
        return rq.redirectWithMsg("/", connectRs.getMsg());
    }

    //-- 백준 연동 이메일 인증 --// : 미완성
    @GetMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    public String connectVerifyForm(
            BaekJoonConnectForm form
    ) {
        log.info("백준 연동 이메일 인증 폼 요청 확인 백준 id = {}", form.getBaekJoonName());
        RsData<Member> getMemberRs = memberService.getByBaekJoonName(form.getBaekJoonName());

        if (getMemberRs.isSuccess()) {
            log.info("이미 연동되어있는 백준 id");
            return rq.historyBack("이미 연동되어있는 id 입니다.");
        }

        // solved ac 를 호출해 존재하는 id 인지 확인하고 해당 계정의 email 값을 반환

        int code = memberService.verifyCode();
        form.setSendCode(code);

        // 반환 받은 email 에 code 를 보냄

        log.info("인증 코드 발송 완료 code = {}", code);
        return "";
    }

    //-- 백준 연동 처리 --// : 미완성
    @PostMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    public String connectVerify(
            BaekJoonConnectForm form
    ) throws IOException, ParseException {
        log.info("백준 연동 처리 요청 확인");

        if (form.getSendCode() != form.getVerifyCode()) {
            log.info("인증 코드가 일치하지 않습니다.");
            return rq.historyBack("인증 코드가 일치하지 않습니다.");
        }
        Member member = rq.getMember();

        // solved ac 를 호출해 해결한 문제 수가 저장된 BaekJoonDto 를 반환받음
        solvedApiService.getSolvedCount(member.getId());
        RsData<Member> memberRs = memberService.connectBaekJoon(member, form.getBaekJoonName());
        if (memberRs.isFail()) {
            log.info("연동 실패 error = {}", memberRs.getMsg());
            return rq.historyBack(memberRs.getMsg());
        }

        log.info("백준 id 연동 성공 member id ={} / 백준 id = {}", member.getId(), form.getBaekJoonName());
        return rq.redirectWithMsg("", memberRs.getMsg());
    }


    //-- member profile --//
    @GetMapping("/member/{id}/{list}")
    public String profile(
            @PathVariable Long id,
            @PathVariable String list,
            MyStudyInviteForm form,
            Model model
    ) {
        log.info("맴버 프로필 요청 확인 member id = {}", id);

        RsData<Member> memberRs = memberService.getMember(id);

        if (memberRs.isFail()) {
            log.info("getMember(id) 조회 실패 msa = {}", memberRs.getMsg());
            return rq.historyBack(memberRs.getMsg());
        }

        // 자기 자신의 프로필일 경우 리다이렉트
        Member member = memberRs.getData();
        if (rq.isLogin())
            if (member.getNickName().equals(rq.getMember().getNickName()))
                return "redirect:/member/profile/rank";

        // 로그인일 때만 스터디 리스트 model 에 전달
        if (rq.isLogin()) {
            List<MyStudy> myStudies = myStudyService.getMyStudyOnlyLeader(rq.getMember()).getData();
            model.addAttribute("myStudies", myStudies);
        }

        List<MemberSnapshot> snapshotList = member.getSnapshotList();
        List<MyStudy> memberStudies = myStudyService.statusMember(member);

        model.addAttribute("memberStudies", memberStudies);
        model.addAttribute("snapshotList", snapshotList);
        model.addAttribute("member", member);
        model.addAttribute("list", list);

        log.info("member 조회 성공 member name = {}", member.getNickName());
        return "member/member";
    }

    //-- member list --//
    @GetMapping("/list")
    public String list(
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        log.info("member list 요청 확인");
        Page<Member> paging = memberService.getAll(page);

        model.addAttribute("paging", paging);
        log.info("member list 응답 완료");
        return "member/list";
    }

    //-- name, about, img 수정 폼 --//
    @GetMapping("/modify")
    @PreAuthorize("isAuthenticated()")
    public String modifyForm(
            MemberModifyForm form,
            Model model
    ) {
        Member member = rq.getMember();
        log.info("프로필 수정폼 요청 확인 member id = {}", member.getId());

        form.setNickName(member.getNickName());

        if (member.getAbout() != null)
            form.setAbout(member.getAbout());

        if (member.getProfileImg() != null)
            form.setProfileImg(member.getProfileImg());

        List<Integer> random = memberService.random();
        random.remove(0);

        model.addAttribute("random", random);
        log.info("프로필 수정폼 응답 완료");
        return "member/modify";
    }

    //-- 프로필 수정 처리 --//
    @PostMapping("/modify")
    @PreAuthorize("isAuthenticated()")
    public String modify(MemberModifyForm form) {
        log.info("프로필 수정 처리 요청 확인 form = {}", form);

        RsData<Member> memberRs = memberService.modify(rq.getMember(), form);

        if (memberRs.isFail()) {
            log.info("프로필 수정 실패 msg = {}", memberRs.getMsg());
            return rq.historyBack(memberRs.getMsg());
        }

        log.info("프로필 수정 완료");
        return rq.redirectWithMsg("/member/profile/rank", memberRs.getMsg());
    }

}