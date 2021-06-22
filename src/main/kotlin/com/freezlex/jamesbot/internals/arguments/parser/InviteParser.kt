package com.freezlex.jamesbot.internals.arguments.parser

import com.freezlex.jamesbot.internals.api.Context
import com.freezlex.jamesbot.internals.arguments.Parser
import com.freezlex.jamesbot.internals.entities.Invite
import java.util.*
import java.util.regex.Pattern

/**
 * The custom parser for the Invite type
 */
class InviteParser : Parser<Invite> {

    /**
     * Parse the argument
     * @param ctx
     *          The context of the event
     * @param param
     *          The params to parse
     */
    override fun parse(ctx: Context, param: String): Optional<Invite> {

        val match = INVITE_REGEX.matcher(param)

        if (match.find()) {
            val code = match.group(1)
            return Optional.of(Invite(ctx.getJda(), match.group(), code))
        }

        return Optional.empty()
    }

    companion object {
        val INVITE_REGEX = Pattern.compile("discord(?:(?:app)?\\.com/invite|\\.gg)/([a-zA-Z0-9]{1,16})")!!
    }
}
