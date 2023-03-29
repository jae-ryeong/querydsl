package study.querydsl.entity;

import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;   // 필드로 빼서 사용해도 좋다.

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL() {
        // member1을 찾아라
        Member findByJPQL = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1").getSingleResult();

        assertThat(findByJPQL.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl(){
        //QMember m = new QMember("m");   // variable "m"은 별칭같은것, 중요하지 않다.
        Member findMember = queryFactory.select(member).from(member)
                .where(member.username.eq("member1"))  // 파라미터 바인딩 처리
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() {
        Member findMember = queryFactory
                .selectFrom(member) // select와 from을 합쳤다
                .where(member.username.eq("member1").and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam() {
        Member findMember = queryFactory
                .selectFrom(member) // select와 from을 합쳤다
                .where(
                        member.username.eq("member1"),
                        (member.age.eq(10)) // AND인 경우에는 and로 자동 조립되기 때문에 위의 예제와 결과가 똑같다.
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch() {
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        Member fetchOne = queryFactory.selectFrom(member)
                .fetchOne();

        Member fetchFirst = queryFactory.selectFrom(member)
                .fetchFirst();// limit(1).fetchOne()와 같은 메서드
        
        /*
         https://velog.io/@nestour95/QueryDsl-fetchResults%EA%B0%80-deprecated-%EB%90%9C-%EC%9D%B4%EC%9C%A0
         fetchResult()는 사용되지 않으며 이렇게 해결
        */
    }

    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())  // 이름이 없으면 맨 마지막출력
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1() { // 조회 건수 제한
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)  // 어디서부터 시작할거냐
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void paging2() { // 전체 조회
        List<Member> results = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)  // 어디서부터 시작할거냐
                .limit(2)
                .fetch();

        int totalSize = queryFactory    // 강의의 queryResults.getTotal()
                .selectFrom(member)
                        .fetch().size();

        int getOffst = results.co;

        assertThat(totalSize).isEqualTo(4); // assertThat(queryResults.getTotal()).isEqualTo(4)
        assertThat(results.size()).isEqualTo(2); // assertThat(queryResults.getResults().size()).isEqualTo(2);
        assertThat(results.size()).isNotEqualTo(totalSize);
        System.out.println("results.size() = " + results.size());
        System.out.println("totalSize = " + totalSize);

    }










}
