package study.querydsl.reopsitory;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import study.querydsl.dto.MemberSearchCond;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import java.util.List;

import static io.micrometer.common.util.StringUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

public class MemberRepositoryImpl /*extends QuerydslRepositorySupport*/ implements MemberRepositoryCustom{

    /**
     * 일반적으로 빈 주입 받을 시
     */
    private final JPAQueryFactory query;

    public MemberRepositoryImpl(JPAQueryFactory query) {
        this.query = query;
    }


    /**
     * QuerydslRepositorySupport 사용 시
     */
   /* public MemberRepositoryImpl() {
        super(Member.class);
    }*/

    @Override
    public List<MemberTeamDto> search(MemberSearchCond cond) {

        /**
         * QuerydslRepositorySupport 사용 시
         */
        /*List<MemberTeamDto> result = from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe()))
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                )).fetch();*/

        /**
         * 평범하게 할 때
         */
        return query
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team,team)// .leftjoin(member.team) 도 가능
                .where(usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe()))
                .fetch();
    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCond cond, Pageable pageable) {
        QueryResults<MemberTeamDto> results = query
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)// .leftjoin(member.team) 도 가능
                .where(usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(member.id.desc())
                .fetchResults();

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<MemberTeamDto>(content,pageable,total);

    }

    /**
     * QuerydslRepositorySupport 사용 시
     *
     */
   /*public Page<MemberTeamDto> searchPageSimple2(MemberSearchCond cond, Pageable pageable) {


        JPQLQuery<MemberTeamDto> jpaQuery = from(member)
                .leftJoin(member.team, team)// .leftjoin(member.team) 도 가능
                .where(usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe()))
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ));

        JPQLQuery<MemberTeamDto> query = getQuerydsl().applyPagination(pageable, jpaQuery);

        QueryResults<MemberTeamDto> memberTeamDtoQueryResults = query.fetchResults();

        //List<MemberTeamDto> content = results.getResults();
       // long total = results.getTotal();

        //return new PageImpl<MemberTeamDto>(content,pageable,total);

    }*/

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCond cond, Pageable pageable) {
        List<MemberTeamDto> content = query
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)// .leftjoin(member.team) 도 가능
                .where(usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(member.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = query
                .select(member.count())
                .from(member)
                .leftJoin(member.team, team)// .leftjoin(member.team) 도 가능
                .where(usernameEq(cond.getUsername()),
                        teamNameEq(cond.getTeamName()),
                        ageGoe(cond.getAgeGoe()),
                        ageLoe(cond.getAgeLoe()));

        return PageableExecutionUtils.getPage(content,pageable,() -> Long.valueOf(countQuery.fetch().size()));

        //return new PageImpl<>(content,pageable,total);

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
