/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.query;

import java.util.Arrays;

import org.hibernate.cfg.AvailableSettings;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.jdbc.SQLStatementInspector;
import org.hibernate.testing.orm.junit.EntityManagerFactoryScope;
import org.hibernate.testing.orm.junit.Jpa;
import org.hibernate.testing.orm.junit.Setting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import static org.junit.Assert.assertTrue;


/**
 * @author Vlad Mihalcea
 */
@TestForIssue(jiraKey = "HHH-12469")
@Jpa(
		annotatedClasses = { InClauseParameterPaddingTest.Person.class },
		integrationSettings = {
				@Setting(name = AvailableSettings.USE_SQL_COMMENTS, value = "true"),
				@Setting(name = AvailableSettings.IN_CLAUSE_PARAMETER_PADDING, value = "true")
		},
		statementInspectorClass = SQLStatementInspector.class

)
public class InClauseParameterPaddingTest {

	@BeforeEach
	protected void afterEntityManagerFactoryBuilt(EntityManagerFactoryScope scope) {
		scope.inTransaction( entityManager -> {
			for ( int i = 1; i < 10; i++ ) {
				Person person = new Person();
				person.setId( i );
				person.setName( String.format( "Person nr %d", i ) );

				entityManager.persist( person );
			}
		} );
	}

	@Test
	public void testInClauseParameterPadding(EntityManagerFactoryScope scope) {
		validateInClauseParameterPadding( scope, "in(?)", 1 );
		validateInClauseParameterPadding( scope, "in(?,?)", 1, 2 );
		validateInClauseParameterPadding( scope, "in(?,?,?,?)", 1, 2, 3 );
		validateInClauseParameterPadding( scope, "in(?,?,?,?)", 1, 2, 3, 4 );
		validateInClauseParameterPadding( scope, "in(?,?,?,?,?,?,?,?)", 1, 2, 3, 4, 5 );
		validateInClauseParameterPadding( scope, "in(?,?,?,?,?,?,?,?)", 1, 2, 3, 4, 5, 6 );
		validateInClauseParameterPadding( scope, "in(?,?,?,?,?,?,?,?)", 1, 2, 3, 4, 5, 6, 7 );
		validateInClauseParameterPadding( scope, "in(?,?,?,?,?,?,?,?)", 1, 2, 3, 4, 5, 6, 7, 8 );
		validateInClauseParameterPadding( scope, "in(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 1, 2, 3, 4, 5, 6, 7, 8, 9 );
		validateInClauseParameterPadding( scope, "in(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 );
	}

	private void validateInClauseParameterPadding(
			EntityManagerFactoryScope scope,
			String expectedInClause,
			Integer... ids) {
		final SQLStatementInspector sqlStatementInterceptor = (SQLStatementInspector) scope.getStatementInspector();
		sqlStatementInterceptor.clear();

		scope.inTransaction( entityManager -> {
			entityManager.createQuery(
							"select p " +
									"from Person p " +
									"where p.id in :ids" )
					.setParameter( "ids", Arrays.asList( ids ) )
					.getResultList();
		} );

		assertTrue( sqlStatementInterceptor.getSqlQueries().get( 0 ).endsWith( expectedInClause ) );
	}

	@Entity(name = "Person")
	public static class Person {

		@Id
		private Integer id;

		private String name;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

}
