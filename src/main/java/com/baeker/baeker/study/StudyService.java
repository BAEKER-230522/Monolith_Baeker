package com.baeker.baeker.study;

import com.baeker.baeker.base.request.RsData;
import com.baeker.baeker.member.Member;
import com.baeker.baeker.member.embed.BaekJoonDto;
import com.baeker.baeker.myStudy.MyStudy;
import com.baeker.baeker.myStudy.MyStudyRepository;
import com.baeker.baeker.study.form.StudyCreateForm;
import com.baeker.baeker.study.form.StudyModifyForm;
import com.baeker.baeker.study.snapshot.StudySnapShot;
import com.baeker.baeker.study.snapshot.StudySnapShotRepository;
import com.baeker.baeker.studyRule.StudyRule;
import com.baeker.baeker.studyRule.StudyRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final StudySnapShotRepository studySnapShotRepository;


    /**
     ** 생성 관련 method **
     * create
     */
    //-- create --//
    @Transactional
    public RsData<Study> create(StudyCreateForm form, Member member) {

        // 스터디 이름 중복검사
        RsData<Study> studyRs = this.getStudy(form.getName());
        if (studyRs.isSuccess())
            return RsData.of("F-1", "는 이미 사용 중입니다.");

        Study study = Study.createStudy(form.getName(), form.getAbout(), form.getCapacity(), member);
        Study saveStudy = studyRepository.save(study);

        return RsData.of("S-1", "새로운 스터디가 개설되었습니다!", saveStudy);
    }


    /**
     ** 조회 관련 method **
     * find by id
     * find by study name
     * find member in study by member id, study id
     * find all member in study
     * find all
     * find all + paging
     */

    //-- find by id --//
    public RsData<Study> getStudy(Long id) {
        Optional<Study> byId = studyRepository.findById(id);

        if (byId.isPresent())
            return RsData.successOf(byId.get());

        return RsData.of("F-1", "존재 하지않는 id");
    }

    //-- find by name --//
    public RsData<Study> getStudy(String name) {
        Optional<Study> byName = studyRepository.findByName(name);

        if (byName.isPresent())
            return RsData.successOf(byName.get());

        return RsData.of("F-1", "존재 하지않는 name");
    }

    //-- 스터디원 id 로 member 찾기 --//
    public RsData<Member> getMember(Long memberId,Long studyId) {
        RsData<List<Member>> studyMemberRs = this.getAllMember(studyId);

        if (studyMemberRs.isFail())
            return RsData.of("F-1", studyMemberRs.getMsg());

        List<Member> members = studyMemberRs.getData();

        for (Member member : members) {
            if (member.getId() == memberId)
                return RsData.successOf(member);
        }

        return RsData.of("F-2", "스터디에 존재하지 않는 회원입니다.");
    }

    //-- find all member --//
    public RsData<List<Member>> getAllMember(Long id) {
        List<Member> members = new ArrayList<>();
        RsData<Study> studyRs = this.getStudy(id);

        if (studyRs.isFail()) return RsData.of("F-1", studyRs.getMsg());
        List<MyStudy> myStudies = studyRs.getData().getMyStudies();

        for (MyStudy myStudy : myStudies) {
            members.add(myStudy.getMember());
        }
        return RsData.successOf(members);
    }

    //-- find all + page --//
    public Page<Study> getAll(int page) {
        ArrayList<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("xp"));

        PageRequest pageable = PageRequest.of(page, 5, Sort.by(sorts));
        return studyRepository.findAll(pageable);
    }

    //-- find all --//
    public List<Study> getAll() {
        return studyRepository.findAll();
    }


    /**
     ** 수정과 삭제 관련 method **
     * 이름, 소개, 최대 인원 수정
     * leader 변경
     * study 가입시 member 의 백준 solved 추가
     * Xp 상승
     * Study 삭제
     */

    //-- 이름, 소개, 최대 인원 수정 --//
    @Transactional
    public RsData<Study> modify(StudyModifyForm form, Long id) {
        RsData<Study> studyRs = this.getStudy(id);

        if (studyRs.isFail()) return studyRs;
        Study study = studyRs.getData();

        if (study.getMyStudies().size() > form.getCapacity())
            return RsData.of("F-2", "최대 인원이 현재 스터디 인원보다 적습니다.");

        Study modifyStudy = study.modifyStudy(form.getName(), form.getAbout(), form.getCapacity());
        studyRepository.save(modifyStudy);
        return RsData.of("S-1", "수정이 완료되었습니다.", modifyStudy);
    }

    //-- 리더 변경 --//
    @Transactional
    public RsData<Study> modifyLeader(Member member, Long id) {
        RsData<Study> studyRs = this.getStudy(id);

        if (studyRs.isFail()) return studyRs;
        Study study = studyRs.getData();

        Study modifyLeader = study.modifyLeader(member.getNickName());
        studyRepository.save(modifyLeader);
        return RsData.of("S-1", "리더가 변경되었습니다.", modifyLeader);
    }

    //-- 스터디 가입시 맴버의 백준 문제 추가 --//
    @Transactional
    public Study addBaekJoon(Study study, Member member) {
        Study updateStudy = study.addBaekJoon(member);
        Study saveStudy = studyRepository.save(updateStudy);
        return saveStudy;
    }

    //-- 경험치 상승 --//
    @Transactional
    public RsData<Study> xpUp(Integer xp, Long id) {
        RsData<Study> studyRs = this.getStudy(id);

        if (studyRs.isFail()) return studyRs;
        Study study = studyRs.getData();

        study.xpUp(xp);
        return RsData.of("S-1", "경험치가 상승했습니다.", study);
    }

    //-- 스터디 삭제 --//
    @Transactional
    public RsData<Study> delete(Long id) {
        RsData<Study> studyRs = this.getStudy(id);

        if (studyRs.isFail())
            return studyRs;

        studyRepository.delete(studyRs.getData());
        return RsData.of("S-1", "스터디가 삭제되었습니다.");
    }


    /**
     ** Snapshot 과 Event 처리 관련 Method **
     * Snapshot 저장
     * 백준 Solved Evnet 처리
     */

    //-- 스냅샷 저장 --//
    private void saveSnapshot(StudySnapShot snapShot) {
        studySnapShotRepository.save(snapShot);
    }

    //-- 백준 스케쥴 이벤트 처리 --//
    @Transactional
    public void whenBaekJoonEventType(Member member, BaekJoonDto dto) {
        List<MyStudy> myStudies = member.getMyStudies();

        for (MyStudy myStudy : myStudies) {
            Study study = myStudy.getStudy();
            StudySnapShot snapShot = StudySnapShot.create(study);

            study.updateBaekJoon(dto);

            saveSnapshot(snapShot);
        }
    }
}
