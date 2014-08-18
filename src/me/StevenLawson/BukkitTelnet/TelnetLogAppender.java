package me.StevenLawson.BukkitTelnet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import me.StevenLawson.BukkitTelnet.session.ClientSession;
import me.StevenLawson.BukkitTelnet.session.FilterMode;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

public class TelnetLogAppender extends AbstractAppender
{
    private final Set<ClientSession> sessions;
    private final SimpleDateFormat dateFormat;

    private TelnetLogAppender()
    {
        super("BukkitTelnet", null, null);

        this.sessions = new HashSet<ClientSession>();
        this.dateFormat = new SimpleDateFormat("HH:mm:ss");

        super.start();
    }

    public Set<ClientSession> getSessions()
    {
        return Collections.unmodifiableSet(sessions);
    }

    public boolean addSession(ClientSession session)
    {
        return sessions.add(session);
    }

    public boolean removeSession(ClientSession session)
    {
        return sessions.remove(session);
    }

    public void removeAllSesssions()
    {
        sessions.clear();
    }

    @Override
    public void append(LogEvent event)
    {
        final String message = event.getMessage().getFormattedMessage();

        for (ClientSession session : sessions)
        {
            if (!session.syncIsConnected())
            {
                continue;
            }

            if (session.getFilterMode() == FilterMode.CHAT_ONLY)
            {
                if (!(message.startsWith("<")
                        || message.startsWith("[Server")
                        || message.startsWith("[CONSOLE") || message.startsWith("[TotalFreedomMod] [ADMIN]")))
                {
                    return;
                }
            }

            if (session.getFilterMode() == FilterMode.NONCHAT_ONLY)
            {
                if (message.startsWith("<")
                        || message.startsWith("[Server")
                        || message.startsWith("[CONSOLE")
                        || message.startsWith("[TotalFreedomMod] [ADMIN]"))
                {
                    return;
                }
            }

            session.printRawLine(formatMessage(message, event));
        }
    }

    private String formatMessage(String message, LogEvent event)
    {
        final StringBuilder builder = new StringBuilder();
        final Throwable ex = event.getThrown();

        builder.append("[");
        builder.append(dateFormat.format(event.getMillis()));
        builder.append(" ");
        builder.append(event.getLevel().name().toUpperCase());
        builder.append("]: ");
        builder.append(message);

        if (ex != null)
        {
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            builder.append(writer);
        }

        return builder.toString();
    }

    public static TelnetLogAppender getInstance()
    {
        return TelnetLogAppenderHolder.INSTANCE;
    }

    private static class TelnetLogAppenderHolder
    {
        private static final TelnetLogAppender INSTANCE = new TelnetLogAppender();
    }
}
