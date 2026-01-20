package my.ExtractToLinux.data;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import my.ExtractText.Util;
import my.ExtractToLinux.util.Utf8Shortener;

/*
 * Describes a single file
 */
public class FileDescriptor
{
    public DirDescriptor parent;

    /* full path relative to root directory */
    public String pathRelativeToRoot;

    /* file name (last path component) */
    public String name;

    /* file size (bytes) */
    public long size;

    /* file last modification time */
    public Instant lastModifiedTime;

    /* name is basename.extension */
    public String basename;
    public String extension;

    /* basename is nameseed + suffix */
    public String nameseed;
    public String suffix; // can be null

    public int rank; // no suffix -> 0, RAW -> 1, OCR -> 2, OCR2 -> 3

    /* shortened file name (last path component) */
    public String shortname;

    /* =========================================================================== */

    public String toString()
    {
        return pathRelativeToRoot;
    }

    public String targetPath(String rootDir)
    {
        return parent.targetPath(rootDir) + File.separator + shortname;
    }

    /* =========================================================================== */

    public void splitNameExtension(String... extensions)
    {
        // reset by default
        basename = null;
        extension = null;

        if (name == null || extensions == null || extensions.length == 0)
            return;

        for (String s : extensions)
        {
            if (s == null || s.isEmpty())
                continue;

            String dotSuffix = "." + s;

            if (name.length() <= dotSuffix.length())
                continue;

            if (name.regionMatches(true,
                                   name.length() - dotSuffix.length(),
                                   dotSuffix,
                                   0,
                                   dotSuffix.length()))
            {
                int baseEnd = name.length() - dotSuffix.length();
                basename = name.substring(0, baseEnd);
                extension = name.substring(baseEnd + 1); // actual case
                return;
            }
        }
    }

    /* =========================================================================== */

    public boolean SplitBasenameSuffix(String... suffixes)
    {
        for (String s : suffixes)
        {
            if (basename.endsWith(s))
            {
                suffix = s;
                nameseed = basename.substring(0, basename.length() - s.length());
                return true;
            }
        }

        return false;
    }

    /* =========================================================================== */

    public void shortenName(int maxbytes) throws Exception
    {
        try
        {
            tryShortenName(maxbytes);
        }
        catch (Exception ex)
        {
            if (ex != TooLong)
                throw ex;
        }

        // trim nameseed

        for (String[] pair : mapSeeds)
        {
            if (nameseed.startsWith(pair[0]))
            {
                nameseed = pair[1] + nameseed.substring(pair[0].length());
                break;
            }
        }

        tryShortenName(maxbytes);
    }

    public void tryShortenName(int maxbytes) throws Exception
    {
        if (name.getBytes(StandardCharsets.UTF_8).length <= maxbytes)
        {
            shortname = name;
        }
        else
        {
            String suffix = this.suffix;
            if (suffix == null)
                suffix = "";

            int endbytes = (suffix + "." + extension).getBytes(StandardCharsets.UTF_8).length;
            shortname = Utf8Shortener.shorten(nameseed, maxbytes - endbytes) + suffix + "." + extension;

            if (shortname.getBytes(StandardCharsets.UTF_8).length > maxbytes)
                throw new RuntimeException("Bugcheck");
        }

        for (FileDescriptor xfd : parent.containedFiles.values())
        {
            if (xfd != this)
            {
                if (xfd.name.equalsIgnoreCase(shortname))
                    conflictWith(xfd);
                if (xfd.shortname != null && xfd.shortname.equalsIgnoreCase(shortname))
                    conflictWith(xfd);
            }
        }
    }

    private void conflictWith(FileDescriptor xfd) throws Exception
    {
        Util.err(this.pathRelativeToRoot);
        Util.err(xfd.pathRelativeToRoot);
        throw TooLong;
    }

    private static Exception TooLong = new Exception("Conflict with file shortened name");

    private static String[][] mapSeeds = {

            { "История марксизма-ленинизма. Марксизм в период формирования массовых социалистических партий II Интернационала (70-90-е годы XIX века) ",
                    "История марксизма-ленинизма. Марксизм в период формирования массовых " },

            { "Миронов - Какая дорога ведет к революции, Имущественное неравенство в России за три столетия, XVIII-начало XXI в. ",
                    "Миронов - Какая дорога ведет к революции, Имущественное неравенство в России " },

            { "Миронов - По классическому сценарию, Русская революция 1917 года в условиях экономического роста и повышения уровня жизни ",
                    "Миронов - По классическому сценарию, Русская революция 1917 года " },

            { "Документы обвиняют. Сборник документов о чудовищных зверствах германских властей на временно захваченных ими советских территориях. ",
                    "Документы обвиняют. Сборник документов о чудовищных зверствах " },

            { "Случ - Советско-германские отношения в сентябре–декабре 1939 года и вопрос о вступлении СССР во Вторую мировую войну ",
                    "Случ - Советско-германские отношения в сентябре–декабре 1939 " }
    };
}
