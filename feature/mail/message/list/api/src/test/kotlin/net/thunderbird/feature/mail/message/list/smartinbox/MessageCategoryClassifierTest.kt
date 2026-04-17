package net.thunderbird.feature.mail.message.list.smartinbox

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class MessageCategoryClassifierTest {
    private val testSubject = MessageCategoryClassifier

    // --- Personal baseline ---

    @Test
    fun `classify plain personal address as Personal`() {
        val result = testSubject.classify(
            senderEmail = "alice@example.com",
            senderDisplayName = "Alice Jones",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Personal)
    }

    @Test
    fun `classify gmail-like plain address as Personal`() {
        val result = testSubject.classify(
            senderEmail = "bob.smith@gmail.com",
            senderDisplayName = "Bob Smith",
            subject = "Lunch Friday?",
        )

        assertThat(result).isEqualTo(MessageCategory.Personal)
    }

    // --- Rule 1: full newsletter-platform domain ---

    @Test
    fun `classify known newsletter platform domain as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "author@substack.com",
            senderDisplayName = "Author Name",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify subdomain of known newsletter platform as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "bounce@mail.beehiiv.com",
            senderDisplayName = "Publication",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify Mailchimp sending domain mcsv-dot-net as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "campaign@foo.mcsv.net",
            senderDisplayName = "Some Campaign",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify mailchimp list-manage tracking domain as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "click@em.list-manage.com",
            senderDisplayName = "Company",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify buttondown email domain as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "hi@buttondown.email",
            senderDisplayName = "Indie Writer",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    // --- Rule 2: marketing subdomain labels ---

    @Test
    fun `classify mail-subdomain as Newsletter via marketing subdomain rule`() {
        val result = testSubject.classify(
            senderEmail = "hello@mail.postman.com",
            senderDisplayName = "Postman",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify email-subdomain as Newsletter via marketing subdomain rule`() {
        val result = testSubject.classify(
            senderEmail = "team@email.example.com",
            senderDisplayName = "Example",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify mailer-subdomain as Newsletter via marketing subdomain rule`() {
        val result = testSubject.classify(
            senderEmail = "team@mailer.example.com",
            senderDisplayName = "Example",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify marketing-subdomain as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "team@marketing.example.com",
            senderDisplayName = "Example",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify news-subdomain as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "team@news.example.com",
            senderDisplayName = "Example",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify updates-subdomain as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "team@updates.example.com",
            senderDisplayName = "Example",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify newsletter-subdomain as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "team@newsletter.example.com",
            senderDisplayName = "Example",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify promo-subdomain as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "team@promo.example.com",
            senderDisplayName = "Example",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    // --- Rule 3: newsletter subject keywords ---

    @Test
    fun `classify subject containing newsletter keyword as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "hello@example.com",
            senderDisplayName = "Example",
            subject = "Our Latest Newsletter",
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify subject containing digest keyword as Newsletter case insensitive`() {
        val result = testSubject.classify(
            senderEmail = "hello@example.com",
            senderDisplayName = null,
            subject = "Product DIGEST for April",
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify subject containing weekly keyword as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "team@example.com",
            senderDisplayName = null,
            subject = "Your Weekly Update",
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify subject containing monthly keyword as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "team@example.com",
            senderDisplayName = null,
            subject = "Monthly summary",
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify subject containing roundup keyword as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "team@example.com",
            senderDisplayName = null,
            subject = "News roundup for April",
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify subject containing this week in phrase as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "team@example.com",
            senderDisplayName = null,
            subject = "This Week In AI",
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify subject containing percent off promo as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "team@example.com",
            senderDisplayName = null,
            subject = "Save 20% off your next order",
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify subject containing deals keyword as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "team@example.com",
            senderDisplayName = null,
            subject = "Best deals this week",
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify subject containing sale keyword as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "team@example.com",
            senderDisplayName = null,
            subject = "Spring Sale Starts Today",
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify subject containing offer keyword as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "team@example.com",
            senderDisplayName = null,
            subject = "Exclusive offer for you",
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify subject with wholesale does not match sale keyword as Personal`() {
        val result = testSubject.classify(
            senderEmail = "alice@example.com",
            senderDisplayName = "Alice",
            subject = "Wholesale prices attached",
        )

        assertThat(result).isEqualTo(MessageCategory.Personal)
    }

    @Test
    fun `classify subject with offered does not match offer keyword as Personal`() {
        val result = testSubject.classify(
            senderEmail = "alice@example.com",
            senderDisplayName = "Alice",
            subject = "I was offered the role",
        )

        assertThat(result).isEqualTo(MessageCategory.Personal)
    }

    // --- Rule 4: display-name newsletter keyword ---

    @Test
    fun `classify display-name containing Newsletter keyword as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "updates@example.com",
            senderDisplayName = "Acme Weekly Newsletter",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify display-name containing Digest keyword as Newsletter case insensitive`() {
        val result = testSubject.classify(
            senderEmail = "hello@example.com",
            senderDisplayName = "Product DIGEST",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify display-name containing weekly keyword as Newsletter`() {
        val result = testSubject.classify(
            senderEmail = "team@example.com",
            senderDisplayName = "My Weekly Brief",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    // --- Rule 5: transactional subdomain labels ---

    @Test
    fun `classify notify-subdomain as Notification`() {
        val result = testSubject.classify(
            senderEmail = "service@notify.stripe.com",
            senderDisplayName = "Stripe",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify notifications-subdomain as Notification`() {
        val result = testSubject.classify(
            senderEmail = "hi@notifications.example.com",
            senderDisplayName = "Example",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify alerts-subdomain as Notification`() {
        val result = testSubject.classify(
            senderEmail = "service@alerts.example.com",
            senderDisplayName = "Example",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify receipts-subdomain as Notification`() {
        val result = testSubject.classify(
            senderEmail = "hi@receipts.example.com",
            senderDisplayName = "Example",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify billing-subdomain as Notification`() {
        val result = testSubject.classify(
            senderEmail = "hi@billing.example.com",
            senderDisplayName = "Example",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify security-subdomain as Notification`() {
        val result = testSubject.classify(
            senderEmail = "hi@security.example.com",
            senderDisplayName = "Example",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify account-subdomain as Notification`() {
        val result = testSubject.classify(
            senderEmail = "hi@account.example.com",
            senderDisplayName = "Example",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    // --- Rule 6: transactional subject keywords ---

    @Test
    fun `classify subject containing receipt keyword as Notification`() {
        val result = testSubject.classify(
            senderEmail = "hello@example.com",
            senderDisplayName = "Example",
            subject = "Your receipt from Foo",
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify subject containing order keyword as Notification`() {
        val result = testSubject.classify(
            senderEmail = "hello@example.com",
            senderDisplayName = "Example",
            subject = "Your order has shipped",
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify subject containing shipped keyword as Notification`() {
        val result = testSubject.classify(
            senderEmail = "hello@example.com",
            senderDisplayName = "Example",
            subject = "It shipped",
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify subject containing delivered keyword as Notification`() {
        val result = testSubject.classify(
            senderEmail = "hello@example.com",
            senderDisplayName = "Example",
            subject = "Your package was delivered",
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify subject containing invoice keyword as Notification`() {
        val result = testSubject.classify(
            senderEmail = "hello@example.com",
            senderDisplayName = "Example",
            subject = "Invoice 12345 ready",
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify subject containing payment keyword as Notification`() {
        val result = testSubject.classify(
            senderEmail = "hello@example.com",
            senderDisplayName = "Example",
            subject = "Payment received",
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify subject containing confirmation keyword as Notification`() {
        val result = testSubject.classify(
            senderEmail = "hello@example.com",
            senderDisplayName = "Example",
            subject = "Booking confirmation",
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify subject containing password keyword as Notification`() {
        val result = testSubject.classify(
            senderEmail = "hello@example.com",
            senderDisplayName = "Example",
            subject = "Reset your password",
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify subject containing sign-in hyphenated keyword as Notification`() {
        val result = testSubject.classify(
            senderEmail = "hello@example.com",
            senderDisplayName = "Example",
            subject = "New sign-in from Chrome",
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify subject containing sign in phrase as Notification`() {
        val result = testSubject.classify(
            senderEmail = "hello@example.com",
            senderDisplayName = "Example",
            subject = "Please sign in to continue",
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify subject containing verify keyword as Notification`() {
        val result = testSubject.classify(
            senderEmail = "hello@example.com",
            senderDisplayName = "Example",
            subject = "Please verify your email",
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify subject containing verification code phrase as Notification`() {
        val result = testSubject.classify(
            senderEmail = "hello@example.com",
            senderDisplayName = "Example",
            subject = "Your verification code is 123456",
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify subject with verification without code does not match verification code phrase`() {
        val result = testSubject.classify(
            senderEmail = "alice@example.com",
            senderDisplayName = "Alice",
            subject = "Some verification notes",
        )

        assertThat(result).isEqualTo(MessageCategory.Personal)
    }

    // --- Rule 7: notification local-part ---

    @Test
    fun `classify noreply local-part as Notification`() {
        val result = testSubject.classify(
            senderEmail = "noreply@example.com",
            senderDisplayName = "Example Service",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify no-reply local-part as Notification`() {
        val result = testSubject.classify(
            senderEmail = "no-reply@example.com",
            senderDisplayName = null,
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify donotreply local-part as Notification`() {
        val result = testSubject.classify(
            senderEmail = "donotreply@bank.example",
            senderDisplayName = "Bank",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify notifications local-part as Notification`() {
        val result = testSubject.classify(
            senderEmail = "notifications@github.com",
            senderDisplayName = "GitHub",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify alerts local-part as Notification`() {
        val result = testSubject.classify(
            senderEmail = "alerts@chase.example",
            senderDisplayName = "Chase",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify noreply with separator suffix as Notification`() {
        val result = testSubject.classify(
            senderEmail = "noreply-status@example.com",
            senderDisplayName = "Service",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify notifications plus-suffix local-part as Notification`() {
        val result = testSubject.classify(
            senderEmail = "notifications+abc123@example.com",
            senderDisplayName = "Service",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify alert exact-match local-part as Notification`() {
        val result = testSubject.classify(
            senderEmail = "alert@statuscake.example",
            senderDisplayName = "StatusCake",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify alertme local-part not matching alert word does not hit notification rule`() {
        val result = testSubject.classify(
            senderEmail = "alertme@example.com",
            senderDisplayName = "A Person",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Personal)
    }

    @Test
    fun `classify noreply suffix after hyphen as Notification`() {
        // Regression: businessprofile-noreply@google.com was previously missed because the rule
        // only matched patterns at the start of the local-part.
        val result = testSubject.classify(
            senderEmail = "businessprofile-noreply@google.com",
            senderDisplayName = "Google",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify noreply suffix with multiple hyphen segments as Notification`() {
        // Regression: drive-shares-dm-noreply@google.com — another real-world example.
        val result = testSubject.classify(
            senderEmail = "drive-shares-dm-noreply@google.com",
            senderDisplayName = "Google Drive",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify noreply suffix after dot as Notification`() {
        val result = testSubject.classify(
            senderEmail = "support.noreply@example.com",
            senderDisplayName = "Example",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify noreply suffix without separator does not match as Personal`() {
        // "somenoreply" has no separator before `noreply` — shouldn't fire the suffix rule.
        val result = testSubject.classify(
            senderEmail = "somenoreply@example.com",
            senderDisplayName = "A Person",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Personal)
    }

    // --- Precedence interactions ---

    @Test
    fun `classify notifications local-part at mail marketing subdomain as Newsletter`() {
        // Critical: marketing-subdomain rule (2) must fire before local-part rule (7).
        val result = testSubject.classify(
            senderEmail = "notifications@mail.postman.com",
            senderDisplayName = "Postman",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify noreply local-part at notify transactional subdomain as Notification via subdomain rule`() {
        val result = testSubject.classify(
            senderEmail = "noreply@notify.stripe.com",
            senderDisplayName = "Stripe",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify newsletter platform domain beats transactional subdomain prefix via full-domain rule`() {
        // substack.com matches the full NEWSLETTER_DOMAINS set (rule 1) even though the leftmost
        // subdomain label "notify" would be a transactional subdomain (rule 5) on an unknown domain.
        val result = testSubject.classify(
            senderEmail = "author@notify.substack.com",
            senderDisplayName = "Author",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify noreply at known newsletter domain as Newsletter not Notification`() {
        val result = testSubject.classify(
            senderEmail = "noreply@substack.com",
            senderDisplayName = "Author",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify newsletter subject beats transactional subdomain`() {
        // subject newsletter keyword (rule 3) fires before transactional subdomain (rule 5).
        val result = testSubject.classify(
            senderEmail = "team@notify.example.com",
            senderDisplayName = "Example",
            subject = "Your Weekly Digest",
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify newsletter subject beats notification local-part`() {
        val result = testSubject.classify(
            senderEmail = "noreply@example.com",
            senderDisplayName = null,
            subject = "Your Weekly Digest",
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify display-name newsletter keyword beats transactional subdomain`() {
        // Rule 4 (display-name) fires before rule 5 (transactional subdomain).
        val result = testSubject.classify(
            senderEmail = "team@notify.example.com",
            senderDisplayName = "The Daily Digest",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify display-name newsletter keyword overrides noreply local-part`() {
        val result = testSubject.classify(
            senderEmail = "noreply@foo.example",
            senderDisplayName = "The Daily Digest",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify transactional subdomain beats transactional subject when sender matches first`() {
        // Not a meaningful conflict — just verifies rule ordering doesn't lead to a Personal result.
        val result = testSubject.classify(
            senderEmail = "hello@billing.example.com",
            senderDisplayName = "Example",
            subject = "Your receipt",
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    @Test
    fun `classify transactional subject beats local-part when local-part is clean`() {
        val result = testSubject.classify(
            senderEmail = "alice@example.com",
            senderDisplayName = "Alice",
            subject = "Order shipped",
        )

        assertThat(result).isEqualTo(MessageCategory.Notification)
    }

    // --- Edge cases ---

    @Test
    fun `classify null email and null display name and null subject as Personal`() {
        val result = testSubject.classify(
            senderEmail = null,
            senderDisplayName = null,
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Personal)
    }

    @Test
    fun `classify malformed email without at sign as Personal`() {
        val result = testSubject.classify(
            senderEmail = "not-an-email",
            senderDisplayName = "Whatever",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Personal)
    }

    @Test
    fun `classify sender email with uppercase characters as Newsletter when domain matches`() {
        val result = testSubject.classify(
            senderEmail = "Author@Substack.Com",
            senderDisplayName = "Author",
            subject = null,
        )

        assertThat(result).isEqualTo(MessageCategory.Newsletter)
    }

    @Test
    fun `classify empty subject is treated as absent subject`() {
        val result = testSubject.classify(
            senderEmail = "alice@example.com",
            senderDisplayName = "Alice",
            subject = "",
        )

        assertThat(result).isEqualTo(MessageCategory.Personal)
    }
}
