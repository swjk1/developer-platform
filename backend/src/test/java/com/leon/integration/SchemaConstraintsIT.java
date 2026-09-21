package com.leon.integration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * The database is the last line of defence. These assert that it refuses bad
 * states even if a future service method forgets to check them.
 */
@Transactional
class SchemaConstraintsIT extends IntegrationTestBase {

    @Autowired
    EntityManager em;

    @Test
    void aPublishedPostMustHaveAPublicationDate() {
        assertThatThrownBy(() -> {
            em.createNativeQuery("""
                    INSERT INTO blog_posts (slug, title, content, status, published_at)
                    VALUES ('bad', 'Bad', 'body', 'PUBLISHED', NULL)
                    """).executeUpdate();
            em.flush();
        }).hasMessageContaining("ck_blog_posts_published_has_date");
    }

    @Test
    void projectSlugsAreUnique() {
        assertThatThrownBy(() -> {
            em.createNativeQuery("""
                    INSERT INTO projects (slug, name, short_description)
                    VALUES ('keyguard', 'Duplicate', 'should fail')
                    """).executeUpdate();
            em.flush();
        }).hasMessageContaining("uq_projects_slug");
    }

    @Test
    void projectStatusIsConstrainedToTheKnownValues() {
        assertThatThrownBy(() -> {
            em.createNativeQuery("""
                    INSERT INTO projects (slug, name, short_description, status)
                    VALUES ('weird', 'Weird', 'bad status', 'SOMETHING_ELSE')
                    """).executeUpdate();
            em.flush();
        }).hasMessageContaining("ck_projects_status");
    }
}
