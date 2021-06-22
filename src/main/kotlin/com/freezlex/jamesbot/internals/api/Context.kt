package com.freezlex.jamesbot.internals.api

import com.freezlex.jamesbot.internals.indexer.Executable
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import java.lang.RuntimeException

/**
 * The context of the event
 * @param messageContext
 *          The command context if the command is executed with a classic message
 * @param slashContext
 *          The slash context if the command is executed with a slash command
 * @param invoked
 *          The invoked command
 */
class Context(val messageContext: MessageContext?,
              val slashContext: SlashContext?,
              val invoked: Executable){
    constructor(ctx: MessageContext, invoked: Executable): this(ctx, null, invoked)
    constructor(ctx: SlashContext, invoked: Executable): this(null, ctx, invoked)

    init {
        if(messageContext == null && slashContext == null)throw RuntimeException("The context must contain at least one SubContext")
    }

    fun reply(content: String) {
        if(messageContext != null)messageContext.event.message.reply(content).queue()
        else slashContext?.event?.reply(content)?.queue()
    }

    fun reply(content: MessageEmbed){
        if(messageContext != null)messageContext.event.message.reply(content).queue()
        else slashContext?.event?.channel?.sendMessage(content)?.queue()
    }

    fun getJda(): JDA {
        return if(isSlash())slashContext!!.event.jda else
            messageContext!!.event.jda
    }

    fun isSlash(): Boolean{
        if(slashContext != null) return true
        return false
    }
}
