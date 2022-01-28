import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main implements EventListener {
    public static void main(String[] args) throws LoginException, InterruptedException, FileNotFoundException {
        // create a file "botToken" containing a discord bot token
        File botTokenFile = new File("botToken");
        String botToken = (new Scanner(botTokenFile)).nextLine();
        JDA jda = JDABuilder.createDefault(botToken)
                .addEventListeners(new Main())
                .build();

        jda.awaitReady();
    }

    @Override
    public void onEvent(@NotNull GenericEvent genericEvent) {
        if (genericEvent instanceof MessageReceivedEvent event) {
            // 357711848197586945 being the id of the webhook we use for sending new primes to #prime-discoveries
            if (event.getMessage().getAuthor().getId().equals("357711848197586945")) {
                event.getMessage().crosspost().queue(null, Throwable::printStackTrace);
            }
        }
    }
}
