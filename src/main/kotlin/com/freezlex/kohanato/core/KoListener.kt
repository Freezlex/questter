package com.freezlex.kohanato.core

import com.freezlex.kohanato.core.events.*
import com.freezlex.kohanato.core.i18n.LangManager
import com.freezlex.kohanato.core.i18n.Language
import com.freezlex.kohanato.core.throwable.CommandThrowable
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import mu.KotlinLogging
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Used to create new instances of JDA's DefaultShardManagerBuilder implementation.
 * A single KohanatoCore cannot be reused multiple times. Each call to launch() initiate an instance using all the provided information.
 * @author Freezlex
 */

@OptIn(DelicateCoroutinesApi::class)
class KoListener(
    val core: KohanatoCore,
    event: GenericEvent
) {

    lateinit var language: LangManager;

    init {
        GlobalScope.async { run(event) }
        language = Language.getLangManager();
    }

    suspend fun run(event: GenericEvent) {
        when (event) {
            is ReadyEvent -> OnReadyEvent.run(event, this)
            is SlashCommandInteractionEvent -> OnSlashCommandEvent.run(event, this)
            is MessageReceivedEvent -> OnMessageReceivedEvent.run(event, this)
            is ButtonInteractionEvent -> OnButtonClickEvent.run(event, this)
            is UserContextInteractionEvent -> OnUserContextInteractionEvent.run(event, this)
            is MessageContextInteractionEvent -> OnMessageContextInteractionEvent.run(event, this)
        }
    }

    fun dispatchSafely(invoker: (CommandThrowable) -> Unit) {
        try {
            CommandThrowable.run(invoker)
        } catch (e: Throwable) {
            try {
                CommandThrowable.onInternalError(e)
            } catch (inner: Throwable) {
                println(inner)
            }
        }
    }
}

val logger = KotlinLogging.logger {}