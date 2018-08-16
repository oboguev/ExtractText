package my.ExtractText;

import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// requires DjVuLibre to be installed https://sourceforge.net/projects/djvu/files/

public class ExtractText
{
    public static void main(String[] args)
    {
        try
        {
            if (args.length != 1)
                throw new Exception("Usage: ExtractText control-file");
            List<String> ctrl = Util.read_lines(args[0]);
            if (ctrl.size() == 0)
                throw new Exception("Control file is empty");
            if ((ctrl.size() % 3) != 0)
                throw new Exception("Number of lines in control file is not multiple of 3");
            for (int k = 0; k < ctrl.size(); k += 3)
                syncExtractText(ctrl.get(k), ctrl.get(k + 1), false);
            for (int k = 0; k < ctrl.size(); k += 3)
                syncExtractText(ctrl.get(k), ctrl.get(k + 2), true);
            System.out.println("*** Finished");
        }
        catch (Exception ex)
        {
            System.err.println("*** Exception:");
            ex.printStackTrace();
        }
    }

    public static void syncExtractText(String srcDir, String dstDir, boolean alphaOnly) throws Exception
    {
        if (alphaOnly && dstDir.equals("-"))
            return;

        EnumFiles efsrc = EnumFiles.enumFiles(srcDir);
        EnumFiles efdst = EnumFiles.enumFiles(dstDir);
        Set<String> removed = new HashSet<String>();

        // remove dst HTML-s that have no corresponding source HTML
        // remove dst HTML-s that are older than corresponding source HTML
        for (FileInfo fidst : efdst.files)
        {
            if (!fidst.path.toLowerCase().endsWith(".htm") &&
                !fidst.path.toLowerCase().endsWith(".html"))
            {
                continue;
            }

            String fp = efdst.root + File.separator + fidst.path;
            boolean delete = false;
            String orig = fidst.path;

            FileInfo fisrc = efsrc.filemap.get(orig);
            if (fisrc != null)
            {
                if (fisrc.lastModified > fidst.lastModified)
                {
                    System.out.print("Removing file " + fp + " because corresponding source HTML file is newer (will copy over))");
                    delete = true;
                }
            }
            else
            {
                System.out.print("Removing file " + fp + " because corresponding source HTML file no longer exists");
                delete = true;
            }

            if (delete)
            {
                (new File(fp)).delete();
                removed.add(fidst.path);
            }
        }

        // remove TXT-s that have no corresponding PDF or DJVU-s
        // remove TXT-s that are older than corresponding DJVU-s and PDF-s
        for (FileInfo fidst : efdst.files)
        {
            if (!fidst.path.endsWith(".txt"))
                continue;

            String fp = efdst.root + File.separator + fidst.path;
            boolean delete = false;
            String orig = Util.stripTail(fidst.path, ".txt");

            FileInfo fisrc = efsrc.filemap.get(orig);
            if (fisrc != null)
            {
                if (fisrc.lastModified > fidst.lastModified)
                {
                    System.out.print("Removing text file " + fp + " because corresponding PDF/DJVU is newer (will regenerate TXT)");
                    delete = true;
                }
            }
            else
            {
                System.out.print("Removing text file " + fp + " because corresponding PDF/DJVU no longer exists");
                delete = true;
            }

            if (delete)
            {
                (new File(fp)).delete();
                removed.add(fidst.path);
            }
        }

        for (String s : removed)
        {
            efdst.files.remove(efdst.filemap.get(s));
            efdst.filemap.remove(s);
        }

        // For src HTML-s that have no corresponding dst HTML-s, copy them over.
        // For DJVU-s and PDF-s that have no corresponding TXT, extract the text.
        for (FileInfo fisrc : efsrc.files)
        {
            String fpsrc = efsrc.root + File.separator + fisrc.path;
            String fpdst = efdst.root + File.separator + fisrc.path;

            if (fisrc.path.toLowerCase().endsWith(".html") ||
                fisrc.path.toLowerCase().endsWith(".htm"))
            {
                // copy HTM or HTML file
                if (!efdst.filemap.containsKey(fisrc.path))
                {
                    System.out.println("Copying HTM file " + fpsrc + " => " + fpdst);
                    createDirectoryForFile(fpdst);
                    Files.copy((new File(fpsrc)).toPath(), (new File(fpdst)).toPath());
                }
            }
            else if (fisrc.path.toLowerCase().endsWith(".djvu"))
            {
                // extract DJVU to TXT
                fpdst += ".txt";
                if (!efdst.filemap.containsKey(fisrc.path + ".txt"))
                {
                    System.out.println("Extracting texf from DVJU file " + fpsrc + " => " + fpdst);
                    String text = DjVuToText.extract(fpsrc);
                    if (text != null)
                    {
                        text = (new TextCleaner(alphaOnly)).clean(text);
                        createDirectoryForFile(fpdst);
                        Util.writeAsUTF8File(fpdst, text);
                    }
                }
            }
            else if (fisrc.path.toLowerCase().endsWith(".pdf"))
            {
                fpdst += ".txt";
                if (!efdst.filemap.containsKey(fisrc.path + ".txt"))
                {
                    System.out.println("Extracting texf from PDF file " + fpsrc + " => " + fpdst);
                    String text = PDFToText.extract(fpsrc);
                    if (text != null)
                    {
                        text = (new TextCleaner(alphaOnly)).clean(text);
                        createDirectoryForFile(fpdst);
                        Util.writeAsUTF8File(fpdst, text);
                    }
                }
            }

        }
    }

    static void createDirectoryForFile(String fp) throws Exception
    {
        File f = new File(fp);
        f = f.getParentFile();
        if (f.exists())
        {
            if (f.isDirectory())
                return;
            throw new Exception("Not a directory: " + f.getCanonicalPath());
        }
        else
        {
            if (f.mkdirs())
                return;
            if (!f.exists())
                throw new Exception("Unable to create directory " + f.getCanonicalPath());
            if (!f.isDirectory())
                throw new Exception("Not a directory: " + f.getCanonicalPath());
        }
    }

}
