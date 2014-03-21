package me.StevenLawson.BukkitTelnet.session;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

public class SessionLogAppender extends AbstractAppender
{
    private final ClientSession session;
    private final SimpleDateFormat dateFormat;

    public SessionLogAppender(ClientSession session)
    {
        super("BukkitTelnet", null, null);

        this.session = session;
        this.dateFormat = new SimpleDateFormat("HH:mm:ss");

        start();
    }

    @Override
    public void append(LogEvent event)
    {
        final String message = event.getMessage().getFormattedMessage();

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

        session.printRawln(formatMessage(message, event));
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
}
