package net.thunderbird.feature.mail.message.list.smartinbox

/**
 * Classifies a message into [MessageCategory] using sender-pattern and subject-pattern
 * heuristics only. Pure Kotlin — no Android dependencies.
 *
 * RFC-defined header signals (`List-ID`, `List-Unsubscribe`, `Precedence`,
 * `Auto-Submitted`) are stronger but require raw headers at classify time, which are
 * not available in the current mailstore projection. Deferred to v0.2 — once plumbed,
 * header rules take precedence over both subject keywords and sender heuristics, and
 * the subject-keyword rules (noisy by construction) should be retired.
 */
object MessageCategoryClassifier {

    private val NEWSLETTER_DOMAINS: Set<String> = setOf(
        "substack.com",
        "beehiiv.com",
        "mailchimp.com",
        "mcsv.net",
        "list-manage.com",
        "mailerlite.com",
        "ghost.io",
        "convertkit.com",
        "convertkit-mail.com",
        "constantcontact.com",
        "ccsend.com",
        "buttondown.email",
        "buttondown.com",
    )

    private val NEWSLETTER_SUBDOMAIN_LABELS: Set<String> = setOf(
        "mail",
        "email",
        "mailer",
        "marketing",
        "news",
        "updates",
        "newsletter",
        "promo",
    )

    private val NOTIFICATION_SUBDOMAIN_LABELS: Set<String> = setOf(
        "notify",
        "notifications",
        "alerts",
        "receipts",
        "billing",
        "security",
        "account",
    )

    private val NEWSLETTER_DISPLAY_NAME_KEYWORDS: List<String> = listOf(
        "newsletter",
        "digest",
        "weekly",
    )

    private val NEWSLETTER_SUBJECT_KEYWORDS: List<String> = listOf(
        "newsletter",
        "digest",
        "weekly",
        "monthly",
        "roundup",
        "this week in",
        "% off",
        "deals",
        "sale",
        "offer",
    )

    private val NOTIFICATION_SUBJECT_KEYWORDS: List<String> = listOf(
        "receipt",
        "order",
        "shipped",
        "delivered",
        "invoice",
        "payment",
        "confirmation",
        "password",
        "sign-in",
        "sign in",
        "verify",
        "verification code",
    )

    private val NOTIFICATION_LOCAL_PARTS: List<String> = listOf(
        "noreply",
        "no-reply",
        "donotreply",
        "do-not-reply",
        "notifications",
        "notification",
        "alerts",
        "alert",
    )

    private val LOCAL_PART_SEPARATORS: Set<Char> = setOf('-', '_', '+', '.')

    private val NEWSLETTER_SUBJECT_KEYWORD_REGEXES: List<Regex> =
        NEWSLETTER_SUBJECT_KEYWORDS.map { it.toKeywordRegex() }

    private val NOTIFICATION_SUBJECT_KEYWORD_REGEXES: List<Regex> =
        NOTIFICATION_SUBJECT_KEYWORDS.map { it.toKeywordRegex() }

    /**
     * Classify a message by sender and subject. First match wins.
     *
     *   1. Sender domain is a known newsletter platform            -> Newsletter
     *   2. Leftmost domain label is a marketing subdomain          -> Newsletter
     *      (mail, email, mailer, marketing, news, updates, newsletter, promo)
     *   3. Subject contains a newsletter keyword                   -> Newsletter
     *   4. Display name contains a newsletter keyword              -> Newsletter
     *   5. Leftmost domain label is a transactional subdomain      -> Notification
     *      (notify, notifications, alerts, receipts, billing, security, account)
     *   6. Subject contains a transactional keyword                -> Notification
     *   7. Sender local-part is an automated-sender token          -> Notification
     *   8. Otherwise                                                -> Personal
     *
     * Newsletter wins in any Newsletter/Notification overlap — e.g.
     * `notifications@mail.postman.com` resolves to Newsletter via rule 2 rather than
     * Notification via rule 7.
     */
    fun classify(
        senderEmail: String?,
        senderDisplayName: CharSequence?,
        subject: CharSequence?,
    ): MessageCategory {
        val normalizedEmail = senderEmail?.trim()?.lowercase().orEmpty()
        val atIndex = normalizedEmail.indexOf('@')
        val hasValidSplit = atIndex in 1..normalizedEmail.length - 2
        val localPart = if (hasValidSplit) normalizedEmail.substring(0, atIndex) else ""
        val domain = if (hasValidSplit) normalizedEmail.substring(atIndex + 1) else ""
        val leftmostLabel = domain.leftmostLabel()

        if (domain.matchesKnownDomain(NEWSLETTER_DOMAINS)) return MessageCategory.Newsletter

        if (leftmostLabel.isNotEmpty() && leftmostLabel in NEWSLETTER_SUBDOMAIN_LABELS) {
            return MessageCategory.Newsletter
        }

        if (subject.matchesAnyKeyword(NEWSLETTER_SUBJECT_KEYWORD_REGEXES)) {
            return MessageCategory.Newsletter
        }

        val displayNameLower = senderDisplayName?.toString()?.lowercase().orEmpty()
        if (NEWSLETTER_DISPLAY_NAME_KEYWORDS.any { displayNameLower.contains(it) }) {
            return MessageCategory.Newsletter
        }

        if (leftmostLabel.isNotEmpty() && leftmostLabel in NOTIFICATION_SUBDOMAIN_LABELS) {
            return MessageCategory.Notification
        }

        if (subject.matchesAnyKeyword(NOTIFICATION_SUBJECT_KEYWORD_REGEXES)) {
            return MessageCategory.Notification
        }

        if (localPart.isNotificationLocalPart()) return MessageCategory.Notification

        return MessageCategory.Personal
    }

    private fun String.matchesKnownDomain(known: Set<String>): Boolean =
        known.any { this == it || this.endsWith(".$it") }

    private fun String.leftmostLabel(): String {
        if (this.isEmpty()) return ""
        val dotIndex = this.indexOf('.')
        return when {
            dotIndex < 0 -> this
            dotIndex == 0 -> ""
            else -> this.substring(0, dotIndex)
        }
    }

    private fun String.isNotificationLocalPart(): Boolean =
        NOTIFICATION_LOCAL_PARTS.any { pattern ->
            this == pattern ||
                (
                    this.length > pattern.length &&
                        this.startsWith(pattern) &&
                        this[pattern.length] in LOCAL_PART_SEPARATORS
                    )
        }

    private fun CharSequence?.matchesAnyKeyword(regexes: List<Regex>): Boolean {
        if (this.isNullOrEmpty()) return false
        val text = this.toString()
        return regexes.any { it.containsMatchIn(text) }
    }
}

/**
 * Builds a case-insensitive regex that requires a word boundary at each end *only when* the
 * boundary character is alphanumeric. This lets short lexical keywords like `sale` refuse to
 * match `wholesale` while still letting phrases like `% off` — whose leading character is a
 * non-word symbol — match naturally.
 */
private fun String.toKeywordRegex(): Regex {
    val escaped = Regex.escape(this)
    val startBoundary = if (this.first().isLetterOrDigit()) "\\b" else ""
    val endBoundary = if (this.last().isLetterOrDigit()) "\\b" else ""
    return Regex("$startBoundary$escaped$endBoundary", RegexOption.IGNORE_CASE)
}
