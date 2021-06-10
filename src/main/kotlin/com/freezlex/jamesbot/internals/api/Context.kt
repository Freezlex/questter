package com.freezlex.jamesbot.internals.api

import com.freezlex.jamesbot.internals.indexer.Executable
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.regex.Pattern

class Context (
    event: MessageReceivedEvent,
    val trigger: MatchResult?,
    val invokedCommand: Executable
) {
    val jda: JDA = event.jda
    val message: Message = event.message

    val author: User = event.author

    val guild: Guild? = if (event.isFromGuild) event.guild else null
    val member: Member? = event.member

    val textChannel: TextChannel? = if (event.isFromType(ChannelType.TEXT)) event.textChannel else null
    val privateChannel: PrivateChannel? = if (event.isFromType(ChannelType.PRIVATE)) event.privateChannel else null
    val messageChannel: MessageChannel = event.channel


    /**
     * Sends a message embed to the channel the Context was created from.
     *
     * @param content
     *        The content of the message.
     */
    fun send(content: String) {
        messageChannel.sendMessage(content).submit()
    }

    /**
     * Sends a message embed to the channel the Context was created from.
     *
     * @param embed
     *        Options to apply to the message embed.
     */
    fun send(embed: EmbedBuilder.() -> Unit) {
        messageChannel.sendMessage(EmbedBuilder().apply(embed).build()).submit()
    }

    /**
     * Sends a message embed to the channel the Context was created from.
     *
     * @param content
     *        The content of the message.
     *
     * @return The created message.
     */
    suspend fun sendAsync(content: String): Message {
        return messageChannel.sendMessage(content).submit().await()
    }

    /**
     * Sends a message embed to the channel the Context was created from.
     *
     * @param embed
     *        Options to apply to the message embed.
     *
     * @return The created message.
     */
    suspend fun sendAsync(embed: EmbedBuilder.() -> Unit): Message {
        return messageChannel.sendMessage(EmbedBuilder().apply(embed).build()).submit().await()
    }

    /**
     * Sends the message author a direct message.
     *
     * @param content
     *        The content of the message.
     */
    fun sendPrivate(content: String) {
        author.openPrivateChannel().submit()
            .thenAccept {
                it.sendMessage(content).submit()
                    .handle { _, _ -> it.close().submit() }
            }
    }

    /**
     * Sends a typing status within the channel until the provided function is exited.
     *
     * @param block
     *        The code that should be executed while the typing status is active.
     */
    fun typing(block: () -> Unit) {
        messageChannel.sendTyping().queue {
            val task = Scheduler.every(5000) {
                messageChannel.sendTyping().queue()
            }
            block()
            task.cancel(true)
        }
    }

    /**
     * Sends a typing status within the channel until the provided function is exited.
     *
     * @param block
     *        The code that should be executed while the typing status is active.
     */
    suspend fun typingAsync(block: suspend () -> Unit) {
        messageChannel.sendTyping().submit().await()
        val task = Scheduler.every(5000) { messageChannel.sendTyping().queue() }

        try {
            block()
        } finally {
            task.cancel(true)
        }
    }

    /**
     * Cleans a string, sanitizing all forms of mentions (role, channel and user), replacing them with
     * their display-equivalent where possible (For example, <@123456789123456789> becomes @User).
     *
     * For cases where the mentioned entity is not cached by the bot, the mention will be replaced
     * with @invalid-<entity type>.
     *
     * It's recommended that you use this only for sending responses back to a user.
     *
     * @param str
     *        The string to clean.
     *
     * @returns The sanitized string.
     */
    fun cleanContent(str: String): String {
        var content = str.replace("e", "е")
        // We use a russian "e" instead of \u200b as it keeps character count the same.
        val matcher = mentionPattern.matcher(str)

        while (matcher.find()) {
            val entityType = matcher.group("type")
            val entityId = matcher.group("id").toLong()
            val fullEntity = matcher.group("mention")

            when (entityType) {
                "@", "@!" -> {
                    val entity = guild?.getMemberById(entityId)?.effectiveName
                        ?: jda.getUserById(entityId)?.name
                        ?: "invalid-user"
                    content = content.replace(fullEntity, "@$entity")
                }
                "@&" -> {
                    val entity = jda.getRoleById(entityId)?.name ?: "invalid-role"
                    content = content.replace(fullEntity, "@$entity")
                }
                "#" -> {
                    val entity = jda.getTextChannelById(entityId)?.name ?: "invalid-channel"
                    content = content.replace(fullEntity, "#$entity")
                }
            }
        }

        for (emote in message.emotes) {
            content = content.replace(emote.asMention, ":${emote.name}:")
        }

        return content
    }

    companion object {
        private val mentionPattern = Pattern.compile("(?<mention><(?<type>@!?|@&|#)(?<id>[0-9]{17,21})>)")
    }
}
