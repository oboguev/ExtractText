package my.ExtractToLinux;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import my.ExtractText.Util;
import my.ExtractToLinux.data.DirDescriptor;
import my.ExtractToLinux.data.FileDescriptor;
import my.ExtractToLinux.util.Utf8Shortener;

public class ExtractToLinux
{
    public static void main(String[] args)
    {
        try
        {
            if (args.length != 2)
                throw new Exception("Usage: ExtractText from-dir to-dir");

            File fp1 = new File(args[0]).getCanonicalFile();
            File fp2 = new File(args[1]).getCanonicalFile();

            if (!(fp1.exists() && fp1.isDirectory()))
                throw new Exception("Directory does not exist:" + fp1.getAbsolutePath());

            if (!(fp2.exists() && fp2.isDirectory()))
                throw new Exception("Directory does not exist:" + fp2.getAbsolutePath());

            String srcRootDir = fp1.getCanonicalPath();
            DirDescriptor ddRoot = DirDescriptor.enumerateFileTree(srcRootDir);
            for (DirDescriptor dd : ddRoot.flat())
                preprocessDir(dd, srcRootDir);

            System.out.println("*** Completed");
        }
        catch (Exception ex)
        {
            System.err.println("*** Exception:");
            ex.printStackTrace();
        }
    }

    /* ==================================================================================== */

    private static void preprocessDir(DirDescriptor dd, String srcRootDir) throws Exception
    {
        for (String fn : new HashSet<>(dd.containedFiles.keySet()))
        {
            /*
             * Eliminate empty files
             */
            FileDescriptor fd = dd.containedFiles.get(fn);
            if (fd.size == 0)
            {
                dd.containedFiles.remove(fn);
                continue;
            }

            /*
             * Split file name -> basename + extension
             */
            fd.splitNameExtension("pdf.txt", "djvu.txt", "txt.txt", "htm.txt", "html.txt");
            if (fd.extension == null)
            {
                Util.err("Undefined file : " + srcRootDir + File.separator + fd.pathRelativeToRoot);
                dd.containedFiles.remove(fn);
                continue;
            }

            /*
             * Split file baaename -> nameseed + suffix
             */
            if (fd.SplitBasenameSuffix(ocr2Suffixes))
            {
                fd.rank = 3;
            }
            else if (fd.SplitBasenameSuffix(ocrSuffixes))
            {
                fd.rank = 2;
            }
            else if (fd.SplitBasenameSuffix(rawSuffixes))
            {
                fd.rank = 1;
            }
            else
            {
                fd.nameseed = fd.basename;
                fd.suffix = null;
                fd.rank = 0;
            }
        }

        /*
         * Eliminate RAW files if OCR exists
         */
        weedOutRawFiles(dd);

        /*
         * Shorten directory name 
         */
        if (dd.parent != null && dd.name.getBytes(StandardCharsets.UTF_8).length > MAX_LINIX_FILE_NAME)
        {
            dd.shortname = Utf8Shortener.shorten(dd.name, MAX_LINIX_FILE_NAME);

            for (DirDescriptor xdd : dd.parent.childDirectories.values())
            {
                if (xdd != dd)
                {
                    if (xdd.name.equalsIgnoreCase(dd.shortname))
                        throw new Exception("Conflict with directory shortened name");
                    if (xdd.shortname != null && xdd.shortname.equalsIgnoreCase(dd.shortname))
                        throw new Exception("Conflict with directory shortened name");
                }
            }
        }
        else
        {
            dd.shortname = dd.name;
        }

        /*
         * Shorten file names 
         */
        for (String fn : dd.containedFiles.keySet())
        {
            FileDescriptor fd = dd.containedFiles.get(fn);
            fd.shortenName(MAX_LINIX_FILE_NAME);
        }
    }

    private static void weedOutRawFiles(DirDescriptor dd) throws Exception
    {
        Map<String, List<FileDescriptor>> m = new HashMap<>();

        for (String fn : new HashSet<>(dd.containedFiles.keySet()))
        {
            FileDescriptor fd = dd.containedFiles.get(fn);
            if (m.get(fd.nameseed) == null)
                m.put(fd.nameseed, new ArrayList<>());
            m.get(fd.nameseed).add(fd);
        }

        for (String nameseed : m.keySet())
        {
            List<FileDescriptor> fds = m.get(nameseed);

            // if contains 2 or 3, remove 1 and 0
            boolean contains23 = false;
            for (FileDescriptor fd : fds)
            {
                if (fd.rank == 2 || fd.rank == 3)
                    contains23 = true;
            }

            if (contains23)
            {
                for (FileDescriptor fd : fds)
                {
                    if (fd.rank == 0 || fd.rank == 1)
                    {
                        dd.containedFiles.remove(fd.name);
                    }
                }
            }
        }
    }

    private static String[] ocr2Suffixes = { " - OCR2" };
    private static String[] ocrSuffixes = { " - OCR", " (OCR)", "-OCR", " - OOCR", " - OCRed", " [OCR]", ", OCR)" };
    private static String[] rawSuffixes = { " - RAW", " (RAW)", "-RAW", " [RAW]", ", RAW)" };

    private static final int MAX_LINIX_FILE_NAME = 255;

    /* ==================================================================================== */
}
