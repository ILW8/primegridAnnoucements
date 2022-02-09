import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;

public class Main implements PropertyChangeListener {
    public static class DiscordBot implements EventListener {
        private static class MessageChecker extends TimerTask {
            JDA jda;
            Timer timer;

            public MessageChecker(JDA jda, Timer timer) {
                this.jda = jda;
                this.timer = timer;
            }

            @Override
            public void run() {
                try {
                    Message message = Objects.requireNonNull(jda.getTextChannelById("357545354356588548"))
                            .retrieveMessageById("357606917670961152")
                            .complete();
                    List<MessageReaction> reactions = message.getReactions();
                    for (MessageReaction reaction : reactions) {
                        System.out.print(reaction.getReactionEmote().getName() + " ");
                        if (reaction.getReactionEmote().getName().equals("\uD83D\uDED1")) {
                            System.out.println("Matched emote, touching shutdown flag file");
                            File flagFile = new File("shutdown.flag");
                            try {
                                System.out.println(flagFile.createNewFile()
                                        ? "Created new file"
                                        : "File already exists");
                                message.clearReactions().complete();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                    if (!reactions.isEmpty()) {
                        System.out.println(ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
                    }
                } catch (RejectedExecutionException ignored) {
                    timer.cancel();
                    timer.purge();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean restart = false;
        private final PropertyChangeSupport propertyChangeSupport;
        private JDA jda;

        public DiscordBot() {
            this.propertyChangeSupport = new PropertyChangeSupport(this);
        }

        public void main(String botToken) {
            System.out.printf("[%s] Reached DiscordBot main()%n", java.time.LocalDateTime.now());
            try {
                try {
                    jda = JDABuilder.createDefault(botToken)
                            .addEventListeners(this)
                            .setAutoReconnect(false)
                            .build();
                    jda.awaitReady();

                } catch (LoginException | InterruptedException e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    this.setRestart(true);
                }

                Timer timer = new Timer();
                MessageChecker messageChecker = new MessageChecker(jda, timer);
                timer.schedule(messageChecker, 250, 1000);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                this.setRestart(true);
            }

        }

        @Override
        public void onEvent(@NotNull GenericEvent genericEvent) {
//            System.out.println(genericEvent.getClass().getSimpleName());

            if (genericEvent instanceof MessageReceivedEvent event) {
                // 357711848197586945 being the id of the webhook we use for sending new primes to #prime-discoveries
                if (event.getMessage().getAuthor().getId().equals("357711848197586945")) {
                    event.getMessage().crosspost().queue(null, Throwable::printStackTrace);
                }
            }

            if (genericEvent instanceof ShutdownEvent) {
                System.out.printf("[%s] Received ShutdownEvent, restarting...%n", java.time.LocalDateTime.now());

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                this.setRestart(true);
            }
        }

        public void setRestart(boolean restart) {
            propertyChangeSupport.firePropertyChange("restart", this.restart, this.restart = restart);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        Main main = new Main();
        main._main();
    }

    public void _main() throws FileNotFoundException {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        DiscordBot bot = new DiscordBot();
        this.startBot(bot);
    }

    String botToken;
    DiscordBot bot;

    public void startBot(DiscordBot bot) throws FileNotFoundException {
        File botTokenFile = new File("botToken");
        this.botToken = (new Scanner(botTokenFile)).nextLine();
        this.bot = bot;
        bot.addPropertyChangeListener(this);
        bot.main(this.botToken);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("restart")) {
            if ((boolean) evt.getNewValue()) {
                DiscordBot bot = new DiscordBot();
                try {
                    this.startBot(bot);
                } catch (FileNotFoundException e) {
                    throw RuntimeException.class.cast(e);
                }
            }
        }
    }

}
