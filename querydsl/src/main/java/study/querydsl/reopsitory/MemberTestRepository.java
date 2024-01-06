package study.querydsl.reopsitory;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import study.querydsl.dto.MemberSearchCond;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.reopsitory.support.Querydsl4RepositorySupport;

import java.util.List;

import static io.micrometer.common.util.StringUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.team;

@Repository
public class MemberTestRepository extends Querydsl4RepositorySupport {


    public MemberTestRepository() {
        super(Member.class);
    }

    public List<Member> basicSelect(){
        return select(member)
                .from(member)
                .fetch();

    }

    public List<Member> basicSelectFrom(){
        return selectFrom(member)
                .fetch();
    }

    public Page<Member> searchPageByApplyPage(MemberSearchCond cond, Pageable pageable){
        JPAQuery<Member> query = selectFrom(member)
                .leftJoin(member.team,team)
                .where(usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe())
                );

        List<Member> content = getQuerydsl().applyPagination(pageable, query).fetch();
        return PageableExecutionUtils.getPage(content,pageable,() -> query.fetchCount());
    }

    public Page<Member> applyPagination(MemberSearchCond cond, Pageable pageable){
        Page<Member> result = applyPagination(pageable, query ->
                query.selectFrom(member)
                        .leftJoin(member.team,team)
                        .where(usernameEq(cond.getUsername()),
                                teamNameEq(cond.getTeamName()),
                                ageGoe(cond.getAgeGoe()),
                                ageLoe(cond.getAgeLoe())
                        )
        );
        return result;
    }
    public Page<Member> applyPagination2(MemberSearchCond cond, Pageable pageable){
        Page<Member> result = applyPagination(pageable, query ->
                query.selectFrom(member)
                        .leftJoin(member.team,team)
                        .where(usernameEq(cond.getUsername()),
                                teamNameEq(cond.getTeamName()),
                                ageGoe(cond.getAgeGoe()),
                                ageLoe(cond.getAgeLoe())
                        ),
                count -> count.selectFrom(member)
                        .leftJoin(member.team,team)
                        .where(usernameEq(cond.getUsername()),
                                teamNameEq(cond.getTeamName()),
                                ageGoe(cond.getAgeGoe()),
                                ageLoe(cond.getAgeLoe()))
        );
        return result;
    }

    private BooleanExpression ageBetween(int ageLoe, int ageGoe){
        return ageGoe(ageGoe).and(ageLoe(ageLoe));
    }
    private BooleanExpression usernameEq(String username) {
        return isEmpty(username) ? null : member.username.eq(username);
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe == null ? null : member.age.goe(ageGoe);
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}
